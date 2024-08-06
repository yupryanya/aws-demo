package com.task07;

import com.syndicate.deployment.annotations.events.RuleEventSource;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.google.gson.Gson;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.Instant;
import java.util.*;

@LambdaHandler(lambdaName = "uuid_generator",
        roleName = "uuid_generator-role",
        isPublishVersion = true,
        aliasName = "${lambdas_alias_name}",
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@EnvironmentVariables(value = {
        @EnvironmentVariable(key = "region", value = "${region}"),
        @EnvironmentVariable(key = "bucket", value = "uuid-storage")
})
@RuleEventSource(
        targetRule = "uuid_trigger"
)
public class UuidGenerator implements RequestHandler<ScheduledEvent, String> {

    public String handleRequest(ScheduledEvent event, Context context) {

        String fileName = Instant.now().toString();
        String bucketName = System.getenv("bucket");

        S3Client s3 = createS3client(Region.of(System.getenv("region")));

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();
        s3.putObject(objectRequest, RequestBody.fromString(generateContent()));

        return "OK";
    }

    private static class UuidWrapper {
        private final List<String> ids;

        public UuidWrapper(List<String> ids) {
            this.ids = ids;
        }

        public List<String> getIds() {
            return ids;
        }
    }

    private static S3Client createS3client(Region region) {
        return S3Client.builder()
                .region(region)
                .build();
    }

    private String generateContent() {
        List<String> uuids = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            uuids.add(UUID.randomUUID().toString());
        }
        Gson gson = new Gson();
        return gson.toJson(new UuidWrapper(uuids));
    }
}
