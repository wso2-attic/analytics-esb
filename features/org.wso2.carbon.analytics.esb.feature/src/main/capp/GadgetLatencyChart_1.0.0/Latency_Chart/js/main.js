var TOPIC = "subscriber";
var page = gadgetUtil.getCurrentPage();
var qs = gadgetUtil.getQueryString();

$(function() {
    if (qs[PARAM_ID] == null) {
        $("#canvas").html(gadgetUtil.getDefaultText());
        return;
    }
    var timeFrom = gadgetUtil.timeFrom();
    var timeTo = gadgetUtil.timeTo();
    console.log("LATENCY_CHART[" + page.name + "]: TimeFrom: " + timeFrom + " TimeTo: " + timeTo);

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
        id: qs.id,
        timeFrom: data.timeFrom,
        timeTo: data.timeTo
    }, onData, onError);
};

function onData(response) {
    try {
        var data = response.message;
        if (data.length == 0) {
            $("#canvas").html(gadgetUtil.getEmptyRecordsText());
            return;
        }
        var columns = ["timestamp", "min", "avg", "max"];
        var schema = [{
            "metadata": {
                "names": ["Time", "Type", "Value"],
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
            var min = row["min"];
            var avg = row["avg"];
            var max = row["max"];

            schema[0].data.push([timestamp,"Minimum", min]);
            schema[0].data.push([timestamp,"Average", avg]);
            schema[0].data.push([timestamp,"Maximum", max]);
        });
        // console.log(schema[0].data); 
        var width = ($('#canvas').width() - 20);
        var height = 200;
        // console.log("Width: " + $('#canvas').width() + " Height: " + height);
        var config = {
            x: "Time",
            charts: [{ type: "line", y: "Value", color: "Type" }],
            width: width,
            height: height,
            padding: page.padding
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