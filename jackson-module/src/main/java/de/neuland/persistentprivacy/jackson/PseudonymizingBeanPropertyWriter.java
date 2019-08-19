package de.neuland.persistentprivacy.jackson;

import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import de.neuland.persistentprivacy.crypto.CryptoService;

public class PseudonymizingBeanPropertyWriter extends BeanPropertyWriter {

//    public static final String PREFIX = "$pseudonymized_";

    private final BeanPropertyWriter delegate;
    private final CryptoService cryptoService;

    public PseudonymizingBeanPropertyWriter(BeanPropertyWriter delegate, CryptoService cryptoService) {
        super(delegate);
//        super(delegate, new SerializedString(PREFIX + delegate.getName() ));
        this.delegate = delegate;
        this.cryptoService = cryptoService;
        assignSerializer(new PseudonymSerializer(cryptoService));
    }

    @Override
    public boolean isVirtual() {
        return true;
    }

}
