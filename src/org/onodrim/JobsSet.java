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
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to group and handle {@link Job}s created from the same original configuration. Instances of this
 * class can be passed to the {@link JobsExecutor} for execution.
 * 
 * @author Luis Rodero-Merino
 * @since 1.0
 */
public class JobsSet {

    // How Jobs must be executed can be set in the properties object used
    private static final String ONODRIM_JOBS_PROPERTIES_HEADER = Onodrim.PROJECT_NAME + ".jobs";
    
    private static final String DEFAULT_ALL_RESULTS_DIR = "results";
    public static final String ALL_RESULTS_DIR_PROPERTY_NAME = ONODRIM_JOBS_PROPERTIES_HEADER + ".allResultsDir";
    public static final String PARAMS_IN_JOB_RESULTS_DIR_PROPERTY_NAME = ONODRIM_JOBS_PROPERTIES_HEADER + ".paramsInJobResultsDirName";
    private static final boolean DEFAULT_CLEAN_ALL_PREV_RESULTS = true;
    public static final String CLEAN_ALL_PREV_RESULTS_PROPERTY_NAME = ONODRIM_JOBS_PROPERTIES_HEADER + ".cleanAllPrevResults";
    private static final boolean DEFAULT_OVERWRITE_JOB_PREV_RESULTS = true;
    public static final String OVERWRITE_JOB_PREV_RESULTS_PROPERTY_NAME = ONODRIM_JOBS_PROPERTIES_HEADER + ".overwriteJobPrevResults";
    private static final int DEFAULT_PARALLEL_JOBS = 1;
    public static final String PARALLEL_JOBS_PROPERTY_NAME = ONODRIM_JOBS_PROPERTIES_HEADER + ".parallel";
    public static final boolean DEFAULT_BLOCK_UNTIL_FINISHED = false;
    public static final String BLOCK_UNTIL_FINISHED_PROPERTY_NAME = ONODRIM_JOBS_PROPERTIES_HEADER + ".blockUntilFinished";
    public static final String RESULTS_TABLES_PROPERTY_NAME = ONODRIM_JOBS_PROPERTIES_HEADER + ".tablesResults";
    
    public static final String COPY_OF_ALLJOBS_CONFIG_FILE_NAME="AllJobsConfigurationAndEnvironment.properties";
    
    // The broken bar ¦ will be used to separate parameter name=value pairs in results dir names
    private static final char PARAM_SEPARATOR_IN_JOB_RESULTS_DIR_NAME=0xa6;
    
    private static Logger logger = Logger.getLogger(JobsSet.class.getCanonicalName());

    /**
     * {@link Job}s to run, that build this set.
     */
    private List<Job> jobs = null;
    /**
     * {@link Job}s being run at each moment.
     */
    private Set<Job> jobsRunning = new HashSet<Job>();
    /**
     * {@link Job}s already run.
     */
    private Set<Job> jobsRun = new HashSet<Job>();
    /**
     * Executor that will run the {@link Job}s.
     */
    private JobsExecutor jobsExecutor = null;
    /**
     * Flag that sets whether some paremeter names and values should be added to the results dir name.
     */
    private List<String> paramsInJobResultsDirName = new ArrayList<String>();
    /**
     * Flag that sets whether, if the folder where all results are going to be
     * stored exists, it should be deleted to erase any previous result.
     */
    private boolean cleanAllPrevResults = DEFAULT_CLEAN_ALL_PREV_RESULTS;
    /**
     * Flag that sets whether, if for any experiment a folder with a previous result
     * exists, those results should be replaced with a new experiment execution or
     * should be read as the {@link Job} execution result.
     */
    private boolean overwriteJobPrevResults = DEFAULT_OVERWRITE_JOB_PREV_RESULTS;
    /**
     * Number of jobs to be run in parallel, i.e. number of threads that should
     * run in parallel to execute the {@link Job}s.
     */
    private int parallelJobs = DEFAULT_PARALLEL_JOBS;
    /**
     * Flag that sets whether the calling thread will block until all jobs are run.
     */
    private boolean blockUntilFinished = DEFAULT_BLOCK_UNTIL_FINISHED;
    /**
     * Folder where {@link Job}s results folders will be stored.
     */
    private File allResultsDir = new File(DEFAULT_ALL_RESULTS_DIR);
    /**
     * Configurations of tables results.
     */
    private List<ResultsTableConf> resultsTablesConfs = new ArrayList<ResultsTableConf>();
    /**
     * We keep the configuration only to write a copy of it in the results directory
     * when the jobs execution is started. 
     */
    private Properties copyOfOriginalConfiguration = null;

