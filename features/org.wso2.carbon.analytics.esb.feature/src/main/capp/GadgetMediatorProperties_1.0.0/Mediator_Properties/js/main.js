var TOPIC = "subscriber";
var type = 45;
var qs = gadgetUtil.getQueryString();
var BEFORE = "before";
var AFTER = "after";

$(function() {
    if (qs[PARAM_ID] == null) {
        $("#gadget-message").html(gadgetUtil.getDefaultText());
        $("#gadget-message").show();
        $("#props").hide();
        return;
    } else {
        $("#gadget-message").hide();
        $("#props").show();
    }
});

gadgets.HubSettings.onConnect = function() {
    gadgets.Hub.subscribe(TOPIC, function(topic, data, subscriberData) {
        mediatorClicked(data);
    });
};

function mediatorClicked(data) {
    var componentId = data.componentId;
    var hashCode = data.hashCode;
    if(componentId && hashCode) {
        gadgetUtil.fetchData(CONTEXT, {
            type: type,
            id: qs.id,
            componentId: componentId,
            hashCode: hashCode
        }, onData, onError);
    }
};

function onData(response) {
    try {
        var data = response.message;
        console.log(data); 
        if (!data) {
            $("#canvas").html(gadgetUtil.getEmptyRecordsText());
            return;
        }
        if(data.payload.before) {
            $("#payloadBfr").text(data.payload.before);
        }
        if(data.payload.after) {
            $("#payloadAftr").text(data.payload.after);
        }

        if(data.transportProperties) {
           drawPropertyTable(data.transportProperties,$("#tblTransportBfr tbody"),BEFORE);
           drawPropertyTable(data.transportProperties,$("#tblTransportAftr tbody"),AFTER);
        }
        if(data.contextProperties) {
           drawPropertyTable(data.contextProperties,$("#tblCtxtBfr tbody"),BEFORE);
           drawPropertyTable(data.contextProperties,$("#tblCtxtAftr tbody"),AFTER);
        }
    } catch (e) {
        $("#gadget-message").html(gadgetUtil.getErrorText(e));
    }
};

function onError(msg) {
    $("#gadget-message").html(gadgetUtil.getErrorText(msg));
};

function drawPropertyTable(properties,tbody,side) {
    tbody.empty();
    properties.forEach(function (property) {
        var tr = jQuery('<tr/>');
        var tdKey = jQuery('<td/>');
        var tdValue = jQuery('<td/>');
        tr.append(tdKey.append(property.name));
        if(side === BEFORE) {
            tr.append(tdValue.append(property.before));
        } else {
            tr.append(tdValue.append(property.after));
        }
        tr.appendTo(tbody);
    });
}