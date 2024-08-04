package com.task04;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;

import com.syndicate.deployment.annotations.events.SqsTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;


@LambdaHandler(lambdaName = "sqs_handler",
        roleName = "sqs_handler-role",
        isPublishVersion = false,
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@SqsTriggerEventSource(
        targetQueue = "async_queue",
        batchSize = 10
)
public class SqsHandler implements RequestHandler<SQSEvent, String> {
    LambdaLogger logger;

    @Override
    public String handleRequest(SQSEvent sqsEvent, Context context) {
        logger = context.getLogger();
        logger.log("Received SQS event: " + sqsEvent);

        return "Successfully processed SQS message.";
    }
}
