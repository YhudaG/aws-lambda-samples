package lambdaFunction.utils;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lambdaFunction.model.PatientCheckoutEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SNSUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(SNSUtils.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AmazonSNS sns = AmazonSNSClient.builder().build();
    private static final String PATIENT_CHECKOUT_TOPIC = System.getenv("PATIENT_CHECKOUT_TOPIC");

    public void publishMethodToSns(List<PatientCheckoutEvent> patientCheckoutEvents) {
        patientCheckoutEvents.forEach(event -> {
            try {
                sns.publish(PATIENT_CHECKOUT_TOPIC, objectMapper.writeValueAsString(event));
            } catch (JsonProcessingException e) {
                LOGGER.error("Failed to write event to sns topic", e);
            }
        });
    }

    public void publishMethodToSns(PatientCheckoutEvent patientCheckoutEvents) {
        try {
            sns.publish(PATIENT_CHECKOUT_TOPIC, objectMapper.writeValueAsString(patientCheckoutEvents));
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to write event to sns topic", e);
        }
    }

}