    /**
     * Build a new set of jobs, building their configurations from the file passed as parameter. When they
     * are run, the {@link JobsExecutor} will use the entry point given (which should contain each {@link Job}
     * functionality)
     * @param jobsConfsPropsFile 
     * @param jobsEntryPoint It can be {@code null}, but then Onodrim will not be able to run automatically this set
     * @throws ConfigurationException Raised if the jobs configuration could not be built
     */
    public JobsSet(File jobsConfsPropsFile, JobEntryPoint jobsEntryPoint) throws ConfigurationException {
        this(JobsSet.extractProperties(jobsConfsPropsFile), jobsEntryPoint);
    }

    /**
     * Build a new set of jobs, building their configurations from {@link Properties} passed as parameter. When they
     * are run, the {@link JobsExecutor} will use the entry point given (which should contain each {@link Job}
     * functionality)
     * @param jobsConfsProps
     * @param jobsEntryPoint It can be {@code null}, but then Onodrim will not be able to run automatically this set
     * @throws ConfigurationException Raised if the jobs configuration could not be built
     */
    public JobsSet(Properties jobsConfsProps, JobEntryPoint jobsEntryPoint) throws ConfigurationException {
        // Getting jobs configuration ready, and building jobs
        buildJobs(readJobsSetConfiguration(jobsConfsProps), jobsEntryPoint);
    }

    /**
     * Get total number of {@link Job}s in the set
     * @return Amount of jobs in this set
     */
    public int getTotalJobsCount() {
        if(jobs == null)
            return 0;
        return jobs.size();
    }

    /**
     * Total amount of {@link Job}s already run (executed)
     * @return Amount of jobs run
     */
    public synchronized int getRunJobsCount() {
        return jobsRun.size();
    }

    
    /**
     * Amount of {@link Job}s in execution
     * @return Amount of jobs being run at this moment
     */
    public synchronized int getRunningJobsCount() {
        return jobsRunning.size();
    }

    /**
     * Returns a list of all {@link Job}s in this set, modifications to the list do
     * not change this set contents 
     * @return A copy of the list of {@link Job}s in this set
     */
    public List<Job> getJobs() {
        return new ArrayList<Job>(jobs);
    }

    private static Properties extractProperties(File jobsConfsPropsFile) throws ConfigurationException {

        if(jobsConfsPropsFile == null) {
            logger.log(Level.WARNING, "Tried to create a new set of job configurations with null properties file");
            throw new IllegalArgumentException("Tried to create a new set of job configurations with null properties file");
        }

        logger.log(Level.FINE, "Building jobs set configuration from file " + jobsConfsPropsFile.getAbsolutePath());

        InputStream propsInputStream;
        try {
            propsInputStream = new FileInputStream(jobsConfsPropsFile);
        } catch (FileNotFoundException exception) {
            logger.log(Level.WARNING, "File " + jobsConfsPropsFile.getAbsolutePath() + " was not found, cannot create configurations", exception);
            throw new ConfigurationException("File " + jobsConfsPropsFile.getAbsolutePath() + " was not found", exception);
        }
        Properties confProperties = new Properties();
        try {
            confProperties.load(propsInputStream);
        } catch (IOException exception) {
            logger.log(Level.WARNING, "Error when trying to read configuration from properties file " + jobsConfsPropsFile.getAbsolutePath() + " cannot create configurations", exception);
            throw new ConfigurationException("Error when trying to read configuration from properties file " + jobsConfsPropsFile.getAbsolutePath(), exception);
        }
        
        return confProperties;
    }

