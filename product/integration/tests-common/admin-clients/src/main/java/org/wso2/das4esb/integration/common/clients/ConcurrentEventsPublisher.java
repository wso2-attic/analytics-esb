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

package org.wso2.das4esb.integration.common.clients;

import java.util.UUID;

import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.das.integration.common.utils.TestConstants;
import org.wso2.das.integration.common.utils.Utils;

public class ConcurrentEventsPublisher implements Runnable {
    
    private DataPublisherClient dataPublisherClient;
    private String entryPointName;
    private int noOfRequests;
    private int noOfMediators;
    private boolean payloadsEnabled;
    private boolean propertiesEnabled;
    private int sleepBetweenRequests;
    private int noOfFaults;
    
    public ConcurrentEventsPublisher(DataPublisherClient dataPublisherClient, int noOfRequests, String entryPointName,
            int noOfMediators, int noOfFaults, boolean payloadsEnabled, boolean propertiesEnabled, int sleepBetweenRequests) {
        this.dataPublisherClient = dataPublisherClient;
        this.entryPointName = entryPointName;
        this.noOfRequests = noOfRequests;
        this.payloadsEnabled = payloadsEnabled;
        this.propertiesEnabled = propertiesEnabled;
        this.sleepBetweenRequests = sleepBetweenRequests;
        this.noOfMediators = noOfMediators;
        this.noOfFaults = noOfFaults;
    }

    @Override
    public void run() {
        Object[] metaData = { true };
        int sentFaults = 0;
        // Publish events
        try {
            for (int j = 0 ; j < this.noOfRequests ; j++) {
                boolean isFault = sentFaults < this.noOfFaults;
                String messageId = "urn_uuid_" + UUID.randomUUID();
                String[] payloadData = new String[2];
                Event event;
                payloadData[0] = messageId;
                payloadData[1] = Utils.getESBCompressedEventString(messageId, this.entryPointName, this.noOfMediators,
                    this.payloadsEnabled, this.propertiesEnabled, isFault);
                event = new Event(null, System.currentTimeMillis(), metaData, null, payloadData);
                this.dataPublisherClient.publish(TestConstants.ESB_FLOW_ENTRY_STREAM_NAME, "1.0.0", event);
                if (isFault) {
                    sentFaults++;
                }
                // sleep to control the throughput
                Thread.sleep(this.sleepBetweenRequests);
            }
        } catch (DataEndpointException e) {
            throw new RuntimeException("Falied to publish event: " + e.getMessage(), e);
        } catch (InterruptedException ignored) {
        }
    }

}
