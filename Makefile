IMG  := quay.io/geotrellis/comparative-analysis-bastion

clean:
	rm -rf target/scala-2.11/comparative-analysis-bastion-assembly-0.0.1.jar
	rm ./build
	rm ./assemble

assemble:
	./sbt assembly
	touch ./assemble

build: assemble
	docker build -t ${IMG}:latest	.
	touch ./build

publish: build
	docker push ${IMG}:latest

test: build
	./sbt test
	docker-compose up -d
	sleep 2 && curl localhost:7070/system/status
	docker-compose down

