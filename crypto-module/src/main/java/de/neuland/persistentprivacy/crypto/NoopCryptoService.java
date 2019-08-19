package de.neuland.persistentprivacy.crypto;

import org.bouncycastle.jcajce.provider.digest.SHA3;

/**
 *
 *
 */
public class NoopCryptoService implements CryptoService {

    @Override
    public byte[] decrypt(CryptedData cryptedData) {
        return cryptedData.data();
    }

    @Override
    public CryptedData encrypt(byte[] data) {
        return CryptedData.create(data, new byte[0], "");
    }

    @Override
    public byte[] pseudonymize(byte[] data) {
        return new SHA3.Digest256().digest(data);
    }
}
