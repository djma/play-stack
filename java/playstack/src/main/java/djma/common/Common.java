package djma.common;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jooq.impl.TableRecordImpl;

import static com.google.common.base.Preconditions.checkNotNull;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Temporary home for common functions.
 */
public class Common {
    public static ObjectMapper simpleObjectMapper = new ObjectMapper()
            /**
             * Silently throws away extra properties that aren't specified in the schema.
             */
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            /**
             * Tells Jackson to serialize Instant as ISO-8601 format
             */
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            /**
             * Allows Jackson to serialize time objects like Instant
             */
            .registerModule(new JavaTimeModule())
            /**
             * Allows us not to specify @JsonProperty on every parameter. Requires the
             * binary to be built with `javac
             * -parameters`.
             * 
             * However, single parameter constructors still need an annotation.
             * https://github.com/FasterXML/jackson-modules-java8/tree/master/parameter-names#usage-example
             * https://github.com/FasterXML/jackson-databind/issues/1498
             */
            .registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));

    /**
     * Json-ifies a jooq table record.
     */
    public static <R extends TableRecordImpl<R>> String recordToJson(R record) {
        try {
            return simpleObjectMapper.writeValueAsString(record.intoMap());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Converts a json into a jooq table record.
     */
    public static <R extends TableRecordImpl<R>> R jsonToRecord(String json, Class<R> recordClass) {
        try {
            return simpleObjectMapper.readValue(json, recordClass);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T ifNull(T val, T defaultVal) {
        return val == null ? defaultVal : val;
    }

    public static <T> List<T> ifNull(List<T> val, List<T> defaultVal) {
        return val == null ? defaultVal : val;
    }

    /**
     * Makes optional chaining in Java a little more readable.
     */
    public static <A, B> B optChain(A nullable, B defaultVal, Function<A, B> func) {
        checkNotNull(func, "func");
        if (nullable == null) {
            return defaultVal;
        }
        return func.apply(nullable);
    }

    /**
     * Makes optional chaining in Java a little more readable.
     */
    public static <A, B> B optChain(A nullable, Function<A, B> func) {
        return optChain(nullable, null, func);
    }

    public static String getBody(HttpServletRequest req) throws IOException {
        return req.getReader().lines().collect(Collectors.joining());
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getBodyJson(HttpServletRequest req) throws IOException {
        return simpleObjectMapper.readValue(getBody(req), Map.class);
    }

}
