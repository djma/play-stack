package djma;

import java.io.IOException;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import djma.common.Env;
import djma.io.ContactSyncService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Hello world!
 *
 */
public class App {
    public record Person(String name, int age) {
    };

    public class SampleJetty extends AbstractHandler {

        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
                throws IOException, ServletException {
            response.setContentType("text/html;charset=utf-8");
            response.setStatus(HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);
            response.getWriter().println("<h1>This is my first test application using Jetty!</h1>");

        }
    }

    public static void main(String[] args) {
        ContactSyncService.get().loop();

        Env env = Env.get();

        // var svc = new TwilioService();
        // svc.sendSMS("MY_NUMBER", "Hello from Java 16!");

        Server serverSample = new Server(env.get("PORT") != null ? Integer.parseInt(env.get("PORT")) : 8080);
        SampleJetty sampleJetty = new App().new SampleJetty();
        try {
            serverSample.setHandler(sampleJetty);
            serverSample.start();
            serverSample.join();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
