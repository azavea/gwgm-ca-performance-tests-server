# GeoWave/GeoMesa Comparative Analysis Bastion

This repository stores the source files and scripts necessary to
build a containerized bastion into geowave and geomesa clusters. Its
primary purpose is to carry out and analyze the performance of queries
over each of these frameworks.

### Building and testing

To build, simply run `make build`. Scala source will be compiled and a
docker image will be generated which serves this (akka-http backed)
project on port 7070.

Testing of the scala source is best done through sbt (for fast
iteration) with either `sbt test` (if unit tests are desired) or
`sbt ~reStart` (if running the server and interacting with it manually
is necessary). `make test` will run `sbt test` and verify that the
docker container is properly constructed and serving data.
