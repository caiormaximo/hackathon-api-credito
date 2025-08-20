#build usa imagem maven com jdk21 para compilar o projeto
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

#execução usa uma imagem jre enxuta para rodar a aplicacao
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
#copia apenas o jar compilado do estágio de build
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
#inicia a aplicacao
ENTRYPOINT ["java","-jar","app.jar"]