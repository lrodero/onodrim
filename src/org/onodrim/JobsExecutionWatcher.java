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

/**
 * Interface that defines some methods that signal events in the lifecycle of a {@link JobsSet} execution. To
 * watch such events, developers only need to implement this interface and pass one instance of the implementing
 * class to the {@link JobsSet#runJobs(JobsExecutionWatcher)} method when calling it to run the jobs (which is equivalent
 * to call to {@link Onodrim#runJobs(JobsSet, JobsExecutionWatcher)}).
 * @author lrodero
 *
 */
public interface JobsExecutionWatcher {
    /**
     * {@link JobsSet} instance execution started.
     * @param jobsSet
     */
    public void jobsExecutionStarted(JobsSet jobsSet);

    /**
     * {@link Job} instance execution started.
     * @param job
     */
    public void jobStarted(Job job);

    /**
     * {@link Job} was discarded (i.e. not run) because results from a previous experiment were found and the
     * framework is configured not to override previous results.
     * @param job
     */
    public void jobWasDiscarded(Job job);

    /**
     * {@link Job} execution was finished.
     * @param job
     */
    public void jobFinished(Job job);

    /**
     * {@link Job} execution failed; if this was due to some error such an exception this is 
     * passed as parameter in the call.
     * @param job
     * @param throwable Cause of the error, if it was due to some exception-like event. If not, it is {@code null} 
     */
    public void jobFailed(Job job, Throwable throwable);

    /**
     * All {@link Job}s in the set have been executed (they have been either successfully run, or discarded, or failed)
     * @param jobsSet
     */
    public void allJobsFinished(JobsSet jobsSet);
}