    private List<Configuration> readJobsSetConfiguration(Properties jobsConfsProps) throws ConfigurationException {

        if(jobsConfsProps == null) {
            logger.log(Level.WARNING, "Tried to create a new set of job configurations with null " + Properties.class.getCanonicalName() + " instance");
            throw new IllegalArgumentException("Tried to create a new set of job configurations with null " + Properties.class.getCanonicalName() + " instance");
        }
        
        copyOfOriginalConfiguration = new Properties(jobsConfsProps); // We keep a copy of the configuration to write it down in the results folder
        
        Configuration jobsConf = new Configuration(jobsConfsProps);

        String allResultsDirName = jobsConf.getParameter(ALL_RESULTS_DIR_PROPERTY_NAME, DEFAULT_ALL_RESULTS_DIR);
        allResultsDir = new File(allResultsDirName);
        jobsConf.remove(ALL_RESULTS_DIR_PROPERTY_NAME);

        paramsInJobResultsDirName = jobsConf.getListParameter(PARAMS_IN_JOB_RESULTS_DIR_PROPERTY_NAME, String.class, paramsInJobResultsDirName);
        jobsConf.remove(PARAMS_IN_JOB_RESULTS_DIR_PROPERTY_NAME);

        cleanAllPrevResults = jobsConf.getBooleanParameter(CLEAN_ALL_PREV_RESULTS_PROPERTY_NAME, cleanAllPrevResults);
        jobsConf.remove(CLEAN_ALL_PREV_RESULTS_PROPERTY_NAME);

        overwriteJobPrevResults = jobsConf.getBooleanParameter(OVERWRITE_JOB_PREV_RESULTS_PROPERTY_NAME, overwriteJobPrevResults);
        jobsConf.remove(OVERWRITE_JOB_PREV_RESULTS_PROPERTY_NAME);

        if(cleanAllPrevResults && !overwriteJobPrevResults) {
            logger.log(Level.WARNING, "Cannot set '" + CLEAN_ALL_PREV_RESULTS_PROPERTY_NAME +"' to true and " +
                    "'" + OVERWRITE_JOB_PREV_RESULTS_PROPERTY_NAME + "' to false");
            throw new ConfigurationException("Cannot set '" + CLEAN_ALL_PREV_RESULTS_PROPERTY_NAME +"' to true and " +
                    "'" + OVERWRITE_JOB_PREV_RESULTS_PROPERTY_NAME + "' to false");
        }

        parallelJobs = jobsConf.getIntParameter(PARALLEL_JOBS_PROPERTY_NAME, parallelJobs);
        if(parallelJobs < 0) {
            logger.log(Level.WARNING, "Cannot set a negative number of parallel jobs, check '" + PARALLEL_JOBS_PROPERTY_NAME + "' property");
            throw new ConfigurationException("Cannot set a negative number of parallel jobs, check '" + PARALLEL_JOBS_PROPERTY_NAME + "' property");
        }
        jobsConf.remove(PARALLEL_JOBS_PROPERTY_NAME);
        
        blockUntilFinished = jobsConf.getBooleanParameter(BLOCK_UNTIL_FINISHED_PROPERTY_NAME, blockUntilFinished);
        jobsConf.remove(BLOCK_UNTIL_FINISHED_PROPERTY_NAME);

        // Getting results tables configurations
        resultsTablesConfs = ResultsTableConf.parseResultsTablesConf(jobsConf.getArrayParameter(RESULTS_TABLES_PROPERTY_NAME, String.class, null));
        // Checking that tables will be built using parameters that are actually set
        for(ResultsTableConf resultsTableConf: resultsTablesConfs) {
            for(String[] rowParamsSet: resultsTableConf.getRowParamsSets()) // Rows sets
                for(String rowParam: rowParamsSet)
                    if(!jobsConf.containsKey(rowParam)) {
                        String errMsg = "Wrong results table configuration detected, is using row param '" + rowParam + "' but such param is not included in job conf";
                        logger.log(Level.WARNING, errMsg);
                        throw new ConfigurationException(errMsg);
                    }
            for(String[] columnParamSet: resultsTableConf.getColumnParamsSets()) // Columns sets
                for(String columnParam: columnParamSet)
                    if(!jobsConf.containsKey(columnParam)) {
                        String errMsg = "Wrong results table configuration detected, is using column param '" + columnParam + "' but such param is not included in job conf";
                        logger.log(Level.WARNING, errMsg);
                        throw new ConfigurationException(errMsg);
                    }
        }
        jobsConf.remove(RESULTS_TABLES_PROPERTY_NAME);

        List<Configuration> jobsConfs = Configuration.buildConfigurations(jobsConf);

        logger.log(Level.FINE, jobsConfs.size() + " configurations ready, building one job per configuration");
        
        return jobsConfs;
    }
    
