var TYPE = 8;
var TOPIC = "subscriber";

$(function() {
    var qs = gadgetUtil.getQueryString();
    if(qs[PARAM_PROXY_NAME] == null) {
        $("#canvas").html(gadgetUtil.getEmptyProxyText());
            return;
    }
    //if there are url elemements present, use them. 
    //Otherwis use DEFAULT_END_TIME defined in the gadget-utils.js
    var timeFrom = gadgetUtil.timeFrom();
    var timeTo = gadgetUtil.timeTo();
    console.log("PROXY_LATENCY: TimeFrom: " + timeFrom + " TimeTo: " + timeTo);

    gadgetUtil.fetchData(CONTEXT, {
        type: TYPE,
        timeFrom: timeFrom,
        timeTo: timeTo
    }, onData, onError);
});

function onData(data) {
    $("#canvas").empty();
    var columns = ["timestamp", "value"];
    var schema = [{
        "metadata": {
            "names": ["Time", "Value"],
            "types": ["time", "linear"]
        },
        "data": []
    }];

    //sort the timestamps
    data.message.sort(function(a, b) {
        return a.timestamp - b.timestamp;
    });

    data.message.forEach(function(row, i) {
        var record = [];
        columns.forEach(function(column) {
            var value = row[column];
            record.push(value);
        });
        schema[0].data.push(record);
    });

    var width = ($('#canvas').width() - 20);
    var height = 200;
    console.log("Width: " + $('#canvas').width() + " Height: " + height);

    var config = {
        x: "Time",
        charts: [{ type: "line", y: "Value" }],
        width: width,
        height: height,
        padding: { "top": 10, "left": 60, "bottom": 40, "right": 20 }
    };
    var chart = new vizg(schema, config);
    chart.draw("#canvas");
};

function onError(msg) {

};