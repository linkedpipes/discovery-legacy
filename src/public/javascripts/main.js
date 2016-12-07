$(document).ready(function () {

    var host = "http://localhost:9000";

    var businessEntitiesCZ = {
        "url": "http://linked.opendata.cz/sparql",
        "defaultGraphIris": ["http://linked.opendata.cz/resource/dataset/ares/basic"],
        "label": "Business Entities in Czech Republic",
        "isLarge": true,
        "isLinkset": false,
        "descriptorIri": "https://raw.githubusercontent.com/linkedpipes/discovery/master/data/rdf/datasources/business-entities-cz-ares/sample.ttl"
    };

    var checkActions_cz_ctia = {
        "url": "http://linked.opendata.cz/sparql",
        "defaultGraphIris": ["http://linked.opendata.cz/resource/dataset/coi.cz/kontroly"],
        "label": "Check Actions - Czech Trade Inspection Authority",
        "isLarge": false,
        "isLinkset": false,
        "descriptorIri": "https://raw.githubusercontent.com/linkedpipes/discovery/master/data/rdf/datasources/checkactions-cz-ctia/sample.ttl"
    };

    var checkActions_cz_sao = {
        "url": "http://linked.opendata.cz/sparql",
        "defaultGraphIris": ["http://data.nku.cz/resource/dataset/check-actions"],
        "label": "Check Actions - Czech Supreme Audit Office",
        "isLarge": false,
        "isLinkset": false,
        "descriptorIri": "https://raw.githubusercontent.com/linkedpipes/discovery/master/data/rdf/datasources/checkactions-cz-sao/sample.ttl"
    };

    var cz_ruian_address_places = {
        "url": "http://ruian.linked.opendata.cz/sparql",
        "defaultGraphIris": [],
        "label": "Address Places in Czech Republic - RÚIAN",
        "isLarge": true,
        "isLinkset": false,
        "descriptorIri": "https://raw.githubusercontent.com/linkedpipes/discovery/master/data/rdf/datasources/cz-ruian-address-places/sample.ttl"
    };

    var cz_ruian_towns = {
        "url": "http://ruian.linked.opendata.cz/sparql",
        "defaultGraphIris": [],
        "label": "Towns in Czech Republic - RÚIAN",
        "isLarge": true,
        "isLinkset": false,
        "descriptorIri": "https://raw.githubusercontent.com/linkedpipes/discovery/master/data/rdf/datasources/cz-ruian-towns/sample.ttl"
    };

    var dblp = {
        "url": "http://dblp.l3s.de/d2r/sparql",
        "defaultGraphIris": [],
        "label": "DBLP",
        "isLarge": false,
        "isLinkset": false,
        "descriptorIri": "https://raw.githubusercontent.com/linkedpipes/discovery/master/data/rdf/datasources/dblp/sample.ttl"
    };

    var dbpedia_earthquakes = {
        "url": "http://dbpedia.org/sparql",
        "defaultGraphIris": ["http://dbpedia.org"],
        "label": "DBPedia",
        "isLarge": true,
        "isLinkset": false,
        "descriptorIri": "https://raw.githubusercontent.com/linkedpipes/discovery/master/data/rdf/datasources/dbpedia-earthquakes/sample.ttl"
    };

    var dpedia_towns = {
        "url": "http://dbpedia.org/sparql",
        "defaultGraphIris": ["http://dbpedia.org"],
        "label": "Towns in DBPedia",
        "isLarge": true,
        "isLinkset": false,
        "descriptorIri": "https://raw.githubusercontent.com/linkedpipes/discovery/master/data/rdf/datasources/dbpedia-towns/sample.ttl"
    };

    var dcsu_sheffield = {
        "url": "http://data.dcs.shef.ac.uk/dump/datadcs-withdblpV1-2Mar2010.rdf",
        "defaultGraphIris": [],
        "label": "University of Sheffield - Department of Computer Science",
        "isLarge": false,
        "isLinkset": false,
        "descriptorIri": "https://raw.githubusercontent.com/linkedpipes/discovery/master/data/rdf/datasources/department-of-computer-science-university-of-sheffield/sample.ttl"
    };

    var edp = {
        "url": "http://www.europeandataportal.eu/sparql",
        "label": "European Data Portal",
        "isLarge": true,
        "isLinkset": false,
        "descriptorIri": "https://raw.githubusercontent.com/linkedpipes/discovery/master/data/rdf/edp/sample.ttl"
    };

    var legislation_cz_acts_versions = {
        "url": "http://linked.opendata.cz/sparql",
        "defaultGraphIris": [],
        "label": "Legislation CZ - Versions of Acts",
        "isLarge": true,
        "isLinkset": false,
        "descriptorIri": "https://raw.githubusercontent.com/linkedpipes/discovery/master/data/rdf/datasources/legislation-cz-acts-versions/sample.ttl"
    };

    var legislation_cz_acts = {
        "url": "http://linked.opendata.cz/sparql",
        "defaultGraphIris": [],
        "label": "Legislation CZ - Acts",
        "isLarge": true,
        "isLinkset": false,
        "descriptorIri": "https://raw.githubusercontent.com/linkedpipes/discovery/master/data/rdf/datasources/legislation-cz-acts/sample.ttl"
    };

    var legislation_gb_acts_versions = {
        "url": "http://gov.tso.co.uk/legislation/sparql",
        "defaultGraphIris": [],
        "label": "Legislation UK - Versions of Acts",
        "isLarge": true,
        "isLinkset": false,
        "descriptorIri": "https://raw.githubusercontent.com/linkedpipes/discovery/master/data/rdf/datasources/legislation-gb-acts-versions/sample.ttl"
    };

    var legislation_gb_acts = {
        "url": "http://gov.tso.co.uk/legislation/sparql",
        "defaultGraphIris": [],
        "label": "Legislation UK - Acts",
        "isLarge": true,
        "isLinkset": false,
        "descriptorIri": "https://raw.githubusercontent.com/linkedpipes/discovery/master/data/rdf/datasources/legislation-gb-acts/sample.ttl"
    };

    var linkedmdb = {
        "url": "http://data.linkedmdb.org/sparql",
        "defaultGraphIris": [],
        "label": "LinkedMDB",
        "isLarge": false,
        "isLinkset": false,
        "descriptorIri": "https://raw.githubusercontent.com/linkedpipes/discovery/master/data/rdf/datasources/linkedmdb/sample.ttl"
    };

    var nomisma = {
        "url": "http://nomisma.org/nomisma.org.ttl",
        "defaultGraphIris": [],
        "label": "Nomisma.org - Persons",
        "isLarge": true,
        "isLinkset": false,
        "descriptorIri": "https://raw.githubusercontent.com/linkedpipes/discovery/master/data/rdf/datasources/nomisma-org-persons/sample.ttl"
    };

    var subsidies_cz_cedr = {
        "url": "http://cedropendata.mfcr.cz/c3lod/cedr/sparql",
        "defaultGraphIris": [],
        "label": "Subsidies from public budgets in Czech Republic",
        "isLarge": true,
        "isLinkset": false,
        "descriptorIri": "https://raw.githubusercontent.com/linkedpipes/discovery/master/data/rdf/datasources/subsidies-cz-cedr/sample.ttl"
    };

    var wikidata_towns = {
        "url": "https://query.wikidata.org/bigdata/namespace/wdq/sparql",
        "defaultGraphIris": [],
        "label": "Towns in Wikidata",
        "isLarge": true,
        "isLinkset": false,
        "descriptorIri": "https://raw.githubusercontent.com/linkedpipes/discovery/master/data/rdf/datasources/wikidata-towns/sample.ttl"
    };

    var datasourceGroups = [
        {
            datasources: [businessEntitiesCZ]
        },

        {
            datasources: [checkActions_cz_ctia]
        },

        {
            datasources: [checkActions_cz_sao]
        },

        {
            datasources: [cz_ruian_address_places]
        },/*

        {
            datasources: [cz_ruian_towns]
        },

        {
            datasources: [dblp]
        },

        {
            datasources: [dbpedia_earthquakes]
        },

        {
            datasources: [dpedia_towns]
        },

        {
            datasources: [dcsu_sheffield]
        },

        {
            datasources: [edp]
        },

        {
            datasources: [legislation_cz_acts_versions]
        },

        {
            datasources: [legislation_cz_acts]
        },

        {
            datasources: [legislation_gb_acts_versions]
        },

        {
            datasources: [legislation_gb_acts]
        },

        {
            datasources: [nomisma]
        },

        {
            datasources: [subsidies_cz_cedr]
        },

        {
            datasources: [wikidata_towns]
        },
        {
            datasources: [linkedmdb]
        },*/
    ];

    var count = 0;
    var doneCount = 0;

    var experimentsCount = datasourceGroups.length;

    if(experimentsCount > 0)
    {
        runExperiment(datasourceGroups[0]);
    }

    function runExperiment(dsGroup) {

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

        request.done(function (idData) {

            count++;
            $("#count").html(count);

            waitDone(idData.id, function (id, duration) {
                var csvRequest = $.ajax({
                    url: host + "/discovery/" + id + "/csv",
                    method: "GET"
                });

                csvRequest.done(function (csv) {
                    doneCount++;
                    $("#donecount").html(doneCount);

                    var dsnames = dsGroup.datasources.map(function (ds) {
                        return ds.label;
                    }).join(",");

                    $("#csv").append("\n\nThe following experiment has finished in " + duration + " ms;" + dsnames + "\n");

                    $("#csv").append(csv);

                    if(doneCount < experimentsCount)
                    {
                        runExperiment(datasourceGroups[doneCount]);
                    }
                });

            });
        });

    }

    function waitDone(id, whenDone) {
        var request = $.ajax({
            url: host + "/discovery/" + id,
            method: "GET",
            contentType: "application/json; charset=utf-8",
            dataType: "json"
        });
        request.done(function (statusData) {
            if (!statusData.isFinished) {
                window.setTimeout(function () {
                    waitDone(id, whenDone);
                }, 1000);
            } else {
                whenDone(id, statusData.duration);
            }
        });
    }


});
