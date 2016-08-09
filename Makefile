IMG  := comparative-analysis-bastion

compile:
	./sbt assembly

build:
	docker build -t ${IMG}:latest	.

publish: build
	docker push ${IMG}:latest

test: build
	./sbt test
	docker run --rm -p 7070:7070 ${IMG}
	# TODO: add tests prior to teardown
	docker stop $(docker ps -q --filter ancestor=${IMG})

