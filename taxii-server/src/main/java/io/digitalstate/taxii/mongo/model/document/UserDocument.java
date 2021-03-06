package io.digitalstate.taxii.mongo.model.document;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.digitalstate.taxii.common.TaxiiParsers;
import io.digitalstate.taxii.mongo.annotation.Indexed;
import io.digitalstate.taxii.mongo.model.TaxiiMongoModel;
import org.immutables.serial.Serial;
import org.immutables.value.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import java.io.IOException;

@Value.Immutable @Serial.Version(1L)
@Value.Style(passAnnotations = {Document.class, CompoundIndexes.class})
@JsonSerialize(as=ImmutableUserDocument.class) @JsonDeserialize(builder = ImmutableUserDocument.Builder.class)
@Document(collection = "users")
@JsonTypeName("user")
@JsonPropertyOrder({"_id", "type", "tenant_id", "created_at", "modified_at", "username", "password_info", "tenant_memberships" })
@CompoundIndexes({
        @CompoundIndex(name = "username", def = "{ 'username': 1 }", unique = true)
})
public interface UserDocument extends TaxiiMongoModel {

    @Override
    @Value.Default
    @NotBlank
    default String type() {
        return "user";
    }

    @JsonProperty("tenant_id")
    @NotBlank
    String tenantId();

    @JsonProperty("username")
    @Indexed(unique = true)
    @NotBlank
    String username();

    @JsonProperty("password_info")
    Password passwordInfo();

    @WritingConverter
    public class MongoWriterConverter implements Converter<UserDocument, org.bson.Document> {
        public org.bson.Document convert(final UserDocument object) {
            org.bson.Document doc = org.bson.Document.parse(object.toMongoJson());
            return doc;
        }
    }

    @ReadingConverter
    public class MongoReaderConverter implements Converter<org.bson.Document, UserDocument> {
        public UserDocument convert(final org.bson.Document object) {
            try {
                return TaxiiParsers.getMongoMapper().readValue(object.toJson(), UserDocument.class);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
