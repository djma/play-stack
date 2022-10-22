package djma;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import djma.common.Env;
import djma.io.ContactSyncService;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        ContactSyncService.get().loop();

        Env env = Env.get();

        QueuedThreadPool threadPool = new QueuedThreadPool();
        Server server = new Server(threadPool);
        int port = env.getInt("PORT", 8080);
        var connector = new ServerConnector(server);
        connector.setPort(port);
        server.setConnectors(new ServerConnector[] { connector });

        ServletHandler handler = new ServletHandler();
        handler.addServletWithMapping(SampleServlet.class, "/*");
        handler.addServletWithMapping(GraphQLServlet.class, "/gql/*");

        GzipHandler gzipHandler = new GzipHandler();
        gzipHandler.setHandler(handler);
        gzipHandler.setIncludedMethods("POST", "GET");
        gzipHandler.setIncludedMimeTypes(
                "text/html",
                "text/plain",
                "text/xml",
                "text/json",
                "application/json",
                "text/css",
                "application/javascript",
                "text/javascript");
        gzipHandler.setInflateBufferSize(2048);
        try {
            server.setHandler(gzipHandler);
            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
