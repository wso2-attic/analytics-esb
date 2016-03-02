    var TYPE = 5;
    var TOPIC = "subscriber";

    $(function() {
        //if there are url elemements present, use them. 
        //Otherwis use DEFAULT_END_TIME defined in the gadget-utils.js
        var timeFrom = gadgetUtil.timeFrom();
        var timeTo = gadgetUtil.timeTo();
        gadgetUtil.fetchData(CONTEXT, {
            type: TYPE,
            timeFrom: timeFrom,
            timeTo: timeTo
        }, onData, onError);
        console.log("TOP_APIS: TimeFrom: " + timeFrom + " TimeTo: " + timeTo); 
    });

    gadgets.HubSettings.onConnect = function() {
        gadgets.Hub.subscribe(TOPIC, function(topic, data, subscriberData) {
            onTimeRangeChanged(data);
        });
    };

    function onTimeRangeChanged(data) {
        gadgetUtil.fetchData(CONTEXT, {
           type: TYPE,
           timeFrom: data.timeFrom,
           timeTo: data.timeTo
       }, onData, onError);
    }

    function onData(data) {
        $("#canvas").empty();
        var schema = [{
            "metadata": {
                "names": ["name", "requests"],
                "types": ["ordinal", "linear"]
            },
            "data": []
        }];

        var config = {
            type: "bar",
            x : "name",
            charts : [{type: "bar",  y : "requests", orientation : "left"}],
            width: 500,
            height: 200,
            padding: { "top": 10, "left": 100, "bottom": 40, "right": 10 }
        };

        data.message.forEach(function(row,i) {
            schema[0].data.push([row.name,row.requests]);
        });

        var onChartClick = function(event, item) {
            var proxyId = -1;
            if(item != null) {
                proxyId = item.datum.name;
            }
            parent.window.location = "/portal/dashboards/esb-analytics/proxies" + "?id=" + proxyId;
        };

        var chart = new vizg(schema, config);
        chart.draw("#canvas", [{ type: "click", callback: onChartClick }]);
    };

    function onError(msg) {

    };

    