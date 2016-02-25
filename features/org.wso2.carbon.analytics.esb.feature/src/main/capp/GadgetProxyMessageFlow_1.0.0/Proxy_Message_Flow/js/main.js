    var TYPE = 5;
    var TOPIC = "subscriber";

    $(function() {
        // var timeFrom = moment().subtract(1, 'hours'); 
        // var timeTo = moment();

        // var qs = getQueryString();
        // if(qs.timeFrom != null) {
        //     timeFrom = qs.timeFrom;
        // }
        // if(qs.timeTo != null) {
        //     timeTo = qs.timeTo;
        // }
        // console.log("TOP_APIS: TimeFrom: " + timeFrom + " TimeTo: " + timeTo); 
        // fetchData(timeFrom, timeTo,null);  

        var workers = {

            "identifier": {
                "consumers": 2,
                "count": 20
            },
            "lost-and-found": {
                "consumers": 1,
                "count": 1,
                "inputQueue": "identifier",
                "inputThroughput": 50
            },
            "monitor": {
                "consumers": 1,
                "count": 0,
                "inputQueue": "identifier",
                "inputThroughput": 50
            },
            "meta-enricher": {
                "consumers": 4,
                "count": 9900,
                "inputQueue": "identifier",
                "inputThroughput": 50
            },
            "geo-enricher": {
                "consumers": 2,
                "count": 1,
                "inputQueue": "meta-enricher",
                "inputThroughput": 50
            }
        };

        // Set up zoom support
        var svg = d3.select("svg"),
            inner = svg.select("g"),
            zoom = d3.behavior.zoom().on("zoom", function() {
                inner.attr("transform", "translate(" + d3.event.translate + ")" +
                    "scale(" + d3.event.scale + ")");
            });
        svg.call(zoom);

        var render = new dagreD3.render();

        // Left-to-right layout
        var g = new dagreD3.graphlib.Graph();
        g.setGraph({
            nodesep: 20,
            ranksep: 50,
            rankdir: "LR",
            marginx: 20,
            marginy: 20
        });

        function draw(isUpdate) {
            for (var id in workers) {
                var worker = workers[id];
                var className = worker.consumers ? "running" : "stopped";
                if (worker.count > 10000) {
                    className += " warn";
                }
                var html = "<div>";
                html += "<span class=status></span>";
                html += "<span class=consumers>" + worker.consumers + "</span>";
                html += "<span class=name>" + id + "</span>";
                html += "<span class=queue><span class=counter>" + worker.count + "</span></span>";
                html += "</div>";

                g.setNode(id, {
                    labelType: "html",
                    label: "<div>Mediator</div>",
                    rx: 5,
                    ry: 5,
                    padding: 5,
                    class: className
                });

                if (worker.inputQueue) {
                    g.setEdge(worker.inputQueue, id, {
                        label: worker.inputThroughput + "/s",
                        width: 40
                    });
                }
            }

            inner.call(render, g);
        }



        draw();
    });

    // gadgets.HubSettings.onConnect = function() {
    //     gadgets.Hub.subscribe(TOPIC, function(topic, data, subscriberData) {
    //         onTimeRangeChanged(data);
    //     });
    // };

    // function onTimeRangeChanged(data) {
    //    fetchData(data.timeFrom,data.timeTo,data.filter);
    // }

    // //Call the backend and read some data
    // function fetchData(timeFrom, timeTo, filter) {
    //     $.ajax({
    //         url: CONTEXT + "?type=" + TYPE + "&timeFrom=" + timeFrom + "&timeTo=" + timeTo,
    //         type: "GET",
    //         success: function(data) {
    //             onData(data);
    //         },
    //         error: function(msg) {
    //             onError(msg);
    //         }
    //     });
    // }

    // function onData(data) {
    //     $("#canvas").empty();
    //     var schema = [{
    //         "metadata": {
    //             "names": ["name", "requests"],
    //             "types": ["ordinal", "linear"]
    //         },
    //         "data": []
    //     }];

    //     var config = {
    //         charts: [{ type: "arc", x: "requests", color: "name", mode: "pie" }],
    //         width: 400,
    //         height: 250
    //     }

    //     data.forEach(function(row,i) {
    //         schema[0].data.push([row.name,row.requests]);
    //     });

    //     var onChartClick = function(event, item) {
    //         parent.window.location = "/portal/dashboards/esb-analytics/proxies";
    //     };

    //     var chart = new vizg(schema, config);
    //     chart.draw("#canvas", [{ type: "click", callback: onChartClick }]);
    // };

    // function onError(msg) {

    // };