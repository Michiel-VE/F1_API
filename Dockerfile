# Stage 1: Build native image inside GraalVM JDK 21 container
FROM ghcr.io/graalvm/native-image-community:21 AS builder
WORKDIR /build

# Install findutils to provide xargs required by gradlew
RUN microdnf install -y findutils && microdnf clean all

ENV ENV=build
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/dummy
ENV SPRING_DATASOURCE_USERNAME=dummy
ENV SPRING_DATASOURCE_PASSWORD=dummy

COPY gradlew settings.gradle build.gradle ./
RUN chmod +x gradlew
COPY gradle gradle
COPY src src

RUN ./gradlew nativeCompile --stacktrace --no-daemon

# Stage 2: AWS Lambda Runtime
FROM public.ecr.aws/lambda/provided:al2023
COPY --from=builder /build/build/native/nativeCompile/f1_api /var/runtime/bootstrap
RUN chmod +x /var/runtime/bootstrap

CMD [ "be.michielve.f1_api.lambdas.LambdaHandler::handleRequest" ]