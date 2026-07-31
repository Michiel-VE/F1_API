package be.michielve.f1_api.lambdas;

import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import be.michielve.f1_api.F1ApiApplication;
import be.michielve.f1_api.config.DotenvInitializer;

public class LambdaHandler implements RequestHandler<AwsProxyRequest, AwsProxyResponse> {
    
    private static final SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;

    static {
        DotenvInitializer.init();
        
        try {
            handler = SpringBootLambdaContainerHandler.getAwsProxyHandler(F1ApiApplication.class);
            
            // Disable FAIL_ON_EMPTY_BEANS directly on the container handler's ObjectMapper
            if (handler.getObjectMapper() != null) {
                handler.getObjectMapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
                handler.getObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not initialize Spring Boot application", e);
        }
    }

    @Override
    public AwsProxyResponse handleRequest(AwsProxyRequest awsProxyRequest, Context context) {
        return handler.proxy(awsProxyRequest, context);
    }
}