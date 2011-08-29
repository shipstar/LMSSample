/**
 * This program is licensed under the terms of the eBay Common Development and
 * Distribution License (CDDL) Version 1.0 (the "License") and any subsequent
 * version thereof released by eBay.  The then-current version of the License
 * can be found at http://www.opensource.org/licenses/cddl1.php
 */

package ebay.dts.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 *
 * @author zhuyang
 */
public class LMSSample {

    private static Properties prop;
    private static String JobStatusQueryInterval = "";
    private static final String[] commandArray = {
        "UploadJobEnd2End", "UploadFile", "DownloadFile",
        "getJobs", "abortJob", "createUploadJob",
        "startUploadJob", "getJobStatus", "startDownloadJob",
        "DownloadJobEnd2End"};
    private static Logger logger = Logger.getLogger("LMSSample.logger");
    /*
     * main entry point of the program
     *
     */

    public LMSSample() {
        prop = new Properties();
        try {
            String path = (new java.io.File(".").getCanonicalPath());
            String CONFIG_PROPERTIES = path + System.getProperty("file.separator") + "configuration.xml";
            if (CONFIG_PROPERTIES == null && CONFIG_PROPERTIES.length() == 0) {
                logger.severe(" the configuration.xml roperties file is not found.");
                System.exit(0);
            }
            prop.loadFromXML(new FileInputStream(CONFIG_PROPERTIES));
            prop.getProperty("getJobStatusQueryInterval");
            logger.info("LMSSample constructor : JobStatusQueryInterval=====" + JobStatusQueryInterval);
        } catch (IOException ioe) {
        }
    }

    public static void main(String args[]) throws Exception {

        if (args.length < 1) {
            // if no parameters are given, then it print out the description of available commands
            System.out.println(printUsage());
            return;
        }
        String action = args[0];

        int actionCode = 0;
        for (int i = 0; i < commandArray.length; i++) {
            if (commandArray[i].equalsIgnoreCase(action)) {
                actionCode = i;
                logger.config("The command argument===> action command=" + action + " |||||  actionCode =" + actionCode);
            }
        }
        executeCmd(args, actionCode);
    }

    private static void executeCmd(String args[], int actionCode) throws Exception {
        String uploadFileName = "", downloadFileName = "";
        String jobId = "", fileRefId = "", uploadXML = "", jobType = "";
        String JobStatusQueryIntervalLocal = "";
        if (prop == null) {
            prop = new Properties();
            try {
                String path = (new java.io.File(".").getCanonicalPath());
                String CONFIG_PROPERTIES = path + System.getProperty("file.separator") + "configuration.xml";
                if (CONFIG_PROPERTIES == null && CONFIG_PROPERTIES.length() == 0) {
                    logger.severe(" the configuration.xml roperties file is not found.");
                    System.exit(0);
                }
                prop.loadFromXML(new FileInputStream(CONFIG_PROPERTIES));
                JobStatusQueryIntervalLocal=prop.getProperty("getJobStatusQueryInterval");
                logger.info("LMSSample.executeCmd : JobStatusQueryInterval=====" + JobStatusQueryIntervalLocal);
            } catch (IOException ioe) {
            }


        }

        switch (actionCode) {
            case 0:
                 {//"UploadJobEnd2End"
                    uploadFileName = args[1];
                    downloadFileName = args[2];
                    logger.info("\n******\nIn LMSSample.main() ===> UploadJobEnd2End Job: uploadFileName=" + uploadFileName + " ;;; downloadFileName" + downloadFileName);
                    if (LMSClientJobs.end2EndUploadJob(uploadFileName, downloadFileName, JobStatusQueryIntervalLocal)) {
                        logger.info("\n******\nUploadJobEnd2End finished successfully.");
                    } else {
                        logger.info("UploadJobEnd2End failed.");
                    }
                }
                break;
            case 1:
                 {// "UploadFile"
                    jobId = args[1];
                    fileRefId = args[2];
                    uploadXML = args[3];
                    LMSClientJobs.uploadJob(jobId, fileRefId, uploadXML);
                }
                break;
            case 2:  //"DownloadFile"
                 {
                    jobId = args[1];
                    fileRefId = args[2];
                    downloadFileName = args[3];
                    LMSClientJobs.downloadJob(jobId, fileRefId, downloadFileName);
                }
                break;
            case 3:
                 { //"getJobs"

                    String conditionsString = null;
                    if (args.length == 2) {
                        conditionsString = args[1];
                        System.out.println("conditionsString : " + conditionsString);
                    }
                    LMSClientJobs.getJobs(conditionsString);
                }
                break;
            case 4:
                 {//"createUploadJob"
                    jobId = args[1];
                    LMSClientJobs.abortJob(jobId);
                }
                break;
            case 5:
                 {//"creatUploadJob"

                    jobType = args[1];
                    LMSClientJobs.createUploadJob(jobType);
                }
                break;
            case 6:
                 {//"startUploadJob"

                    jobId = args[1];
                    LMSClientJobs.startUploadJob(jobId);
                }
                break;
            case 7:
                 {//"getJobStatus"
                    jobId = args[1];
                    LMSClientJobs.getJobStatus(jobId);
                }

                break;
            case 8:
                 { //"startDownloadJob"
                    String startTimeString = null;
                    jobType = args[1];
                    if (args.length == 3) {
                        startTimeString = args[2];
                    }
                    LMSClientJobs.startDownloadJob(jobType, startTimeString);
                }
                break;
            case 9:
                 { //"end2EndDownloadJob"

                    if (args.length == 3) {
                        jobType = args[1];
                        downloadFileName = args[2];
                    }
                    LMSClientJobs.end2EndDownloadJob(jobType, downloadFileName, JobStatusQueryIntervalLocal);
                }
                break;

            default:
                 {
                    // print out the description of available commands
                    System.out.println(printUsage());
                }
                break;
        }
    } // ENDOF executeCmd()

