var configs = [{
        name: TYPE_PROXY,
        type: 9,
        columns: [
            { name: "messageFlowId", label: "Message ID", type: "ordinal" },
            { name: "host", label: "Host", type: "ordinal" },
            { name: "startTime", label: "Timestamp", type: "ordinal" },
            { name: "status", label: "Message Status", type: "ordinal" }
        ]
    }, {
        name: TYPE_API,
        type: 14,
        columns: [
            { name: "messageFlowId", label: "Message ID", type: "ordinal" },
            { name: "host", label: "Host", type: "ordinal" },
            { name: "startTime", label: "Timestamp", type: "ordinal" },
            { name: "status", label: "Message Status", type: "ordinal" }
        ]
    },{
        name: TYPE_MEDIATOR,
        type: 19,
        columns: [
            { name: "messageFlowId", label: "Message ID", type: "ordinal" },
            { name: "host", label: "Host", type: "ordinal" },
            { name: "startTime", label: "Timestamp", type: "ordinal" },
            { name: "status", label: "Message Status", type: "ordinal" }
        ]
    }
];