    private void buildJobs(List<Configuration> configurations, JobEntryPoint jobsEntryPoint) {

        if(jobsEntryPoint == null)
            logger.log(Level.WARNING, "Trying to create a new set of job configurations with null " + JobEntryPoint.class.getCanonicalName() + " instance");
        
        jobs = new ArrayList<Job>();
        for(int jobIndex = 1; jobIndex <= configurations.size(); jobIndex++) {
            // Setting job results directory name
            Configuration conf = configurations.get(jobIndex-1);
            String jobResultsDirName = "Job-" + Job.indexAsString(jobIndex, configurations.size());
            for(String paramName: paramsInJobResultsDirName)
                if(conf.parameterDefined(paramName))
                    jobResultsDirName += PARAM_SEPARATOR_IN_JOB_RESULTS_DIR_NAME + paramName + "=" + conf.getParameter(paramName);
            File jobResultsDir = new File(allResultsDir, jobResultsDirName);
            jobs.add(new Job(configurations.get(jobIndex-1), jobIndex, this, jobsEntryPoint, jobResultsDir));
        }
        logger.log(Level.FINE, jobs.size() + " jobs built and ready");
    }
    
    public void runJobs() throws JobExecutionException {
        runJobs(null);
    }

    public void runJobs(JobsExecutionWatcher watcher) throws JobExecutionException {
    	
    	for(Job job: jobs)
    		if(job.jobEntryPointIsNull())
    			throw new IllegalStateException("Some (of all) jobs were build without a " + JobEntryPoint.class.getName() +
    					                        " instance, Onodrim cannot run them automatically");

        if(isRunning())
            throw new IllegalStateException("Jobs are already being run");

        // Creating all results dir (if it already exists it will be recreated, unless users set it otherwise)
        createAllResultsDir();
        
        // Saving configuration
        File allJobsFile = null;
        try {
            allJobsFile = new File(allResultsDir, COPY_OF_ALLJOBS_CONFIG_FILE_NAME);
            // copyOfOriginalConfiguration.store(new FileOutputStream(allJobsFile), ""); -> It is not working!!
            PrintWriter writer = new PrintWriter(new FileOutputStream(allJobsFile), true);
            writer.println("# " + new Date().toString() + " #");
            writer.println();
            // First the non-Onodrim parameters
            for(String parameter: new TreeSet<String>(copyOfOriginalConfiguration.stringPropertyNames()))
                if(!Configuration.paramBelongsToPacket(parameter, Onodrim.PROJECT_NAME))
                    writer.println(parameter + "=" + copyOfOriginalConfiguration.getProperty(parameter));
            // Now the Onodrim parameters
            writer.println("\n\n\n");
            writer.println("# Onodrim parameters #");
            for(String parameter: new TreeSet<String>(copyOfOriginalConfiguration.stringPropertyNames()))
                if(Configuration.paramBelongsToPacket(parameter, Onodrim.PROJECT_NAME))
                    writer.println(parameter + "=" + copyOfOriginalConfiguration.getProperty(parameter));
            // Finally the environment settings and system properties
            writer.println("\n\n\n");
            writer.println("# Environment settings #");
            Map<String, String> env = System.getenv();
            for(String envParamName: env.keySet())
                writer.println(envParamName + "=" + env.get(envParamName));
            writer.println("\n\n\n");
            writer.println("# System settings #");
            Properties systemProps = System.getProperties();
            for(String systemPropName: systemProps.stringPropertyNames())
                writer.println(systemPropName + "=" + systemProps.getProperty(systemPropName));
            // We are done
            writer.close();
        } catch (FileNotFoundException exception) {
            logger.log(Level.SEVERE, "Could not save jobs set configuration in file " + allJobsFile.getAbsolutePath(), exception);
        }
        
        // Now, a bit of checking. It is necessary to keep results from previous jobs?
        if(!overwriteJobPrevResults) {
            // If there are results of previous jobs we have to keep them.
            // So it goes like this: we now read the configuration and results of
            // each one of the previous jobs, then we check in the configurations
            // created which ones have the same configuration of a previous job,
            // and assign to it the results of that job.
            // Yeah, a bit messy... but useful I hope :|
            List<Job> jobsCopy = new ArrayList<Job>(jobs);
            for(File folderInAllResultsDir: allResultsDir.listFiles(new FileFilter() {
                                                                                    @Override
                                                                                    public boolean accept(File file) {
                                                                                        if(!file.isDirectory())
                                                                                            return false;
                                                                                        return true;
                                                                                }})) {
                Properties prevJobConfig = new Properties();
                File prevJobConfigFile = new File(folderInAllResultsDir, Job.CONFIG_STORE_FILE_NAME);
                if(prevJobConfigFile.exists()) {
                    try {
                        prevJobConfig.load(new FileReader(prevJobConfigFile));
                    } catch (FileNotFoundException exception) {
                        throw new Error("Something weird has happened, file " + prevJobConfigFile.getAbsolutePath() + " does not exist anymore?");
                    } catch (IOException exception) {
                        logger.log(Level.SEVERE, "Cannot read prev job configuration from file " + prevJobConfigFile.getAbsolutePath() + ": '" + exception.getMessage() + ", experiment will be repeated", exception);
                        continue;
                    }
                    // Ok, found a previous results file. Now checking if any of the jobs to run have that same configuration.
                    for(Job jobToRun: jobsCopy) {
                        Configuration jobConf = jobToRun.getConfiguration();
                        if(!jobConf.equals(prevJobConfig))
                            continue;
                        // Well, at least the configuration of the previous job has the same configuration parameters, now reading its values
                        boolean allParamsEqual = true;
                        for(String paramName: jobConf.getParameterNames()) {
                            if(!jobConf.getParameter(paramName).equals(prevJobConfig.getProperty(paramName).trim())) {
                                allParamsEqual = false;
                                break;
                            }
                        }
                        if(allParamsEqual) {
                            // Found job with identical configuration! Must assign results from previous job to it
                            // First, reading those results
                            File prevJobResultsFile = new File(folderInAllResultsDir, Job.SUCESSFUL_EXECUTION_REPORT_FILE_NAME);
                            logger.log(Level.FINE, "Results from previous execution found in file " + prevJobResultsFile.getAbsolutePath()
                                                    + ", loading them and discarding job " + jobToRun.getIndex());
                            jobToRun.discardAndLoadResulfsFromFile(prevJobResultsFile);
                            jobsCopy.remove(jobToRun);
                            break;
                        } // if(all parameters are equal) i.e. this is the 'same job'
                    } // for(job in list of jobs to run)
                } // if(exits a results file from a previous execution)
            } // for(folder in global results directory)
        } // if(not overwrite previous job results)
        
        if(parallelJobs == 0)
            parallelJobs = Runtime.getRuntime().availableProcessors();
        
        logger.log(Level.FINE, "Starting jobs execution, " + jobs.size() + " jobs to run in " + parallelJobs + " parallel threads");

        jobsExecutor = new JobsExecutor(parallelJobs, this, watcher);
        jobsExecutor.runJobs(blockUntilFinished);
    }

