var TOPIC = "subscriber";
var page = gadgetUtil.getCurrentPageName();
var qs = gadgetUtil.getQueryString();
var prefs = new gadgets.Prefs();
var type;
var chart = gadgetUtil.getChart(prefs.getString(PARAM_GADGET_ROLE));

if (chart) {
    type = gadgetUtil.getRequestType(page, chart);
}

var rangeStart;
var rangeEnd;

document.body.onmouseup = function() {
    var div = document.getElementById("dChart");
    div.innerHTML = "<p> Start : " + rangeStart + "</p>" + "<p> End : " + rangeEnd + "</p>";
}

var callbackmethod = function(start, end) {
    rangeStart = start;
    rangeEnd = end;
    var message = {
        timeFrom: new Date(rangeStart).getTime(),
        timeTo: new Date(rangeEnd).getTime(),
        timeUnit: "Custom"
    };
    gadgets.Hub.publish("chart-zoomed", message);
};

$(function() {
    if (!chart) {
        $("#canvas").html(gadgetUtil.getErrorText("Gadget initialization failed. Gadget role must be provided."));
        return;
    }
    if (page != TYPE_LANDING && qs[PARAM_ID] == null) {
        $("#canvas").html(gadgetUtil.getDefaultText());
        return;
    }
    var timeFrom = gadgetUtil.timeFrom();
    var timeTo = gadgetUtil.timeTo();
    console.log("LINE_CHART[" + page + "]: TimeFrom: " + timeFrom + " TimeTo: " + timeTo);
    gadgetUtil.fetchData(CONTEXT, {
        type: type,
        id: qs.id,
        timeFrom: timeFrom,
        timeTo: timeTo,
        entryPoint: qs.entryPoint
    }, onData, onError);
});

gadgets.HubSettings.onConnect = function() {
    gadgets.Hub.subscribe(TOPIC, function(topic, data, subscriberData) {
        onTimeRangeChanged(data);
    });
};

function onTimeRangeChanged(data) {
    gadgetUtil.fetchData(CONTEXT, {
        type: type,
        id: qs.id,
        timeFrom: data.timeFrom,
        timeTo: data.timeTo,
        entryPoint: qs.entryPoint
    }, onData, onError);
};


function onData(response) {
    try {
        var data = response.message;
        if (data.length == 0) {
            $('#canvas').html(gadgetUtil.getEmptyRecordsText());
            return;
        }
        //sort the timestamps
        data.sort(function(a, b) {
            return a.timestamp - b.timestamp;
        });
        //perform necessary transformation on input data
        chart.schema[0].data = chart.processData(data);
        //finally draw the chart on the given canvas
        chart.chartConfig.width = $('body').width();
        chart.chartConfig.height = $('body').height();

        var vg = new vizg(chart.schema, chart.chartConfig);
        $("#canvas").empty();
        vg.draw("#canvas",[{type:"range", callback:callbackmethod}]);
    } catch (e) {
        $('#canvas').html(gadgetUtil.getErrorText(e));
    }
};

function onError(msg) {
    $("#canvas").html(gadgetUtil.getErrorText(msg));
};

// $(window).resize(function() {
//     // if (page != TYPE_LANDING && qs[PARAM_ID]) {
//         drawChart();
//     // }
// });