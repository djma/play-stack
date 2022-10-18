package djma.io;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import djma.common.Env;

public class TwilioService {
    final String myNumber;

    public TwilioService() {
        Env env = Env.get();
        Twilio.init(
                env.get("TWILIO_ACCOUNT_SID"),
                env.get("TWILIO_AUTH_TOKEN"));
        this.myNumber = env.get("TWILIO_MY_NUMBER");
    }

    public void sendSMS(String to, String message) {
        Message.creator(
                new PhoneNumber(to),
                new PhoneNumber(myNumber),
                message)
                .create();
    }
}