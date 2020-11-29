FROM openjdk:11-jdk

ADD . /authentication_service
WORKDIR /authentication_service

# Run Maven build
RUN chmod +x mvnw && ./mvnw clean install -DskipTests

MAINTAINER enrico.vompa@gmail.com

ENV JAVA_OPTS="-Xms8192m -Xmx8192m -Djava.security.egd=file:/dev/./urandom -jar"

# Fire up our Spring Boot app by default
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS /authentication_service/target/authentication_service-1.0.jar" ]

EXPOSE 8001:8001
