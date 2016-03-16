var charts = [{
    name: ROLE_TPS,
    columns: ["timestamp", "tps"],
    schema: [{
        "metadata": {
            "names": ["Time", "TPS"],
            "types": ["time", "linear"]
        },
        "data": []
    }],
    chartConfig: {
        x: "Time",
        charts: [{ type: "line", y: "TPS" }],
        padding: { "top": 10, "left": 50, "bottom": 40, "right": 30 }
    },
    types: [
        { name: TYPE_LANDING, type: 1 }
    ],
    processData: function(data) {
        var schema = this.schema;
        var columns = this.columns;
        data.forEach(function(row, i) {
            var record = [];
            columns.forEach(function(column) {
                var value = row[column];
                record.push(value);
            });
            schema[0].data.push(record);
        });
    }
}, {
    name: ROLE_RATE,
    schema: [{
        "metadata": {
            "names": ["Time", "Status", "Count"],
            "types": ["time", "ordinal", "linear"]
        },
        "data": []
    }],
    chartConfig: {
        x: "Time",
        charts: [{ type: "line", y: "Count", color: "Status" }],
        padding: { "top": 10, "left": 80, "bottom": 40, "right": 100 }
    },
    types: [
        { name: TYPE_LANDING, type: 2 },
        { name: TYPE_PROXY, type: 7 },
        { name: TYPE_API, type: 12 },
        { name: TYPE_MEDIATOR, type: 17 }
    ],
    processData: function(data) {
        var schema = this.schema;
        data.forEach(function(row, i) {
            var timestamp = row['timestamp'];
            var success = row["success"];
            var fault = row["faults"];
            schema[0].data.push([timestamp, "SUCCESS", success]);
            schema[0].data.push([timestamp, "FAULT", fault]);
            schema[0].data.push([timestamp, "TOTAL", success + fault]);
        });
    }
}, {
    name: ROLE_LATENCY,
    schema: [{
        "metadata": {
            "names": ["Time", "Type", "Value"],
            "types": ["time", "ordinal", "linear"]
        },
        "data": []
    }],
    chartConfig: {
        x: "Time",
        charts: [{ type: "line", y: "Value", color: "Type" }]
    },
    types: [
        { name: TYPE_PROXY, type: 8 },
        { name: TYPE_API, type: 13 },
        { name: TYPE_MEDIATOR, type: 18 }
    ],
    processData: function(data) {
        var schema = this.schema;
        data.forEach(function(row, i) {
            var timestamp = row['timestamp'];
            var min = row["min"];
            var avg = row["avg"];
            var max = row["max"];

            schema[0].data.push([timestamp, "Minimum", min]);
            schema[0].data.push([timestamp, "Average", avg]);
            schema[0].data.push([timestamp, "Maximum", max]);
        });
    }
}];