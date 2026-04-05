FROM public.ecr.aws/lambda/java:21

COPY build/libs/dependency/* ${LAMBDA_TASK_ROOT}/lib/
COPY build/libs/f1_api.jar ${LAMBDA_TASK_ROOT}/lib/

CMD ["be.michielve.f1_api.lambdas.LambdaHandler::handleRequest"]