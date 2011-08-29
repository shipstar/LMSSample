/**
 * This program is licensed under the terms of the eBay Common Development and
 * Distribution License (CDDL) Version 1.0 (the "License") and any subsequent
 * version thereof released by eBay.  The then-current version of the License
 * can be found at http://www.opensource.org/licenses/cddl1.php
 */

package ebay.dts.client;

import com.ebay.marketplace.services.BulkDataExchangeService;
import com.ebay.marketplace.services.BulkDataExchangeServicePort;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;

/**
 *
 * @author zhuyang
 */
public class BulkDataExchangeCall {

    private static Logger logger = Logger.getLogger("BulkDataExchangeCall.logger");
    private static BindingProvider bp;

    public String getCallName() {
        return callName;
    }

    public void setCallName(String callName) {
        this.callName = callName;
    }

    public String getServerURL() {
        return serverURL;
    }

    public void setServerURL(String serverURL) {
        this.serverURL = serverURL;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }
    private String callName;
    private String userToken;
    private String serverURL;

    public BulkDataExchangeCall(String serverURL, String userToken) {
        this.serverURL = serverURL;
        this.userToken = userToken;
    }

    public BulkDataExchangeCall(String callName, String userToken, String serverURL) {
        this.callName = callName;
        this.userToken = userToken;
        this.serverURL = serverURL;
    }

    public BulkDataExchangeCall() {
    }

    public BulkDataExchangeServicePort setRequestContext(String callName) {

        if (this.serverURL == null && this.serverURL.length() == 0) {
            logger.severe(" BulkDataExchangeService endpoint URL is not set ");
            return null;
        }

        BulkDataExchangeServicePort port = null;
        try { // Call Web Service Operation
            BulkDataExchangeService service = new BulkDataExchangeService();
            port = service.getBulkDataExchangeServiceSOAP();
            bp = (BindingProvider) port;
            // Add the logging handler
            List handlerList = bp.getBinding().getHandlerChain();
            if (handlerList == null) {
                handlerList = new ArrayList();
            }
            LoggingHandler loggingHandler = new LoggingHandler();
            handlerList.add(loggingHandler);
            // register the handerList
            bp.getBinding().setHandlerChain(handlerList);
            // initialize WS operation arguments here
            Map requestProperties = bp.getRequestContext();
            requestProperties.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, this.serverURL);
            if (this.userToken == null) {
                throw new Exception(" userToken can't be null ");
            }
            Map<String, List<String>> httpHeaders = new HashMap<String, List<String>>();
            httpHeaders.put("X-EBAY-SOA-MESSAGE-PROTOCOL", Collections.singletonList("SOAP11"));
            httpHeaders.put("X-EBAY-SOA-OPERATION-NAME", Collections.singletonList(callName));
            httpHeaders.put("X-EBAY-SOA-SECURITY-TOKEN", Collections.singletonList(this.userToken));
            requestProperties.put(MessageContext.HTTP_REQUEST_HEADERS, httpHeaders);
        //http://developer.ebay.com/DevZone/bulk-data-exchange/CallRef/createUploadJob.html#Request.uploadJobType

        } catch (Exception ex) {
            ex.printStackTrace();
        // TODO handle custom exceptions here
        }
        return port;
    }

    public BulkDataExchangeServicePort setRequestContext() {
        BulkDataExchangeServicePort port = null;
        try { // Call Web Service Operation
            BulkDataExchangeService service = new BulkDataExchangeService();
            port = service.getBulkDataExchangeServiceSOAP();
            bp = (BindingProvider) port;
            // Add the logging handler
            List handlerList = bp.getBinding().getHandlerChain();
            if (handlerList == null) {
                handlerList = new ArrayList();
            }
            LoggingHandler loggingHandler = new LoggingHandler();
            handlerList.add(loggingHandler);
            // register the handerList
            bp.getBinding().setHandlerChain(handlerList);
            // initialize WS operation arguments here
            Map requestProperties = bp.getRequestContext();
            // set http address
            if (this.serverURL == null) {
                throw new Exception(" serverURL can't be null ");
            }
            requestProperties.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, serverURL);

            Map<String, List<String>> httpHeaders = new HashMap<String, List<String>>();
            httpHeaders.put("X-EBAY-SOA-MESSAGE-PROTOCOL", Collections.singletonList("SOAP11"));
            httpHeaders.put("X-EBAY-SOA-OPERATION-NAME", Collections.singletonList(this.callName));
            httpHeaders.put("X-EBAY-SOA-SECURITY-TOKEN", Collections.singletonList(this.userToken));

            requestProperties.put(MessageContext.HTTP_REQUEST_HEADERS, httpHeaders);
            retrieveHttpHeaders(bp,"Response");
        } catch (Exception ex) {
            ex.printStackTrace();
        // TODO handle custom exceptions here
        }
        return port;
    }

    private Map<String, Object> retrieveHttpHeaders(BindingProvider bp, String headerType) {
        System.out.println("headerType " + headerType);

        Map<String, Object> headerM = null;
        Map<String, Object> contextMap = null;
        String headerTypeName = null;
        if (headerType.equalsIgnoreCase("request")) {
            headerTypeName = "javax.xml.ws.http.request.headers";
            contextMap =
                    bp.getRequestContext();
        } else {
            headerTypeName = "javax.xml.ws.http.response.headers";
            contextMap =
                    bp.getResponseContext();
        }

        if (contextMap != null) {
            dumpMap(headerType + " context", contextMap);
            Map requestHeaders = (Map<String, List<String>>) contextMap.get(headerTypeName);
            if (requestHeaders != null) {
                headerM = insertHttpsHeadersMap(headerType, requestHeaders);
            }

        }
        return headerM;
    }

    public static Map insertHttpsHeadersMap(String name, Map<String, List<String>> maplist) {
        System.out.println("=== " + name);
        Map headers = new HashMap<String, Object>();
        Iterator headerIter = null;
        if (maplist != null) {
            maplist.entrySet();
            headerIter = maplist.keySet().iterator();
            while (headerIter.hasNext()) {
                String key = (String) headerIter.next();
                 System.out.print("Key: " + key);
                List l = (List<String>) maplist.get(key);
                Iterator iter = l.iterator();
                String value = null;
                while (iter.hasNext()) {
                    value = (String) iter.next();
                  System.out.println("; Value: " + value);
                }
                headers.put(key, value);
            }

        }
        return headers;
    }

    public static void dumpMap(String name, Map<String, Object> map) {
        System.out.println("=== " + name);
        for (Map.Entry e : map.entrySet()) {
            System.out.println(e.getKey() + " : " + e.getValue());
        }
    }
}
