# Stage 1: Build GraalVM Native Executable
FROM ghcr.io/graalvm/native-image-community:25 AS builder
WORKDIR /build

# Install findutils for gradlew
RUN microdnf install -y findutils && microdnf clean all

ENV ENV=build
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/dummy
ENV SPRING_DATASOURCE_USERNAME=dummy
ENV SPRING_DATASOURCE_PASSWORD=dummy

COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN chmod +x gradlew

COPY src src

# Compile Native Executable via GraalVM plugin
RUN ./gradlew nativeCompile --no-daemon -x test

# Stage 2: AWS Lambda Execution Image
FROM public.ecr.aws/lambda/provided:al2023

# Copy AWS Lambda Web Adapter extension to proxy events to port 8080
COPY --from=public.ecr.aws/awsguru/aws-lambda-adapter:1.0.1 /lambda-adapter /opt/extensions/lambda-adapter

WORKDIR /var/task

# Copy compiled native binary and assign it as AWS custom runtime bootstrap
COPY --from=builder /build/build/native/nativeCompile/f1_api /var/task/bootstrap
RUN chmod +x /var/task/bootstrap

# Environment settings for Web Adapter readiness checks
ENV PORT=8080
ENV AWS_LWA_READINESS_CHECK_PROTOCOL=HTTP
ENV AWS_LWA_READINESS_CHECK_PATH=/actuator/health
ENV AWS_LWA_READINESS_CHECK_PORT=8080

CMD ["/var/task/bootstrap"]