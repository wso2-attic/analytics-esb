/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.das.integration.common.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsConstants;
import org.wso2.carbon.analytics.spark.core.util.PublishingPayload;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.xml.bind.DatatypeConverter;

/**
 *   Class contains the utility methods required by integration tests
 */
public class Utils {
    
    private static final Log logger = LogFactory.getLog(Utils.class);

    //use this method since HttpRequestUtils.doGet does not support HTTPS.
    public static HttpResponse doGet(String endpoint, Map<String, String> headers) throws
                                                                                   IOException {
        HttpResponse httpResponse;
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setDoOutput(true);
        conn.setReadTimeout(30000);
        //setting headers
        if (headers != null && headers.size() > 0) {
            for (String key : headers.keySet()) {
                if (key != null) {
                    conn.setRequestProperty(key, headers.get(key));
                }
            }
            for (String key : headers.keySet()) {
                conn.setRequestProperty(key, headers.get(key));
            }
        }
        conn.connect();
        // Get the response
        StringBuilder sb = new StringBuilder();
        BufferedReader rd = null;
        try {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            httpResponse = new HttpResponse(sb.toString(), conn.getResponseCode());
            httpResponse.setResponseMessage(conn.getResponseMessage());
        } catch (IOException ignored) {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            httpResponse = new HttpResponse(sb.toString(), conn.getResponseCode());
            httpResponse.setResponseMessage(conn.getResponseMessage());
        } finally {
            if (rd != null) {
                rd.close();
            }
        }
        return httpResponse;
    }
    