    private static String printUsage() {
        StringBuilder sb = new StringBuilder("");
        int actionCommand = 0;
        sb.append("LMSSample sample commands\n");
        sb.append("=========================\n");
        sb.append("#1: java -jar LMSSample.jar UploadJobEnd2End D:/ReviseFixedPriceItem.xml D:/download1.zip\n");
        sb.append("-------------------\n");
        sb.append("Input parameters\n");
        sb.append("1.Action String (UploadJobEnd2End)\n");
        sb.append("2.Location of XML file to upload (D:/ReviseFixedPriceItem.xml)\n");
        sb.append("3.File name for the download attachment (D:/download1.zip)\n");
        sb.append("\n");
        sb.append("This command does the following steps:\n");
        sb.append("1.Get the JobType from the input XML file\n");
        sb.append("2.Compress the input XML file into gzip file\n");
        sb.append("3.Call BDX createUploadloadJob service to create a job\n");
        sb.append("4.Call FTS uploadFile service to upload the gzip file that was created in Step #2\n");
        sb.append("5.Call BDX startUploadJob service to start the job that was created in Step #3\n");
        sb.append("6.Call BDX getJobStatus service to see if the Job is completed\n");
        sb.append("7.If the JobStatus returned is not yet Completed,  then sleep for 10 seconds (configurable in configuration.xml) and repeat Step #5. When the JobStatus returns Completed, move to Step #7 \n");
        sb.append("8.Call FTS downloadFile service to download the result file that is specified by the 3rd input parameter\n");
        sb.append("\n");
        sb.append("\n");
        sb.append("#2: java -jar LMSSample.jar UploadFile 5000000636 50000000236 D:/ReviseFixedPriceItem.xml \n");
        sb.append("-------------------\n");
        sb.append("Input parameters:\n");
        sb.append("1.Action String (UploadFile)\n");
        sb.append("2.TaskRefId (This is returned in the createUploadloadJob service call)\n");
        sb.append("3.FileRefId (This is returned in the createUploadloadJob service call)\n");
        sb.append("4.Full file name of the XML file\n");
        sb.append("5.useSOAP or useHTTP to indicate which protocol the invocation will use\n");
        sb.append("This command does the following steps:\n");
        sb.append("1.Compress the input XML file into gzip file\n");
        sb.append("2.Call FTS uploadFile service to upload file where JobID is the 2nd parameter and FileReferenceID is the 3rd parameter \n");
        sb.append("\n");
        sb.append("\n");
        sb.append("#3: java -jar LMSSample.jar DownloadFile 5000000636 50000000236 D:/downloadResult.zip \n");
        sb.append("\n");
        sb.append("Input parameters:\n");
        sb.append("1.Action String (DownloadFile)\n");
        sb.append("2.TaskRefId (This is returned in the createUploadloadJob service call)\n");
        sb.append("3.FileRefId (This is returned in the createUploadloadJob service call)\n");
        sb.append("4.Full file name of the file it will save locally\n");
        sb.append("5.useSOAP or useHTTP to indicate which protocol the invocation will use\n");
        sb.append("This command does the following steps:\n");
        sb.append("1.Call FTS downloadFile to download file for the job where JobID is the 2nd parameter and FileReferenceID is the 3rd parameter \n");
        sb.append("\n");
        sb.append("\n");
        sb.append("#4: java -jar LMSSample.jar getJobs creationTimeFrom=2008-09-01&creationTimeTo=2008-10-02&jobType=RelistFixedPriceItem&jobStatus=Failed\n");
        sb.append("\n");
        sb.append("Input parameters:\n");
        sb.append("1.Action String (getJobs)\n");
        sb.append("2.query criteria string\n");
        sb.append("This command calls BDX getJobs service to get job profiles that satisfy the query criteria.\n");
        sb.append("\n");
        sb.append("\n");
        sb.append("#5: java -jar LMSSample.jar abortJob 5000000636\n");
        sb.append("\n");
        sb.append("Input parameters:\n");
        sb.append("1.Action String (abortJob)\n");
        sb.append("2.JobID\n");
        sb.append("This command calls BDX abortJob service to abort an unterminated job where JobID is the 2nd parameter.\n");
        sb.append("\n");
        sb.append("\n");
        sb.append("#6: java -jar LMSSample.jar createUploadJob ReviseFixedPriceItem\n");
        sb.append("\n");
        sb.append("Input parameters:\n");
        sb.append("1.Action String (createUploadJob)\n");
        sb.append("2.JobType\n");
        sb.append("This command calls BDX createUploadJob service to create an upload job with the given JobType (e.g. ReviseFixedPriceItem).\n");
        sb.append("\n");
        sb.append("\n");
        sb.append("#7: java -jar LMSSample.jar startUploadJob 5000000636\n");
        sb.append("\n");
        sb.append("Input parameters:\n");
        sb.append("1.Action String (startUploadJob)\n");
        sb.append("2.JobID\n");
        sb.append("This command calls BDX startUploadJob service to start the created upload job where JobID is the 2nd parameter.\n");
        sb.append("\n");
        sb.append("\n");
        sb.append("#8: java -jar LMSSample.jar getJobStatus 5000000636\n");
        sb.append("\n");
        sb.append("Input parameters:\n");
        sb.append("1.Action String (getJobStatus)\n");
        sb.append("2.JobID\n");
        sb.append("This command calls BDX getJobStatus service to query the job status where JobID is the 2nd parameter.\n");
        sb.append("\n");
        sb.append("\n");
        sb.append("#9: a> java -jar LMSSample.jar startDownloadJob SoldReport\n");
        sb.append("    b> java -jar LMSSample.jar startDownloadJob SoldReport 2008-10-10_12:0:0\n");
        sb.append("\n");
        sb.append("Input parameters:\n");
        sb.append("1.Action String (startDownloadJob)\n");
        sb.append("2.JobType (ActiveInventoryReport, SoldReport, or FeeSettlementReport)\n");
        sb.append("3.startTime (Optional) format = \"yyyy-mm-dd_hh:mm:ss\"\n");
        sb.append("This command calls BDX startDownloadJob service to start the download job, it returns a JobID.\n");
        sb.append("\n");
        sb.append("\n");
        sb.append("#10: java -jar LMSSample.jar DownloadJobEnd2End SoldReport downloadResult.zip \n");
        sb.append("-------------------\n");
        sb.append("Input parameters\n");
        sb.append("1.Action String (DownloadJobEnd2End)\n");
        sb.append("2.File name for the download attachment (C:/download2.zip)\n");
        sb.append("This command does the following steps:\n");
        sb.append("1.Call BDX startDownloadJob to start processing the data for a report file to download\n");
        sb.append("2.Call BDX getJobStatus service to see if the Job is completed\n");
        sb.append("3.If the JobStatus returned is not yet Completed,  then sleep for 10 seconds (configurable in configuration.xml) and repeat Step #2. When the JobStatus returns Completed, move to Step #3 \n");
        sb.append("4.Call FTS downloadFile service to download the result file that is specified by the 2nd input parameter\n");
        sb.append("\n");

        return sb.toString();
    }
}
