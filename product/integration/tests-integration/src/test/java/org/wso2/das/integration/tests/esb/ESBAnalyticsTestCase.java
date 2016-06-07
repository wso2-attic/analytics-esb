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

package org.wso2.das.integration.tests.esb;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.das.integration.common.utils.DASIntegrationTest;
import org.wso2.das4esb.integration.common.clients.DataPublisherClient;

/**
 * Class contains basic test cases for analytics-esb
 */
public class ESBAnalyticsTestCase extends DASIntegrationTest {
    private DataPublisherClient dataPublisherClient;

    @BeforeClass(alwaysRun = true, dependsOnGroups = "wso2.das")
    protected void init() throws Exception {
        super.init();
        dataPublisherClient = new DataPublisherClient();
    }

    @Test(groups = "wso2.das4esb.publishing", description = "Test Publishing configs")
    public void testPublishingConfigs() throws Exception {
        // FIXME
        Object[] metaData = null;
        String[] payloadData = new String[1];
        Event event = new Event();
        event.setPayloadData(payloadData);
        event.setMetaData(metaData);
        this.dataPublisherClient.publish("esb-config-entry-stream", "1.0.0", event);
        
    }
    
    
    @Test(groups = "wso2.das4esb.publishing", description = "Test Publishing esb events")
    public void testPublishingEsbEvents() throws Exception {
        // TODO
        /*Object[] metaData = { true };
        String[] payloadData = new String[1];
        Event event = new Event();
        this.dataPublisherClient.publish("esb-config-entry-stream", "1.0.0", event);*/
    }
}
