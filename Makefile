IMG  := quay.io/geotrellis/comparative-analysis-bastion

compile:
	./sbt assembly

build: compile
	docker build -t ${IMG}:latest	.

publish: build
	docker push ${IMG}:latest

test: build
	./sbt test
	docker run --rm ${IMG}:latest
	# TODO: add tests prior to teardown
	docker stop $(docker ps -q --filter ancestor=${IMG})

