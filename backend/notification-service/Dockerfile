#image information
# base image
FROM openjdk:21-slim-buster
#maintainer
MAINTAINER dvdcwd@gmail.com

#Copy the jar file
COPY target/notification-service-0.0.1-SNAPSHOT.jar notification-service-0.0.1-SNAPSHOT.jar

# run the application
ENTRYPOINT ["java", "-jar", "notification-service-0.0.1-SNAPSHOT.jar"]