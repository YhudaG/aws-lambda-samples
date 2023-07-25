package lambdaFunction.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PatientCheckoutEvent {

    private int id;
    private String firstName;
    private String middleName;
    private String lastName;
    private String ssn;

}
