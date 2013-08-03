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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Job implements Runnable {

    private static Logger logger = Logger.getLogger(Job.class.getCanonicalName());

    protected static final String SUCESSFUL_EXECUTION_REPORT_FILE_NAME = "JobResults.txt";
    private static final String FAILED_EXECUTION_REPORT_FILE_NAME = "JOB_ERROR_REPORT.txt";
    protected static final String CONFIG_STORE_FILE_NAME = "JobConfiguration.properties";

    /**
     * Configuration instance that sets the parameters for this job.
     */
    private Configuration conf = null;
    /**
     * Index of job in the set to be executed (each job belongs to one set)
     */
    private int jobIndex = -1;
    /**
     * Set this job belongs to
     */
    private JobsSet jobsSet = null;
    /**
     * When this job is executed, it will call to the {@link JobEntryPoint#startJob()} method of
     * this instance
     */
    private JobEntryPoint jobEntryPoint = null;
    /**
     * Folder where this job results will be stored
     */
    private File jobResultsDir = null;
    /**
     * Flag that signals whether this job has already been started
     */
    private boolean alreadyStarted = false;
    /**
     * Flag that signals whether this job has finished
     */
    private boolean finished = false;
    /**
     * Flag that signals whether there was some error during the execution of this job
     */
    private boolean errorInExecution = false;
    /**
     * In case there was an error, this string should store a description of what happened
     */
    private String errorInExecutionMsg = null;
    /**
     * Mapping of results names and values of this job
     */
    private Map<String, Object> results = new HashMap<String, Object>();
    /**
     * Lock to control concurrent access to state flags {@link #alreadyStarted} and {@link #finished}
     */
    private Lock stateLock = new ReentrantLock();
    /**
     * Flag that signals whether this job should be discarded (i.e. not run)
     */
    private boolean discard = false;

    /**
     * Creates a job.
     * @param conf Job configuration, (parameters names and values)
     * @param jobIndex Index of job in the {@link JobsSet} instance it belongs to
     * @param jobsSet Set of jobs this job belongs to
     * @param jobEntryPoint This entity implements the functionality to run when this job is executed
     * @param jobResultsDir Folder where job output should be stored
     * @throws IllegalArgumentException If any of the parameters given is {@code null}, or the job index has a negative value
     */
    public Job(Configuration conf, int jobIndex, JobsSet jobsSet, JobEntryPoint jobEntryPoint, File jobResultsDir) {
        if(conf == null)
            throw new IllegalArgumentException("Cannot create a job instance with a null configuration");
        if(jobsSet == null)
            throw new IllegalArgumentException("Cannot create a job instance that does not belong to a job set");
        if (jobEntryPoint == null)
            throw new IllegalArgumentException("Cannot create a job instance with a null entry point");
        if (jobResultsDir == null)
            throw new IllegalArgumentException("Cannot create a job instance with a null dir to store results");
        if(jobIndex < 0)
            throw new IllegalArgumentException("Cannot create a job with a negative index");
        this.conf = conf;
        this.jobIndex = jobIndex;
        this.jobsSet = jobsSet;
        this.jobEntryPoint = jobEntryPoint;
        this.jobResultsDir = jobResultsDir;
        // Just in case, checking no other experiment with the same index is already in the set
        for(Job job: jobsSet.getJobs())
            if(job.getJobIndex() == jobIndex)
                throw new IllegalArgumentException("Cannot add a new job with index '" + jobIndex + "', there is already one job with that index in the same set");
    }

    /** 
     * This job configuration
     * @return This job configuration.
     */
    public Configuration getConfiguration() {
        return conf;
    }

    /**
     * Index in the {@link JobsSet} this job belongs to.
     * @return Index in the {@link JobsSet} this job belongs to.
     */
    public int getJobIndex() {
        return jobIndex;
    }
    
    /**
     * {@link JobsSet} this job belongs to.
     * @return {@link JobsSet} this job belongs to.
     */
    public JobsSet getJobsSet() {
        return jobsSet;
    }

    /**
     * Returns the folder this job results must be stored into.
     * @return Folder to store results into.
     */
    public File getJobResultsDir() {
        return jobResultsDir;
    }

    /**
     * Returns {@code true} if this job has finished, {@code false} otherwise.
     * @return Whether this job is finished or not. 
     */
    public boolean finished() {
        return finished;
    }

    /**
     * Returns {@code true} if this job has already been started (and maybe finished), {@code false} otherwise.
     * @return Whether this job has started or not.
     */
    public boolean started() {
        return alreadyStarted;
    }

    /**
     * Store a result of this experiment
     * @param resultName Result name
     * @param result Result value
     */
    public void addResult(String resultName, Object result) {
        if (discard)
            throw new Error("Cannot add a result to a discarded experiment");
        results.put(resultName, result);
    }

    /**
     * Used to signal that this Job execution failed.
     * @param errorInExecutionMsg Description of the error found when executing the experiment, it can be null
     */
    public void setErrorInExecution(String errorInExecutionMsg) {
        this.errorInExecution = true;
        this.errorInExecutionMsg = errorInExecutionMsg;
    }

    /**
     * Returns {@code true} if the execution of this job failed (see method {@link #setErrorInExecution(String)}, {@code false} otherwise.
     * @return Whether there was an error during the execution of this job.
     */
    public boolean executionFailed() {
        return errorInExecution;
    }

    /**
     * All results (names and values) obtained by this job, they have been set by calls to {@link #addResult(String, Object)}
     * method
     * @return Results obtained during the execution of this job.
     */
    public Map<String, Object> getResults() {
        return results;
    }

    /**
     * Returns {@code true} if this job was discarded because the results from a previous experiment
     * were found in this job folder, and previous experiments are not to be overwritten (see method
     * {@link #discardAndLoadResulfsFromFile(File)})
     */
    public boolean discarded() {
        return discard;
    }

    /**
     * Method called when results from a previous experiment
     * were found in this job folder, and previous experiments are not to be overwritten. The results
     * to be returned (see method {@link #getResults()}) are loaded from the previous experiment.
     * @param resultsFile Results from the previous experiment
     */
    protected void discardAndLoadResulfsFromFile(File resultsFile) {
        discard = true;
        errorInExecution = false;
        Properties prevResults = new Properties();
        try {
            prevResults.load(new FileReader(resultsFile));
        } catch (FileNotFoundException exception) {
            throw new Error("Could not load results file " + resultsFile.getAbsolutePath(), exception);
        } catch (IOException exception) {
            throw new Error("Could not load results file " + resultsFile.getAbsolutePath(), exception);
        }
        for (Entry<Object, Object> entry : prevResults.entrySet()) {
            results.put(entry.getKey().toString(), entry.getValue());
        }
    }

    /**
     * Method automatically called by the framework right before the job is executed, it creates the folder
     * where results will be stored.
     */
    protected void preExecutionProcess() {
        
        if(discard) {
            // Well, it could be that the job results directory is not present, in that case we have to create it
            if(!jobResultsDir.exists()) {
                logger.log(Level.FINE, "Job " + jobIndex + " discarded, just creating results folder");
                if(!jobResultsDir.mkdirs()) {
                    logger.log(Level.SEVERE, "Could not create results directory '" + jobResultsDir.getAbsolutePath() + "'");
                    throw new Error("Could not create results directory '" + jobResultsDir.getAbsolutePath() + "'");
                }
            } else
                logger.log(Level.FINE, "Job " + jobIndex + " discarded");
            return;
        }

        logger.log(Level.FINE, "Running pre-execution of job " + jobIndex);

        // Removing previous experiment results
        logger.log(Level.FINE, "Removing folder with previous execution results '" + jobResultsDir.getAbsolutePath() + "' (if any)");
        if(!Util.removeDirRecursively(jobResultsDir)) {
            logger.log(Level.SEVERE, "Could not remove previous results directory '" + jobResultsDir.getAbsolutePath() + "'");
            throw new Error("Could not remove previous results directory '" + jobResultsDir.getAbsolutePath() + "'");
        }
        if (!jobResultsDir.mkdirs()) {
            logger.log(Level.SEVERE, "Could not create results directory '" + jobResultsDir.getAbsolutePath() + "'");
            throw new Error("Could not create results directory '" + jobResultsDir.getAbsolutePath() + "'");
        }

        // Storing job configuration in corresponding file, if a conf file from
        // a previous execution
        // exists it will be overwritten
        File fileToStoreConf = new File(jobResultsDir, CONFIG_STORE_FILE_NAME);
        try {
            conf.saveInFile(fileToStoreConf);
        } catch (IOException exception) {
            logger.log(Level.SEVERE, "IOException caught when trying to save configuration in file "
                            + fileToStoreConf.getAbsolutePath(), exception);
            throw new Error("IOException caught when trying to save configuration in file "
                            + fileToStoreConf.getAbsolutePath(), exception);
        }

    }

    /**
     * Method automatically called by the framework right before the job is executed, it creates the folder
     * where results will be stored if the job was successfully executed, or where information about the found error
     * can be found otherwise.
     * @param throwable It contains the {@link Throwable} instance that caused the job to fail
     */
    protected void postExecutionProcess(Throwable throwable) {

        logger.log(Level.FINE, "Job " + jobIndex + " finished, running after-execution process");

        if ((throwable != null) || errorInExecution)
            try {
                // Something went wrong when running the job, we record error
                // message in file for user check.
                File failedExecutionReportFile = new File(jobResultsDir, FAILED_EXECUTION_REPORT_FILE_NAME);
                logger.log(Level.INFO, "Job " + jobIndex + " execution failed, recording failure information in file "
                                        + failedExecutionReportFile.getAbsolutePath());
                PrintWriter writer = new PrintWriter(failedExecutionReportFile);
                if (throwable != null) {
                    writer.println("Exception caught when running experiment: " + throwable.getMessage());
                    throwable.printStackTrace(writer);
                } else
                    writer.println("Execution was not successful, recorded error message:\n" + errorInExecutionMsg);
                writer.close();
            } catch (IOException ioException) {
                logger.log(Level.SEVERE, "Could not store error message for job " + jobIndex
                                        + ", IOException caught", ioException);
                throw new Error("Could not store error message for job " + jobIndex
                                + ", IOException caught", ioException);
            }
        else
            try {
                // Storing results
                File successfulExecutionReportFile = new File(jobResultsDir, SUCESSFUL_EXECUTION_REPORT_FILE_NAME);
                if(discard)
                    logger.log(Level.INFO, "Job " + jobIndex + " was discarded, storing results in file "
                                            + successfulExecutionReportFile.getAbsolutePath());
                else
                    logger.log(Level.INFO, "Job " + jobIndex + " execution successful, storing results in file "
                                            + successfulExecutionReportFile.getAbsolutePath());
                PrintWriter writer = new PrintWriter(successfulExecutionReportFile);
                writer.println("# " + new Date().toString() + " #");
                for(String resultName: new TreeSet<String>(results.keySet()))
                    writer.println(resultName + "=" + results.get(resultName));
                writer.close();
            } catch (IOException ioException) {
                logger.log(Level.SEVERE, "Could not store results of job " + jobIndex + ", IOException caught",
                            ioException);
                throw new Error("Could not store results of job " + jobIndex + ", IOException caught", ioException);
            }
    }

    /**
     * Run the job! (unless discarded because results from a previous execution were found and cannot override them)
     * This method is called right after {@link #preExecutionProcess()} is called, when it finishes then
     * {@link #postExecutionProcess(Throwable)} will be executed likewise.
     */
    @Override
    public void run() {

        if (discard) {
            logger.log(Level.FINE, "Job " + jobIndex + " discarded, nothing to do");
            return;
        }
            

        logger.log(Level.FINE, "Starting job " + jobIndex);

        boolean illegalState = false;
        stateLock.lock();
        if (alreadyStarted)
            illegalState = true;
        alreadyStarted = true;
        stateLock.unlock();
        if (illegalState)
            throw new IllegalStateException("Job " + jobIndex + " was already started");

        jobEntryPoint.startJob(); // Well, finally! Running job!!

        finished = true;

    }

    /**
     * Utility function to get the job index in a convenient manner (as a string lead by '0's)
     * @param jobIndex Job index
     * @param maxIndex Max index of all jobs (possibly in the same {@link JobsSet}
     * @return String containing this job index with leading '0's if necessary.
     */
    protected static String indexAsString(int jobIndex, int maxIndex) {
        return String.format("%1$0" + (maxIndex + "").length() + "d", jobIndex);
    }

}
