var TOPIC = "subscriber";

$(function() {
    var page = gadgetUtil.getCurrentPage();
    var qs = gadgetUtil.getQueryString();
    if (qs[PARAM_ID] == null) {
        $("#canvas").html(gadgetUtil.getDefaultText());
        return;
    }
    //if there are url elemements present, use them. 
    //Otherwis use DEFAULT_END_TIME defined in the gadget-utils.js
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
        var columns = ["timestamp", "value"];
        var schema = [{
            "metadata": {
                "names": ["Time", "Value"],
                "types": ["time", "linear"]
            },
            "data": []
        }];

        //sort the timestamps
        data.sort(function(a, b) {
            return a.timestamp - b.timestamp;
        });

        data.forEach(function(row, i) {
            var record = [];
            columns.forEach(function(column) {
                var value = row[column];
                record.push(value);
            });
            schema[0].data.push(record);
        });
        var width = ($('#canvas').width() - 20);
        var height = 200;
        // console.log("Width: " + $('#canvas').width() + " Height: " + height);

        var config = {
            x: "Time",
            charts: [{ type: "line", y: "Value" }],
            width: width,
            height: height,
            padding: { "top": 10, "left": 60, "bottom": 40, "right": 20 }
        };
        var chart = new vizg(schema, config);
        $("#canvas").empty();
        chart.draw("#canvas");
    } catch (e) {
        $("#canvas").html(gadgetUtil.getErrorText(e));
    }
};

function onError(msg) {
    $("#canvas").html(gadgetUtil.getErrorText());
};