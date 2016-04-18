package org.wso2.carbon.analytics.esb.siddhi.extension;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.analytics.esb.util.CompressedEventProcessorConstants;
import org.wso2.carbon.analytics.esb.util.CompressedEventProcessorUtils;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.populater.ComplexEventPopulater;
import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.stream.StreamProcessor;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;

public class CompressedEventProcessor extends StreamProcessor {
    
    private Map<String,String> fields = new LinkedHashMap<String,String>();
    private int[] dataColumnIndex;
    private int[] meta_compressedIndex;

    @Override
    protected List<Attribute> init(AbstractDefinition inputDefinition, ExpressionExecutor[] 
            attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        this.fields = CompressedEventProcessorUtils.getOutputFields();
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
        return outputAttributes;
    }
    
    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor, StreamEventCloner
            streamEventCloner, ComplexEventPopulater complexEventPopulater) {
        ComplexEventChunk<StreamEvent> decompressedStreamEventChunk = new ComplexEventChunk<StreamEvent>();
        while (streamEventChunk.hasNext()) {
            StreamEvent compressedEvent = streamEventChunk.next();
            try {
                String eventsJson = (String) compressedEvent.getAttribute(this.dataColumnIndex);
                if (!eventsJson.isEmpty()) {
                    if ((Boolean) compressedEvent.getAttribute(this.meta_compressedIndex)) {
                        eventsJson = CompressedEventProcessorUtils.decompress(eventsJson);
                    }
                    JSONObject eventsAggregated = new JSONObject(eventsJson);
                    JSONArray eventsArray = eventsAggregated.getJSONArray(CompressedEventProcessorConstants
                        .JSON_FIELD_EVENTS);
                    Map<Integer, Map<String, String>> payloadsMap = null;
                    if (eventsAggregated.has(CompressedEventProcessorConstants.JSON_FIELD_PAYLOADS)) {
                        JSONArray payloadsArray = eventsAggregated.getJSONArray(CompressedEventProcessorConstants
                            .JSON_FIELD_PAYLOADS);
                        payloadsMap = CompressedEventProcessorUtils.getPayloadsAsMap(payloadsArray);
                    }
                    // Iterate over the array of events
                    for (int i = 0; i < eventsArray.length(); i++) {
                        StreamEvent decompressedEvent = streamEventCloner.copyStreamEvent(compressedEvent);
                        // Create a new event with decompressed fields
                        Object[] decompressedFields = CompressedEventProcessorUtils.getFieldValues(eventsAggregated,
                            eventsArray.getJSONObject(i), this.fields.keySet().toArray(new String[0]), payloadsMap, i,
                            compressedEvent.getTimestamp());
                        complexEventPopulater.populateComplexEvent(decompressedEvent, decompressedFields);
                        decompressedStreamEventChunk.add(decompressedEvent);
                    }
                } else {
                    throw new ExecutionPlanRuntimeException("Error occured while decompressing events. No compressed" +
                        " data found.");
                }
            } catch (JSONException e) {
                throw new ExecutionPlanRuntimeException("Error occured while decompressing the compressed event: "
                    + e.getMessage(), e);
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
                    case CompressedEventProcessorConstants.DATA_COLUMN :
                        this.dataColumnIndex = variable.getPosition();
                        break;
                    case CompressedEventProcessorConstants.META_FIELD_COMPRESSED :
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
}
