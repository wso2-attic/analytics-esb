/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.analytics.esb.util;

import java.util.List;

import org.wso2.carbon.analytics.spark.core.util.AnalyticsConstants;
import org.wso2.carbon.analytics.spark.core.util.PublishingPayload;
import org.wso2.analytics.esb.util.ESBAnalyticsConstants;

public class CompressedEventUtils {

    /**
     * 
     * @param columns       List of output column names
     * @param event         event
     * @param payloadsList  List of payloads
     * @param eventIndex    Index if the event
     * @param timestamp     Timestamp
     * @param metaTenantId  Teanant ID meta field
     * @param host          Host
     * @return
     */
    public static Object[] getFieldValues(List<String> columns, List<Object> event, List<PublishingPayload> payloadsList,
            int eventIndex, long timestamp, int metaTenantId, String host) {
        Object [] fieldsVals = new Object[columns.size()];
        int eventFieldIndex = 0;
        // Adding component attributes
        if (event != null) {
            for (int i = 0; i < columns.size(); i++) {
                if (columns.get(i).equals(AnalyticsConstants.TIMESTAMP_FIELD)) {
                    fieldsVals[i] = timestamp;
                } else if (columns.get(i).equals(ESBAnalyticsConstants.META_TENANT_ID_ATTRIBUTE)) {
                    fieldsVals[i] = metaTenantId;
                } else if (columns.get(i).equals(AnalyticsConstants.HOST_ATTRIBUTE)) {
                    fieldsVals[i] = host;
                } else {
                    fieldsVals[i] = event.get(eventFieldIndex);
                    eventFieldIndex++;
                }
            }
        }
        
        // Adding payloads
        if (payloadsList != null) {
            for (int j = 0 ; j < payloadsList.size() ; j++) {
                PublishingPayload publishingPalyload = payloadsList.get(j);
                String payload = publishingPalyload.getPayload();
                List<Integer> mappingAttributes = publishingPalyload.getEvents().get(eventIndex);
                if (mappingAttributes != null) {
                    for (int k = 0 ; k < mappingAttributes.size() ; k++) {
                        fieldsVals[mappingAttributes.get(k)] = payload;
                    }
                }
            }
        }
        return fieldsVals;
    }
}
