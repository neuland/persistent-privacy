package de.neuland.persistentprivacy.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.neuland.persistentprivacy.annotations.PersonalData;
import de.neuland.persistentprivacy.crypto.CryptedData;
import de.neuland.persistentprivacy.crypto.CryptoService;

import java.io.IOException;

public class EncryptedBeanDeserializer extends Deserializers.Base {
    private static final String DO_DECRYPTION = "decryption";
    private final CryptoService cryptoService;

    EncryptedBeanDeserializer(CryptoService cryptoService) {this.cryptoService = cryptoService;}

    @Override
    public JsonDeserializer<?> findBeanDeserializer(JavaType type, DeserializationConfig config, BeanDescription beanDesc) {

        if (shouldDecrypt(config) && containsPersonalData(beanDesc)) {
            return new StdDeserializer<>(type) {
                @Override
                public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                    ObjectMapper codec = ((ObjectMapper) p.getCodec()).copy();
                    codec.setConfig(disableDecryptionForNestedProperties(codec));

                    ObjectNode treeNode = p.readValueAsTree();
                    TreeNode encrypted = treeNode.get(PersonalDataEncryptionModule.ENCRYPTED_FIELD_NAME);

                    CryptedData cryptedData = codec.treeToValue(encrypted, CryptedData.class);

                    ObjectNode decrypted = (ObjectNode) codec.readTree(cryptoService.decrypt(cryptedData));

                    treeNode.remove(PersonalDataEncryptionModule.ENCRYPTED_FIELD_NAME);
                    treeNode.setAll(decrypted);

                    return codec.treeToValue(treeNode, type.getRawClass());
                }
            };
        }
        return null;
    }


    private boolean shouldDecrypt(DeserializationConfig config) {
        Object attr = config.getAttributes().getAttribute(DO_DECRYPTION);
        return attr == null || (boolean) attr;
    }

    private DeserializationConfig disableDecryptionForNestedProperties(ObjectMapper codec) {
        return codec.getDeserializationConfig().withAttribute(DO_DECRYPTION, false);
    }

    private boolean containsPersonalData(BeanDescription beanDesc) {
        return beanDesc
                .findProperties()
                .stream()
                .anyMatch(this::containsPersonalData);
    }

    private boolean containsPersonalData(BeanPropertyDefinition b) {
        return b.getAccessor().hasAnnotation(PersonalData.class)
                || (b.hasField() && b.getField().hasAnnotation(PersonalData.class))
                || (b.hasGetter() && b.getGetter().hasAnnotation(PersonalData.class));
    }


}