    public synchronized boolean isRunning() {
        return (jobsExecutor != null) && (jobsExecutor.isRunning());
    }

    protected synchronized void allJobsFinished() {

        logger.log(Level.FINE, "All jobs finished");

        if(jobsRun.size() != jobs.size()) {
            logger.log(Level.SEVERE, "The amount of total jobs run '" + jobsRun.size() + " does not match the amount of configurations '" + jobs.size() + "'");
            throw new IllegalStateException("The amount of total jobs run '" + jobsRun.size() + " does not match the amount of configurations '" + jobs.size() + "'");
        }

        jobsExecutor = null;

        if(resultsTablesConfs == null)
            return;

        logger.log(Level.FINE, "Printing results table");
        printHTMLResultsTables();
    }
    
    /**
     * Write each results table in its corresponding HTLM file. The tables configurations were read
     * from the configuration passed to the instance constructor.
     */
    public void printHTMLResultsTables() {
        
        List<ResultsTable> tables = ResultsTable.buildResultsTables(jobs, resultsTablesConfs);
        
        for(int tableIndex = 0; tableIndex < tables.size(); tableIndex++) {

            ResultsTable table = tables.get(tableIndex);
            ResultsTableConf tableConf = resultsTablesConfs.get(tableIndex);
            
            String htmlTable = null;
            if(table == null)
                htmlTable = "<b>Some error found when creating results table, it could be that not all params were defined</b>";
            else
                htmlTable = table.toHTML();

            // Writing table to file
            File htmlTableFile = new File(allResultsDir, tableConf.getResultFileName() + ".html");
            FileWriter htmlTextTableWriter = null;
            try {
                htmlTextTableWriter = new FileWriter(htmlTableFile);
                htmlTextTableWriter.write(htmlTable);
                htmlTextTableWriter.close();
            } catch (IOException exception) {               
                logger.log(Level.SEVERE, "Could not build file to store results table" + htmlTableFile.getAbsolutePath() + ", exception caught", exception);
            }
        }

    }

