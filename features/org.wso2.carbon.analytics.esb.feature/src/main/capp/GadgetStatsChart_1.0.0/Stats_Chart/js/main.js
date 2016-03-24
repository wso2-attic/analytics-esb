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
    if (page) {
        type = page.type;
    }
    gadgetUtil.fetchData(CONTEXT, {
        type: type,
        id: qs.id,
        entryPoint: qs.entryPoint,
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
        var total = data.total;
        var failed = data.failed;
        if(total <= 1) {
            $('#gadget-messge').html(gadgetUtil.getEmptyRecordsText());
            $('#gadget-messge').show();
            $('#stats').hide();
            return;
        } else{
            $('#gadget-messge').hide();
            $('#stats').show();
        }

        var success = total - failed;
        var failedPct = (failed / total) * 100;
        var successPct = 100 - failedPct;

        $("#totalCount").html(total);
        $("#failedCount").html(failed);
        $("#failedPercent").html(parseFloat(failedPct).toFixed(2));
        $("#successCount").html(success);
        $("#successPercent").html(parseFloat(successPct).toFixed(2));

        //draw donuts
        var dataT = [{
            "metadata": {
                "names": ["rpm", "torque", "horsepower", "EngineType"],
                "types": ["linear", "linear", "ordinal", "ordinal"]
            },
            "data": [
                [0, 37, 12, "YES"],
                [0, 63, 12, "NO"]
            ]
        }];

        var dataF = [{
            "metadata": {
                "names": ["rpm", "torque", "horsepower", "EngineType"],
                "types": ["linear", "linear", "ordinal", "ordinal"]
            },
            "data": [
                [0, 63, 12, "YES"],
                [0, 37, 12, "NO"]
            ]
        }];

        var configT = {
            charts: [{ type: "arc", x: "torque", color: "EngineType" }],

            tooltip: { "enabled": false },
            legend: false,
            percentage: true,
            colorScale: ["#5CB85C", "#121822"],
            width: 400,
            height: 250
        }

        var configF = {
            charts: [{ type: "arc", x: "torque", color: "EngineType" }],

            tooltip: { "enabled": false },
            legend: false,
            percentage: true,
            colorScale: ["#D9534F", "#121822"],
            width: 400,
            height: 250
        }
        var chartT = new vizg(dataT, configT);
        chartT.draw("#dChartTrue");

        var chartF = new vizg(dataF, configF);
        chartF.draw("#dChartFalse");

    } catch (e) {
        $("#canvas").html(gadgetUtil.getErrorText(e));
    }
};

function onError(msg) {
    $("#canvas").html(gadgetUtil.getErrorText(msg));
}