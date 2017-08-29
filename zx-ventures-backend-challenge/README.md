ZX Ventures Backend Challenge
=============================
scala + play + akka + jts + mongodb (sorta)



Building and Running
--------------------
To build the platform I am using sbt, but I'm pretty sure you can build it using activator.

To build the distribution version using sbt, you should run:

                $ sbt universal:packageBin

Then you get the **target/universal/zx-ventures-backend-challenge-1.0.zip** and unzip it where you see fit.

To run it, just run the **<path>/zx-ventures-backend-challenge** file.

You can also change where Play listens for connections, using Play's properties:

                <path>/bin/zx-ventures-backend-challenge -Dhttp.port=8080
                <path>/bin/zx-ventures-backend-challenge -Dhttp.address=127.0.0.1


REST Routes
-----------
The routes are:
    get by id:          GET  /pdv/:pdvId
    get all:            GET  /pdvs
    insert:             PUT  /pdv
    bulk insert:        PUT  /pdvs
    find by latlng:     POST /pdv/find


I use [httpie](https://github.com/jkbrzt/httpie) for accessing the rest endpoints.
httpie or curl makes a bulk insert a very easy task.

    $ http GET http://localhost:9000/assets/pdvs.json | http PUT http://localhost:9000/pdvs
            HTTP/1.1 200 OK
            Content-Length: 15
            Content-Type: application/json
            Date: Tue, 29 Aug 2017 17:18:33 GMT
            Referrer-Policy: origin-when-cross-origin, strict-origin-when-cross-origin
            X-Content-Type-Options: nosniff
            X-Frame-Options: DENY
            X-Permitted-Cross-Domain-Policies: master-only
            X-XSS-Protection: 1; mode=block

            {
                "status": "ok"
            }

You may point your browser to <address>/view so you can interact with a leaflet map (just click the map to get the nearest PoS).
The smiley face button shows all PoS loaded (either in memory if you are using the InMemoryPDVActor or in store, in case you are using the MongoPDVActor).
The sad face button clears the map (also, clicking anywhere on the map where is a POS available, will remove all other POS).


The Actors
----------
I build two example actors: InMemoryPDVActor and MongoPDVActor.
The InMemoryPDVActor stores the POS in memory and uses JTS for topology functions (distance, within queries, etc).
The MongoPDVActor stores the data in a mongodb instance and uses the Geospatial MongoDB queries.