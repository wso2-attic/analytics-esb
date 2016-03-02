    var TYPE = 1;
    var TOPIC = "subscriber";

    $(function() {
        var timeFrom = gadgetUtil.timeFrom();
        var timeTo = gadgetUtil.timeTo();
        console.log("PROXY_MESSAGE_FLOW: TimeFrom: " + timeFrom + " TimeTo: " + timeTo);

        gadgetUtil.fetchData(CONTEXT, {
            type: TYPE,
            timeFrom: timeFrom,
            timeTo: timeTo
        }, onData, onError);

        var nodes = {
            "a": {
                "count": 2
            },
            "b": {
                "count": 5,
                "parent": ["a"]
            },
            "c": {
                "count": 4,
                "parent": ["a"]
            },
            "d": {
                "count": 7,
                "parent": ["b", "c"]
            }
        };

        // Set up zoom support
        var svg = d3.select("svg"),
            inner = svg.select("g"),
            zoom = d3.behavior.zoom().on("zoom", function() {
                inner.attr("transform", "translate(" + d3.event.translate + ")" +
                    "scale(" + d3.event.scale + ")");
            });
        // svg.call(zoom);
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

        function draw() {
            for (var id in nodes) {
                var node = nodes[id];

                var html = "<div>";
                html += "<span class=name>" + id + "</span>";
                html += "<span class=queue><span class=counter>" + node.count + "</span></span>";
                html += "</div>";

                var className = "foo";

                g.setNode(id, {
                    labelType: "html",
                    label: html,
                    rx: 5,
                    ry: 5,
                    padding: 5,
                    class: className
                });

                if (node.parent) {
                    node.parent.forEach(function(parent) {
                        g.setEdge(parent, id, {
                            label: "s",
                            width: 40
                        });
                    });

                }
            }
            inner.call(render, g);
        };

        draw();
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
    };

    function onData(data) {
        if (data.message.length == 0) {
            $("#canvas").html('<div align="center" style="margin-top:20px"><h4>No records found.</h4></div>');
            return;
        }
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
        data.message.sort(function(a, b) {
            return a.timestamp - b.timestamp;
        });

        data.message.forEach(function(row, i) {
            var record = [];
            columns.forEach(function(column) {
                var value = row[column];
                record.push(value);
            });
            schema[0].data.push(record);
        });

       
    };

    function onError(msg) {

    };