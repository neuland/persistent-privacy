package de.neuland.persistentprivacy.crypto;

import de.neuland.persistentprivacy.crypto.CryptedData;
import de.neuland.persistentprivacy.crypto.CryptoService;

public class NoopCryptoService implements CryptoService {

    @Override
    public byte[] decrypt(CryptedData cryptedData) {
        return cryptedData.data();
    }

    @Override
    public CryptedData encrypt(byte[] data) {
        return CryptedData.create(data, new byte[0], "");
    }
}
