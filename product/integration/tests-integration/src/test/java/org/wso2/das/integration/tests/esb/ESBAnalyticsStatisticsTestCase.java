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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.analytics.api.CarbonAnalyticsAPI;
import org.wso2.carbon.analytics.dataservice.commons.AggregateField;
import org.wso2.carbon.analytics.dataservice.commons.AggregateRequest;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsIterator;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.das.integration.common.utils.DASIntegrationTest;
import org.wso2.das.integration.common.utils.TestConstants;
import org.wso2.das4esb.integration.common.clients.ConcurrentEventsPublisher;
import org.wso2.das4esb.integration.common.clients.DataPublisherClient;

/**
 * Class contains test cases related to statistics
 */
public class ESBAnalyticsStatisticsTestCase extends DASIntegrationTest {
    private DataPublisherClient dataPublisherClient;
    private AnalyticsDataAPI analyticsDataAPI;
    private int totalRequests = 100;
    private int noOfProxies = 5;
    private int noOfMediators = 10;
    private int noOfFaults = 20;
    
    @BeforeClass(alwaysRun = true, dependsOnGroups = "wso2.das4esb.publishing")
    protected void init() throws Exception {
        log.info("Start publishing events");
        super.init();
        this.dataPublisherClient = new DataPublisherClient();
        // Publish events for five proxies simultaneously
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        for (int i = 0; i < noOfProxies; i++) {
            executorService.execute(new ConcurrentEventsPublisher(this.dataPublisherClient, this.totalRequests,
                    "AccuracyTestProxy_" + i, this.noOfMediators, this.noOfFaults, false, false, 0));
        }
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.MINUTES);
        log.info("Publishing complete. Waiting for indexing...");
        // wait for indexing to complete
        Thread.sleep(90000);
        
        String apiConf = new File(this.getClass().getClassLoader().getResource("dasconfig" + File.separator + "api" +
                File.separator + "analytics-data-config.xml").toURI()).getAbsolutePath();
        this.analyticsDataAPI = new CarbonAnalyticsAPI(apiConf);
        
    }

    
    @Test(groups = "wso2.das4esb.stats", description = "Test total invocation counts in per-second table")
    public void testSecondTableTotalCount() throws Exception {
        int count;
        count = getTotalInvocationCounts(TestConstants.ESB_STAT_PER_SECOND_ALL_TABLE);
        Assert.assertEquals(count, this.totalRequests*this.noOfProxies, "Total invocation count is incorrect in " +
                "per-second table");
    }
    
    @Test(groups = "wso2.das4esb.stats", description = "Test total invocation counts in per-minute table")
    public void testMinuteTableTotalCount() throws Exception {
        int count;
        count = getTotalInvocationCounts(TestConstants.ESB_STAT_PER_MINUTE_ALL_TABLE);
        Assert.assertEquals(count, this.totalRequests*this.noOfProxies, "Total invocation count is incorrect in " +
                "per-minute table");
    }
    
    @Test(groups = "wso2.das4esb.stats", description =  "Test mediator invocation counts in per-second table")
    public void testSecondTableMediatorCount() throws Exception {
        testMediatorInvocationCounts(TestConstants.MEDIATOR_STAT_PER_SECOND_TABLE);
    }
    
    @Test(groups = "wso2.das4esb.stats", description = "Test mediator invocation counts in per-minute table")
    public void testMinuteTableMediatorCount() throws Exception {
        testMediatorInvocationCounts(TestConstants.MEDIATOR_STAT_PER_MINUTE_TABLE);
    }
    
    
    /**
     * Get the total number of invocations from a given table
     * 
     * @param table Table Name
     * @return      Invocation Count
     * @throws      AnalyticsException
     */
    private int getTotalInvocationCounts(String table) throws AnalyticsException {
        List<AggregateField> fields = new ArrayList<AggregateField>();
        fields.add(new AggregateField(new String[] { TestConstants.NUMBER_OF_INVOCATION }, "sum",
                TestConstants.REQUEST_COUNT));
        AggregateRequest aggregateRequest = new AggregateRequest();
        aggregateRequest.setFields(fields);
        aggregateRequest.setAggregateLevel(0);
        aggregateRequest.setParentPath(new ArrayList<String>());
        aggregateRequest.setGroupByField(TestConstants.COMPONENT_ID);
        aggregateRequest.setQuery(TestConstants.COMPONENT_ID + ":\"ALL\"");
        aggregateRequest.setTableName(table);
        AnalyticsIterator<Record> resultItr = this.analyticsDataAPI.searchWithAggregates(-1234, 
                aggregateRequest);
        return ((Double) resultItr.next().getValue(TestConstants.REQUEST_COUNT)).intValue();
    }
    
    
    /**
     * Check the total number of invocations in all the medaitors in a given table
     * 
     * @param table Table Name
     * @throws      AnalyticsException
     * @throws      InterruptedException 
     */
    private void testMediatorInvocationCounts(String table) throws AnalyticsException, InterruptedException {
        List<AggregateField> fields = new ArrayList<AggregateField>();
        fields.add(new AggregateField(new String[] { TestConstants.NUMBER_OF_INVOCATION }, "sum", 
                TestConstants.REQUEST_COUNT));
        AggregateRequest aggregateRequest = new AggregateRequest();
        aggregateRequest.setFields(fields);
        aggregateRequest.setAggregateLevel(0);
        aggregateRequest.setParentPath(new ArrayList<String>());
        aggregateRequest.setGroupByField(TestConstants.COMPONENT_ID);
        int count;
        
        for (int proxyNumber = 0; proxyNumber < this.noOfProxies; proxyNumber++) {
            for (int mediatorNumber = 1; mediatorNumber <= this.noOfMediators; mediatorNumber++) {
                String mediatorId = "AccuracyTestProxy_" + proxyNumber + "@" + mediatorNumber + ":mediator_" + 
                        mediatorNumber;
                aggregateRequest.setTableName(table);
                aggregateRequest.setQuery(TestConstants.COMPONENT_ID + ":\"" + mediatorId + "\"");
                AnalyticsIterator<Record> secondsTableItr = this.analyticsDataAPI.searchWithAggregates(-1234, aggregateRequest);
                count = ((Double) secondsTableItr.next().getValue(TestConstants.REQUEST_COUNT)).intValue();
                Assert.assertEquals(count, this.totalRequests, "Invocation count is incorrect for mediator: " + 
                        mediatorId + "in " + table + " table");
            }
            log.info("AccuracyTestProxy_" + proxyNumber + ": All mediators: Ok");
            Thread.sleep(2000);
        }
    }
}
