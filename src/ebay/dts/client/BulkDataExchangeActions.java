/**
 * This program is licensed under the terms of the eBay Common Development and
 * Distribution License (CDDL) Version 1.0 (the "License") and any subsequent
 * version thereof released by eBay.  The then-current version of the License
 * can be found at http://www.opensource.org/licenses/cddl1.php
 */

package ebay.dts.client;

import com.ebay.marketplace.services.*;
import com.ebay.marketplace.services.JobProfile;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 *
 * @author zhuyang
 */
public class BulkDataExchangeActions {

    private BulkDataExchangeCall call;
    private static Logger logger = Logger.getLogger(" BulkDataExchangeActions.logger");

    public BulkDataExchangeActions() {
        Properties prop = new Properties();

        try {
            String path = (new java.io.File(".").getCanonicalPath());
            String CONFIG_PROPERTIES = path + System.getProperty("file.separator") + "configuration.xml";
            if (CONFIG_PROPERTIES == null && CONFIG_PROPERTIES.length() == 0) {
                System.err.println("configuration.xml properties file is not found.   ");
                System.exit(0);
            }
            //System.out.println("properties file is : " + CONFIG_PROPERTIES);
            prop.loadFromXML(new FileInputStream(CONFIG_PROPERTIES));
            call = new BulkDataExchangeCall(prop.getProperty("bulkDataExchangeURL"), prop.getProperty("userToken"));
        } catch (IOException ioe) {

            ioe.toString();
        }
    }

    public GetJobsResponse getJobs(String conditionsStr) throws Exception {
        String callName = "getJobs";
        BulkDataExchangeServicePort port = call.setRequestContext(callName);
        com.ebay.marketplace.services.GetJobsRequest getJobsReq = new com.ebay.marketplace.services.GetJobsRequest();

        if (conditionsStr != null && conditionsStr.length() > 0) {
            XMLGregorianCalendar fromCal = null;
            // validating the input conditions
            XMLGregorianCalendar toCal = null;
            // java.util.Map<String, String> conditions = getPairs(conditionsStr, "&");
            java.util.Map<String, String> conditions = getPairs(conditionsStr, "&");
            if (conditions.get("creationTimeFrom") != null && !conditions.get("creationTimeFrom").equals("")) {
                fromCal = parseDate(conditions.get("creationTimeFrom"));
                if (fromCal == null) {
                    logger.info("Criteria creationTimeFrom has been ignored.");
                }
                getJobsReq.setCreationTimeFrom(fromCal);
            }
            if (conditions.get("creationTimeTo") != null && !conditions.get("creationTimeTo").equals("")) {
                toCal = parseDate(conditions.get("creationTimeTo"));
                if (toCal == null) {
                    logger.info("Criteria creationTimeTo has been ignored.");
                }
                getJobsReq.setCreationTimeTo(toCal);
            }

            if (conditions.get("jobType") != null && !conditions.get("jobType").equals("")) {
                List<String> jobType = getJobsReq.getJobType();
                jobType.add(conditions.get("jobType"));
            }

            if (conditions.get("jobStatus") != null && !conditions.get("jobStatus").equals("")) {
                logger.info(" jobStatus condition= " + conditions.get("jobStatus").toString());
                List<JobStatus> jobStatuss = getJobsReq.getJobStatus();
                try {
                    jobStatuss.add(JobStatus.fromValue(conditions.get("jobStatus")));
                } catch (java.lang.IllegalArgumentException e) {
                    logger.severe("Service call failed on the client side. \nThe input jobStatus is not valid, please check it and try again.");
                    return null;
                }
            }
        }
        com.ebay.marketplace.services.GetJobsResponse getJobsResp = port.getJobs(getJobsReq);
        List<JobProfile> jobs = getJobsResp.getJobProfile();
        Iterator itr = jobs.iterator();
        while (itr.hasNext()) {
            JobProfile job = (JobProfile) itr.next();
            logger.info(job.getJobId() + " : " + job.getJobType() + " : " + job.getJobStatus());
        }

        return getJobsResp;

    }//ENDOF getJobs()

    public GetJobStatusResponse getJobStatus(String jobId) {
        String callName = "getJobStatus";
        BulkDataExchangeServicePort port = call.setRequestContext(callName);
        com.ebay.marketplace.services.GetJobStatusRequest req = new com.ebay.marketplace.services.GetJobStatusRequest();
        req.setJobId(jobId);

        com.ebay.marketplace.services.GetJobStatusResponse getJobsResp = port.getJobStatus(req);
        return getJobsResp;
    }//ENDOF getJobStatus()

