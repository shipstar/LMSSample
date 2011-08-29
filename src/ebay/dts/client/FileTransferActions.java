/**
 * This program is licensed under the terms of the eBay Common Development and
 * Distribution License (CDDL) Version 1.0 (the "License") and any subsequent
 * version thereof released by eBay.  The then-current version of the License
 * can be found at http://www.opensource.org/licenses/cddl1.php
 */

package ebay.dts.client;

import com.ebay.marketplace.services.AckValue;
import com.ebay.marketplace.services.DownloadFileResponse;
import com.ebay.marketplace.services.FileAttachment;
import com.ebay.marketplace.services.FileTransferServicePort;
import com.ebay.marketplace.services.UploadFileRequest;
import com.ebay.marketplace.services.UploadFileResponse;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;

/**
 *
 * @author zhuyang
 */
public class FileTransferActions {

    FileTransferCall call;
    private static Logger logger = Logger.getLogger(" FileTransferActions.logger");
    public FileTransferActions() {
        Properties prop = new Properties();
        try {
            String path = (new java.io.File(".").getCanonicalPath());
            String CONFIG_PROPERTIES = path + System.getProperty("file.separator")+"configuration.xml";
             if (CONFIG_PROPERTIES == null && CONFIG_PROPERTIES.length() == 0) {
                System.err.println(" the configuration.xml roperties file is not found.");
                System.exit(0);
            }
            logger.info("properties file is : " + CONFIG_PROPERTIES);
            prop.loadFromXML(new FileInputStream(CONFIG_PROPERTIES));
            call = new FileTransferCall(
                    prop.getProperty("fileTransferURL"),
                    prop.getProperty("userToken"));
        } catch (IOException ioe) {
            ioe.toString();
        }
    }

    public boolean uploadFile(String xmlFile,String jobId, String fileReferenceId ) {
        String callName = "uploadFile";
        boolean uploadFileOK = false;
        try {
            // get JobType from the XML file
           /* String jobType = getJobTypeFromXML(xmlFile);
            if (jobType == null) {
                return (uploadFileOK = false);
            }*/

            String compressedFileName = compressFileToGzip(xmlFile);
            if (compressedFileName == null) {
                System.out.println("\nFailed to compress your XML file into gzip file. Aborted.");
                return (uploadFileOK = false);
            }
            FileTransferServicePort port = call.setFTSMessageContext(callName);
            UploadFileRequest request = new UploadFileRequest();
            FileAttachment attachment = new FileAttachment();
            File fileToUpload = new File(compressedFileName);
            DataHandler dh = new DataHandler(new FileDataSource(fileToUpload));
            attachment.setData(dh);
            attachment.setSize(fileToUpload.length());
            String fileFormat = "gzip";
            request.setFileFormat(fileFormat);
            /*
             *For instance, the Bulk Data Exchange Service uses a job ID as a primary identifier,
             * so, if you're using the Bulk Data Exchange Service, enter the job ID as the taskReferenceId.
             */

            request.setTaskReferenceId(jobId);
            request.setFileReferenceId(fileReferenceId);
            request.setFileAttachment(attachment);
            //request.
            if (port != null && request != null) {
                UploadFileResponse response = port.uploadFile(request);
                if (response.getAck().equals(AckValue.SUCCESS)) {
                    return (uploadFileOK = true);
                }else {
                    logger.severe(response.getErrorMessage().getError().get(0).getMessage());
                     return (uploadFileOK = false);
                }
            }

        } catch (Exception e) {

            logger.severe(e.getMessage());
            return (uploadFileOK = false);
        }
        return uploadFileOK;
    }

    public boolean downloadFile(String fileName, String jobId,String fileReferenceId ) {
        boolean downloadOK = false;
        String callName = "downloadFile";
        try {
            FileTransferServicePort port = call.setFTSMessageContext(callName);
            com.ebay.marketplace.services.DownloadFileRequest request = new com.ebay.marketplace.services.DownloadFileRequest();
            request.setFileReferenceId(fileReferenceId);
            request.setTaskReferenceId(jobId);
            DownloadFileResponse response = port.downloadFile(request);
            if (response.getAck().equals(AckValue.SUCCESS)) {
                System.out.println(response.getAck().SUCCESS);
                downloadOK = true;
            }else {
                 System.out.println(response.getErrorMessage().getError().get(0).getMessage());
                return (downloadOK = false);
            }
            FileAttachment attachment = response.getFileAttachment();
            DataHandler dh = attachment.getData();
            try {
                InputStream in = dh.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(in);

                FileOutputStream fo = new FileOutputStream(new File(fileName)); // "C:/myDownLoadFile.gz"
                BufferedOutputStream bos = new BufferedOutputStream(fo);
                int bytes_read = 0;
                byte[] dataBuf = new byte[4096];
                while ((bytes_read = bis.read(dataBuf)) != -1) {
                    bos.write(dataBuf, 0, bytes_read);
                }
                bis.close();
                bos.flush();
                bos.close();
                System.out.println("File attachment has been saved successfully to " + fileName);

            } catch (IOException e) {
                logger.severe("\nException caught while trying to save the attachement.");
                return (downloadOK = false);
            }
        } catch (Exception e) {
            e.fillInStackTrace();
            return (downloadOK = false);
        }
        return downloadOK;
    }

    private static String compressFileToGzip(String inFilename) {
        // compress the xml file into gz file in the save folder
        String outFilename = null;
        String usingPath = inFilename.substring(0, inFilename.lastIndexOf(File.separator) + 1);
        String fileName = inFilename.substring(inFilename.lastIndexOf(File.separator) + 1);
        outFilename = usingPath + fileName + ".gz";

        try {
            BufferedReader in = new BufferedReader(new FileReader(inFilename));
            BufferedOutputStream out = new BufferedOutputStream(
                    new GZIPOutputStream(new FileOutputStream(outFilename)));
            logger.info("Writing gz file...");
            int c;
            while ((c = in.read()) != -1) {
                out.write(c);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            logger.severe("Cannot find file: " + inFilename);
        } catch (IOException e) {
           logger.severe("IOException:" + e.toString());
        }
        logger.info("The compressed file has been saved to " + outFilename);
        return outFilename;
    }

}
