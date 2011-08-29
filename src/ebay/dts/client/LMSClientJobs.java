/**
 * This program is licensed under the terms of the eBay Common Development and
 * Distribution License (CDDL) Version 1.0 (the "License") and any subsequent
 * version thereof released by eBay.  The then-current version of the License
 * can be found at http://www.opensource.org/licenses/cddl1.php
 */

package ebay.dts.client;

import com.ebay.marketplace.services.AckValue;
import com.ebay.marketplace.services.CreateUploadJobResponse;
import com.ebay.marketplace.services.GetJobStatusResponse;
import com.ebay.marketplace.services.JobProfile;
import com.ebay.marketplace.services.JobStatus;
import com.ebay.marketplace.services.StartDownloadJobResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author zhuyang
 */
public class LMSClientJobs {

    private static Logger logger = Logger.getLogger("LMSClientJobs.logger");

    public static boolean end2EndUploadJob(String uploadFileName,
            String downloadFileName,String JobStatusQueryInterval) throws Exception {
        logger.info("\n******\nUploadJobEnd2End Job: uploadFileName=" + uploadFileName + " ;;; downloadFileName" + downloadFileName);
        boolean done = false;
        BulkDataExchangeActions bdeActions = new BulkDataExchangeActions();
        FileTransferActions ftActions = new FileTransferActions();
        String jobType = null;
        // verify file to upload
        if (verifyFileForUploadJob(uploadFileName)) {
            jobType = getJobTypeFromXML(uploadFileName);
            if (jobType == null) {
                return false;
            }
        }

        CreateUploadJobResponse response = bdeActions.createUploadJob(jobType);
        if (response.getAck().equals(AckValue.FAILURE) || response.getAck().equals(AckValue.PARTIAL_FAILURE)) {
            System.out.println(response.getErrorMessage().getError().get(0).getMessage());
            return (done = false);
        }
        String fileReferenceId = response.getFileReferenceId();
        String jobId = response.getJobId();
        if (!ftActions.uploadFile(uploadFileName,
                jobId, fileReferenceId)) {
            return (done = false);
        }
        bdeActions.startUploadJob(jobId);
        done = download(downloadFileName, jobId,JobStatusQueryInterval);
        return done;
    }

    public static boolean end2EndDownloadJob(String downloadJobType,
            String downloadFileName,String JobStatusQueryInterval) throws Exception {
        logger.info("\n******\n DownloadJobEnd2End : downloadJobType=" + downloadJobType + " ;;; downloadFileName" + downloadFileName);
        boolean done = false;
        StartDownloadJobResponse sdljResp = startDownloadJob(downloadJobType, null);
        String jobid = sdljResp.getJobId();
        if (jobid == null && jobid.length() == 0) {
            sdljResp.getErrorMessage().getError().get(0);
            return (done);
        }
        return download(downloadFileName, jobid,JobStatusQueryInterval);
    }


    private static boolean download(String downloadFileName, String jobId, String JobStatusQueryInterval) {
        BulkDataExchangeActions bdeActions = new BulkDataExchangeActions();
        FileTransferActions ftActions = new FileTransferActions();
        boolean fileProcessIsDone = false;
        boolean downloadIsDone = false;
        logger.info("JobStatusQueryInterval =========" +JobStatusQueryInterval );
        if (JobStatusQueryInterval.length()==0){

            JobStatusQueryInterval="10000";
        }
        do {
            GetJobStatusResponse getJobStatusResp = bdeActions.getJobStatus(jobId);
            if (getJobStatusResp.getAck().equals(AckValue.FAILURE)) {
                return false;
            }
            List<JobProfile> jobs = getJobStatusResp.getJobProfile();
            Iterator itr = jobs.iterator();
            while (itr.hasNext()) {
                JobProfile job = (JobProfile) itr.next();
                if (job.getJobStatus().equals(JobStatus.COMPLETED) && job.getPercentComplete() == 100.0) {
                    logger.info(job.getJobId() + " : " + job.getJobType() + " : " + job.getJobStatus());
                    fileProcessIsDone = true;
                    if (ftActions.downloadFile(downloadFileName, job.getJobId(), job.getFileReferenceId())) {
                        downloadIsDone = true;
                    }
                } else {
                    logger.info(job.getJobId() + " : " + job.getJobType() + " : " + job.getJobStatus());
                    try {
                        logger.info( " SLEEP FOR " + JobStatusQueryInterval);
                        Thread.sleep(Integer.parseInt(JobStatusQueryInterval));
                    } catch (InterruptedException x) {
                        fileProcessIsDone = false;
                        downloadIsDone = false;
                    }
                }
            // System.out.println(job.getPercentComplete()) ;
            }
        } while (!fileProcessIsDone);
        return downloadIsDone;
    }

