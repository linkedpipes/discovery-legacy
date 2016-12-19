$(document).ready(function () {

    var host = "http://localhost:9000";

    var datasourceGroups = [

        {
            label: "",
            description: "",
            datasources: [
                {
                    "url": "http://linked.opendata.cz/sparql",
                    "defaultGraphIris": ["http://linked.opendata.cz/resource/dataset/ares/basic"],
                    "label": "Business Entities in Czech Republic",
                    "isLarge": true,
                    "isLinkset": false,
                    "descriptorIri": "https://raw.githubusercontent.com/linkedpipes/discovery/master/data/rdf/datasources/business-entities-cz-ares/sample.ttl"
                }
            ]
        }

    ];

    _.each(datasourceGroups, function (dsGroup) {

        var startData = {
            "sparqlEndpoints": dsGroup.datasources
        };

        var request = $.ajax({
            url: host + "/discovery/start",
            method: "POST",
            contentType: "application/json; charset=utf-8",
            data: JSON.stringify(startData),
            dataType: "json"
        });

        request.done(function( idData ) {
           console.log(idData.id);
        });

    });


});
