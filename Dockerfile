FROM java:8

MAINTAINER Nathan Zimmerman <npzimmerman@gmail.com>

EXPOSE 7070

COPY target/scala-2.11/comparative-analysis-bastion-assembly-0.0.1.jar /opt/app/comparative-analysis-bastion-assembly-0.0.1.jar

CMD sleep 200 && java -jar /opt/app/comparative-analysis-bastion-assembly-0.0.1.jar

