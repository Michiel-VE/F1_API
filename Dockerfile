FROM public.ecr.aws/lambda/java:21

# The AWS image sets the WORKDIR to /var/task by default
COPY build/libs/f1_api.jar ${LAMBDA_TASK_ROOT}/lib/

# Set the CMD to the handler string
CMD ["be.michielve.f1_api.lambdas.LambdaHandler::handleRequest"]