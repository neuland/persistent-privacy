package de.neuland.persistentprivacy.crypto;

import java.security.Key;

public interface KeyRepository {

    Key keyForName(String keyRef);
    KeyData defaultAesKey();
}
