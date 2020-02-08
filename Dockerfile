FROM openjdk:11-jdk

ADD . /arete_ui_back
WORKDIR /arete_ui_back

# Run Maven build
RUN chmod +x mvnw && ./mvnw clean install -DskipTests

MAINTAINER enrico.vompa@gmail.com

ENV JAVA_OPTS=""

# Fire up our Spring Boot app by default
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /arete_ui_back/target/arete-ui-back-0.0.1-SNAPSHOT.jar" ]

EXPOSE 8001:8001
