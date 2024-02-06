package com.ballaci.pipeline;

import io.github.cdklabs.cdknag.NagPackSuppression;
import io.github.cdklabs.cdknag.NagSuppressions;
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

    public PipelineStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        final CodePipeline pipeline = new CodePipeline(this, "CDKPipeline",
                CodePipelineProps.builder()
                        .crossAccountKeys(true)
                        .pipelineName("CDKPipeline")
                        .synth(new ShellStep("build", ShellStepProps.builder()
                                .input(CodePipelineSource
                                        .gitHub(new StringBuilder(githubOrg)
                                                .append(pathSeparator)
                                                .append(githubRepo)
                                                .toString(), githuBranch))
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

        StageDeployment devStage = pipeline.addStage(new PipelineAppStage(this, "DEV", StageProps.builder()
                .env(Environment.builder()
                        .account("637423573379")
                        .region("eu-central-1")
                        .build())
                .build()));

        StageDeployment prodStage = pipeline.addStage(new PipelineAppStage(this, "PROD", StageProps.builder()
                .env(Environment.builder()
                        .account("211125352266")
                        .region("eu-central-1")
                        .build())
                .build()));

        prodStage.addPost(new ManualApprovalStep("approval"));

        PipelineAppStage deploy = new PipelineAppStage(this, "Deploy");

        devStage.addPost(

                CodeBuildStep.Builder.create("TestAPIGatewayEndpoint")
                        .projectName("TestAPIGatewayEndpoint")
                        .envFromCfnOutputs(Map.of("ENDPOINT_URL", deploy.helloUrl))
                        .commands(List.of(
                                "curl -Ssf $ENDPOINT_URL",
                                "curl -Ssf $ENDPOINT_URL/hello",
                                "curl -Ssf $ENDPOINT_URL/test"
                        ))
                        .build()
        );
    }
}
