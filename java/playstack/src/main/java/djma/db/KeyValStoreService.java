package djma.db;

import static djma.db.jooq.tables.Kvstore.KVSTORE;

/**
 * A simple persistent key-value store service backed by postgres.
 */
public class KeyValStoreService {
    private static KeyValStoreService INSTANCE;

    DB db;

    private KeyValStoreService() {
        this.db = DB.get();
    }

    public static KeyValStoreService get() {
        if (INSTANCE == null) {
            INSTANCE = new KeyValStoreService();
        }
        return INSTANCE;
    }

    public String get(String key) {
        return this.db.run(ctx -> {
            return ctx.selectFrom(KVSTORE)
                    .where(KVSTORE.KEY.eq(key))
                    .fetchOptional()
                    .map(r -> r.getValue())
                    .orElse(null);
        });
    }

    public void set(String key, String value) {
        this.db.run(ctx -> {
            return ctx.insertInto(KVSTORE, KVSTORE.KEY, KVSTORE.VALUE)
                    .values(key, value)
                    .onDuplicateKeyUpdate()
                    .set(KVSTORE.VALUE, value)
                    .execute();
        });
    }
}
