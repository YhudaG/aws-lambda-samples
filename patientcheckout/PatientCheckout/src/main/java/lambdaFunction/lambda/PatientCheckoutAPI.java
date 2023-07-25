package lambdaFunction.lambda;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lambdaFunction.model.PatientCheckoutEvent;
import lambdaFunction.utils.SNSUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatientCheckoutAPI {

    private final DynamoDB dynamoDB = new DynamoDB(AmazonDynamoDBAsyncClientBuilder.defaultClient());
    private final SNSUtils snsUtils = new SNSUtils();
    private final ObjectMapper objectMapper =  new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(PatientCheckoutAPI.class);


    public APIGatewayProxyResponseEvent handler(APIGatewayProxyRequestEvent request) {

        try {

            LOGGER.info("request body: " + request.getBody());
            PatientCheckoutEvent patientCheckoutEvent = objectMapper.readValue(request.getBody(), PatientCheckoutEvent.class);

            Table patientCheckoutTable = dynamoDB.getTable(System.getenv("PATIENT_CHECKOUT_TABLE"));
            Item item = new Item().withPrimaryKey("id", patientCheckoutEvent.getId())
                    .withString("firstName", patientCheckoutEvent.getFirstName())
                    .withString("middleName", patientCheckoutEvent.getMiddleName())
                    .withString("lastName", patientCheckoutEvent.getLastName())
                    .withString("ssn", patientCheckoutEvent.getSsn());
            patientCheckoutTable.putItem(item);

            snsUtils.publishMethodToSns(patientCheckoutEvent);

            return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatus.SC_OK).withBody("Patient Checkout ID: " + patientCheckoutEvent.getId());

        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to parse input to PatientCheckoutEvent", e);

            // send the failed data to dead letter queue
            AmazonSQS sqsClient = AmazonSQSClientBuilder.defaultClient();
            String queueUrl  = System.getenv("PATIENT_CHECKOUT_DLQ");

            sqsClient.sendMessage(queueUrl, request.getBody());

            throw new RuntimeException(e);
        }


    }

}