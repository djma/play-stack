package djma.common;

import java.util.HashMap;
import java.util.Map;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvBuilder;

/**
 * Lightly handles local and remote environment variables. Local environment
 * variables are loaded from the actual environment, and then overwritten with a
 * .env file in the root of the project.
 */
public class Env {
    private static Env INSTANCE = null;

    private Map<String, String> env = new HashMap<>();

    private Env() {
    }

    public static Env get() {
        if (INSTANCE == null) {
            INSTANCE = new Env();

            INSTANCE.env.putAll(System.getenv());

            DotenvBuilder configure = Dotenv.configure();
            configure.ignoreIfMissing().load().entries().forEach((e) -> {
                System.out.println("Env: " + e.getKey() + " = " + e.getValue());
                INSTANCE.env.put(e.getKey(), e.getValue());
            });

            INSTANCE.env.put("isProd", INSTANCE.env.containsKey("DYNO") ? "true" : "false");
        }
        System.out.println("isProd " + INSTANCE.env.get("isProd"));
        return INSTANCE;
    }

    public String get(String key) {
        return env.get(key);
    }

    public int getInt(String key) {
        return Integer.parseInt(env.get(key));
    }

    public int getInt(String key, int defaultValue) {
        return env.containsKey(key) ? Integer.parseInt(env.get(key)) : defaultValue;
    }

    public boolean isProd() {
        return env.get("isProd").equals("true");
    }
}
