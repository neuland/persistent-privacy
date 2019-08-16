package de.neuland.persistentprivacy.crypto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Base64;

@SuppressWarnings("WeakerAccess")
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor_ = {@JsonCreator} )
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class CryptedData {
    @JsonProperty
    private String data;
    @JsonProperty
    private String iv;
    @JsonProperty
    private String keyRef;

    public static CryptedData create(byte[] cipherText, byte[] iv, String keyRef) {
        return new CryptedData(
                Base64.getEncoder().encodeToString(cipherText),
                Base64.getEncoder().encodeToString(iv),
                keyRef);
    }

    public byte[] data() {
        return Base64.getDecoder().decode(data);
    }

    public byte[] iv() {
        return Base64.getDecoder().decode(iv);
    }

    public String keyRef() {
        return keyRef;
    }

    public String serializeAsString() {
        return serializeAsString("$crypt");
    }
    public String serializeAsString(String prefix) {
        if (prefix.contains(":")) {
            throw new IllegalArgumentException("Prefix must not contain colons!");
        }
        return String.format("%s:%s:%s:%s", prefix, keyRef, iv, data);
    }

    public static CryptedData deserializeFromString(String str) {
        String[] split = str.split(":");
        return new CryptedData(split[3], split[2], split[1]);
    }

}
