IMG  := comparative-analysis-bastion

compile:
	./sbt assembly

build:
	docker build -t ${IMG}:latest	.

publish: build
	docker push ${IMG}:latest
	# TODO: add publishing logic which makes sense
	#if [ "${TAG}" != "" -a "${TAG}" != "latest" ]; then docker tag ${IMG}:latest ${IMG}:${TAG} && docker push ${IMG}:${TAG}; fi

test: build
	docker run --rm -p 9090:9090 ${IMG}
	# TODO: add tests prior to teardown
	docker stop $(docker ps -q --filter ancestor=${IMG})

