package djma;

import java.io.IOException;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.http.entity.ContentType;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import djma.common.Env;
import djma.db.AuthService;
import djma.db.AuthService.Session;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static djma.common.Common.getBodyJson;
import static djma.common.Common.ifNull;
import static djma.common.Common.optChain;
import static djma.common.Common.simpleObjectMapper;

/**
 * Warning: Do not use in production. This is for learning purpose only. Do not
 * roll your own authentication.
 * 
 * An http authentication servlet that implements a very simplified version of
 * SIWE (Sign-in with Ethereum). The full EIP is here:
 * https://eips.ethereum.org/EIPS/eip-4361
 * 
 */
public class AuthServlet extends HttpServlet {
    private final Env env = Env.get();
    private final AuthService authSvc = AuthService.get();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setAccessControl(resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setAccessControl(resp);
        String pathInfo = req.getPathInfo();

        // Java 18 can match on null case
        // https://stackoverflow.com/questions/10332132/how-to-use-null-in-switch
        switch (ifNull(pathInfo, "default")) {
            case "/logout" -> logout(req, resp);
            default -> resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setAccessControl(resp);
        String pathInfo = req.getPathInfo();

        switch (ifNull(pathInfo, "default")) {
            case "/nonce" -> nonce(req, resp);
            case "/verify" -> verify(req, resp);
            case "/me" -> me(req, resp);
            default -> resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    private void verify(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, Object> body = getBodyJson(req);

        String address = normalizePublicAddress((String) body.get("address"));
        String message = (String) body.get("message");
        String signature = (String) body.get("signature");

        String recoveredAddress = normalizePublicAddress(getAddressUsedToSignHashedMessage(signature, message));

        // Validate signature, nonce, and address
        if (recoveredAddress == null || !recoveredAddress.equals(address)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        UUID nonce = UUID.fromString(message);
        Session session = authSvc.getSessionFromNonce(nonce);

        if (session == null) {
            System.out.println("Nonce not found: " + nonce);
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Login the address with the nonce. Give cookie
        // Currently unused. Don't know how to have the front-end keep the cookie.
        // Probably because Access-Control-Allow-Origin is * in local dev.
        UUID authToken = session.authToken();
        System.out.println("Logged in " + address + " with authToken " + authToken);
        Cookie cookie = new Cookie("authToken", authToken.toString());
        cookie.setMaxAge(60 * 60 * 24 * 1); // 1 day
        cookie.setPath("/");
        resp.addCookie(cookie);

        HashMap<String, Object> jsonResp = new HashMap<>();
        jsonResp.put("ok", true);
        jsonResp.put("authToken", authToken);
        resp.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        byte[] respBytes = simpleObjectMapper.writeValueAsBytes(jsonResp);
        resp.getOutputStream().write(respBytes);
    }

    private void nonce(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, Object> body = getBodyJson(req);
        // req.getSession().setAttribute("nonce", nonce); // requires session manager

        String address = normalizePublicAddress((String) body.get("address"));
        UUID nonce = authSvc.createSessionForEthAddress(address).nonce();
        System.out.println("Saving nonce: " + nonce);
        resp.setHeader("Content-Type", "text/plain");
        resp.getWriter().print(nonce);
    }

    private void setAccessControl(HttpServletResponse resp) {
        if (env.isProd()) {
            resp.setHeader("Access-Control-Allow-Origin", "https://play-stack.vercel.app");
            resp.setHeader("Access-Control-Request-Headers", "https://play-stack.vercel.app");
            resp.setHeader("Access-Control-Allow-Credentials", "true");
        } else {
            resp.setHeader("Access-Control-Allow-Origin", "*");
            resp.setHeader("Access-Control-Request-Headers", "*");
        }
        resp.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    private void me(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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

        HashMap<String, String> jsonResp = new HashMap<>();
        jsonResp.put("address",
                optChain(authToken, at -> authSvc.getSessionFromAuthToken(UUID.fromString(at)).address()));
        resp.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        byte[] respBytes = simpleObjectMapper.writeValueAsBytes(jsonResp);
        resp.getOutputStream().write(respBytes);
    }

    private void logout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    }

    /**
     * Normalizes an Ethereum address by lowercasing it and removing the leading 0x
     */
    private String normalizePublicAddress(String address) {
        if (address == null) {
            return null;
        }
        if (address.startsWith("0x")) {
            address = address.substring(2);
        }
        return address.toLowerCase();
    }

    /**
     * This method is the reverse of the EIP-712 Ethereum message signing process.
     * 
     * @param signedMessageInHex
     *                           The signature in hex format. It is 65 bytes long,
     *                           32 bytes for r, 32 bytes for s, and 1 byte for v.
     *                           May or may not be pre-pended with "0x".
     * @param originalMessage
     *                           The original message that was signed. Not hashed.
     * @return
     *         The address that was used to sign the message. Or null if the
     *         signature is invalid.
     */
    private static String getAddressUsedToSignHashedMessage(String signedMessageInHex, String originalMessage) {
        if (signedMessageInHex.startsWith("0x")) {
            signedMessageInHex = signedMessageInHex.substring(2);
        }

        // No need to prepend these strings with 0x because
        // Numeric.hexStringToByteArray() accepts both formats
        String r = signedMessageInHex.substring(0, 64);
        String s = signedMessageInHex.substring(64, 128);
        String v = signedMessageInHex.substring(128, 130);

        // Using Sign.signedPrefixedMessageToKey for EIP-712 compliant signatures.
        String pubkey;
        try {
            pubkey = Sign.signedPrefixedMessageToKey(originalMessage.getBytes(),
                    new Sign.SignatureData(
                            Numeric.hexStringToByteArray(v)[0],
                            Numeric.hexStringToByteArray(r),
                            Numeric.hexStringToByteArray(s)))
                    .toString(16);
        } catch (SignatureException e) {
            return null;
        }

        return Keys.getAddress(pubkey);
    }
}