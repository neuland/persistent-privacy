package de.neuland.persistentprivacy.crypto;

import lombok.AllArgsConstructor;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

@AllArgsConstructor
public final class KeyData {
    final String name;
    final Key key;

    public static KeyData of(String name, String base64encodedKey) {
        byte[] decodedKey = Base64.getDecoder().decode(base64encodedKey);
        return new KeyData(name, new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES"));
    }

}
