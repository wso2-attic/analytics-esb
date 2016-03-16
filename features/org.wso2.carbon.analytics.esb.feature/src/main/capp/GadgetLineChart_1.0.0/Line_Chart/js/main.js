var TOPIC = "subscriber";
var page = gadgetUtil.getCurrentPageName();
var qs = gadgetUtil.getQueryString();
var prefs = new gadgets.Prefs();
var type;
var chart = gadgetUtil.getChart(prefs.getString(PARAM_GADGET_ROLE));
if(chart) {
    type = gadgetUtil.getRequestType(page, chart);
}
console.log("%%%%%%%%%%% page " + page); 

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
        type: type,
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
        //sort the timestamps
        data.sort(function(a, b) {
            return a.timestamp - b.timestamp;
        });
        //perform necessary transformation on input data
        chart.processData(data);
        //finally draw the chart on the given canvas
        chart.chartConfig.width = ($('#canvas').width() - 20);
        chart.chartConfig.height = 200;
        var vg = new vizg(chart.schema, chart.chartConfig);
        $("#canvas").empty();
        vg.draw("#canvas");
    } catch (e) {
        $("#canvas").html(gadgetUtil.getErrorText(e));
    }
};

function onError(msg) {
    $("#canvas").html(gadgetUtil.getErrorText(msg));
};