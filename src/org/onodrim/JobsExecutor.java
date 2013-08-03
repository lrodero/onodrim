/*
 * Copyright 2012 Luis Rodero-Merino.
 * 
 * This file is part of Onodrim.
 * 
 * Onodrim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Onodrim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Onodrim.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package org.onodrim;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

class JobsExecutor extends ThreadPoolExecutor {

    // TODO: INTEGRATION WITH SOME (ONE OR MORE) GRID FRAMEWORK(S)

    // In fact, as the number of core threads equals the number of max threads,
    // this has no effect.
    private static final long KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.MILLISECONDS;

    private static Logger logger = Logger.getLogger(JobsExecutor.class.getCanonicalName());

    /**
     * {@link JobsSet} instance that contains the {@link Job}s to be run
     */
    private JobsSet jobsSet = null;
    /**
     * If not {@code null}, it will be notified about the events in the lifecycle
     * of the {@link JobsSet} execution.
     */
    private JobsExecutionWatcher watcher = null;

    private Lock runningCheckLock = new ReentrantLock();
    private boolean started = false;
    private boolean finished = false;

    /**
     * 
     * @param maxJobsRunInParallel Max amount of threads that will be running {@link Job}s in parallel.
     * @param jobsSet Set that contains the {@link Job}s to execute.
     * @param watcher Instance that will watch for events of the {@link JobsSet} execution (set execution
     *                started, some job was finished...). It can be {@code null}.
     * @throws IllegalArgumentException If the {@link JobsSet} instance is {@code null}
     */
    public JobsExecutor(int maxJobsRunInParallel, JobsSet jobsSet, JobsExecutionWatcher watcher) {
        super(  maxJobsRunInParallel, maxJobsRunInParallel, KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT, new LinkedBlockingQueue<Runnable>(jobsSet.getJobs()));
        //if(jobsSet == null)
        //    throw new IllegalArgumentException("Cannot create a jobs executor instance with a null set of jobs to execute");
        this.jobsSet = jobsSet;
        this.watcher = watcher;
    }

    /**
     * Run all {@link Job}s in the {@link JobsSet} passed as parameter to the constructor.
     * @throws JobExecutionException There was some problem when running the {@link JobsSet}, e.g. the results
     * folder could not be created.
     * @throws IllegalArgumentException The folder where results must be stored is {@code null}.
     * @throws IllegalStateException The {@link JobsSet} was already run or it is being run now.
     */
    public void runJobs(boolean blockUntilFinished) throws JobExecutionException {

        if (super.isTerminated())
            throw new IllegalStateException("Jobs were already run");

        logger.log(Level.FINE, "Starting jobs execution");

        boolean illegalState = false;
        runningCheckLock.lock();
        if (started)
            illegalState = true;
        started = true;
        runningCheckLock.unlock();
        if (illegalState) {
            logger.log(Level.WARNING, "Ignoring petition to start jobs execution, executor is already running");
            throw new IllegalStateException("Executor is already running");
        }
        
        // By this method, tasks already in queue will be executed, no new tasks
        // will be accepted, and
        // isTerminated will return true once all tasks are run.
        if (watcher != null)
            watcher.jobsExecutionStarted(jobsSet);
        super.prestartAllCoreThreads();
        super.shutdown();
        if(blockUntilFinished)
            try {
                super.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            } catch (InterruptedException exception) {
                logger.log(Level.SEVERE, "Interrupted while blocked waiting for jobs to finish", exception);
            }
    }

    @Override
    protected void beforeExecute(Thread thread, Runnable runnable) {

        if (!(runnable instanceof Job)) {
            logger.log(Level.SEVERE, "A Runnable instance that is not an Job has reached the jobs executor to be run");
            throw new Error("A Runnable instance that is not an Job has reached the jobs executor to be run");
        }

        Job job = (Job) runnable;

        thread.setName("T" + thread.getId() + "-J" + Job.indexAsString(job.getJobIndex(), jobsSet.getTotalJobsCount()));

        // Updating thread map to associate the job (and so its configuration)
        // to this thread and its descendants
        Onodrim.registerThreadJob(job);

        job.preExecutionProcess();

        super.beforeExecute(thread, job);

        jobsSet.jobStarted(job);
        
        if (watcher != null)
            watcher.jobStarted(job);
    }

    @Override
    protected void afterExecute(Runnable runnable, Throwable throwable) {

        if (!(runnable instanceof Job)) {
            logger.log(Level.SEVERE, "A Runnable instance that is not an Job has been run by the jobs executor");
            throw new Error("A Runnable instance that is not an Job has been run by the jobs executor");
        }

        Job job = (Job) runnable;

        job.postExecutionProcess(throwable);

        super.afterExecute(runnable, throwable);

        jobsSet.jobFinished(job);

        if (watcher != null) {
            if (job.discarded())
                watcher.jobWasDiscarded(job);
            else if ((throwable != null) || job.executionFailed())
                watcher.jobFailed(job, throwable);
            else
                watcher.jobFinished(job);
        }

        Onodrim.unregisterThreadJob(job);

        logger.log(Level.FINE, "After-execution process for job " + job.getJobIndex() + " done");
    }

    @Override
    protected void terminated() {
        super.terminated();
        runningCheckLock.lock();
        finished = true;
        runningCheckLock.unlock();
        logger.log(Level.INFO, "All jobs terminated");
        jobsSet.allJobsFinished();
        if (watcher != null)
            watcher.allJobsFinished(jobsSet);
    }

    protected boolean isRunning() {
        runningCheckLock.lock();
        boolean isRunning = started && !finished;
        runningCheckLock.unlock();
        return isRunning;
    }

}
