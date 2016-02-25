$(function() {
    var timeFrom = moment().subtract(1, 'hours'); 
    var timeTo = moment();
    var dataTable = null;

    var qs = getQueryString();
    if(qs.timeFrom != null) {
        timeFrom = qs.timeFrom;
    }
    if(qs.timeTo != null) {
        timeTo = qs.timeTo;
    }
    console.log("TimeFrom: " + timeFrom + " TimeTo: " + timeTo); 

    $.ajax({
        url: CONTEXT + "?type=2&timeFrom=" + timeFrom + "&timeTo=" + timeTo,
        type: "GET",
        success: function(data) {
            onData(data);
        },
        error: function(msg) {
            onError(msg);
        }
    });

    $('#tblProxies tbody').on('click', 'tr', function() {
        parent.window.location = "https://localhost:9443/portal/dashboards/esb-analytics/proxies";
        if ($(this).hasClass('selected')) {
            $(this).removeClass('selected');
        } else {
            dataTable.$('tr.selected').removeClass('selected');
            $(this).addClass('selected');
        }
    });

});

gadgets.HubSettings.onConnect = function() {
    gadgets.Hub.subscribe('city-list', function(topic, data, subscriberData) {
        onTimeRangeChange(data);
    });
};

function onTimeRangeChange(data) {
    $.ajax({
        url: CONTEXT + "?type=2",
        type: "GET",
        success: function(data) {
            onData(data);
        },
        error: function(msg) {
            onError(msg);
        }
    });
}

function onData(data) {
    $("#tblProxies tbody").empty();
    var columns = ["name", "min", "avg", "max", "requests", "faults"];
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