package de.neuland.persistentprivacy.crypto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Base64;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
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

}
