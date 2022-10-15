package djma;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class TwilioService {
    String myNumber = System.getenv("TWILIO_MY_NUMBER");

    public TwilioService() {
        Twilio.init(
                System.getenv("TWILIO_ACCOUNT_SID"),
                System.getenv("TWILIO_AUTH_TOKEN"));
    }

    public void sendSMS(String to, String message) {
        Message.creator(
                new PhoneNumber(to),
                new PhoneNumber(myNumber),
                message)
                .create();
    }
}