var qs = gadgetUtil.getQueryString();
var pageName = gadgetUtil.getCurrentPageName();
var currentLocation;

$(function() {
    currentLocation = pageName;
    if (currentLocation != TYPE_LANDING) {
    	appendTrail(currentLocation);
        if (qs[PARAM_ID] != null) {
        	appendTrail(qs[PARAM_ID]);
        }

    }
});

function appendTrail(text) {
    var ol = $(".breadcrumb");
    var li = jQuery('<li/>');
    li.addClass("active dashboard-name truncate");
    li.text(text);
    ol.append(li);
}