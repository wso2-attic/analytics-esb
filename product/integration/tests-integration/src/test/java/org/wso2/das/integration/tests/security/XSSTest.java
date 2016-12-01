/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.das.integration.tests.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.das.integration.tests.DASIntegrationBaseTest;

import java.util.HashMap;
import java.util.Map;

/**
 * This class contains the integration tests for XSS security vulnerabilities.
 */
public class XSSTest extends DASIntegrationBaseTest {
    private static final String URL_TYPE = "url";
    private static final String DRIVER_TYPE = "driver";
    private Log log = LogFactory.getLog(XSSTest.class);
    private Map<String, String> httpHeaders;

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        super.init();
        httpHeaders = new HashMap<>();
    }

    @Test(groups = "wso2.das", description = "Test XSS for adding nes datasource")
    public void xssNewDatasource() throws Exception {
        log.info("Executing XSSTest.xssNewDatasource");
        String expectedResponseForUrl = "Database URL cannot be empty";
        String expectedResponseForDriver = "Driver Class Name cannot be empty";
        String urlForUrl = generateUrl(URL_TYPE, "%3cscript%3ealert(1)%3c/script%3e");
        String urlForDriver = generateUrl(DRIVER_TYPE, "%3cscript%3ealert(1)%3c/script%3e");

        HttpResponse responseForUrl = HttpRequestUtil.doGet(urlForUrl, httpHeaders);
        HttpResponse responseForDriver = HttpRequestUtil.doGet(urlForDriver, httpHeaders);

        String actualResponseForUrl = responseForUrl.getData();
        String actualResponseForDriver = responseForDriver.getData();
        Assert.assertEquals(actualResponseForUrl, expectedResponseForUrl, "Doesn't Receive correct response for url xss");
        Assert.assertEquals(actualResponseForDriver, expectedResponseForDriver, "Doesn't Receive correct response for driver xss");
    }

    /**
     * Generate url for each scenario.
     *
     * @param type  {String} edit url for mentioned field
     * @param input {String} appendable xss for url
     * @return {String} generated url
     */
    private String generateUrl(String type, String input) {
        String url = "";
        switch (type) {
            case URL_TYPE:
                url = "https://localhost:11343/carbon/ndatasource/validateconnection-ajaxprocessor.jsp?" +
                        "&dsName=ads" +
                        "&driver=org.h2.Driver" +
                        "&url=" + input +
                        "&username=&dsType=RDBMS" +
                        "&customDsType=" +
                        "&dsProviderType=default" +
                        "&dsclassname=undefined" +
                        "&dsclassname=undefined" +
                        "&dsproviderProperties=undefined" +
                        "&editMode=false" +
                        "&password=";
                break;
            case DRIVER_TYPE:
                url = "https://localhost:11343/carbon/ndatasource/validateconnection-ajaxprocessor.jsp?" +
                        "&dsName=ads" +
                        "&driver=" + input +
                        "&url=jdbc%3Ah2%3Atcp%3A%5BHOST%5D%3A%5BPORT%5D%2F%5Bdatabase%5D" +
                        "&username=&dsType=RDBMS" +
                        "&customDsType=" +
                        "&dsProviderType=default" +
                        "&dsclassname=undefined" +
                        "&dsclassname=undefined" +
                        "&dsproviderProperties=undefined" +
                        "&editMode=false" +
                        "&password=";
                break;
            default:
                break;
        }
        return url;
    }
}
