package djma.io;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.lookups.v1.PhoneNumber;

import djma.common.Env;

public class TwilioService {
    private static TwilioService INSTANCE;
    final String myNumber;

    private TwilioService() {
        Env env = Env.get();
        Twilio.init(
                env.get("TWILIO_ACCOUNT_SID"),
                env.get("TWILIO_AUTH_TOKEN"));
        this.myNumber = env.get("TWILIO_MY_NUMBER");
    }

    public static TwilioService get() {
        if (INSTANCE == null) {
            INSTANCE = new TwilioService();
        }
        return INSTANCE;
    }

    public void sendSMS(String to, String message) {
        Message.creator(
                new com.twilio.type.PhoneNumber(to),
                new com.twilio.type.PhoneNumber(myNumber),
                message)
                .create();
    }

    public String formatNumber(String number) {
        PhoneNumber phoneNumber = PhoneNumber.fetcher(new com.twilio.type.PhoneNumber(number)).fetch();
        return phoneNumber.getNationalFormat();
    }
}