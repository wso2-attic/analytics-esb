$(function() {

    //if there are url elemements present, use them. Otherwis use last hour
    var timeFrom = moment().subtract(1, 'hours'); 
    var timeTo = moment();

    var qs = getQueryString();
    if(qs.timeFrom != null) {
        timeFrom = qs.timeFrom;
    }
    if(qs.timeTo != null) {
        timeTo = qs.timeTo;
    }
    console.log("TimeFrom: " + timeFrom + " TimeTo: " + timeTo); 

    //get the selected value of the dropdown
    var filter = $("#filter").val();
    var count = 0;

    //draw the date range picker first
    function cb(start, end) {
        $('#timerange span').html(start.format('MMMM D, YYYY HH:mm') + ' - ' + end.format('MMMM D, YYYY HH:mm'));
        timeFrom = new Date(start).getTime();
        timeTo = new Date(end).getTime();
        fetchData(timeFrom,timeTo,filter);

        // Object (or String) to be publish
        var message = {
            timeFrom: timeFrom,
            timeTo: timeTo,
            filter: filter
        };
        if(count > 0) {
            gadgets.Hub.publish('publisher', message);
        }
        count++;
    }
    // cb(moment().subtract(1, 'hours'), moment());
    cb(timeFrom, timeTo);

    $('#timerange').daterangepicker({
        opens: "left",
        ranges: {
            'Last 24 Hours': [moment().subtract(1, 'days'), moment().subtract(1, 'days')],
            'Last 30 Days': [moment().subtract(29, 'days'), moment()],
            'Last Year': [moment().subtract(1, 'year'), moment()]
        }
    }, cb);

    $('#filter').change(function(){
        var message = {
            timeFrom: timeFrom,
            timeTo: timeTo,
            filter: $("#filter").val()
        };
        gadgets.Hub.publish('publisher', message);
        fetchData(timeFrom, timeTo,$('#filter').val());
    });

    //Call the backend and read some data
    function fetchData(timeFrom,timeTo,filter) {
        $.ajax({
            url: CONTEXT + "?type=1&timeFrom=" + timeFrom + "&timeTo=" + timeTo,
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
        $("#canvas").empty();
        var columns = ["timestamp", "tps"];
        var schema = [{
            "metadata": {
                "names": ["Time", "TPS"],
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
        // console.log(schema[0].data); 

        // var width = ($('#canvas').width() - 180);
        // var height = 350;

        var width = ($('#canvas').width() - 20);
        var height = 300;
        console.log("Width: " + $('#canvas').width() + " Height: " + height);

        var config = {
            x: "Time",
            charts: [{ type: "line", y: "TPS"}],
            maxLength: 10,
            width: width,
            height: height,
            padding:{"top": 20, "left": 100, "bottom": 40, "right": 100}
        };
        var chart = new vizg(schema, config);
        chart.draw("#canvas");
    };

    function onError(msg) {

    };


});