package djma;

import static djma.common.Common.getBodyJson;
import static djma.common.Common.ifNull;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.HandlerWrapper;

import djma.db.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthHandler extends HandlerWrapper {
    final AuthService authSvc = AuthService.get();

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        if (target.startsWith("/auth") || req.getMethod().equals("OPTIONS")) {
            super.handle(target, baseRequest, req, resp);
        } else {
            // String token = request.getHeader("Authorization"); // ??

            // Currently unused. Don't know how to have the front-end keep the cookie.
            // Probably because Access-Control-Allow-Origin is * in local dev.
            Cookie[] cookies = ifNull(req.getCookies(), new Cookie[0]);
            String authToken = null;
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("authToken")) {
                    authToken = cookie.getValue();
                    break;
                }
            }

            if (authToken == null) {
                Map<String, Object> body = getBodyJson(req);
                authToken = (String) body.get("authToken");
            }

            boolean isAuthorized = authToken != null && authSvc.isAuthorized(UUID.fromString(authToken));

            if (!isAuthorized) {
                resp.setStatus(401);
                resp.getWriter().println("Unauthorized");
                baseRequest.setHandled(true);
            } else {
                super.handle(target, baseRequest, req, resp);
            }
        }
    }
}
