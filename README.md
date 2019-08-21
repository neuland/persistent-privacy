# persistent-privacy

The GDPR encourages us to build our applications _secure by default_:

> Companies/organisations are encouraged to implement technical and
> organisational measures, at the earliest stages of the design of the
> processing operations, in such a way that  safeguards privacy and data
> protection principles right from the start (‘data protection by design’)
> [...]

One measure to protect personal data is to encrypt it at rest. Of course 
this can be done at the file system level, e.g. when using a crpypt file system
for database volumes. But this won't protect the data if an attacker gains access 
to the mounted file system or for example to the database itself, e.g. via 
SQL injection or stolen credentials.

So to add a level of security you have apply data encryption at rest on the 
application level.

This project tries to make application level encryption easier by providing 
annotations to simply mark entity attributes as personal data.

Lets have a look at how to do this for spring-data-mongodb: The follwing example `Customer` class inspired by the 
[Spring Data mongo db getting started guide](https://spring.io/guides/gs/accessing-data-mongodb/):

```java
public class Customer {
    @Id
    public String id;
    public String email;
    @PersonalData
    public String firstName;
    @PersonalData
    public String lastName;

    public Customer() {}

    Customer(String email, String firstName, String lastName) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
```

The first and the last name are marked as `@PersonalData` thus are not stored in plain text
(we will lok at the e-mail address later). The resulting BSON looks like this:

```bson
{
  "_id": ObjectId("5d5cfa3c184a3d77e6a3121c"),
  "email": "max@mustermann.de",
  "_class": "de.neuland.persistentprivacy.mongodb.example.Customer",
  "_personal_data": {
    "data": "7lKNVT5FoBO9mb4HOP5SuZ+v7QkzxZ4KG+3SqtGn4fMtVHAJLOt+EgAu01CztV3ufxhljDGI0X0LQUo=",
    "iv": "gzY1cZvXJoLd7nRv",
    "keyRef": "key"
  }
}
```

The actual encryption is done by registering a 
[PrivacyProtectionListener](mongo-adapter/src/main/java/de/neuland/persistentprivacy/mongodb/PrivacyProtectionListener.java):
in your configuration: 

```java

@Configuration
public class ExampleConfiguration {

    private KeyRepository keyRepository = new KeyRepository() {
        byte[] testKeyData = Base64.getDecoder().decode("SXrCuhJwJSbLif8+Ol6wwe5gqHYnM7H6DX2PmBMg8WM=");
        private Key key = new SecretKeySpec(testKeyData, "AES");

        @Override
        public Key keyForName(String keyRef) {
            return key;
        }

        @Override
        public KeyData defaultAesKey() {
            return new KeyData("key",key);
        }
    };

    @Bean
    public ApplicationListener<MongoMappingEvent<?>> privacyProtectionListener() {
        CryptoService cryptoService = new DefaultCryptoService(keyRepository);
        ObjectMapper personalDataObjectMapper = new ObjectMapper();
        return new PrivacyProtectionListener(cryptoService, personalDataObjectMapper);
    }

}
```
