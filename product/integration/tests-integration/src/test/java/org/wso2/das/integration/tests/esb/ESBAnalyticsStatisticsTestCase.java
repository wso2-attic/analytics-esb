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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.dataservice.commons.AggregateField;
import org.wso2.carbon.analytics.dataservice.commons.AggregateRequest;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsIterator;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.spark.admin.stub.AnalyticsProcessorAdminServiceStub;
import org.wso2.das.integration.common.utils.DASIntegrationTest;
import org.wso2.das.integration.common.utils.TestConstants;
import org.wso2.das4esb.integration.common.clients.ConcurrentEventsPublisher;
import org.wso2.das4esb.integration.common.clients.DataPublisherClient;

/**
 * Class contains test cases related to statistics
 */
public class ESBAnalyticsStatisticsTestCase extends DASIntegrationTest {
    
    protected static final Log log = LogFactory.getLog(ESBAnalyticsStatisticsTestCase.class);
    private DataPublisherClient dataPublisherClient;
    private static final int TOTAL_REQUESTS_PER_PROXY = 100000;
    private static final int NUMBER_OF_PROXIES = 5;
    private static final int NUMBER_OF_MEDIATORS = 10;
    private static final int NUMBER_OF_FAULTS = 20;
    private static final boolean ENABLE_PAYLOADS = false;
    private static final boolean ENABLE_PROPERTIES = false;
    private static final int SLEEP_BETWEEN_REQUESTS = 6;
    private static final int WAIT_FOR_PUBLISHING_IN_MINUTES = 12;
    private static final int WAIT_FOR_INDEXING = 120000;
    private static final int WAIT_FOR_SPARK_SCRIPT = 60000;
    
    @BeforeClass(groups = "wso2.das4esb.stats", alwaysRun = true)
    protected void init() throws Exception {
        log.info("Start publishing events");
        super.init();
        this.dataPublisherClient = new DataPublisherClient();
        // Publish events for five proxies simultaneously
        ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_PROXIES);
        for (int i = 0; i < NUMBER_OF_PROXIES; i++) {
            executorService.execute(new ConcurrentEventsPublisher(this.dataPublisherClient, TOTAL_REQUESTS_PER_PROXY,
                    "AccuracyTestProxy_" + i, NUMBER_OF_MEDIATORS, NUMBER_OF_FAULTS, ENABLE_PAYLOADS, 
                    ENABLE_PROPERTIES, SLEEP_BETWEEN_REQUESTS));
        }
        executorService.shutdown();
        executorService.awaitTermination(WAIT_FOR_PUBLISHING_IN_MINUTES, TimeUnit.MINUTES);
        
        log.info("Publishing complete. Waiting for indexing...");
        Thread.sleep(WAIT_FOR_INDEXING);
        
