package djma.db;

import org.jooq.codegen.GenerationTool;

import com.zaxxer.hikari.HikariDataSource;

/**
 * Generates the JOOQ code from database tables.
 */
public class JooqCodegen {
    public static void main(String[] args) {
        try (HikariDataSource source = new HikariDataSource()) {
            String config = new String(JooqCodegen.class.getResourceAsStream("jooq-config.xml").readAllBytes());
            GenerationTool.generate(config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}