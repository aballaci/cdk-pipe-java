package com.ballaci.pipeline;

import io.github.cdklabs.cdknag.NagPackSuppression;
import io.github.cdklabs.cdknag.NagSuppressions;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.StageProps;
import software.amazon.awscdk.pipelines.*;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;

public class PipelineStack extends Stack {


    private static String githubOrg = "aballaci";
    private static String githubRepo = "cdk-pipe-java";
    private static String githuBranch = "master";
    private static String pathSeparator = "/";
    private static String REGION = "eu-central-1";
    private static String DEV_ACCOUNT = "637423573379";
    private static String PROD_ACCOUNT = "211125352266";

    public PipelineStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        Environment envDev = makeEnv(DEV_ACCOUNT, REGION);
        Environment envPROD = makeEnv(PROD_ACCOUNT, REGION);

        final CodePipeline pipeline = new CodePipeline(this, "CDKPipelineJava",
                CodePipelineProps.builder()
                        .crossAccountKeys(true)
                        .pipelineName("CDKPipelineJava")
                        .synth(new ShellStep("build", ShellStepProps.builder()
                                .input(CodePipelineSource
                                        .gitHub(githubOrg + pathSeparator + githubRepo, githuBranch))
                                .installCommands(List.of(
                                        "npm install -g aws-cdk"   // Commands to run before build
                                ))
                                .commands(List.of(
                                        "mvn package",            // Language-specific build commands
                                        "npx cdk synth"           // Synth command (always same)
                                ))
                                .build())
                        )

                        .build());

        PipelineAppStage dev = new PipelineAppStage(this, "DEV", StageProps.builder()
                .env(envDev)
                .build());

        StageDeployment devStage = pipeline.addStage(dev);

        PipelineAppStage prod = new PipelineAppStage(this, "PROD", StageProps.builder()
                .env(envPROD)
                .build());

        StageDeployment prodStage = pipeline.addStage(prod);

        prodStage.addPost(new ManualApprovalStep("approval"));

        devStage.addPost(

                CodeBuildStep.Builder.create("TestAPIGatewayEndpoint")
                        .projectName("TestAPIGatewayEndpoint")
                        .envFromCfnOutputs(Map.of("ENDPOINT_URL", dev.helloUrl))
                        .commands(List.of(
                                "curl -Ssf $ENDPOINT_URL",
                                "curl -Ssf $ENDPOINT_URL/hello",
                                "curl -Ssf $ENDPOINT_URL/test"
                        ))
                        .build()
        );
    }


    // Helper method to build an environment
    static Environment makeEnv(String account, String region) {
        return Environment.builder()
                .account(account)
                .region(region)
                .build();
    }
}