        log.info("Indexing complete. Executing the spark scripts...");
        AnalyticsProcessorAdminServiceStub analyticsStub = getAnalyticsProcessorStub();
        analyticsStub.executeScript("esb_stat_analytics");
        Thread.sleep(WAIT_FOR_SPARK_SCRIPT);
    }

    
    /**************** Testing Overall Counts ****************/
    
    @Test(groups = "wso2.das4esb.stats", description = "Test total invocation counts in per-second table")
    public void testSecondTableTotalCount() throws Exception {
        int count = getCounts(TestConstants.ESB_STAT_PER_SECOND_ALL_TABLE, TestConstants.NUMBER_OF_INVOCATION, "ALL");
        Assert.assertEquals(count, TOTAL_REQUESTS_PER_PROXY*NUMBER_OF_PROXIES, "Total invocation count is incorrect" +
                " in per-second table.");
    }
    
    @Test(groups = "wso2.das4esb.stats", description = "Test total invocation counts in per-minute table")
    public void testMinuteTableTotalCount() throws Exception {
        int count = getCounts(TestConstants.ESB_STAT_PER_MINUTE_ALL_TABLE, TestConstants.NUMBER_OF_INVOCATION, "ALL");
        Assert.assertEquals(count, TOTAL_REQUESTS_PER_PROXY*NUMBER_OF_PROXIES, "Total invocation count is incorrect" +
                " in per-minute table.");
    }
    
    @Test(groups = "wso2.das4esb.stats", description = "Test total invocation counts in per-hour table")
    public void testHourTableTotalCount() throws Exception {
        int count = getCounts(TestConstants.ESB_STAT_PER_HOUR_TABLE, TestConstants.NUMBER_OF_INVOCATION, "ALL");
        Assert.assertEquals(count, TOTAL_REQUESTS_PER_PROXY*NUMBER_OF_PROXIES, "Total invocation count is incorrect" +
                " in per-hour table.");
    }
    
    @Test(groups = "wso2.das4esb.stats", description = "Test total invocation counts in per-day table")
    public void testDayTableTotalCount() throws Exception {
        int count = getCounts(TestConstants.ESB_STAT_PER_DAY_TABLE, TestConstants.NUMBER_OF_INVOCATION, "ALL");
        Assert.assertEquals(count, TOTAL_REQUESTS_PER_PROXY*NUMBER_OF_PROXIES, "Total invocation count is incorrect" +
                " in per-day table.");
    }
    
    @Test(groups = "wso2.das4esb.stats", description = "Test total invocation counts in per-month table")
    public void testMonthTableTotalCount() throws Exception {
        int count = getCounts(TestConstants.ESB_STAT_PER_MONTH_TABLE, TestConstants.NUMBER_OF_INVOCATION, "ALL");
        Assert.assertEquals(count, TOTAL_REQUESTS_PER_PROXY*NUMBER_OF_PROXIES, "Total invocation count is incorrect" +
                " in per-month table.");
    }
    
    
    /**************** Testing Mediator Counts ****************/
    
    @Test(groups = "wso2.das4esb.stats", description =  "Test mediator invocation counts in per-second table")
    public void testSecondTableMediatorCount() throws Exception {
        testMediatorInvocationCounts(TestConstants.MEDIATOR_STAT_PER_SECOND_TABLE);
    }
    
    @Test(groups = "wso2.das4esb.stats", description = "Test mediator invocation counts in per-minute table")
    public void testMinuteTableMediatorCount() throws Exception {
        testMediatorInvocationCounts(TestConstants.MEDIATOR_STAT_PER_MINUTE_TABLE);
    }
    
    @Test(groups = "wso2.das4esb.stats", description =  "Test mediator invocation counts in per-hour table")
    public void testHourTableMediatorCount() throws Exception {
        testMediatorInvocationCounts(TestConstants.MEDIATOR_STAT_PER_HOUR_TABLE);
    }
    
    @Test(groups = "wso2.das4esb.stats", description = "Test mediator invocation counts in per-day table")
    public void testDayTableMediatorCount() throws Exception {
        testMediatorInvocationCounts(TestConstants.MEDIATOR_STAT_PER_DAY_TABLE);
    }
    
    @Test(groups = "wso2.das4esb.stats", description =  "Test mediator invocation counts in per-month table")
    public void testMonthTableMediatorCount() throws Exception {
        testMediatorInvocationCounts(TestConstants.MEDIATOR_STAT_PER_MONTH_TABLE);
    }

    
    /**************** Testing Total fault Counts ****************/
    
    @Test(groups = "wso2.das4esb.stats", description = "Test total faults count in per-second table")
    public void testSecondTableTotalErrorCount() throws Exception {
        int count = getCounts(TestConstants.ESB_STAT_PER_SECOND_ALL_TABLE, TestConstants.FAULT_COUNT, "ALL");
        Assert.assertEquals(count, NUMBER_OF_FAULTS*NUMBER_OF_PROXIES, "Total faults count is incorrect" +
                " in per-second table.");
    }
    
    @Test(groups = "wso2.das4esb.stats", description = "Test total faults count in per-minute table")
    public void testMinuteTableTotalErrorCount() throws Exception {
        int count = getCounts(TestConstants.ESB_STAT_PER_MINUTE_ALL_TABLE, TestConstants.FAULT_COUNT, "ALL");
        Assert.assertEquals(count, NUMBER_OF_FAULTS*NUMBER_OF_PROXIES, "Total faults count is incorrect" +
                " in per-minute table.");
    }
    
    @Test(groups = "wso2.das4esb.stats", description = "Test total faults count in per-hour table")
    public void testHourTableTotalErrorCount() throws Exception {
        int count = getCounts(TestConstants.ESB_STAT_PER_HOUR_TABLE, TestConstants.FAULT_COUNT, "ALL");
        Assert.assertEquals(count, NUMBER_OF_FAULTS*NUMBER_OF_PROXIES, "Total faults count is incorrect" +
                " in per-hour table.");
    }
    
    @Test(groups = "wso2.das4esb.stats", description = "Test total faults count in per-day table")
    public void testDayTableTotalErrorCount() throws Exception {
        int count = getCounts(TestConstants.ESB_STAT_PER_DAY_TABLE, TestConstants.FAULT_COUNT, "ALL");
        Assert.assertEquals(count, NUMBER_OF_FAULTS*NUMBER_OF_PROXIES, "Total faults count is incorrect" +
                " in per-day table.");
    }
    
    @Test(groups = "wso2.das4esb.stats", description = "Test total faults count in per-month table")
    public void testMonthTableTotalErrorCount() throws Exception {
        int count = getCounts(TestConstants.ESB_STAT_PER_MONTH_TABLE, TestConstants.FAULT_COUNT, "ALL");
        Assert.assertEquals(count, NUMBER_OF_FAULTS*NUMBER_OF_PROXIES, "Total fault scount is incorrect" +
                " in per-month table.");
    }
    
    
    /**************** Testing mediator fault counts ****************/
    
    @Test(groups = "wso2.das4esb.stats", description = "Test mediator faults count in per-second table")
    public void testSecondTableMediatorErrorCount() throws Exception {
        testMediatorFaultCounts(TestConstants.MEDIATOR_STAT_PER_SECOND_TABLE);
    }
    
    @Test(groups = "wso2.das4esb.stats", description = "Test mediator faults count in per-minute table")
    public void testMinuteTableMediatorErrorCount() throws Exception {
        testMediatorFaultCounts(TestConstants.MEDIATOR_STAT_PER_MINUTE_TABLE);
    }
    
    @Test(groups = "wso2.das4esb.stats", description = "Test mediator faults count in per-hour table")
    public void testHourTableMediatorErrorCount() throws Exception {
        testMediatorFaultCounts(TestConstants.MEDIATOR_STAT_PER_HOUR_TABLE);
    }
    
    @Test(groups = "wso2.das4esb.stats", description = "Test mediator faults count in per-day table")
    public void testDayTableMediatorErrorCount() throws Exception {
        testMediatorFaultCounts(TestConstants.MEDIATOR_STAT_PER_DAY_TABLE);
    }
    
    @Test(groups = "wso2.das4esb.stats", description = "Test mediator faults count in per-month table")
    public void testMonthTableMediatorErrorCount() throws Exception {
        testMediatorFaultCounts(TestConstants.MEDIATOR_STAT_PER_MONTH_TABLE);
    }
    
    
    @AfterClass(alwaysRun = true, groups = "wso2.das4esb.publishing")
    public void cleanUpTables() throws AnalyticsException, InterruptedException {
        cleanUpAllTables(120000);
    }
    
    
    /**
     * Get the total number of invocations from a given table
     * 
     * @param table Table Name
     * @return      Invocation Count
     * @throws      AnalyticsException
     */
    private int getCounts(String table, String aggregateAttribute, String componentId) throws AnalyticsException {
        List<AggregateField> fields = new ArrayList<AggregateField>();
        fields.add(new AggregateField(new String[] { aggregateAttribute }, "sum", TestConstants.REQUEST_COUNT));
        AggregateRequest aggregateRequest = new AggregateRequest();
        aggregateRequest.setFields(fields);
        aggregateRequest.setAggregateLevel(0);
        aggregateRequest.setParentPath(new ArrayList<String>());
        aggregateRequest.setGroupByField(TestConstants.COMPONENT_ID);
        aggregateRequest.setQuery(TestConstants.COMPONENT_ID + ":\"" + componentId + "\"");
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
        log.info("Checking mediator invocation count in " + table + " table:");
        for (int proxyNumber = 0; proxyNumber < NUMBER_OF_PROXIES; proxyNumber++) {
            for (int mediatorNumber = 1; mediatorNumber <= NUMBER_OF_MEDIATORS; mediatorNumber++) {
                String mediatorId = "AccuracyTestProxy_" + proxyNumber + "@" + mediatorNumber + ":mediator_" + 
                        mediatorNumber;
                int count = getCounts(table, TestConstants.NUMBER_OF_INVOCATION, mediatorId);
                Assert.assertEquals(count, TOTAL_REQUESTS_PER_PROXY, "Invocation count is incorrect for mediator: " +
                        mediatorId + "in " + table + " table.");
            }
            log.info("AccuracyTestProxy_" + proxyNumber + ": All mediators: Ok");
        }
    }
    
    /**
     * Check the total number of faults in all the medaitors in a given table
     * 
     * @param table     Table Name
     * @throws          AnalyticsException
     */
    private void testMediatorFaultCounts(String table) throws AnalyticsException {
        log.info("Checking mediator faults count in " + table + " table:");
        for (int proxyNumber = 0; proxyNumber < NUMBER_OF_PROXIES; proxyNumber++) {
            for (int mediatorNumber = 1; mediatorNumber <= NUMBER_OF_MEDIATORS; mediatorNumber++) {
                int expectedFaultCount = 0;
                if (mediatorNumber == NUMBER_OF_MEDIATORS) {
                    expectedFaultCount = NUMBER_OF_FAULTS;
                }
                String mediatorId = "AccuracyTestProxy_" + proxyNumber + "@" + mediatorNumber + ":mediator_" + 
                        mediatorNumber;
                int count = getCounts(table, TestConstants.FAULT_COUNT, mediatorId);
                Assert.assertEquals(count, expectedFaultCount, "Fault count is incorrect for " + mediatorId + 
                        " in per-minute table.");
            }
            log.info("AccuracyTestProxy_" + proxyNumber + ": All mediators: Ok");
        }
    }
}
