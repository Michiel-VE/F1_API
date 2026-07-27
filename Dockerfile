FROM public.ecr.aws/lambda/provided:al2023

COPY build/native/nativeCompile/f1_api /var/runtime/bootstrap

RUN chmod +x /var/runtime/bootstrap

CMD [ "be.michielve.f1_api.lambdas.LambdaHandler::handleRequest" ]