$(function() {
    var TOPIC = "publisher";

    //if there are url elemements present, use them. Otherwis use last hour
    var timeFrom = moment().subtract(29, 'days');
    var timeTo = moment();

    var qs = getQueryString();
    if (qs.timeFrom != null) {
        timeFrom = qs.timeFrom;
    }
    if (qs.timeTo != null) {
        timeTo = qs.timeTo;
    }
    console.log("TimeFrom: " + timeFrom + " TimeTo: " + timeTo);

   $("#lastHour").click(function () {
        var message = {
            timeFrom: new Date(moment().subtract(24, 'hours')).getTime(),
            timeTo: new Date(moment()).getTime()
        };
        gadgets.Hub.publish(TOPIC, message);
        window.location.hash = "newvalue";
   });

   $("#lastDay").click(function () {
        var message = {
            timeFrom: new Date(moment().subtract(1, 'day')).getTime(),
            timeTo: new Date(moment()).getTime()
        };
        gadgets.Hub.publish(TOPIC, message);
   });

   $("#lastMonth").click(function () {
        var message = {
            timeFrom: new Date(moment().subtract(29, 'days')).getTime(),
            timeTo: new Date(moment()).getTime()
        };
        gadgets.Hub.publish(TOPIC, message);
   });

   $("#lastYear").click(function () {
        var message = {
            timeFrom: new Date(moment().subtract(1, 'year')).getTime(),
            timeTo: new Date(moment()).getTime()
        };
        gadgets.Hub.publish(TOPIC, message);
   });


});