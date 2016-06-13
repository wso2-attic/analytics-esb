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
package org.wso2.das.integration.common.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.analytics.api.CarbonAnalyticsAPI;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.spark.admin.stub.AnalyticsProcessorAdminServiceStub;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;

public class DASIntegrationTest {

    private static final String ANALYTICS_SERVICE_NAME = "AnalyticsProcessorAdminService";
    protected AutomationContext dasServer;
    protected String backendURL;
    protected String webAppURL;
    protected LoginLogoutClient loginLogoutClient;
    protected User userInfo;
    protected String thriftURL;
    protected AnalyticsDataAPI analyticsDataAPI;

    protected void init() throws Exception {
        init(TestUserMode.SUPER_TENANT_ADMIN);
        String apiConf = new File(this.getClass().getClassLoader().getResource("dasconfig" + File.separator + "api" +
                File.separator + "analytics-data-config.xml").toURI()).getAbsolutePath();
        this.analyticsDataAPI = new CarbonAnalyticsAPI(apiConf);
    }

    protected void init(TestUserMode testUserMode) throws Exception {
        dasServer = new AutomationContext("DAS", testUserMode);
        loginLogoutClient = new LoginLogoutClient(dasServer);
        backendURL = dasServer.getContextUrls().getBackEndUrl();
        webAppURL = dasServer.getContextUrls().getWebAppURL();
        userInfo = dasServer.getContextTenant().getContextUser();
    }

    protected void init(String domainKey, String userKey) throws Exception {
        dasServer = new AutomationContext("DAS", "das001", domainKey, userKey);
        loginLogoutClient = new LoginLogoutClient(dasServer);
        backendURL = dasServer.getContextUrls().getBackEndUrl();
        webAppURL = dasServer.getContextUrls().getWebAppURL();
    }

    protected void init(String domainKey, String instance, String userKey) throws Exception {
        dasServer = new AutomationContext("DAS", instance, domainKey, userKey);
        loginLogoutClient = new LoginLogoutClient(dasServer);
        backendURL = dasServer.getContextUrls().getBackEndUrl();
        webAppURL = dasServer.getContextUrls().getWebAppURL();
    }

    protected String getSessionCookie() throws Exception {
        return loginLogoutClient.login();
    }

    protected String getResourceContent(Class testClass, String resourcePath) throws Exception {
        String content = "";
        URL url = testClass.getClassLoader().getResource(resourcePath);
        if (url != null) {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(
                    new File(url.toURI()).getAbsolutePath()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content += line;
            }
            return content;
        }else {
            throw new Exception("No resource found in the given path : "+ resourcePath);
        }
    }
    
    protected AnalyticsProcessorAdminServiceStub getAnalyticsProcessorStub() throws Exception {
        ConfigurationContext configContext = ConfigurationContextFactory.
                createConfigurationContextFromFileSystem(null);
        String loggedInSessionCookie = getSessionCookie();
        AnalyticsProcessorAdminServiceStub analyticsStub = new AnalyticsProcessorAdminServiceStub(configContext,
                backendURL + "/services/" + ANALYTICS_SERVICE_NAME);
        ServiceClient client = analyticsStub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                loggedInSessionCookie);
        return analyticsStub;
    }
    
    /**
     * 
     * @param maxWaitTime   Maximum time in seconds, to wait polling to check if the tables are cleaned-up.
     * 
     * @throws AnalyticsException
     * @throws InterruptedException
     */
    protected void cleanUpAllTables(int maxWaitTime) throws AnalyticsException, InterruptedException {
        long startTime = System.currentTimeMillis();
        String [] tables =  new String[] {TestConstants.ESB_EVENTS_TABLE, TestConstants.ESB_STAT_PER_SECOND_TABLE,
                TestConstants.ESB_STAT_PER_SECOND_ALL_TABLE, TestConstants.ESB_STAT_PER_MINUTE_TABLE,
                TestConstants.ESB_STAT_PER_MINUTE_ALL_TABLE, TestConstants.ESB_STAT_PER_HOUR_TABLE, 
                TestConstants.ESB_STAT_PER_DAY_TABLE, TestConstants.ESB_STAT_PER_MONTH_TABLE,
                TestConstants.MEDIATOR_STAT_PER_SECOND_TABLE, TestConstants.MEDIATOR_STAT_PER_MINUTE_TABLE,
                TestConstants.MEDIATOR_STAT_PER_HOUR_TABLE, TestConstants.MEDIATOR_STAT_PER_DAY_TABLE, 
                TestConstants.MEDIATOR_STAT_PER_MONTH_TABLE};
        for (String table : tables){
            this.analyticsDataAPI.delete(-1234, table, Long.MIN_VALUE, Long.MAX_VALUE);
        }
        long currentTime = System.currentTimeMillis();
        while ((currentTime - startTime) < maxWaitTime) {
            boolean isCleaned = true;
            for (String table : tables){
                int recordsCount = this.analyticsDataAPI.searchCount(-1234, table, "*:*");
                if (recordsCount > 0) {
                    isCleaned = false;
                    Thread.sleep(5000);
                    currentTime = System.currentTimeMillis();
                    break;
                }
            }
            if (isCleaned) {
                break;
            }
        }
    }
}

