package com.ballaci.pipeline;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.services.apigateway.LambdaRestApi;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
// import software.amazon.awscdk.Duration;
// import software.amazon.awscdk.services.sqs.Queue;

public class CdkMainStack extends Stack {

    public final CfnOutput helloUrl;
    public CdkMainStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public CdkMainStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // The code that defines your stack goes here

        // Defines a new lambda resource
        final Function hello = Function.Builder.create(this, "HelloHandler")
                .runtime(Runtime.NODEJS_18_X)    // execution environment
                .code(Code.fromAsset("lambda"))  // code loaded from the "lambda" directory
                .handler("hello.handler")        // file is "hello", function is "handler"
                .build();

        // Defines an API Gateway REST API resource backed by our "hello" function
        final LambdaRestApi gateway = LambdaRestApi.Builder.create(this, "Endpoint")
                .handler(hello)
                .build();

        helloUrl = CfnOutput.Builder.create(this, "TableViewerUrl")
                .value(gateway.getUrl())
                .build();
    }
}