    /**
     * Create a compressed event string.
     * 
     * @param messageId             Message Id of the esb event
     * @param proxyName             Name of the proxy of the event
     * @param noOfMediators         Number of mediators to be inlcuded in the event
     * @param payloadsEnabled       Flag indicating whether to include payloads
     * @param propertiesEnabled     Flag indicating whether to include synapse properties
     * @param isFault               Flag to indicate whether this event is faulty
     * @param time                  Timestamps for the event
     * @return                      Event string 
     */
    public static String getESBCompressedEventString(String messageId, String proxyName, int noOfMediators, boolean payloadsEnabled, 
            boolean propertiesEnabled, boolean isFault, long time) {
        Map <String, Object> flowMap = new HashMap <String, Object>();
        ArrayList<List<Object>> eventsList = new ArrayList<List<Object>>();
        ArrayList<PublishingPayload> payloadsList = new ArrayList<PublishingPayload>();
        Map<Integer, List<Integer>> eventList  = new HashMap<Integer, List<Integer>>();
        
        for (int i = 0 ; i <= noOfMediators ; i++) {
            List<Object> singleEvent = new ArrayList<Object>();
            //messageID
            singleEvent.add(messageId);
            //component Type and name
            if (i == 0){
                singleEvent.add("Proxy Service");
                singleEvent.add(proxyName);
            } else {
                singleEvent.add("Mediator");
                singleEvent.add("mediator_" + i);
            }
            //component Index
            singleEvent.add(i);
            //component ID. Having format: 'ProxyName@ComponentIndex:ComponentName'
            singleEvent.add(proxyName + "@" + i +  ":" + singleEvent.get(2));
            long start = time;
            //startTime
            singleEvent.add(start);
            long end = time + 15;
            //endTime
            singleEvent.add(end);
            //duration
            singleEvent.add(end - start);
            //before payload
            singleEvent.add(null);
            //after payload
            singleEvent.add(null);
            
            // transport/context properties, if enabled
            if (propertiesEnabled) {
                if (i%2 == 0) {
                    singleEvent.add("{mediation.flow.statistics.parent.index=" + (i - 1) + ", tenant.info.domain=" +
                        "carbon.super, mediation.flow.statistics.statistic.id=" + messageId + ", mediation.flow." +
                        "statistics.index.object=org.apache.synapse.aspects.flow.statistics.util." +
                        "UniqueIdentifierObject@7a26fe97, tenant.info.id=-1234, mediation.flow.trace.collected=true, " +
                        "CREDIT_CARD=bbbbb, TRANSPORT_IN_NAME=http, proxy.name=LicenseServiceProxy, mediation.flow." +
                        "statistics.collected=true, VEHICLE_ID=aaaaa}");
                    singleEvent.add("{Transfer-Encoding=chunked, Host=localhost.localdomain:8282, MessageID=" + 
                        messageId + ", To=/services/LicenseServiceProxy.LicenseServiceProxyHttpSoap12Endpoint," +
                        "SOAPAction=urn:renewLicense, WSAction=urn:renewLicense, User-Agent=Axis2, Content-Type=" +
                        "application/soap+xml; charset=UTF-8; action=\"urn:renewLicense\"}");
                } else {
                    singleEvent.add(null);
                    singleEvent.add(null);
                }
            } else {
                singleEvent.add(null);
                singleEvent.add(null);
            }
            
            //children
            if (i != noOfMediators) {
                singleEvent.add(Arrays.toString(new int[]{i+1}));
            } else {
                singleEvent.add(null);
            }
            
            //entry point
            singleEvent.add(proxyName);
            //entry point hash code
            singleEvent.add("1241186573");
            //fault count
            if (isFault && (i == 0 || i == noOfMediators)) {
                singleEvent.add(1);
            } else {
                singleEvent.add(0);
            }
            
            //hash code
            singleEvent.add("1241186573" + i);
            
            eventsList.add(singleEvent);
            List<Integer> attributeIndices = new ArrayList<Integer>();
            attributeIndices.add(8);
            attributeIndices.add(9);
            eventList.put(i , attributeIndices);
        }
       
        // Add payloads, if enabled
        if (payloadsEnabled) {
            PublishingPayload publishingPayload = new PublishingPayload();
            publishingPayload.setEvents(eventList);
            publishingPayload.setPayload("<?xml version='1.0' encoding='utf-8'?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:sam=\"http://sample.esb.org\"><soapenv:Body><sam:renewLicense><sam:vehicleNumber>aaaaa</sam:vehicleNumber><sam:insurancePolicy>-2081631303</sam:insurancePolicy><sam:ecoCert>311989168</sam:ecoCert></sam:renewLicense></soapenv:Body></soapenv:Envelope>");
            payloadsList.add(publishingPayload);
        }
        
        flowMap.put("host", "localhost");
        flowMap.put(AnalyticsConstants.EVENTS_ATTRIBUTE, eventsList);
        flowMap.put(AnalyticsConstants.PAYLOADS_ATTRIBUTE, payloadsList);
        
        ByteArrayOutputStream out = kryoSerialize(flowMap);
        return compress(out);
    }
    
    
    /**
     * Serialize the esbEven object with kryo
     * 
     * @param esbEevent esb event object
     * @return          Bye array stream of the serialized object
     */
    private static ByteArrayOutputStream kryoSerialize(Map <String, Object> esbEevent) {
        ThreadLocal<Kryo> kryoTL = new ThreadLocal<Kryo>() {
            protected Kryo initialValue() {
                Kryo kryo = new Kryo();
                /* Class registering precedence matters. Hence intentionally giving a registration ID */
                kryo.register(HashMap.class, 111);
                kryo.register(ArrayList.class, 222);
                kryo.register(PublishingPayload.class, 333);
                return kryo;
            }
        };
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Output output = new Output(out);
        kryoTL.get().writeObject(output, esbEevent);
        output.flush();
        return out;
    }
    
    
    /**
     * Compress a byte array stream with gzip, and encode with Base64
     * 
     * @param out   Byte array stream to be compressed
     * @return      Base64 encoded string of the compressed byte array 
     */
    private static String compress (ByteArrayOutputStream out) {
        ByteArrayOutputStream gzipOut = new ByteArrayOutputStream();
        GZIPOutputStream gzip = null;
        try {
            gzip = new GZIPOutputStream(gzipOut);
            gzip.write(out.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (gzip != null) {
                try {
                    gzip.close();
                } catch (IOException e) {
                    logger.info("Error occured while closing gzip stream");
                }
            }
        }
        String str = DatatypeConverter.printBase64Binary(gzipOut.toByteArray());
        return str;
    }
}
