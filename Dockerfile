FROM openjdk:11-jdk

ADD . /authentication_service
WORKDIR /authentication_service

# Run Maven build
RUN chmod +x mvnw && ./mvnw clean install -DskipTests

MAINTAINER enrico.vompa@gmail.com

ENV JAVA_OPTS="-Xms8192m -Xmx8192m"

# Fire up our Spring Boot app by default
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /authentication_service/target/authentication-service-0.0.1-SNAPSHOT.jar" ]

EXPOSE 8001:8001
