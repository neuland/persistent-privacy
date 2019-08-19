package de.neuland.persistentprivacy.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import de.neuland.persistentprivacy.crypto.CryptoService;
import org.apache.commons.codec.binary.Hex;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class PseudonymSerializer extends JsonSerializer<Object> {
    private final CryptoService cryptoService;

    public PseudonymSerializer(CryptoService cryptoService) {
        this.cryptoService = cryptoService;}

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        String pseudonymized = cryptoService.pseudonymizeAsHex(((String) value).getBytes(StandardCharsets.UTF_8));
        serializers.defaultSerializeValue(pseudonymized, gen);

    }
}
