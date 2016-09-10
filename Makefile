IMG  := quay.io/geotrellis/comparative-analysis-bastion

ASSEMBLY_JAR := target/scala-2.11/comparative-analysis-bastion-assembly-*.jar

clean:
	rm -rf target/scala-2.11/comparative-analysis-bastion-assembly-0.0.1.jar
	rm ./build
	rm ./assemble

rwildcard=$(foreach d,$(wildcard $1*),$(call rwildcard,$d/,$2) $(filter $(subst *,%,$2),$d))

${ASSEMBLY_JAR}: $(call rwildcard, src, *.scala) build.sbt
	./sbt assembly

assembly: ${ASSEMBLY_JAR}

build: Dockerfile assembly
	docker build -t ${IMG}:rob	.

publish: build
	docker push ${IMG}:rob

test: build
	./sbt test
	docker-compose up -d
	sleep 2 && curl localhost:7070/system/status
	docker-compose down
