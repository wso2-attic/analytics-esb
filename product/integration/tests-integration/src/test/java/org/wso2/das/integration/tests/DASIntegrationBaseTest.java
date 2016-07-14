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
package org.wso2.das.integration.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import javax.activation.DataHandler;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.analytics.api.CarbonAnalyticsAPI;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.spark.admin.stub.AnalyticsProcessorAdminServiceStub;
import org.wso2.carbon.analytics.webservice.stub.beans.StreamDefinitionBean;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.carbon.integration.common.admin.client.CarbonAppUploaderClient;
import org.wso2.carbon.integration.common.utils.ClientConnectionUtil;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.das4esb.integration.common.clients.AnalyticsWebServiceClient;
import org.wso2.das.integration.common.utils.TestConstants;

public class DASIntegrationBaseTest {

    protected static final Log log = LogFactory.getLog(DASIntegrationBaseTest.class);
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
            BufferedReader bufferedReader = null;
            try {
                bufferedReader = new BufferedReader(new FileReader(
                        new File(url.toURI()).getAbsolutePath()));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    content += line;
                }
            } catch (IOException e) {
                throw new Exception(e);
            } finally {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
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
                backendURL + ANALYTICS_SERVICE_NAME);
        ServiceClient client = analyticsStub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                loggedInSessionCookie);
        return analyticsStub;
    }
    
    /**
     * Cleanup all Analytics Tables and restart the server to clean in memory events.
     * 
     * @param maxWaitTime   Maximum time in seconds, to wait polling to check if the tables are cleaned-up.
     * @throws Exception 
     * @throws AnalyticsException
     */
    protected void restartAndCleanUpTables(int maxWaitTime) throws Exception {
        long startTime = System.currentTimeMillis();
        // first restart the server to clear any in memory events and to clear timebatchWindows
        log.info("Restarting server..");
        ServerConfigurationManager serverManager = new ServerConfigurationManager(dasServer);
        serverManager.restartGracefully();
        ClientConnectionUtil.waitForLogin(dasServer);
        log.info("Restarting complete. Cleaning up analytics tables...");
        String [] tables =  new String[] {TestConstants.ESB_EVENTS_TABLE, TestConstants.ESB_STAT_PER_SECOND_TABLE,
                TestConstants.ESB_STAT_PER_SECOND_ALL_TABLE, TestConstants.ESB_STAT_PER_MINUTE_TABLE,
                TestConstants.ESB_STAT_PER_MINUTE_ALL_TABLE, TestConstants.ESB_STAT_PER_HOUR_TABLE, 
                TestConstants.ESB_STAT_PER_DAY_TABLE, TestConstants.ESB_STAT_PER_MONTH_TABLE,
                TestConstants.MEDIATOR_STAT_PER_SECOND_TABLE, TestConstants.MEDIATOR_STAT_PER_MINUTE_TABLE,
                TestConstants.MEDIATOR_STAT_PER_HOUR_TABLE, TestConstants.MEDIATOR_STAT_PER_DAY_TABLE, 
                TestConstants.MEDIATOR_STAT_PER_MONTH_TABLE};
        long currentTime = System.currentTimeMillis();
        for (String[] tenant: TestConstants.TENANTS) {
            String username = tenant[0] + "@" + tenant[1];
            while ((currentTime - startTime) < maxWaitTime) {
                boolean isCleaned = true;
                for (String table : tables){
                    int recordsCount = 0;
                    try {
                        recordsCount = this.analyticsDataAPI.searchCount(username, table, "*:*");
                    } catch (Exception ignoredException) {
                    }
                    if (recordsCount > 0) {
                        isCleaned = false;
                        try {
                            this.analyticsDataAPI.delete(username, table, Long.MIN_VALUE, Long.MAX_VALUE);
                        } catch (Exception ignoredException) {
                        }
                    }
                }
                if (isCleaned) {
                    break;
                }
                Thread.sleep(5000);
                currentTime = System.currentTimeMillis();
            }
        }
    }
    
    
    /**
     * @param CApp          CApp to be deployed
     * @param tenants       list of tenant names and domains
     * @throws Exception
     */
    public void deployCarbonAppForTenants(String CApp, String [][] tenants) throws Exception {
        URL carbonAppFileURL = new URL("file:" + File.separator + File.separator + CarbonUtils.getCarbonHome() +  
                File.separator + "capps"  + File.separator + CApp);
        DataHandler carbonAppUrlDataHandler = new DataHandler(carbonAppFileURL);
        for (String[] tenant: tenants) {
            AutomationContext dasServerCtx = new AutomationContext("DAS", "das001", tenant[2], tenant[0]);
            LoginLogoutClient loginLogoutClient = new LoginLogoutClient(dasServerCtx);
            String loggedInSessionCookie = loginLogoutClient.login();
            String tenantBackendURL = dasServerCtx.getContextUrls().getBackEndUrl();
            if (!isStreamDeployed(tenantBackendURL, loggedInSessionCookie, TestConstants.ESB_FLOW_ENTRY_STREAM_NAME)) {
                if (!tenant[1].equals("carbon.super")) {
                    CarbonAppUploaderClient carbonAppUploaderClient = new CarbonAppUploaderClient(tenantBackendURL,
                            loggedInSessionCookie);
                    carbonAppUploaderClient.uploadCarbonAppArtifact(CApp, carbonAppUrlDataHandler);
                }
                boolean isDeployed = waitForStreamDeployement(tenantBackendURL, loggedInSessionCookie,
                        TestConstants.ESB_FLOW_ENTRY_STREAM_NAME);
                if (isDeployed) {
                    log.info("CApp: " + CApp + " successfully deployed under tenant: " + tenant[1]);
                } else {
                    throw new Exception("CApp: " + CApp + " failed to deployed under tenant: " + tenant[1]);
                }
            } else {
                log.info("CApp: " + CApp + " is already deployed under tenant: " + tenant[1]);
            }
        }
    }
    
    private boolean waitForStreamDeployement(String tenantBackendURL, String sessionCookie, String streamName)
            throws Exception {
        int maxWaitTime = 60000;
        log.info("Waiting for Stream deployment: " + streamName);
        boolean streamDeployed = false;
        long startTime = System.currentTimeMillis();
        long currentTime = System.currentTimeMillis();
        while (!streamDeployed && (currentTime - startTime) < maxWaitTime) {
            try { 
                streamDeployed = isStreamDeployed(tenantBackendURL, sessionCookie, streamName);
            } catch (Exception ignored) {
            } finally {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {
                }
                currentTime = System.currentTimeMillis();
            }
        }
        return streamDeployed;
    }
    
    private boolean isStreamDeployed(String tenantBackendURL, String sessionCookie, String streamName) 
            throws AxisFault {
        AnalyticsWebServiceClient webServiceClient  = new AnalyticsWebServiceClient(tenantBackendURL, sessionCookie);
        StreamDefinitionBean streamDefinition = null;
        try { 
            streamDefinition = webServiceClient.getStreamDefinition(streamName, "1.0.0");
            if (streamDefinition != null) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}

