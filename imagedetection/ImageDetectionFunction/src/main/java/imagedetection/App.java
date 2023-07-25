package imagedetection;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Collectors;


public class App {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
    // the region of rekognition must be the same as the s3 region
    private final AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.standard().withRegion(Regions.EU_WEST_2).build();

    public void handleRequest(final S3Event s3Event) {

        LOGGER.info("S3 Event Received");

        s3Event.getRecords().forEach(s3Record -> {

            String bucketName = s3Record.getS3().getBucket().getName();
            String objectKey = s3Record.getS3().getObject().getKey();

            // Amazon Rekognition Image currently supports the JPEG and PNG image formats. (https://aws.amazon.com/rekognition/faqs/)
            if (Boolean.FALSE.equals(objectKey.toLowerCase().endsWith(".jpeg") || objectKey.toLowerCase().endsWith(".png"))){
                LOGGER.error("Image type {} is not supported", objectKey.substring(objectKey.lastIndexOf(".")));
                return;
            }

            S3Object s3Object = new S3Object().withName(objectKey).withBucket(bucketName);
            Image image = new Image().withS3Object(s3Object);

            DetectLabelsRequest detectLabelsRequest = new DetectLabelsRequest()
                    .withImage(image)
                    .withMaxLabels(10)
                    .withMinConfidence(75f);

            DetectLabelsResult detectLabelsResult = rekognitionClient.detectLabels(detectLabelsRequest);

            Set<String> labelNames = detectLabelsResult.getLabels().stream().map(Label::getName).collect(Collectors.toSet());
            LOGGER.info(" *** Detected labels ***");
            LOGGER.info(labelNames.toString());
        });
    }

}
