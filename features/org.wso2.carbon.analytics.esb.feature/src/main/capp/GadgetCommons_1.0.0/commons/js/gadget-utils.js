/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * This Javascript module groups utility methods that are being used by all the gadgets in the ESB analytics dashboard
 */

var CONTEXT = "/portal/apis/esbanalytics";

var PARAM_PROXY_NAME = "proxyName";
var PARAM_API_NAME = "apiName";
var PARAM_MEDIATOR_ID = "mediatorId";
var PARAM_MESSAGE_ID = "messageId";

var PROXY_PAGE_URL = "/portal/dashboards/esb-analytics/proxies";

function GadgetUtil() {
    var DEFAULT_START_TIME = new Date(moment().subtract(29, 'days')).getTime();
    var DEFAULT_END_TIME = new Date(moment()).getTime();

    this.getQueryString = function() {
        var queryStringKeyValue = window.parent.location.search.replace('?', '').split('&');
        var qsJsonObject = {};
        if (queryStringKeyValue != '') {
            for (i = 0; i < queryStringKeyValue.length; i++) {
                qsJsonObject[queryStringKeyValue[i].split('=')[0]] = queryStringKeyValue[i].split('=')[1];
            }
        }
        return qsJsonObject;
    };

    this.timeFrom = function () {
    	var timeFrom = DEFAULT_START_TIME;
    	var qs = this.getQueryString();
    	if (qs.timeFrom != null) {
    	    timeFrom = qs.timeFrom;
    	}
    	return timeFrom;
    };

    this.timeTo = function() {
    	var timeTo = DEFAULT_END_TIME;
    	var qs = this.getQueryString();
    	if (qs.timeTo != null) {
    	    timeTo = qs.timeTo;
    	}
    	return timeTo;
    };

    this.fetchData = function (context, params, callback, error) {
        // console.log("++ Fetching data from: " + new Date(params.timeFrom) + " To: " + new Date(params.timeTo));
        $.ajax({
            url: context + "?type=" + params.type + "&timeFrom=" + params.timeFrom + "&timeTo=" + params.timeTo,
            type: "GET",
            success: function(data) {
                callback(data);
            },
            error: function(msg) {
                error(msg);
            }
        });
    };

    this.getEmptyProxyText = function () {
        return '<div align="center" style="margin-top:20px"><h4>No Proxy selected</h4></div>';
    };

}

var gadgetUtil = new GadgetUtil();