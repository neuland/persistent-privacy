package de.neuland.persistentprivacy.crypto;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;
import lombok.SneakyThrows;
import org.bouncycastle.jcajce.provider.digest.SHA3;

/**
 * Sample Crypto service based on google tink.
 */
public class TinkCryptoService implements CryptoService {

    private final TinkKeysetRepository keyRepository;

    @SneakyThrows
    public TinkCryptoService(TinkKeysetRepository keyRepository) {
        this.keyRepository = keyRepository;
    }

    @SneakyThrows
    @Override
    public byte[] decrypt(CryptedData cryptedData) {
        KeysetHandle keysetHandle = keyRepository.forKeyId(cryptedData.getKeyRef());
        Aead aead = keysetHandle.getPrimitive(Aead.class);
        return aead.decrypt(cryptedData.data(), null);
    }

    @SneakyThrows
    @Override
    public CryptedData encrypt(byte[] data) {
        KeysetHandle keysetHandle = keyRepository.defaultKeyset();
        Aead aead = keysetHandle.getPrimitive(Aead.class);
        byte[] ciphertext = aead.encrypt(data, null);
        String keyRef = Integer.toString(keysetHandle.getKeysetInfo().getPrimaryKeyId());
        return CryptedData.inlineIv(ciphertext, keyRef);
    }

    @Override
    public byte[] pseudonymize(byte[] data) {
        return new SHA3.Digest256().digest(data);
    }
}
