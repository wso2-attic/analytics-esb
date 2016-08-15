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

public class TestConstants {

    public static final String CONTENT_TYPE_JSON = "application/json";
	public static final String BASE64_ADMIN_ADMIN = "Basic YWRtaW46YWRtaW4=";
    public static final String BASE64_TENANT_ADMIN_ADMIN = "Basic YWRtaW5Ad3NvMi5jb206YWRtaW4=";
    public static final String ANALYTICS_JS_ENDPOINT = "https://localhost:11343/portal/apis/analytics";
    public static final String REQUEST_COUNT = "requestCount";
    
    // Table fields
    public static final String COMPONENT_ID = "componentId";
    public static final String NUMBER_OF_INVOCATION = "noOfInvocation";
    public static final String FAULT_COUNT = "faultCount";
    public static final String META_TENANT_ID = "meta_tenantId";
    
    // Event streams
    public static final String MEDIATOR_STAT_PER_SECOND_TABLE = "org_wso2_esb_analytics_stream_MediatorStatPerSecond";
    public static final String MEDIATOR_STAT_PER_MINUTE_TABLE = "org_wso2_esb_analytics_stream_MediatorStatPerMinute";
    public static final String MEDIATOR_STAT_PER_HOUR_TABLE = "org_wso2_esb_analytics_stream_MediatorStatPerHour";
    public static final String MEDIATOR_STAT_PER_DAY_TABLE = "org_wso2_esb_analytics_stream_MediatorStatPerDay";
    public static final String MEDIATOR_STAT_PER_MONTH_TABLE = "org_wso2_esb_analytics_stream_MediatorStatPerMonth";
    
    public static final String ESB_STAT_PER_SECOND_TABLE = "org_wso2_esb_analytics_stream_StatPerSecond";
    public static final String ESB_STAT_PER_SECOND_ALL_TABLE = "org_wso2_esb_analytics_stream_StatPerSecondAll";
    public static final String ESB_STAT_PER_MINUTE_TABLE = "org_wso2_esb_analytics_stream_StatPerMinute";
    public static final String ESB_STAT_PER_MINUTE_ALL_TABLE = "org_wso2_esb_analytics_stream_StatPerMinuteAll";
    public static final String ESB_STAT_PER_HOUR_TABLE = "org_wso2_esb_analytics_stream_StatPerHour";
    public static final String ESB_STAT_PER_DAY_TABLE = "org_wso2_esb_analytics_stream_StatPerDay";
    public static final String ESB_STAT_PER_MONTH_TABLE = "org_wso2_esb_analytics_stream_StatPerMonth";
    
    public static final String ESB_FLOW_ENTRY_STREAM_NAME = "org.wso2.esb.analytics.stream.FlowEntry";
    public static final String ESB_CONFIGS_TABLE = "org_wso2_esb_analytics_stream_ConfigEntry";
    public static final String ESB_EVENTS_TABLE = "org_wso2_esb_analytics_stream_Event";
    
    /* 
     * IDs of tenants define at automation.xml
     */
    public static final int TENANT_IDS[] = new int[] { 1, 2 };
    
}
