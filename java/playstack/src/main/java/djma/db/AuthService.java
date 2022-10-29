package djma.db;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A simple session manager service.
 */
public class AuthService {

    private static AuthService INSTANCE;

    // DB db;

    // TODO: Save to db
    // TODO: Token expiry
    public record Session(UUID nonce, UUID authToken, String address, Instant creationTime) {
    }

    // private final Map<UUID, String> authTokenToAddress = new HashMap<>();
    // private final Map<UUID, Instant> nonceCreationTime = new HashMap<>();
    // private final Map<UUID, UUID> nonceToAuthToken = new HashMap<>();
    private final Map<UUID, Session> nonceToSession = new HashMap<>();
    private final Map<UUID, Session> authTokenToSession = new HashMap<>();

    private AuthService() {
        // this.db = DB.get();
    }

    public static AuthService get() {
        if (INSTANCE == null) {
            INSTANCE = new AuthService();
        }
        return INSTANCE;
    }

    public Session getSessionFromNonce(UUID nonce) {
        return nonceToSession.get(nonce);
    }

    public Session getSessionFromAuthToken(UUID authToken) {
        return authTokenToSession.get(authToken);
    }

    public Session createSessionForEthAddress(String address) {
        UUID nonce = UUID.randomUUID();
        UUID authToken = UUID.randomUUID();
        Session newSession = new Session(nonce, authToken, address, Instant.now());
        nonceToSession.put(nonce, newSession);
        authTokenToSession.put(authToken, newSession);
        return newSession;
    }

    public boolean isAuthorized(UUID authToken) {
        return authTokenToSession.containsKey(authToken);
    }

}
