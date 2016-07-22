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

import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.das.integration.common.utils.TestConstants;
import org.wso2.das.integration.common.utils.Utils;
import org.wso2.das.integration.tests.DASIntegrationBaseTest;
import org.wso2.das4esb.integration.common.clients.DataPublisherClient;

/**
 * Class contains basic test cases for analytics-esb
 */
public class ESBAnalyticsPublishingTestCase extends DASIntegrationBaseTest {
    
    @BeforeClass(groups = "wso2.das4esb.publishing", dependsOnGroups = "wso2.das", alwaysRun = true)
    protected void init() throws Exception {
        super.init();
    }

    @Test(groups = "wso2.das4esb.publishing", description = "Test Publishing configs")
    public void testPublishingConfigs() throws Exception {
        for (int tenantId: TestConstants.TENANT_IDS) {
            DataPublisherClient  dataPublisherClient = new DataPublisherClient();
            Object[] metaData = { tenantId };
            String[] payloadData = new String[3];
            payloadData[0] = "1243212601";
            payloadData[1] = "PublishingTestProxy1";
            payloadData[2] = "[{\"id\":\"PublishingTestProxy1@0:PublishingTestProxy1\",\"parentId\":null,\"group\":null}" +
                ",{\"id\":\"PublishingTestProxy1@1:PROXY_INSEQ\",\"parentId\":\"PublishingTestProxy1@0:" +
                "PublishingTestProxy1\",\"group\":\"PublishingTestProxy1@0:PublishingTestProxy1\"},{\"id\":\"" +
                "PublishingTestProxy1@2:PropertyMediator:prop1\",\"parentId\":\"PublishingTestProxy1@1:PROXY_INSEQ\"," +
                "\"group\":\"PublishingTestProxy1@1:PROXY_INSEQ\"},{\"id\":\"PublishingTestProxy1@3:LogMediator\",\"" +
                "parentId\":\"PublishingTestProxy1@2:PropertyMediator:prop1\",\"group\":\"PublishingTestProxy1@1:" +
                "PROXY_INSEQ\"},{\"id\":\"PublishingTestProxy1@4:PayloadFactoryMediator\",\"parentId\":\"" +
                "PublishingTestProxy1@3:LogMediator\",\"group\":\"PublishingTestProxy1@1:PROXY_INSEQ\"},{\"id\":\"" +
                "PublishingTestProxy1@5:PropertyMediator:prop2\",\"parentId\":\"PublishingTestProxy1@4:" +
                "PayloadFactoryMediator\",\"group\":\"PublishingTestProxy1@1:PROXY_INSEQ\"},{\"id\":\"" +
                "PublishingTestProxy1@6:RespondMediator\",\"parentId\":\"PublishingTestProxy1@5:PropertyMediator:prop2\"" +
                ",\"group\":\"PublishingTestProxy1@1:PROXY_INSEQ\"}]";
            Event event = new Event();
            event.setPayloadData(payloadData);
            event.setMetaData(metaData);
            dataPublisherClient.publish(TestConstants.ESB_CONFIGS_TABLE, "1.0.0", event);
            Thread.sleep(5000);
            int configsCount = this.analyticsDataAPI.searchCount(-1234, TestConstants.ESB_CONFIGS_TABLE, 
                    TestConstants.META_TENANT_ID + ":" + tenantId);
            Assert.assertEquals(configsCount, 1, "ESB configs has not correctly published for tenant: " + tenantId);
        }
    }
    
    
    @Test(groups = "wso2.das4esb.publishing", description = "Test Publishing esb events")
    public void testPublishingEsbEvents() throws Exception {
        for (int tenantId: TestConstants.TENANT_IDS) {
            DataPublisherClient  dataPublisherClient = new DataPublisherClient();
            String messageId = "urn_uuid_" + UUID.randomUUID();
            int noOfMediators = 10;
            String[] payloadData = new String[2];
            payloadData[0] = messageId;
            payloadData[1] = Utils.getESBCompressedEventString(messageId,"PublishingTestProxy2", noOfMediators, true, true,
                    false, System.currentTimeMillis());
            Object[] metaData = { true, tenantId };
            Event event = new Event(null, System.currentTimeMillis(), metaData, null, payloadData);
            dataPublisherClient.publish(TestConstants.ESB_FLOW_ENTRY_STREAM_NAME, "1.0.0", event);
            Thread.sleep(10000);
            int esbEventsCount = this.analyticsDataAPI.searchCount(-1234, TestConstants.ESB_EVENTS_TABLE, 
                    TestConstants.META_TENANT_ID + ":" + tenantId);
            Assert.assertEquals(esbEventsCount, noOfMediators + 1, "ESB event has not correctly published for tenant: "
                    + tenantId);
        }
    }
    
    @AfterClass(alwaysRun = true, groups = "wso2.das4esb.publishing")
    public void cleanUpTables() throws Exception {
        restartAndCleanUpTables(120000);
    }
}
