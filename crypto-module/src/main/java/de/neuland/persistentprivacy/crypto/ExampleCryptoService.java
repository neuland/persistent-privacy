package de.neuland.persistentprivacy.crypto;

import lombok.SneakyThrows;
import org.bouncycastle.jcajce.provider.digest.SHA3;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import java.security.SecureRandom;

/**
 * This is an EXAMPLE crypto service intended for demo purposes and NOT READY FOR PRODUCTION!
 */
public class ExampleCryptoService implements CryptoService {

    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int GCM_NONCE_LENGTH_BYTES = 12;

    private final KeyRepository keyRepository;

    private SecureRandom random;

    @SneakyThrows
    public ExampleCryptoService(KeyRepository keyRepository) {
        this.keyRepository = keyRepository;
        random = SecureRandom.getInstance("NativePRNGNonBlocking");
    }

    @SneakyThrows
    @Override
    public byte[] decrypt(CryptedData cryptedData) {
        Cipher cipher = cipher();
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, cryptedData.iv());
        cipher.init(Cipher.DECRYPT_MODE, keyRepository.keyForName(cryptedData.keyRef()), spec);
        return cipher.doFinal(cryptedData.data());
    }

    @SneakyThrows
    @Override
    public CryptedData encrypt(byte[] input) {
        Cipher cipher = cipher();

        byte[] iv = new byte[GCM_NONCE_LENGTH_BYTES];
        random.nextBytes(iv);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
        KeyData keyData = keyRepository.defaultAesKey();
        cipher.init(Cipher.ENCRYPT_MODE, keyData.key, spec);

        byte[] cipherText = cipher.doFinal(input);

        return CryptedData.create(cipherText, iv, keyData.name);
    }

    @SneakyThrows
    private Cipher cipher() {
        return Cipher.getInstance("AES/GCM/NoPadding", "SunJCE");
    }

    @Override
    public byte[] pseudonymize(byte[] data) {
        return new SHA3.Digest256().digest(data);
    }


}
