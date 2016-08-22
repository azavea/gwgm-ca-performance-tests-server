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

```
docker run -it --net=desktop_default --rm -p 7070:7070 \
       -e GM_USER=root -e GM_PASS=GisPwd -e GM_INSTANCE=geomesa -e GM_ZK=zookeeper \
       -e GW_USER=root -e GW_PASS=GisPwd -e GW_INSTANCE=geowave -e GW_ZK=zookeeper \
       -v $(pwd):/code:rw -v $HOME/.ivy2:/root/.ivy2:rw -v $HOME/.sbt:/root/.sbt:rw openjdk:8-jdk \
       bash
```
