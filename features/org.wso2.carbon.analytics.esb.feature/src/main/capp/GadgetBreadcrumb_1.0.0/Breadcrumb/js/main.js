var href = parent.window.location.href;
var lastSegment = href.substr(0,href.lastIndexOf('/') + 1);
var baseUrl = parent.window.location.protocol + "//" + parent.window.location.host + BASE_URL;
var qs = gadgetUtil.getQueryString();
var pageName = gadgetUtil.getCurrentPageName();
var currentLocation;

$(function() {
    $("#homeLink").attr("href", baseUrl);
    currentLocation = pageName;
    if (currentLocation != TYPE_LANDING) {
        appendTrail(lastSegment + pageName,currentLocation);
        if (qs[PARAM_ID] != null) {
            appendTrail(lastSegment + pageName + "?" + PARAM_ID + "=" + qs[PARAM_ID],qs[PARAM_ID]);
        }

    }
});

function appendTrail(url,text) {
    var ol = $(".breadcrumb");
    var li = jQuery('<li/>');
    var a = jQuery('<a/>');
    li.addClass("active dashboard-name truncate");
    a.attr("data-href",url);
    a.text(text);
    li.append(a);
    ol.append(li);
}

$(".breadcrumb a").click(function(e) {
     e.preventDefault();
    // return false;
    alert(e); 
    // parent.window.location = $(this);
});
