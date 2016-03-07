    var TYPE = 4;
    var TOPIC = "subscriber";

    $(function() {
        //if there are url elemements present, use them. 
        //Otherwis use DEFAULT_END_TIME defined in the gadget-utils.js
        var timeFrom = gadgetUtil.timeFrom();
        var timeTo = gadgetUtil.timeTo();
        console.log("TOP_PROXIES: TimeFrom: " + timeFrom + " TimeTo: " + timeTo); 
        gadgetUtil.fetchData(CONTEXT, {
            type: TYPE,
            timeFrom: timeFrom,
            timeTo: timeTo
        }, onData, onError);
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
        if (data.message.length == 0) {
            $("#canvas").html('<div align="center" style="margin-top:20px"><h4>No records found.</h4></div>');
            return;
        }
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
            padding: { "top": 10, "left": 140, "bottom": 40, "right": 50 }
        };

        data.message.forEach(function(row,i) {
            schema[0].data.push([row.name,row.requests]);
        });

        var onChartClick = function(event, item) {
            var proxyName = -1;
            if(item != null) {
                proxyName = item.datum.name;
            }
            parent.window.location = PROXY_PAGE_URL + "?" + PARAM_ID + "=" + proxyName;
        };

        var chart = new vizg(schema, config);
        chart.draw("#canvas", [{ type: "click", callback: onChartClick }]);
    };

    function onError(msg) {

    };

    