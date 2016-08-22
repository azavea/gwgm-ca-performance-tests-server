IMG  := comparative-analysis-bastion

clean:
	rm -rf target/scala-2.11/comparative-analysis-bastion-assembly-0.0.1.jar

assemble:
	./sbt assembly

build: clean assemble
	docker build -t ${IMG}:latest	.

publish: build
	docker push ${IMG}:latest

test: build
	./sbt test
	docker run --rm -p 7070:7070 ${IMG}
	# TODO: add tests prior to teardown
	docker stop $(docker ps -q --filter ancestor=${IMG})

