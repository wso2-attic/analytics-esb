    var TYPE = 5;
    var TOPIC = "subscriber";

    $(function() {
        var timeFrom = moment().subtract(1, 'hours'); 
        var timeTo = moment();

        var qs = getQueryString();
        if(qs.timeFrom != null) {
            timeFrom = qs.timeFrom;
        }
        if(qs.timeTo != null) {
            timeTo = qs.timeTo;
        }
        console.log("TOP_APIS: TimeFrom: " + timeFrom + " TimeTo: " + timeTo); 
        fetchData(timeFrom, timeTo,null);        
    });

    gadgets.HubSettings.onConnect = function() {
        gadgets.Hub.subscribe(TOPIC, function(topic, data, subscriberData) {
            onTimeRangeChanged(data);
        });
    };

    function onTimeRangeChanged(data) {
       fetchData(data.timeFrom,data.timeTo,data.filter);
    }

    //Call the backend and read some data
    function fetchData(timeFrom, timeTo, filter) {
        $.ajax({
            url: CONTEXT + "?type=" + TYPE + "&timeFrom=" + timeFrom + "&timeTo=" + timeTo,
            type: "GET",
            success: function(data) {
                onData(data);
            },
            error: function(msg) {
                onError(msg);
            }
        });
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
            charts: [{ type: "arc", x: "requests", color: "name", mode: "pie" }],
            width: 400,
            height: 250
        }

        data.forEach(function(row,i) {
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

    