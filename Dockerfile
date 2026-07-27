FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

RUN chmod +x gradlew

RUN ./gradlew clean bootJar

EXPOSE 8080

CMD ["java","-jar","build/libs/Backend-0.0.1-SNAPSHOT.jar"]