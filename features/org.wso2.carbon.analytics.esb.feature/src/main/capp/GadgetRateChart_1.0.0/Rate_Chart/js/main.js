var TOPIC = "subscriber";

$(function() {
    var page = gadgetUtil.getCurrentPage();
    var qs = gadgetUtil.getQueryString();
    if (qs[PARAM_ID] == null) {
        $("#canvas").html(gadgetUtil.getDefaultText());
        return;
    }
    var timeFrom = gadgetUtil.timeFrom();
    var timeTo = gadgetUtil.timeTo();
    console.log("RATE_CHART[" + page.name + "]: TimeFrom: " + timeFrom + " TimeTo: " + timeTo);

    gadgetUtil.fetchData(CONTEXT, {
        type: page.type,
        id: qs.id,
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
        type: page.type,
        id: page.id,
        timeFrom: timeFrom,
        timeTo: timeTo
    }, onData, onError);
};

function onData(response) {
    try {
        var data = response.message;
        if (data.length == 0) {
            $("#canvas").html(gadgetUtil.getEmptyRecordsText());
        }
        var columns = ["timestamp", "success", "faults"];
        var schema = [{
            "metadata": {
                "names": ["Time", "Status", "Count"],
                "types": ["time", "ordinal","linear"]
            },
            "data": []
        }];

        //sort the timestamps
        data.sort(function(a, b) {
            return a.timestamp - b.timestamp;
        });

        data.forEach(function(row, i) {
            var timestamp = row['timestamp'];
            var success = row["success"];
            var fault = row["faults"];

            schema[0].data.push([timestamp,"SUCCESS", success]);
            schema[0].data.push([timestamp,"FAULT", fault]);
            schema[0].data.push([timestamp,"TOTAL", success+fault]);
        });
        // console.log(schema[0].data); 

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
        $("#canvas").empty();
        chart.draw("#canvas");
    } catch (e) {
        $("#canvas").html(gadgetUtil.getErrorText(e));
    }
};

function onError(msg) {
    $("#canvas").html(gadgetUtil.getErrorText(msg));
};