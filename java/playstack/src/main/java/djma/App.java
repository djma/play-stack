package djma;

import java.io.IOException;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

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
        var p = new Person("John", 42);
        System.out.println("Hello %s!".formatted(p.name()));

        Server serverSample = new Server(8000);
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
