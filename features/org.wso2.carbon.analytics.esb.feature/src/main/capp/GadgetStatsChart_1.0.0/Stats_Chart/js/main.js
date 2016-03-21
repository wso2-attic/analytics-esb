var TOPIC = "subscriber";
var page = gadgetUtil.getCurrentPage();
var qs = gadgetUtil.getQueryString();
var type = 38;

$(function() {
    if (page && qs[PARAM_ID] == null) {
        $("body").html(gadgetUtil.getDefaultText());
        return;
    }
    var timeFrom = gadgetUtil.timeFrom();
    var timeTo = gadgetUtil.timeTo();
    console.log("STATS_CHART[" + page + "]: TimeFrom: " + timeFrom + " TimeTo: " + timeTo);
    if(page) {
        type = page.type;
    }
    gadgetUtil.fetchData(CONTEXT, {
        type: type,
        id: qs.id,
        entryPoint:qs.entryPoint,
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
        timeTo: data.timeTo,
        entryPoint: qs.entryPoint
    }, onData, onError);
};

function onData(response) {
    try {
        var data = response.message;
        console.debug(data);
        if(!data) {

        }
        var total = data.total;
        var failed = data.failed;
        var success = total - failed;
        var failedPct = (failed/total) * 100;
        var successPercnt = 100 - failedPct;

        $("#totalCount").html(total);
        $("#failedCount").html(failed);
        $("#failedPercent").html(Math.round(failedPct));
        $("#successCount").html(success);
        $("#successPercent").html(Math.round(successPercnt));

    } catch (e) {
        $("#canvas").html(gadgetUtil.getErrorText(e));
    }
};

function onError(msg) {
    $("#canvas").html(gadgetUtil.getErrorText(msg));
};