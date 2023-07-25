package lambdaFunction.errorHandling;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ErrorHandler {

    private final AmazonS3 s3 = AmazonS3Client.builder().build();
    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void handler(SQSEvent sqsEvent) {

        sqsEvent.getRecords().forEach(sqsMessage -> {

            String messageBody = sqsMessage.getBody();
            LOGGER.info("Dead Letter Queue received message: {}", messageBody);

            try {
                S3ObjectInputStream s3InputStream = getS3ObjectInputStream(messageBody);

                logMessage(s3InputStream);

            } catch (Exception e) {
                LOGGER.error("Failed to parse DLQ event to Json: {}", e.getMessage());
            }
        });
    }

    private S3ObjectInputStream getS3ObjectInputStream(String messageBody) throws JsonProcessingException {
        LOGGER.info("inside getS3ObjectInputStream method");

        try {

            JsonNode jsonNode = objectMapper.readTree(messageBody);

            JsonNode bucketName = jsonNode
                    .path("Records")
                    .get(0)
                    .path("s3")
                    .path("bucket")
                    .path("name");

            JsonNode objectKey = jsonNode
                    .path("Records")
                    .get(0)
                    .path("s3")
                    .path("object")
                    .path("key");

            if (bucketName == null || objectKey == null) {
                throw new RuntimeException("No content in bucket, input received from API Gateway");
            }

            return s3.getObject(bucketName.asText(), objectKey.asText())
                    .getObjectContent();

        } catch (Exception e) {
            throw new RuntimeException("No content in bucket, input received from API Gateway");
        }
    }

    private void logMessage(S3ObjectInputStream s3InputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(s3InputStream))) {
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            LOGGER.info("The failed message is:\n{}", stringBuilder);
        } catch (IOException e) {
            throw new IOException(e);
        }
    }
}