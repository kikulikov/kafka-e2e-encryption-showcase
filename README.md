# Kafka e2e Encryption Showcase

The project provides sample configurations for the proprietary Confluent's library for sensitive data encryption.

## Features

- Plaintext producers/consumers/streams configurations.
- Full payload encryption samples using locally provided keys.
- Full payload encryption samples using Hashicorp Vault as KMS. 
- Field level encryption samples using locally provided keys.
- Kafka Connect configuration examples (source & sink).
- Various unit tests utilising specific serializers/deserializers.
- Integration tests utilising `testcontainers` for more complex scenarios.

## Prerequisites

### Java

The project is based on Java Development Kit (JDK) version 17. 
You will need to install JDK 17 (or later) on your development machine.

### Library

Confluent employees: 

- see https://go/e2e to download jar files and get the detailed documentation.
- see [Artifactory FAQ](https://confluentinc.atlassian.net/wiki/spaces/TOOLS/pages/2930704487/Java+Artifactory+Migration+FAQ) to configure the artifactory.

### Maven

The project is depended on proprietary encryption libraries.
It is recommended to place libraries in the artifactory so all parties could retrieve them easily.

Alternatively, for development purposes, you can add libraries to the local maven with the commands like below.
I've had some troubles with newer Maven releases. Maven version 3.2.5 worked well for me.
You can download it from https://dlcdn.apache.org/maven/maven-3/3.2.5/binaries/apache-maven-3.2.5-bin.zip.

```shell
mvn install:install-file -Dfile=../confluent-encryption-3.0.0-cp-7.3/confluent-encryption-common-3.0.0-cp-7.3/confluent-encryption-common-3.0.0-cp-7.3.jar \
-DgroupId=io.confluent.confluent-encryption -DartifactId=confluent-encryption-common -Dversion=3.0.0-cp-7.3 -Dpackaging=jar

mvn install:install-file -Dfile=../confluent-encryption-3.0.0-cp-7.3/confluent-encryption-kafka-3.0.0-cp-7.3/confluent-encryption-kafka-3.0.0-cp-7.3.jar \
-DgroupId=io.confluent.confluent-encryption -DartifactId=confluent-encryption-kafka -Dversion=3.0.0-cp-7.3 -Dpackaging=jar

mvn install:install-file -Dfile=../confluent-encryption-3.0.0-cp-7.3/confluent-encryption-serializer-3.0.0-cp-7.3/confluent-encryption-serializer-3.0.0-cp-7.3.jar \
-DgroupId=io.confluent.confluent-encryption -DartifactId=confluent-encryption-serializer -Dversion=3.0.0-cp-7.3 -Dpackaging=jar

mvn install:install-file -Dfile=../confluent-encryption-3.0.0-cp-7.3/confluent-encryption-kstreams-3.0.0-cp-7.3/confluent-encryption-kstreams-3.0.0-cp-7.3.jar \
-DgroupId=io.confluent.confluent-encryption -DartifactId=confluent-encryption-kstreams -Dversion=3.0.0-cp-7.3 -Dpackaging=jar

mvn install:install-file -Dfile=../confluent-encryption-3.0.0-cp-7.3/confluent-encryption-vault-3.0.0-cp-7.3/confluent-encryption-vault-3.0.0-cp-7.3.jar \
-DgroupId=io.confluent.confluent-encryption -DartifactId=confluent-encryption-vault -Dversion=3.0.0-cp-7.3 -Dpackaging=jar
```

The commands above will copy jar files to you local `.m2` folder. The 'm2' folder in Maven is the default location 
where Maven stores its local repository, which is a cache of all the dependencies downloaded by Maven for your project.

`mvn clean` is a command used in Maven build automation tool to remove any files and directories generated by previous builds.
When you execute the `mvn clean` command, it will delete the `target` directory of the project. 
This directory is where the compiled classes, generated resources, and other build artifacts are stored.

The project is using Apache Avro data serialization format. 
The code can be generated automatically with `mvn generate-sources`.

To run Maven tests, you can use the `mvn test` command in the project's root directory. 
This will compile the project's source code and execute all unit tests found in the `src/test/java` directory.

### Kafka

The project uses the Apache Kafka distributed streaming platform.
You can run Kafka locally or use remote Kafka (e.g. Confluent Cloud). 
Currently, there are 2 popular ways to run Kafka locally: `confluent cli` & `docker`. 

#### Confluent CLI

Please, see https://docs.confluent.io/confluent-cli/current/overview.html for details.

When installed, make sure that provided right `JAVA_HOME` and `CONFLUENT_HOME` environment configurations.
Adding confluent binaries to the `PATH` might be helpful too.

```shell
export JAVA_HOME="/usr/local/opt/openjdk@11"
export CONFLUENT_HOME="/Users/kirill.kulikov/confluent/confluent-7.3.1"
export PATH="$PATH:/Users/kirill.kulikov/confluent/confluent-7.3.1/bin"
```

Clean up your local setup with 

```shell
confluent local destroy
```

Start required services with

```shell
confluent local services kafka start
confluent local services schema-registry start
confluent local services connect start
```

#### Docker

You can run Apache Kafka in Docker.
There is a `docker-compose.yml` provided for you.
It will spin up a single Zookeeper instance, single Kafka instance & single Schema Registry instance.
Run it with `docker-compose up` or `docker-compose up -d` (for detached mode: run containers in the background). 

### OpenSSL

For keys generation make sure that `OpenSSL` cryptography toolkit is installed.
The `openssl` program is a command line tool for using the various cryptography functions of `openssl`'s crypto library from the shell.

## Getting Started

AES, which stands for Advanced Encryption Standard, is an encryption algorithm used to protect sensitive data. 
It is a symmetric-key block cipher, meaning that the same secret key is used for both encryption and decryption.
The algorithm operates by taking a block of plaintext and applying a series of mathematical operations to transform it into an encrypted block of ciphertext. 
The resulting ciphertext is then transmitted or stored, and can only be decrypted back to its original plaintext using the same secret key.

Generate AES key with

```shell
openssl rand -base64 32
```

RSA, which stands for Rivest-Shamir-Adleman, is an asymmetric cryptographic algorithm used to provide encryption and digital signatures.
Asymmetric cryptography involves the use of a public key and a private key, with the public key being used for encryption and the private key being used for decryption. 
RSA works by generating a pair of keys, where the public key can be freely distributed and used to encrypt data, while the private key is kept secret and used to decrypt the data.

Generate RSA public and private keys with

```shell
openssl genpkey -algorithm RSA -outform PEM -out private_key.pem -pkeyopt rsa_keygen_bits:2048
openssl rsa -in private_key.pem -out public_key.pem -pubout -outform PEM
```

## Run Spring Boot Application

You can pass a directory to `--spring.config.location`.
Spring will load only the file application.properties and profile specific file like `application-{profile name}.properties`.

Then you can start your applications as

```shell
# Producer
mvn spring-boot:run -Dmaven.test.skip=true -Dstart-class=io.confluent.producer.BasicProducerApplication \
-Dspring-boot.run.profiles=local-encrypted-payload

# Consumer
mvn spring-boot:run -Dmaven.test.skip=true -Dstart-class=io.confluent.consumer.BasicConsumerApplication \
-Dspring-boot.run.profiles=local-encrypted-payload

# Streams
mvn spring-boot:run -Dmaven.test.skip=true -Dstart-class=io.confluent.streams.CountApplication \
-Dspring-boot.run.profiles=local-encrypted-payload

# Web
mvn spring-boot:run -Dmaven.test.skip=true -Dstart-class=io.confluent.web.BasicWebApplication \
-Dspring-boot.run.profiles=local-encrypted-payload -Dspring.main.web-application-type=servlet
```

## Hashicorp Vault

You can run Hashicorp Vault locally using Docker with

```shell
docker-compose -f docker-hashicorp-vault/docker-compose.yml up
```

## MongoDB Source Connector

You can run MongoDB locally using Docker with

```shell
docker-compose -f docker-mongo-source/docker-compose.yml up
```

Submit the connector configuration:

```shell
curl -s -X POST -H "Content-Type: application/json" \
--data @docker-mongo-source/mongo-source-connector.json \
http://localhost:8083/connectors -w "\n"
```

## MongoDB Sink Connector

```shell
docker-compose -f docker-mongo-sink/docker-compose.yml up
```

Submit the connector configuration:

```shell
curl -s -X POST -H "Content-Type: application/json" \
--data @docker-mongo-sink/encrypted-payload-mongodb-sink.json \
http://localhost:8083/connectors -w "\n"
```

## Field-level encryption

To enable field-level encryption you can upload classifications and metadata configs to Schema Registry:  

```shell
echo "{ \"schema\": \"$(cat field-classifications.json | sed 's/\"/\\\"/g')\" }" > field-classifications.schema
curl localhost:8081/subjects/field-classifications -XDELETE # delete the subject if incompatible
curl localhost:8081/subjects/field-classifications/versions -XPOST -H "Content-Type: application/json" -d @field-classifications.schema

echo "{ \"schema\": \"$(cat field-metadata.json | sed 's/\"/\\\"/g')\" }" > field-metadata.schema
curl localhost:8081/subjects/field-metadata -XDELETE # delete the subject if incompatible
curl localhost:8081/subjects/field-metadata/versions -XPOST -H "Content-Type: application/json" -d @field-metadata.schema
```

Alternatively, you can just run `./field-encryption-configs/submit.sh` which will do all of the above for you.

## Contributing

Contributions to the project are welcome. 
To contribute, fork the repository, make your changes, and submit a pull request. 
Please ensure that your changes are well-documented and thoroughly tested.

## License

The project is licensed under the `MIT License`.
See the LICENSE file for more information.
