package org.wso2.carbon.analytics.esb.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;

public class CompressedEventProcessorUtils {
    private static final Log log = LogFactory.getLog(CompressedEventProcessorUtils.class);
    
    /**
     * Decompress a compressed event string.
     * 
     * @param str   Compressed string
     * @return      Decompressed string
     */
    public static String decompress(String str) {
        ByteArrayInputStream byteInputStream = null;
        GZIPInputStream gzipInputStream = null;
        BufferedReader br = null;
        try {
            byteInputStream = new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(str));
            gzipInputStream = new GZIPInputStream(byteInputStream);
            br = new BufferedReader(new InputStreamReader(gzipInputStream, CharEncoding.UTF_8));
            StringBuilder jsonStringBuilder = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                jsonStringBuilder.append(line);
            }
            return jsonStringBuilder.toString();
        } catch (IOException e) {
            throw new ExecutionPlanRuntimeException("Error occured while decompressing events string: "
                    + e.getMessage(), e);
        } finally {
            try {
                if (byteInputStream != null) {
                    byteInputStream.close();
                }
                if (gzipInputStream != null) {
                    gzipInputStream.close();
                }
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                log.error("Error occured while closing streams: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Get the values of each field of an event, as an Array.
     * 
     * @param event         Current event
     * @param payloadsMap   Payloads Map
     * @param eventIndex    Index of the current event
     * @return              Array of values of the fields in the event
     */
    public static Object[] getFieldValues(JSONObject eventsAggregated, JSONObject event, String[] outputColumns,
            Map<Integer, Map<String, String>> payloadsMap, int eventIndex, long timestamp) {
        Map<String, Object> extendedRowVals = new LinkedHashMap<String, Object>();
        String[] commonColumns = null;
        try {
            // Iterate over new (split) fields and add them
            for (int j = 0; j < outputColumns.length; j++) {
                String fieldName = outputColumns[j];
                if (fieldName.equals(CompressedEventProcessorConstants.TIMESTAMP_FIELD)) {
                    extendedRowVals.put(fieldName, timestamp);
                } else if (fieldName.equalsIgnoreCase(CompressedEventProcessorConstants.COMPONENT_INDEX)) {
                    extendedRowVals.put(fieldName, eventIndex);
                } else if (event.has(fieldName)) {
                    if (event.isNull(fieldName) && payloadsMap != null && payloadsMap.containsKey(eventIndex)) {
                        extendedRowVals.put(fieldName, payloadsMap.get(eventIndex).get(fieldName));
                    } else {
                        Object value = event.get(fieldName);
                        if (value instanceof JSONArray || value instanceof JSONObject) {
                            extendedRowVals.put(fieldName, value.toString());
                        } else {
                            extendedRowVals.put(fieldName, value);
                        }
                    }
                } else {
                    extendedRowVals.put(fieldName, null);
                }
            }
            
            // Iterate over common fields to all events, and add them
            commonColumns = JSONObject.getNames(eventsAggregated);
            for (int k = 0; k < commonColumns.length; k++) {
                String fieldName = commonColumns[k];
                if (!fieldName.equalsIgnoreCase(CompressedEventProcessorConstants.DATA_COLUMN) ||
                    !fieldName.equalsIgnoreCase(CompressedEventProcessorConstants.JSON_FIELD_EVENTS)) {
                    Object value = eventsAggregated.get(fieldName);
                    if (value instanceof JSONArray || value instanceof JSONObject) {
                        extendedRowVals.put(fieldName, value.toString());
                    } else {
                        extendedRowVals.put(fieldName, value);
                    }
                }
            }
            return extendedRowVals.values().toArray();
        } catch (JSONException e) {
            throw new ExecutionPlanRuntimeException("Error occured while splitting the record to rows: "
                    + e.getMessage(), e);
        }
    }
    
    /**
     * Convert json payload to map.
     * 
     * @param payloadsArray     JSON Array containing payload details
     * @return                  map of payloads
     */
    public static Map<Integer, Map<String, String>> getPayloadsAsMap(JSONArray payloadsArray) {
        Map<Integer, Map<String, String>> payloadsMap = new HashMap<Integer, Map<String, String>>();
        for (int i = 0; i < payloadsArray.length(); i++) {
            try {
                String payload = payloadsArray.getJSONObject(i).getString(CompressedEventProcessorConstants
                    .JSON_FIELD_PAYLOAD);
                JSONArray eventRefs = payloadsArray.getJSONObject(i).getJSONArray(CompressedEventProcessorConstants
                    .JSON_FIELD_EVENTS);
                for (int j = 0; j < eventRefs.length(); j++) {
                    int eventIndex = eventRefs.getJSONObject(j).getInt(CompressedEventProcessorConstants
                        .JSON_FIELD_EVENT_INDEX);
                    Map<String, String> existingPayloadMap = payloadsMap.get(eventIndex);
                    if (existingPayloadMap == null) {
                        Map<String, String> attributesMap = new HashMap<String, String>();
                        attributesMap.put(eventRefs.getJSONObject(j).getString(CompressedEventProcessorConstants
                            .JSON_FIELD_ATTRIBUTE), payload);
                        payloadsMap.put(eventIndex, attributesMap);
                    } else {
                        existingPayloadMap.put(eventRefs.getJSONObject(j).getString(CompressedEventProcessorConstants
                            .JSON_FIELD_ATTRIBUTE), payload);
                    }
                }
            } catch (JSONException e) {
                throw new ExecutionPlanRuntimeException("Error occured while generating payload map: " 
                        + e.getMessage(), e);
            }
        }
        return payloadsMap;
    }
    
    /**
     * Get the definition of the output fields
     * 
     * @return  Name and type of decompressed fields
     */
    public static Map<String,String> getOutputFields() {
        Map<String,String> fields = new LinkedHashMap<String,String>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            String[] lines = IOUtils.toString(classLoader.getResourceAsStream("decompressedEventDefinition")).split("\n");
            for (String line : lines) {
                if (!StringUtils.startsWithIgnoreCase(line, "#") && StringUtils.isNotEmpty(line)) {
                    String [] fieldDef = StringUtils.deleteWhitespace(line).split(":");
                    if (fieldDef.length == 2) {
                        fields.put(fieldDef[0], fieldDef[1]);
                    }
                }
            }
        } catch (IOException e) {
            new ExecutionPlanCreationException("Error occured while reading decompressed event definitions: " 
                    + e.getMessage(), e);
        }
        return fields;
    }
}
