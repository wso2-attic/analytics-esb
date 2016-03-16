    var TYPE = 1;
    var TOPIC = "subscriber";
    var page = gadgetUtil.getCurrentPage();
    var qs = gadgetUtil.getQueryString();
    var timeFrom, timeTo, timeUnit = null;

    $(function() {
        if (qs[PARAM_ID] == null) {
            $("#canvas").html(gadgetUtil.getDefaultText());
            return;
        }
        timeFrom = gadgetUtil.timeFrom();
        timeTo = gadgetUtil.timeTo();
        console.log("MESSAGE_FLOW[" + page.name + "]: TimeFrom: " + timeFrom + " TimeTo: " + timeTo);

        gadgetUtil.fetchData(CONTEXT, {
            type: page.type,
            id: qs.id,
            timeFrom: timeFrom,
            timeTo: timeTo
        }, onData, onError);

    });

    gadgets.HubSettings.onConnect = function() {
        gadgets.Hub.subscribe(TOPIC, function(topic, data, subscriberData) {
            onTimeRangeChanged(data);
        });
    };

    function onTimeRangeChanged(data) {
        timeFrom = data.timeFrom;
        timeTo = data.timeTo;
        timeUnit = data.timeUnit;
        gadgetUtil.fetchData(CONTEXT, {
            type: page.type,
            id: qs.id,
            timeFrom: timeFrom,
            timeTo: timeTo
        }, onData, onError);
    };

    function onData(response) {
        var data = response.message;
        console.log(data); 
        if (data.length == 0) {
            $("#canvas").html(gadgetUtil.getEmptyRecordsText());
            return;
        }
        $("#canvas").empty();
        // var g = new dagreD3.graphlib.Graph({ compound: true })
        //     .setGraph({})
        //     .setDefaultEdgeLabel(function() {
        //         return {}; });

        var nodes = data;
        // nodes.forEach(function(node, i) {
        //     var label = buildLabel(node);
        //     g.setNode(node.id, { labelType: "html", label: label });
        //     if (node.children) {
        //         node.children.forEach(function(child) {
        //             g.setEdge(node.id, child, { lineInterpolate: 'basis', arrowheadClass: 'arrowhead' });
        //         });
        //     }
        //     if (node.group) {
        //         g.setParent(node.id, node.group);
        //     }
        //     if (node.type && node.type === "group") {
        //         g.setNode(node.id, { label: node.label, clusterLabelPos: 'top' });
        //     }
        // });

        // // Round the corners of the nodes
        // g.nodes().forEach(function(v) {
        //     var node = g.node(v);
        //     node.rx = node.ry = 7;
        // });
        // // Create the renderer
        // var render = new dagreD3.render();
        // // Set up an SVG group so that we can translate the final graph.
        // var svg = d3.select("svg"),
        //     svgGroup = svg.append("g");
        // // Run the renderer. This is what draws the final graph.
        // render(d3.select("svg g"), g);
        // // Center the graph
        // var xCenterOffset = (svg.attr("width") - g.graph().width) / 2;
        // svgGroup.attr("transform", "translate(" + xCenterOffset + ", 20)");
        // svg.attr("height", g.graph().height + 140);

        // Create the input graph
        var g = new dagreD3.graphlib.Graph({compound:true})
          .setGraph({rankdir: "TD"})
          .setDefaultEdgeLabel(function() { return {}; });


          var groups = [];

          for (var i =0; i < nodes.length; i ++) {
            if (nodes[i].id != null) {
              //Set Nodes
              if (nodes[i].type == "group") {
                g.setNode(nodes[i].id,   {label:  "", clusterLabelPos: 'top'});

                //Add arbitary nodes for group
                g.setNode(nodes[i].id + "-s", {label: nodes[i].label, style: 'stroke-width: 0px;'});
               // g.setEdge(nodes[i].id + "-s", nodes[i].id + "-e",  { style: 'stroke-width: 0px; fill: #ffd47f'});
                g.setNode(nodes[i].id + "-e", {label:  "", style: 'stroke-width: 0px;'});
                g.setParent(nodes[i].id + "-s", nodes[i].id);
                g.setParent(nodes[i].id + "-e", nodes[i].id);

                groups.push(nodes[i]);
              } else {
                var label = buildLabel(nodes[i]);
                g.setNode(nodes[i].id, {labelType: "html",label: label});
                // g.setNode(nodes[i].id, {label: nodes[i].label});
              }

              //Set Edges
              if (nodes[i].parents != null) {
              for (var x =0; x < nodes[i].parents.length; x ++) {
                var isParentGroup = false;
                 for (var y = 0; y < groups.length; y++) {
                      if (groups[y].id == nodes[i].parents[x] && groups[y].type == "group") {
                            isParentGroup = true;
                      }
                 }
            
                if (nodes[i].type == "group") { 
                      if (isParentGroup) {
                            g.setEdge(nodes[i].parents[x] + "-e", nodes[i].id + "-s",{ lineInterpolate: 'basis', arrowheadClass: 'arrowhead' });
                         } else {
                            g.setEdge(nodes[i].parents[x], nodes[i].id + "-s", { lineInterpolate: 'basis', arrowheadClass: 'arrowhead' });
                         }
                } else {
                   if (isParentGroup) {
                    g.setEdge(nodes[i].parents[x] + "-e", nodes[i].id, { lineInterpolate: 'basis', arrowheadClass: 'arrowhead' });
                   } else {
                      g.setEdge(nodes[i].parents[x], nodes[i].id, { lineInterpolate: 'basis', arrowheadClass: 'arrowhead' });
                   }
                }
            }
          }

          if (nodes[i].group != null) {
            g.setParent(nodes[i].id, nodes[i].group);
              if (nodes[i].type != "group" && nodes[i].parents.length == 0) {  
                g.setEdge(nodes[i].group + "-s", nodes[i].id,  { style: 'stroke-width: 0px; '});
                g.setEdge(nodes[i].id, nodes[i].group + "-e",  { style: 'stroke-width: 0px; '});
          } 

          }
         
            }
                
          }

        g.nodes().forEach(function(v) {
          var node = g.node(v);

          node.rx = node.ry = 7;
        });


        // Create the renderer
        var render = new dagreD3.render();

        // Set up an SVG group so that we can translate the final graph.
        var svg = d3.select("svg"),
            svgGroup = svg.append("g");

        // Run the renderer. This is what draws the final graph.
        render(d3.select("svg g"), g);

        // Center the graph
        var xCenterOffset = (svg.attr("width") - g.graph().width) / 2;
        svgGroup.attr("transform", "translate(" + xCenterOffset + ", 20)");
        svg.attr("height", g.graph().height + 140);
    };

    function buildLabel(node) {
        var pageUrl = MEDIATOR_PAGE_URL;
        if(node.type === "Sequence") {
            pageUrl = SEQUENCE_PAGE_URL;
        } else if(node.type === "Endpoint") {
            pageUrl = ENDPOINT_PAGE_URL;
        }
        var hiddenParams = "";
        if(node.hiddenAttributes) {
            node.hiddenAttributes.forEach(function(item,i) {
                hiddenParams += "&" + item.name + "=" + item.value;
            });   
        }
        var targetUrl = pageUrl + "?" + hiddenParams;
        var labelText = '<div><h4><a target="_blank" href="' + targetUrl + '">' + node.label + "</a></h4>";;
        if (node.dataAttributes) {
            node.dataAttributes.forEach(function(item, i) {
                labelText += "<h5><label>" + item.name + " : </label><span>" + item.value + "</span></h5>";
            });
        }
        labelText += "</div>";
        return labelText;
    };

    function onError(msg) {
        $("#canvas").html(gadgetUtil.getErrorText(msg));
    };