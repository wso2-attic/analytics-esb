    var TYPE = 2;
    var TOPIC = "subscriber";

    $(function() {
       //if there are url elemements present, use them. 
       //Otherwis use DEFAULT_END_TIME defined in the gadget-utils.js
       var timeFrom = gadgetUtil.timeFrom();
       var timeTo = gadgetUtil.timeTo();
       console.log("OVERALL_TPS: TimeFrom: " + timeFrom + " TimeTo: " + timeTo);

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
        var columns = ["timestamp", "success", "fault"];
        var schema = [{
            "metadata": {
                "names": ["Time", "Status", "Count"],
                "types": ["time", "ordinal","linear"]
            },
            "data": []
        }];

        //sort the timestamps
        data.message.sort(function(a, b) {
            return a.timestamp - b.timestamp;
        });

        data.message.forEach(function(row, i) {
            var timestamp = row['timestamp'];
            var success = row["success"];
            var fault = row["fault"];

            schema[0].data.push([timestamp,"SUCCESS", success]);
            schema[0].data.push([timestamp,"FAULT", fault]);
            schema[0].data.push([timestamp,"TOTAL", success+fault]);
        });
        console.log(schema[0].data); 

        var width = $('#canvas').width();
        var height = 200;
        console.log("Width: " + $('#canvas').width() + " Height: " + height);

        var config = {
            x: "Time",
            charts: [{ type: "line", y: "Count",color: "Status"}],
            width: width,
            height: height,
            padding: { "top": 10, "left": 100, "bottom": 40, "right": 100 }
        };
        var chart = new vizg(schema, config);
        chart.draw("#canvas");
    };

    function onError(msg) {

    };