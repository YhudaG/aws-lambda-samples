package lambdaFunction.lambda;

import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lambdaFunction.model.PatientCheckoutEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BillManagementLambda {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(BillManagementLambda.class);

    public void handler(SNSEvent snsEvent){
        snsEvent.getRecords().forEach(snsRecord -> {
            try {
                PatientCheckoutEvent patientCheckoutEvent = objectMapper.readValue(snsRecord.getSNS().getMessage(), PatientCheckoutEvent.class);
                LOGGER.info("Received a new SNS message");
                LOGGER.info(patientCheckoutEvent.toString());
            } catch (JsonProcessingException e) {
                LOGGER.error("Failed to read sns message", e);
            }
        });

    }

}
