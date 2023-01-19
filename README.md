# Demo

## Guide

Generate RSA public and private keys

```shell
openssl genpkey -algorithm RSA -outform PEM -out private_key.pem -pkeyopt rsa_keygen_bits:2048
openssl rsa -in private_key.pem -out public_key.pem -pubout -outform PEM
```

Add libraries to the local maven

```shell
mvn install:install-file -Dfile=confluent-encryption-common-2.0.8-cp-7.1/confluent-encryption-common-2.0.8-cp-7.1.jar \
-DgroupId=io.confluent.confluent-encryption -DartifactId=confluent-encryption-common -Dversion=2.0.8-cp-7.1 -Dpackaging=jar

mvn install:install-file -Dfile=confluent-encryption-kafka-2.0.8-cp-7.1/confluent-encryption-kafka-2.0.8-cp-7.1.jar \
-DgroupId=io.confluent.confluent-encryption -DartifactId=confluent-encryption-kafka -Dversion=2.0.8-cp-7.1 -Dpackaging=jar

mvn install:install-file -Dfile=confluent-encryption-serializer-2.0.8-cp-7.1/confluent-encryption-serializer-2.0.8-cp-7.1.jar \
-DgroupId=io.confluent.confluent-encryption -DartifactId=confluent-encryption-serializer -Dversion=2.0.8-cp-7.1 -Dpackaging=jar

mvn install:install-file -Dfile=confluent-encryption-kstreams-2.0.8-cp-7.1/confluent-encryption-kstreams-2.0.8-cp-7.1.jar \
-DgroupId=io.confluent.confluent-encryption -DartifactId=confluent-encryption-kstreams -Dversion=2.0.8-cp-7.1 -Dpackaging=jar
```

Create POJO classes for Avro

```shell
mvn generate-sources
```

https://github.com/confluentinc/confluent-encryption/blob/master/docs_src/kafka_clients.md#field-level-encryption-overview

```shell
export CONFLUENT_HOME="/Users/kirill.kulikov/confluent/confluent-7.3.0"
export PATH="$PATH:/Users/kirill.kulikov/confluent/confluent-7.3.0/bin"
export JAVA_HOME="/usr/local/opt/openjdk@11"

confluent local destroy
confluent local services connect start

kafka-topics --bootstrap-server localhost:9092 --list

echo "{ \"schema\": \"$(cat field-classifications.json | sed 's/\"/\\\"/g')\" }" > field-classifications.schema
curl localhost:8081/subjects/field-classifications -XDELETE # delete the subject if incompatible
curl localhost:8081/subjects/field-classifications/versions -XPOST -H "Content-Type: application/json" -d @field-classifications.schema

echo "{ \"schema\": \"$(cat field-metadata.json | sed 's/\"/\\\"/g')\" }" > field-metadata.schema
curl localhost:8081/subjects/field-metadata -XDELETE # delete the subject if incompatible
curl localhost:8081/subjects/field-metadata/versions -XPOST -H "Content-Type: application/json" -d @field-metadata.schema
```

