package com.ballaci.pipeline;

import software.amazon.awscdk.CfnOutput;
import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.Stage;
import software.amazon.awscdk.StageProps;

public class PipelineAppStage extends Stage {

    public final CfnOutput helloUrl;
    public PipelineAppStage(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public PipelineAppStage(final Construct scope, final String id, final StageProps props) {
        super(scope, id, props);

        CdkMainStack service = new CdkMainStack(this, "MainStack");

        helloUrl = service.helloUrl;
    }

}