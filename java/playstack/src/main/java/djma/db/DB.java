package djma.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import com.zaxxer.hikari.HikariDataSource;

import djma.common.Env;

import static djma.db.generated.tables.Contact.CONTACT;

public class DB {
    private static DB INSTANCE = null;

    public HikariDataSource source;

    public static DB get() {
        if (INSTANCE == null) {
            INSTANCE = new DB();
        }
        return INSTANCE;
    }

    private DB() {
        Env env = Env.get();
        String dbUrl = env.get("DATABASE_URL");
        Matcher dbUrlMatcher = Pattern.compile("([^:]+)://([^:]+):([^@]+)@([^:]+):([0-9]+)/(.+)").matcher(dbUrl);
        if (!dbUrlMatcher.find()) {
            System.out.println(
                    "Invalid DATABASE_URL. Must be in the form: <dbtype>://<user>:<password>@<host>:<port>/<database>");
            return;
        }
        int i = 0;
        String dbType = dbUrlMatcher.group(++i) + "ql";
        if (!dbType.endsWith("ql")) {
            // https://stackoverflow.com/questions/34741443/hikaricp-postgresql-driver-claims-to-not-accept-jdbc-url
            dbType = dbType + "ql";
        }
        String username = dbUrlMatcher.group(++i);
        String password = dbUrlMatcher.group(++i);
        String dbHost = dbUrlMatcher.group(++i);
        String port = dbUrlMatcher.group(++i);
        String database = dbUrlMatcher.group(++i);

        this.source = new HikariDataSource();
        this.source.setDriverClassName(org.postgresql.Driver.class.getName());
        this.source.setJdbcUrl(String.format("jdbc:%s://%s:%s/%s", dbType, dbHost, port, database));
        this.source.setUsername(username);
        this.source.setPassword(password);
        this.source.setMaximumPoolSize(8);
        this.source.setAutoCommit(true);
    }

    public <T> T run(Function<DSLContext, T> f) {
        try (Connection conn = this.source.getConnection()) {
            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);
            return f.apply(create);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) throws SQLException {
        DB db = DB.get();

        Connection conn = db.source.getConnection();
        conn.createStatement().execute("SELECT * from contact");

        DSLContext context = DSL.using(conn, SQLDialect.POSTGRES);
        Result<Record> contacts = context.select().from("contact").fetch();
        System.out.printf("contacts:\n");
        contacts.forEach(c -> {
            String name = c.getValue(CONTACT.NAME);
            String phone = c.getValue(CONTACT.PHONE);
            String email = c.getValue(CONTACT.EMAIL);
            System.out.printf("  %s %s %s\n", name, phone, email);
        });
    }
}