    public void abortJobs(String jobId) {
        String callName = "abortJob";
        BulkDataExchangeServicePort port = call.setRequestContext(callName);
        com.ebay.marketplace.services.AbortJobRequest abortJobsReq = new com.ebay.marketplace.services.AbortJobRequest();
        abortJobsReq.setJobId(jobId);
        //com.ebay.marketplace.services.AbortJobResponse abortJobsResp = port.abortJob(abortJobsReq);


    }//ENDOF abortJobs()

    public CreateUploadJobResponse createUploadJob(String uploadJobType) {
        String callName = "createUploadJob";
        BulkDataExchangeServicePort port = call.setRequestContext(callName);
        CreateUploadJobRequest createUploadJobReq = new CreateUploadJobRequest();
        createUploadJobReq.setUploadJobType(uploadJobType);// ("AddFixedPriceItem");
        String uuid = java.util.UUID.randomUUID().toString();
        createUploadJobReq.setUUID(uuid);
        // process result here
        CreateUploadJobResponse result = port.createUploadJob(createUploadJobReq);
        return result;

    }// ENDOF createUploadJob()

    public StartDownloadJobResponse startDownloadJob(String downloadJobType, String startTimeString) throws Exception {
        String callName = "startDownloadJob";
        BulkDataExchangeServicePort port = call.setRequestContext(callName);

        com.ebay.marketplace.services.StartDownloadJobRequest startDownloadReq = new StartDownloadJobRequest();
        startDownloadReq.setDownloadJobType(downloadJobType);

        XMLGregorianCalendar xmlGregorianStart = null;
        if (startTimeString != null && startTimeString.length() > 0) {
            FeeSettlementReportFilter feeFilter = new FeeSettlementReportFilter();
            DownloadRequestFilter reqFilter = new DownloadRequestFilter();
            xmlGregorianStart = parseDateTime(startTimeString);
            feeFilter.setStartTime(xmlGregorianStart);
            reqFilter.setFeeSettlementReportFilter(feeFilter);
            startDownloadReq.setDownloadRequestFilter(reqFilter);
        }
        String uuid = java.util.UUID.randomUUID().toString();
        startDownloadReq.setUUID(uuid);
        // process result here
        StartDownloadJobResponse result = port.startDownloadJob(startDownloadReq);
        return result;

    }// ENDOF startDownloadJob()

    public void startUploadJob(String jobid) {
        String callName = "startUploadJob";
        BulkDataExchangeServicePort port = call.setRequestContext(callName);
        com.ebay.marketplace.services.StartUploadJobRequest request = new StartUploadJobRequest();

        request.setJobId(jobid);
        // process result here
        StartUploadJobResponse result = port.startUploadJob(request);

    } // ENDOF startUploadJob();
    
    public CreateRecurringJobResponse createRecurringJob(String downloadJobType, int frequencyInMinutes) {
      String callName = "createRecurringJob";
      BulkDataExchangeServicePort port = call.setRequestContext(callName);
      CreateRecurringJobRequest createRecurringJobReq = new CreateRecurringJobRequest();
      createRecurringJobReq.setDownloadJobType(downloadJobType);
      createRecurringJobReq.setFrequencyInMinutes(frequencyInMinutes);
      
      String uuid = java.util.UUID.randomUUID().toString();
      createRecurringJobReq.setUUID(uuid);
      // process result here
      CreateRecurringJobResponse result = port.createRecurringJob(createRecurringJobReq);
      return result;
    } // ENDOF createRecurringJob();
    
    public DeleteRecurringJobResponse deleteRecurringJob(String recurringJobId) {
      String callName = "deleteRecurringJob";
      BulkDataExchangeServicePort port = call.setRequestContext(callName);
      DeleteRecurringJobRequest deleteRecurringJobReq = new DeleteRecurringJobRequest();
      deleteRecurringJobReq.setRecurringJobId(recurringJobId);
      
      DeleteRecurringJobResponse result = port.deleteRecurringJob(deleteRecurringJobReq);
      return result;
    } // ENDOF deleteRecurringJob();
    