    protected synchronized void jobStarted(Job job) {
        logger.log(Level.FINE, "Job " + job.getIndex() + " started");
        jobsRunning.add(job);
    }

    protected synchronized void jobFinished(Job job) {
        logger.log(Level.FINE, "Job " + job.getIndex() + " finished");
        jobsRunning.remove(job);
        jobsRun.add(job);
    }

    public synchronized List<Job> succesfulJobs() {
        List<Job> runJobsCopy = new ArrayList<Job>(jobsRun);
        Iterator<Job> runJobsCopyIter = runJobsCopy.iterator();
        while(runJobsCopyIter.hasNext())
            if(runJobsCopyIter.next().executionFailed())
                runJobsCopyIter.remove();
        return runJobsCopy;
    }

    public synchronized List<Job> failedJobs() {
        List<Job> runJobsCopy = new ArrayList<Job>(jobsRun);
        runJobsCopy.removeAll(succesfulJobs());
        return runJobsCopy;
    }

    /**
     * Utility method that creates the folder where all the jobs results will be stored. If a folder
     * in the same path already exists, then the actions to perform depend on the configuration of
     * the jobs execution: if previous results are to be erased, then that previous folder is deleted
     * and a new one is recreated; if previous results are to be kept then nothing is done
     * @throws JobExecutionException
     */
    private void createAllResultsDir() throws JobExecutionException {
        if (!allResultsDir.exists()) {
            logger.log(Level.INFO, "Creting results dir " + allResultsDir.getAbsolutePath());
            if (!allResultsDir.mkdirs()) {
                logger.log(Level.SEVERE, "Could not create results dir " + allResultsDir.getAbsolutePath());
                throw new JobExecutionException("Could not create results dir " + allResultsDir.getAbsolutePath());
            }
        } else {
            logger.log(Level.INFO, "Results dir " + allResultsDir.getAbsolutePath() + " already exists");
            if (cleanAllPrevResults) {
                logger.log(Level.INFO, "Removing previous results dir " + allResultsDir.getAbsolutePath());
                // Folders in Java must be deleted recursively
                if (!Util.removeDirRecursively(allResultsDir)) {
                    logger.log(Level.SEVERE, "Could not delete previous results dir " + allResultsDir.getAbsolutePath());
                    throw new JobExecutionException("Could not delete previous results dir " + allResultsDir.getAbsolutePath());
                }
                logger.log(Level.INFO, "Recreating results dir " + allResultsDir.getAbsolutePath());
                if (!allResultsDir.mkdirs()) {
                    logger.log(Level.SEVERE, "Could not recreate results dir " + allResultsDir.getAbsolutePath() + " after deleting it");
                    throw new JobExecutionException("Could not recreate results dir " + allResultsDir.getAbsolutePath() + " after deleting it");
                }
            } else {
                logger.log(Level.INFO, "Execution was configured so not to remove previous results, so nothing to do!");
            }
        }
    }
    
    protected File getAllResultsDir() {
        return allResultsDir;
    }
    
}
