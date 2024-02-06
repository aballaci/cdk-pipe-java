package com.ballaci.pipeline;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Aspects;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;
import io.github.cdklabs.cdknag.AwsSolutionsChecks;

public class CdkApp {
    public static void main(final String[] args) {
        App app = new App();

//        Aspects.of(app).add(AwsSolutionsChecks.Builder.create().verbose(true).build());

        new PipelineStack(app, "JavaCdkPipeStack", StackProps.builder()
                .env(Environment.builder()
                        .account(System.getenv("CDK_DEFAULT_ACCOUNT"))
                        .region(System.getenv("CDK_DEFAULT_REGION"))
                        .build())
                .build());

        app.synth();
    }
}