```shell
curl -X POST \
     -H "Content-Type: application/json" \
     --data '
     {
       "name": "mongo-sink",
       "config": {
         "connector.class":"com.mongodb.kafka.connect.MongoSinkConnector",
         "connection.uri":"mongodb://root:example@localhost:27017",
         "database":"quickstart",
         "collection":"customers",
         "topics":"avro-customers",
         "value.converter": "io.confluent.encryption.connect.SecuredAvroConverter",
         "value.converter.schema.registry.url": "http://localhost:8081", 
         "value.converter.encryption.metadata.name": "field-metadata",
         "value.converter.encryption.metadata.policy.class": "CatalogPolicy",
         "value.converter.encryption.classifications.name": "field-classifications",
         "value.converter.encryption.provider.name": "cached",
         "value.converter.cached.provider.class": "CacheCipherProvider",
         "value.converter.cached.provider.name": "generator",
         "value.converter.generator.provider.class": "GeneratorCipherProvider",
         "value.converter.generator.provider.name": "local",
         "value.converter.local.provider.class": "LocalCipherProvider",
         "value.converter.local.provider.keys": "RSAWrappingKey",
         "value.converter.local.provider.RSAWrappingKey.key.type": "KeyPair",
         "value.converter.local.provider.RSAWrappingKey.key.private": "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDnaSw7eWcppqCW0dMJ9Q1aC+aUvg7K0xTYfEStyMOym9a+EKjcr/6hU0LkIUksmSmLGBgYVAey2OPXFmO1B3Qbvxla9EPtdQ/qilJluB7HH2z10oeBuS7tkdo+zA/u4AHDOCfa9+yk47IWNLPBjkVs4xMo3cL6rVyM5hu5hVq3lRVYbVV2AT7oYdM3C6nEw5kdE01uSgjkcpd7Rg58ZiLNYdr91rwEblxxoF40tdHBs5oQ4DWMyx1hZ1npyekEsVYsq/ACsEaIMGQXNZsLZ7XXmAV14sZXvSVsHiUGkv/7LKgsUEZ0lUH9PRqs4jK+6nSQOxiLqM6yCRAzr5MSiOlXAgMBAAECggEAKC3fqzfqDJZM3cLyxJDSz6avU3YodVjvDqOM/SuVpwZd48RavW6lZHdjbt7EqMSzLN5zGI5Gg+waqzbM+xqfM04b5enxfWJM8CkyI00zstkm/wud0Y1Is6EWZr7hqVUlmTK/4MoZQYvzWN2vtFSygzRuGDNg7kt5fVFa0PxxgtxAPwOXFWDMT7bU7csjrsOtxm7bgwm+rxSCSPU9J834TTzQlPID8tP9Ajvj4TrllZN55oKdDNO5aVaMkYRKMBbg/mzHpQE9wWeKX1iGYR0x8N3ctKJvAC6SYcnOiAKKPg0C5Qi6RFg88GdaYq1tUtDbjcP5CUWMZ2X9M5XEqO2WAQKBgQD3iRy7Fm91EFy2duJxNULe0qU5hbRekCmEX7/5HNk+BjWDFLLVFkgtWop7+p+G/TwmmPdpEtRYCbT1k2qHrPgohReo2VzF+K7Q3yppnFu2dPy5T2AL02J9J4BCzxB1uwzpnGd2PqwUX79WsF2ol18F04sMeXUzuKggOm7M2nl8VwKBgQDvUugqw3Yfl9TF5VkVyA26/FOfMYQZ2ovzpTg9Qq7il1F37h8Lg83zvI63NXUlY2khweo0FyMbtZpG0a9dpQBE0f4D2n9JsmpuGbgjYYNNEWUQvuI3bqdQ/ecPpMGvYXWvtnTPG0jr6GbSobL/RXwgOp4H8tOpFNEejaARjDfbAQKBgQCmpYdTKMqGnCpeqPDP2FSZoGSdsjb5BsL8nF2ov1Q93n4+LjwrGuIirnbW+qZVgbzyGz9NXODaGEbcoY8xojA7T0bbZOKBYWeHtQZfrWVNE7tkolx9+aSvr105HR/usqwBxksdHxpIaSFuojObobTWPlG5ZzeRR3rgn5Yikd8B5wKBgQC2+GLJ9wBDbTheSFdQoM9miu1/w0Kk5YKkN0gFBgtg76F4mJQhoJZ/50QRbAxxFkzVY0UkqB/OWoxl4oA5jyHie83BsnYoqQBXxtASNMZG0Kq9H8Mh8DZ5ZHUYb7Uo2dE5ErzBbrHUsqySEtAf/EbG+SnDF/KczW6H9m1PnS1DAQKBgCANQP+nXYE2/81+wmlPqOZqpXdZaTc8tNB2vHfpamw/OCZqoRmZB9iUPOVfeTH6JmlL3K1BRaDnYJci/rWP/QjGLcterlihnZvOzVOfBgsS8MXw1jUjThNI6YAzhEOKBantke/ngwC+3X0s7GoEUIP+D3zdGAk34RhvEryeF+dY",
         "value.converter.local.provider.RSAWrappingKey.key": "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA52ksO3lnKaagltHTCfUNWgvmlL4OytMU2HxErcjDspvWvhCo3K/+oVNC5CFJLJkpixgYGFQHstjj1xZjtQd0G78ZWvRD7XUP6opSZbgexx9s9dKHgbku7ZHaPswP7uABwzgn2vfspOOyFjSzwY5FbOMTKN3C+q1cjOYbuYVat5UVWG1VdgE+6GHTNwupxMOZHRNNbkoI5HKXe0YOfGYizWHa/da8BG5ccaBeNLXRwbOaEOA1jMsdYWdZ6cnpBLFWLKvwArBGiDBkFzWbC2e115gFdeLGV70lbB4lBpL/+yyoLFBGdJVB/T0arOIyvup0kDsYi6jOsgkQM6+TEojpVwIDAQAB"
         }
     }
     ' \
     http://localhost:8083/connectors -w "\n"
```