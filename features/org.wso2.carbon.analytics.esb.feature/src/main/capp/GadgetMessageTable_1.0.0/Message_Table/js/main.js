var TOPIC = "subscriber";
var page = gadgetUtil.getCurrentPage();

$(function() {
    var qs = gadgetUtil.getQueryString();
    if (qs[PARAM_ID] == null) {
        $("#canvas").html(gadgetUtil.getDefaultText());
        return;
    }
    var timeFrom = gadgetUtil.timeFrom();
    var timeTo = gadgetUtil.timeTo();
    console.log("MESSAGE_TABLE[" + page.name + "]: TimeFrom: " + timeFrom + " TimeTo: " + timeTo);

    gadgetUtil.fetchData(CONTEXT, {
        type: page.type,
        id: qs.id,
        timeFrom: timeFrom,
        timeTo: timeTo
    }, onData, onError);

    $('#tblMessages tbody').on('click', 'tr', function() {
        var id = $(this).find("td:first").html(); 
        if ($(this).hasClass('selected')) {
            $(this).removeClass('selected');
        } else {
            dataTable.$('tr.selected').removeClass('selected');
            $(this).addClass('selected');
        }
        parent.window.location = MESSAGE_PAGE_URL + "?" + PARAM_ID + "=" + id;
    });

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
            $("#canvas").html('<div align="center" style="margin-top:20px"><h4>No records found.</h4></div>');
            return;
        }
        $("#tblMessages tbody").empty();
        
        var columns = page.columns;
        var thead = $("#tblMessages thead tr");
        columns.forEach(function(column) {
            var th = jQuery('<th/>');
            th.append(column.label);
            th.appendTo(thead);
        });

        var tbody = $("#tblMessages tbody");
        data.forEach(function(row, i) {
            var tr = jQuery('<tr/>');
            columns.forEach(function(column) {
                var td = jQuery('<td/>');
                var value = row[column.name];
                td.text(value);
                td.appendTo(tr);

            });
            tr.appendTo(tbody);

        });
        dataTable = $('#tblMessages').DataTable();
    } catch (e) {
        $("#canvas").html(gadgetUtil.getErrorText(e));
    }
};

function onError(msg) {
    $("#canvas").html(gadgetUtil.getErrorText(msg));
};