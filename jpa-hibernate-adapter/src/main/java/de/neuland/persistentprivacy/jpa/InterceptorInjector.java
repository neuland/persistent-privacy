package de.neuland.persistentprivacy.jpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class InterceptorInjector implements HibernatePropertiesCustomizer {

    @Autowired
    private CryptoInterceptor cryptoInterceptor;

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put("hibernate.ejb.interceptor", cryptoInterceptor);
    }
}
