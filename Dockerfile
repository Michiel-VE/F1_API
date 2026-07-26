FROM public.ecr.aws/lambda/provided:al2023

COPY build/native/nativeCompile/f1_api ${LAMBDA_TASK_ROOT}/bootstrap

RUN chmod +x ${LAMBDA_TASK_ROOT}/bootstrap

CMD ["handler"]