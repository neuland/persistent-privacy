package de.neuland.persistentprivacy.crypto;

import org.apache.commons.codec.binary.Hex;

public interface CryptoService {
    byte[] decrypt(CryptedData cryptedData);
    CryptedData encrypt(byte[] data);
    byte[] pseudonymize(byte[] data);

    default String pseudonymizeAsHex(byte[] data) {
        return Hex.encodeHexString(pseudonymize(data));
    }

}
