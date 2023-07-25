package lambdaFunction.lambda;

import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import lambdaFunction.model.PatientCheckoutEvent;
import lambdaFunction.utils.SNSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class PatientCheckoutLambda {

    private final AmazonS3 s3 = AmazonS3Client.builder().build();
    private final SNSUtils snsUtils = new SNSUtils();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(PatientCheckoutLambda.class);


    public void handler(S3Event s3Event) {
        // when a file received to s3 bucket it sends event that contains bucket name and the key of the new object that was received.
        // by those properties we can extract the content of the object from s3 bucket
        s3Event.getRecords().forEach(record -> {
            try (S3ObjectInputStream s3InputStream = s3
                    .getObject(record.getS3().getBucket().getName(), record.getS3().getObject().getKey())
                    .getObjectContent()) {
                List<PatientCheckoutEvent> patientCheckoutEvents = Arrays.asList(objectMapper.readValue(s3InputStream, PatientCheckoutEvent[].class));
                // insert the received message to the sns topic
                snsUtils.publishMethodToSns(patientCheckoutEvents);
            } catch (IOException e) {
                LOGGER.error("Failed to parse input to list of PatientCheckoutEvent", e);
                throw new RuntimeException(e);
            }
        });
    }

}
