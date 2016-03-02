var TYPE = 9;
var TOPIC = "subscriber";

$(function() {
    var qs = gadgetUtil.getQueryString();
    if (qs[PARAM_PROXY_NAME] == null) {
        $("#canvas").html(gadgetUtil.getEmptyProxyText());
        return;
    }
    var timeFrom = gadgetUtil.timeFrom();
    var timeTo = gadgetUtil.timeTo();
    console.log("PROXY_MESSAGES: TimeFrom: " + timeFrom + " TimeTo: " + timeTo);

    gadgetUtil.fetchData(CONTEXT, {
        type: TYPE,
        timeFrom: timeFrom,
        timeTo: timeTo
    }, onData, onError);

    $('#tblProxies tbody').on('click', 'tr', function() {
        parent.window.location = PROXY_PAGE_URL;
        if ($(this).hasClass('selected')) {
            $(this).removeClass('selected');
        } else {
            dataTable.$('tr.selected').removeClass('selected');
            $(this).addClass('selected');
        }
    });

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

function onData(response) {
    var data = response.message;
    if (data.length == 0) {
        $("#canvas").html('<div align="center" style="margin-top:20px"><h4>No records found.</h4></div>');
        return;
    }
    $("#tblProxies tbody").empty();
    var columns = ["messageId", "payload", "entryType","timestamp"];
    var tbody = $("#tblProxies tbody");
    data.forEach(function(row, i) {
        var tr = jQuery('<tr/>');
        columns.forEach(function(column) {
            var td = jQuery('<td/>');
            var value = row[column];
            td.append(value);
            td.appendTo(tr);

        });
        tr.appendTo(tbody);

    });
    dataTable = $('#tblProxies').DataTable();
};

function onError(msg) {

};