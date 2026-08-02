FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /build

# Copy parent dependencies
COPY pom.xml .

# Maven tools
COPY mvnw ./
COPY .mvn/ .mvn/
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY . .

# Generate sources and package application
RUN ./mvnw -q -B clean generate-sources
RUN ./mvnw -q -B clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

ARG APP_VERSION=1.0.0-SNAPSHOT
ENV APP_VERSION=${APP_VERSION}

# Copy artifact from previous build phase
COPY --from=build /build/target/*.jar app.jar

ARG PORT_APP=8080
ENV PORT $PORT_APP

EXPOSE $PORT

ENTRYPOINT ["java", "-jar", "app.jar"]