    public static boolean uploadJob(String jobId, String fileReferenceId, String uploadFileName) throws Exception {
        boolean done = false;
        // verify file to upload
        if (verifyFileForUploadJob(uploadFileName)) {
            // get JobType from the XML file
            if (getJobTypeFromXML(uploadFileName) == null) {
                return false;
            }
        }
        FileTransferActions ftActions = new FileTransferActions();
        if (ftActions.uploadFile(uploadFileName, jobId, fileReferenceId)) {
            done = true;
        }
        return done;
    }

    public static boolean downloadJob(String jobId, String fileReferenceId, String downloadFileName) throws Exception {
        boolean done = false;
        FileTransferActions ftActions = new FileTransferActions();
        if (ftActions.downloadFile(downloadFileName, jobId, fileReferenceId)) {
            done = true;
        }
        return done;
    }

    public static boolean abortJob(String jobId) throws Exception {
        boolean done = false;
        BulkDataExchangeActions bdeActions = new BulkDataExchangeActions();
        bdeActions.abortJobs(jobId);
        return done;
    }

    public static boolean createUploadJob(String jobType) throws Exception {
        boolean done = false;
        BulkDataExchangeActions bdeActions = new BulkDataExchangeActions();
        bdeActions.createUploadJob(jobType);
        return done;
    }

    public static void startUploadJob(String jobId) throws Exception {
        BulkDataExchangeActions bdeActions = new BulkDataExchangeActions();
        bdeActions.startUploadJob(jobId);
    }

    public static void getJobStatus(String jobId) throws Exception {
        BulkDataExchangeActions bdeActions = new BulkDataExchangeActions();
        bdeActions.getJobStatus(jobId);
    }

    public static void getJobs(String conditionsStr) throws Exception {
        BulkDataExchangeActions bdeActions = new BulkDataExchangeActions();
        bdeActions.getJobs(conditionsStr);
    }
    
    public static void createRecurringJob(String downloadJobType, int frequencyInMinutes) throws Exception {
        BulkDataExchangeActions bdeActions = new BulkDataExchangeActions();
        bdeActions.createRecurringJob(downloadJobType, frequencyInMinutes);
    }
    
    public static void deleteRecurringJob(String recurringJobId) {
        BulkDataExchangeActions bdeActions = new BulkDataExchangeActions();
        bdeActions.deleteRecurringJob(recurringJobId);
    }
    
    public static void getRecurringJobs() throws Exception {
        BulkDataExchangeActions bdeActions = new BulkDataExchangeActions();
        bdeActions.getRecurringJobs();
    }

    public static StartDownloadJobResponse startDownloadJob(String downloadJobType, String startTimeString) throws Exception {
        BulkDataExchangeActions bdeActions = new BulkDataExchangeActions();
        return (bdeActions.startDownloadJob(downloadJobType, startTimeString));
    }

    private static boolean verifyFileForUploadJob(String uploadFileName) {
        // verify file to upload
        boolean found = false;
        BufferedReader file;
        try {
            file = new BufferedReader(new FileReader(uploadFileName));
            String st = file.readLine();
            //logger.info("The first line of " + uploadFileName + " is:" + st);
            logger.info("File existence check passed.");
            found = true;
        } catch (FileNotFoundException e) {
            logger.severe("File '" + uploadFileName + "' not found.");
            return false;
        } catch (IOException e) {
            return false;
        }

        return found;
    }

    private static String getJobTypeFromXML(String inFilename) {
        // get the JobType from the input xml file
        File inputXml = null;
        inputXml = new File(inFilename);
        CreateLMSParser parser = new CreateLMSParser();
        boolean parseOk = parser.parse(inputXml);
        if (!parseOk) {
            logger.severe("Failed to extract the JobType from the file [" + inFilename + "]");
            return null;
        }

        // extract the JObType String successfully
        String jobType = parser.getJobType();
        if (jobType == null) {
            logger.severe("Invalid job type in the XML file.");
        } else {
            logger.info("Found the job type from the XML file, it is " + jobType);
        }
        return jobType;
    }
}
