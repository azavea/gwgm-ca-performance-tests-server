FROM java:8

MAINTAINER Nathan Zimmerman, npzimmerman@gmail.com

COPY target/scala-2.11/comparative-analysis-bastion-assembly-0.0.1.jar /opt/app/comparative-analysis-bastion-assembly-0.0.1.jar

CMD [ "java", "-jar", "/opt/app/comparative-analysis-bastion-assembly-0.0.1.jar" ]
