
$(function() {
    var page = gadgetUtil.getCurrentPage();
    var qs = gadgetUtil.getQueryString();

    gadgetUtil.fetchData(CONTEXT, {
        type: page.type,
    }, onData, onError);

    function onData(response) {
        $('.typeahead').typeahead({
            hint: true,
            highlight: true,
            minLength: 1
        }, {
            name: 'proxyName',
            source: substringMatcher(response.message)
        });
    }

    function onError(error) {

    }

    $('.typeahead').on('typeahead:selected', function(evt, item) {
        var href = parent.window.location.href;
        if(qs[PARAM_ID]) {
            href = href.replace(/(id=)[^\&]+/, '$1' + item);
        } else {
            href = href + "?" + PARAM_ID + "=" + item;
        }
        // console.log(href); 
        parent.window.location = href;
    });

    var substringMatcher = function(strs) {
        return function findMatches(q, cb) {
            var matches, substringRegex;

            // an array that will be populated with substring matches
            matches = [];

            // regex used to determine if a string contains the substring `q`
            substrRegex = new RegExp(q, 'i');

            // iterate through the pool of strings and for any string that
            // contains the substring `q`, add it to the `matches` array
            $.each(strs, function(i, str) {
                if (substrRegex.test(str)) {
                    matches.push(str);
                }
            });

            cb(matches);
        };
    };

});

$(window).load(function() {
    var typeAhead = $('.tt-menu'),
        parentWindow = window.parent.document,
        thisParentWrapper = $('#' + gadgets.rpc.RPC_ID, parentWindow).closest('.gadget-body');

    $('head', parentWindow).append('<link rel="stylesheet" type="text/css" href="../../store/carbon.super/gadget/auto-complete-search/css/autocomplete.css" />');
    $('body', parentWindow).append('<script src="../../store/carbon.super/gadget/auto-complete-search/js/typeahead.bundle.js" type="text/javascript"></script>');
    $(thisParentWrapper).append(typeAhead);
    $(thisParentWrapper).closest('.ues-component-box').addClass('widget form-control-widget');
    //    nanoScrollerSelector[0].nanoscroller.reset();
});