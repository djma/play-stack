package djma.io;

import static djma.common.Common.simpleObjectMapper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * A simplified representation of a
 * {@link com.google.api.services.people.v1.model.Person}
 */
@JsonInclude(Include.NON_NULL)
public record Contact(
        /**
         * The unique identifier for the contact.
         */
        String resourceName,
        String name,
        String email,
        String phone,
        boolean hasExtraEmail,
        boolean hasExtraPhone) {

    public static Contact fromJson(String json) {
        try {
            return simpleObjectMapper.readValue(json, Contact.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String toJson() {
        try {
            return simpleObjectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}