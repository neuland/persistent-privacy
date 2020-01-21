package de.neuland.persistentprivacy.crypto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Base64;
import java.util.Optional;

@SuppressWarnings("WeakerAccess")
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

    @JsonCreator
    private CryptedData(@JsonProperty("data") String data, @JsonProperty("iv") String iv, @JsonProperty("keyRef") String keyRef) {
        this.data = data;
        this.iv = iv;
        this.keyRef = keyRef;
    }

    public static CryptedData create(byte[] cipherText, byte[] iv, String keyRef) {
        return new CryptedData(
                Base64.getEncoder().encodeToString(cipherText),
                Base64.getEncoder().encodeToString(iv),
                keyRef);
    }

    public static CryptedData inlineIv(byte[] cipherText, String keyRef) {
        return new CryptedData(
                Base64.getEncoder().encodeToString(cipherText),
                null,
                keyRef);
    }

    public byte[] data() {
        return Base64.getDecoder().decode(data);
    }

    public byte[] iv() {
        return iv != null ? Base64.getDecoder().decode(iv) : new byte[0];
    }

    public String keyRef() {
        return keyRef;
    }

    public String serializeAsString(String prefix) {
        if (prefix.contains(":")) {
            throw new IllegalArgumentException("Prefix must not contain colons!");
        }
        return String.format("%s:%s:%s:%s", prefix, keyRef, iv, data);
    }

    public static CryptedData deserializeFromString(String str) {
        return tryDeserializeFromString(str).orElse(null);
    }

    public static Optional<CryptedData> tryDeserializeFromString(String str) {
        if (str == null) {
            return Optional.empty();
        }
        String[] split = str.split(":");
        if (split.length != 4) {
            return Optional.empty();
        }
        return Optional.of(new CryptedData(split[3], split[2], split[1]));
    }

}
