package de.neuland.persistentprivacy.crypto;

import com.google.crypto.tink.KeysetHandle;

public interface TinkKeysetRepository {
    KeysetHandle defaultKeyset();

    KeysetHandle forKeyId(String keyId);
}
