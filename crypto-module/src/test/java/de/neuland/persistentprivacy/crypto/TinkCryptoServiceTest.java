package de.neuland.persistentprivacy.crypto;

import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AeadKeyTemplates;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.GeneralSecurityException;

import static org.assertj.core.api.Assertions.assertThat;

class TinkCryptoServiceTest {

    @BeforeAll
    static void beforeAll() throws GeneralSecurityException {
        AeadConfig.register();

    }

    @Test
    void name() throws Exception {
        KeysetHandle keysetHandle = KeysetHandle.generateNew(AeadKeyTemplates.AES128_GCM);
        System.out.println(keysetHandle);


    }
}
