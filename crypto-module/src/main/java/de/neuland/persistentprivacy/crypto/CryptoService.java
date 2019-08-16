package de.neuland.persistentprivacy.crypto;

public interface CryptoService {
    byte[] decrypt(CryptedData cryptedData);
    CryptedData encrypt(byte[] data);
}