    public void getRecurringJobs() {
      String callName = "getRecurringJobs";
      BulkDataExchangeServicePort port = call.setRequestContext(callName);
      com.ebay.marketplace.services.GetRecurringJobsRequest request = new GetRecurringJobsRequest();
      
      GetRecurringJobsResponse result = port.getRecurringJobs(request);
    } // ENDOF getRecurringJobs();

    /*
     * util method, construct the Calendar object from the input string,
     * it accepts format of "yyyy-MM-dd"
     */
    private static XMLGregorianCalendar parseDate(String cal) throws DatatypeConfigurationException {
        GregorianCalendar ret = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {
            df.parse(cal);
            String[] parts = cal.split("-");
            ret = (GregorianCalendar) Calendar.getInstance();
            ret.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1,
                    Integer.parseInt(parts[2]), 0, 0, 0);
        // get here and we know the format is correct
        } catch (ParseException e) {
            System.out.println("ParseException caught when parsing your date string, please fix it and retry.");
            return null;
        }
        DatatypeFactory factory = DatatypeFactory.newInstance();
        XMLGregorianCalendar xmlGregorianCalendar = factory.newXMLGregorianCalendar(ret);
        return xmlGregorianCalendar;
    }



    private static XMLGregorianCalendar parseDateTime(String cal) throws DatatypeConfigurationException {

        DatatypeFactory factory1 = DatatypeFactory.newInstance();
        XMLGregorianCalendar calendar1 = factory1.newXMLGregorianCalendar();
        String[] dateItems;
        try {
            // df.parse(cal);
            if (cal.indexOf("_") == -1) {
                dateItems = cal.split("-");
                calendar1.setYear(Integer.parseInt(dateItems[0]));
                calendar1.setMonth(Integer.parseInt(dateItems[1]));
                calendar1.setDay(Integer.parseInt(dateItems[2]));
               // calendar1.setTime(00, 00, 00,000);
                calendar1.setTime(00, 00, 00);
            } else  {
                String[] parts = cal.split("_");
                dateItems = parts[0].split("-");
                String[] timeItems = parts[1].split(":");
                calendar1.setYear(Integer.parseInt(dateItems[0]));
                calendar1.setMonth(Integer.parseInt(dateItems[1]));
                calendar1.setDay(Integer.parseInt(dateItems[2]));
                if (timeItems.length != 0) {
                    switch (timeItems.length) {
                        case 1: {
                            calendar1.setTime(Integer.parseInt(timeItems[0]), 00, 00, 000);
                            break;
                        }
                        case 2: {
                            calendar1.setTime(Integer.parseInt(timeItems[0]), Integer.parseInt(timeItems[1]), 00, 000);
                            break;
                        }
                        case 3: {
                            calendar1.setTime(Integer.parseInt(timeItems[0]),
                                    Integer.parseInt(timeItems[1]),
                                    Integer.parseInt(timeItems[2]), 000);
                            break;
                        }
                        case 4: {
                            calendar1.setTime(Integer.parseInt(timeItems[0]),
                                    Integer.parseInt(timeItems[1]),
                                    Integer.parseInt(timeItems[2]),
                                    Integer.parseInt(timeItems[3]));
                            break;
                        }
                    }

                }
            }

        // get here and we know the format is correct
        } catch (java.lang.NumberFormatException e) {
            System.out.println("NumberFormatException caught when parse the DateTime string: " + cal);

            return null;
        }

        calendar1.setTimezone(0);
        System.out.println(calendar1.toXMLFormat());
        return calendar1;
    }

    /*
     * getPairs method parse the getJobs condition String, and put the name
     * value pair in to Map object for easier accessing. Sample condition string
     * can be:
     * "creationTimeFrom=2008-09-01&creationTimeTo=2008-10-02&jobType=RelistItem&jobStatus=Failed"
     * if the delimited char is "&"
     */
    public static Map<String, String> getPairs(String pairs, String splitStr) {
        if (pairs == null || pairs.equals("")) {
            return null;
        }
        String[] reqFields = pairs.split(splitStr);
        Map<String, String> parameterMap = new HashMap<String, String>();
        for (int i = 0; i < reqFields.length; i++) {
            String nameValuepair = reqFields[i];
            String[] nameValueArray = nameValuepair.split("=");
            parameterMap.put(nameValueArray[0].trim(),
                    nameValueArray.length <= 1 ? null : nameValueArray[1].trim());
        }
        return parameterMap;
    }


}
