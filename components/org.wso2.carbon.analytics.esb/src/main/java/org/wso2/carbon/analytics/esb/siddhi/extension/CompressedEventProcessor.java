/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.analytics.esb.siddhi.extension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsConstants;
import org.wso2.carbon.analytics.spark.core.util.CompressedEventAnalyticsUtils;
import org.wso2.carbon.analytics.spark.core.util.PublishingPayload;
import org.wso2.carbon.analytics.spark.core.util.PublishingPayloadEvent;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.populater.ComplexEventPopulater;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.stream.StreamProcessor;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

public class CompressedEventProcessor extends StreamProcessor {
    
    private Map<String,String> fields = new LinkedHashMap<String,String>();
    private int[] dataColumnIndex;
    private int[] meta_compressedIndex;
    private Kryo kryo = new Kryo();

    @Override
    protected List<Attribute> init(AbstractDefinition inputDefinition, ExpressionExecutor[] 
            attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        this.fields = getOutputFields();
        List<Attribute> outputAttributes = new ArrayList<Attribute>();
        for (String fielname : this.fields.keySet()) {
            String fieldType = this.fields.get(fielname);
            Attribute.Type type = null;
            if (fieldType.equalsIgnoreCase("double")) {
                type = Attribute.Type.DOUBLE;
            } else if (fieldType.equalsIgnoreCase("float")) {
                type = Attribute.Type.FLOAT;
            } else if (fieldType.equalsIgnoreCase("integer")) {
                type = Attribute.Type.INT;
            } else if (fieldType.equalsIgnoreCase("long")) {
                type = Attribute.Type.LONG;
            } else if (fieldType.equalsIgnoreCase("boolean")) {
                type = Attribute.Type.BOOL;
            } else if (fieldType.equalsIgnoreCase("string")) {
                type = Attribute.Type.STRING;
            }
            outputAttributes.add(new Attribute(fielname, type));
        }
        
        /* Class registering precedence matters. Hence intentionally giving a registration ID */
        kryo.register(HashMap.class, 111);
        kryo.register(ArrayList.class, 222);
        kryo.register(PublishingPayload.class, 333);
        kryo.register(PublishingPayloadEvent.class, 444);
        return outputAttributes;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor, StreamEventCloner
            streamEventCloner, ComplexEventPopulater complexEventPopulater) {
        ComplexEventChunk<StreamEvent> decompressedStreamEventChunk = new ComplexEventChunk<StreamEvent>();
        while (streamEventChunk.hasNext()) {
            StreamEvent compressedEvent = streamEventChunk.next();
            String eventString = (String) compressedEvent.getAttribute(this.dataColumnIndex);
            if (!eventString.isEmpty()) {
                ByteArrayInputStream unzippedByteArray;
                if ((Boolean) compressedEvent.getAttribute(this.meta_compressedIndex)) {
                    unzippedByteArray = CompressedEventAnalyticsUtils.decompress(eventString);
                } else {
                    unzippedByteArray = new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(eventString));
                }
                Input input = new Input(unzippedByteArray);
                Map<String, Object> aggregatedEvent = this.kryo.readObjectOrNull(input, HashMap.class);
                
                ArrayList<Map<String, Object>> eventsList = (ArrayList<Map<String, Object>>) aggregatedEvent.get(
                    AnalyticsConstants.EVENTS_ATTRIBUTE);
                ArrayList<PublishingPayload> payloadsList = (ArrayList<PublishingPayload>) aggregatedEvent.get(
                    AnalyticsConstants.PAYLOADS_ATTRIBUTE);
                Map<Integer, Map<String, String>> payloadsMap = null;
                if (payloadsList != null) {
                    payloadsMap =  CompressedEventAnalyticsUtils.getPayloadsAsMap(payloadsList);
                }
                
                String host = (String)aggregatedEvent.get(AnalyticsConstants.HOST_ATTRIBUTE);
                String messageFlowId = (String)aggregatedEvent.get(AnalyticsConstants.MESSAGE_FLOW_ID_ATTRIBUTE);
                // Iterate over the array of events
                for (int i = 0; i < eventsList.size(); i++) {
                    StreamEvent decompressedEvent = streamEventCloner.copyStreamEvent(compressedEvent);
                    // Create a new event with decompressed fields
                    Set<String> outputColumns = this.fields.keySet();
                    Object[] decompressedFields = CompressedEventAnalyticsUtils.getFieldValues(eventsList.get(i),
                        outputColumns.toArray(new String[outputColumns.size()]), payloadsMap, i, 
                        compressedEvent.getTimestamp(), host, messageFlowId);
                    complexEventPopulater.populateComplexEvent(decompressedEvent, decompressedFields);
                    decompressedStreamEventChunk.add(decompressedEvent);
                }
            } else {
                throw new ExecutionPlanRuntimeException("Error occured while decompressing events. No compressed" +
                    " data found.");
            }
        }
        nextProcessor.process(decompressedStreamEventChunk);
    }
    
    public void start() {
        for (ExpressionExecutor expressionExecutor : attributeExpressionExecutors) {
            if(expressionExecutor instanceof VariableExpressionExecutor) {
                VariableExpressionExecutor variable = (VariableExpressionExecutor) expressionExecutor;
                String variableName = variable.getAttribute().getName();
                switch (variableName) {
                    case AnalyticsConstants.DATA_COLUMN :
                        this.dataColumnIndex = variable.getPosition();
                        break;
                    case AnalyticsConstants.META_FIELD_COMPRESSED :
                        this.meta_compressedIndex = variable.getPosition();
                        break;
                }
            }
        }
    }

    public void stop() {
    }

    public Object[] currentState() {
        return new Object[0];
    }

    public void restoreState(Object[] arg0) {
    }
    
    /**
     * Get the definition of the output fields
     * 
     * @return  Name and type of decompressed fields
     */
    private static Map<String,String> getOutputFields() {
        Map<String,String> fields = new LinkedHashMap<String,String>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            String[] lines = IOUtils.toString(classLoader.getResourceAsStream("decompressedEventDefinition"))
                    .split("\n");
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
