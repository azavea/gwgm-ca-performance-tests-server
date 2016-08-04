IMG  := quay.io/geotrellis/comparative-analysis-bastion

compile:
	./sbt assembly

build:
	docker build -t ${IMG}:latest	.

publish: build
	docker push ${IMG}:latest

test: build
	./sbt test
	docker run --rm -p 9090:9090 ${IMG}:latest
	# TODO: add tests prior to teardown
	docker stop $(docker ps -q --filter ancestor=${IMG})

