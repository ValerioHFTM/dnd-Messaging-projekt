 # DnD Service (Service A)

 In diesem ReadMe wird beschrieben wie das Projekt in Ubuntu 22 aufgebaut wird.

 ## Vorbereitung
 **Stelle sicher, dass du die Richtige Software Installiert hast**
 
 öffne dazu das Terminal und prüfe die Verionen 

  ```
  java -version
  mvn -version
  docker --version
  ```

  ## Quarkus Projekt erstellen

  ```
  mvn io.quarkus.platform:quarkus-maven-plugin:3.2.0.Final:create \
    -DprojectGroupId=com.dnd \
    -DprojectArtifactId=dnd-character-service \
    -Dextensions="hibernate-orm-panache,jdbc-mariadb,resteasy-reactive,smallrye-reactive-messaging-kafka" \
    -DnoExamples

  ```
Dieser Befehl fügt  alle dependecies direkt hinzu

 - Hibernate ORM Panache (Erleichtert die Arbeit mit der DB)
 - MariaDB JDBC-Treiber (Verbindung zur Datenbank)
 - RESTEasy Reactive (Für die API)
 - Kafka SmallRye (Für Event-Kommunikation)

---
## Projekt aufbauen

- Entity für Character erstellen
- Repository für die Character erstellen

### Swagger extension hinzufügen

im projekt ordner terminal öffnen und ausführen

```
mvn quarkus:add-extension -Dextensions="smallrye-openapi,swagger-ui"
```

### Rest API erstellen

dazu fügen wir die Resource hinzu

das ganze kann dann ausgeführt werden mit
```
mvn quarkus:dev
```

und unter http://localhost:8080/q/swagger-ui sieht man den zwischenstand der API.


## Datenbank anbinden

wir erstellen dazu ein file "docker-compose.yml" im Projektverzeichnis.

```
version: '3.8'

services:
  mariadb:
    image: mariadb:10.5
    container_name: mariadb_dnd
    restart: always
    environment:
      MARIADB_ROOT_PASSWORD: root
      MARIADB_DATABASE: dnd_db
      MARIADB_USER: dnd_user
      MARIADB_PASSWORD: secret
    ports:
      - "3306:3306"
    volumes:
      - mariadb_data:/var/lib/mysql

volumes:
  mariadb_data:
```

Wir starten den Container mit dem befehl im Terminal
```
docker compose up -d
```
 - Erstellt eine MariaDB-Datenbank (dnd_db).
 - Nutzt den User dnd_user mit Passwort secret.
 - Läuft auf Port 3306 (Standard für MariaDB).
 - Speichert die Daten persistent im mariadb_data-Volume.

 wir prüfen den status 
 ```
 docker ps
 ```

 um die Verbindung zu testen kann man sich einloggen

 ```
 docker exec -it mariadb_dnd mysql -u dnd_user -p
 ```

ergänze die application.properties in Projekt mit 
```
# Datenbankverbindung
quarkus.datasource.db-kind=mariadb
quarkus.datasource.username=dnd_user
quarkus.datasource.password=secret
quarkus.datasource.jdbc.url=jdbc:mariadb://localhost:3306/dnd_db
quarkus.hibernate-orm.database.generation=update
```

nun starte Quarkus mit  

```
mvn quarkus:dev
```

## Datenbank testen

Vorher sicherstellen, dass jackson im POM.xml hinterlegt ist.

```
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-resteasy-reactive-jackson</artifactId>
</dependency>
```

### POST create Character

```
curl -POST "http://localhost:8080/characters" -H "Content-Type: application/json" -d '{
    "name": "Thalindor",
    "level": 1,
    "strength": 10,
    "dexterity": 12,
    "intelligence": 14
}'
```

### GET by Name

```
curl -X GET "http://localhost:8080/characters/name/Thalindor"

```


### Put Levelup by Name


```
curl -X PUT "http://localhost:8080/characters/levelup/Thalindor"
```


## KAFKA einbinden

wir werden mit Kafka eine kommunikation erstellen damit Service a mit Service b komunizieren kann. 
Dabei soll bei einem Level up ein zufällges Attribut des Charakters erhöht werden.

### Kafka als Messagebroker in Docker starten

ergänze dazu das Docker-compose.yml file

```
version: '3.8'

services:
  # MariaDB Datenbank
  mariadb:
    image: mariadb:10.5
    container_name: mariadb_dnd
    restart: always
    environment:
      MARIADB_ROOT_PASSWORD: root
      MARIADB_DATABASE: dnd_db
      MARIADB_USER: dnd_user
      MARIADB_PASSWORD: secret
    ports:
      - "3306:3306"
    volumes:
      - mariadb_data:/var/lib/mysql

  # Zookeeper für Kafka
  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    container_name: zookeeper
    restart: always
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000

  # Kafka Broker
  kafka:
    image: confluentinc/cp-kafka:latest
    container_name: kafka
    restart: always
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092,PLAINTEXT_HOST://localhost:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    depends_on:
      - zookeeper

  # Kafka UI für Verwaltung
  kafka-ui:
    image: provectuslabs/kafka-ui:latest
    container_name: kafka-ui
    restart: always
    ports:
      - "8081:8080"
    environment:
      KAFKA_CLUSTERS_0_NAME: local
      KAFKA_CLUSTERS_0_BOOTSTRAP_SERVERS: kafka:9092
    depends_on:
      - kafka

volumes:
  mariadb_data:
```

Starte die Container

```
docker compose down
docker compose up -d
```


Ergänze das application.properties mit

```
# Kafka Verbindung
mp.messaging.outgoing.level-up.connector=smallrye-kafka
mp.messaging.outgoing.level-up.topic=level-up
mp.messaging.outgoing.level-up.value.serializer=org.apache.kafka.common.serialization.StringSerializer
mp.messaging.outgoing.level-up.bootstrap.servers=localhost:9092

```
 - level-up ist das Topic, in das Service A schreibt.
 - bootstrap.servers=localhost:9092 verbindet Kafka mit Quarkus.
 - StringSerializer sorgt dafür, dass Nachrichten als String gesendet werden.

### Ergänze die Resource

Ändere die Put Methode

```

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import jakarta.inject.Inject;


@PUT
@Path("/levelup/{characterName}")
@Transactional
@Operation(summary = "Charakter leveln", description = "Erhöht das Level eines Charakters anhand des Namens und sendet ein Kafka-Event")
public Response levelUpCharacter(@PathParam("characterName") String characterName) {
    CharacterEntity character = repository.find("name", characterName).firstResult();

    if (character == null) {
        return Response.status(Response.Status.NOT_FOUND).entity("Character not found").build();
    }

    character.level++;

    // Kafka Event senden
    String event = character.name + " leveled up to " + character.level;
    levelUpEmitter.send(event);

    return Response.ok(character).build();
}

@Inject
@Channel("level-up")
Emitter<String> levelUpEmitter;
```




# LevelUp Service (Service B)

### Erstelle ein neues Projekt 

```
mvn io.quarkus.platform:quarkus-maven-plugin:3.2.0.Final:create \
    -DprojectGroupId=com.dnd \
    -DprojectArtifactId=level-up-service \
    -Dextensions="smallrye-reactive-messaging-kafka,resteasy-reactive" \
    -DnoExamples

```
kopiere ausserdem das POM.xml von meinem ersten Projekt und ersetzt dieses hier.

### Application Properties

```
# Set a unique port for the Level-Up Service to avoid conflicts
quarkus.http.port=9999

# Kafka Consumer (Listens to level-up events from the Character Service)
mp.messaging.incoming.level-up.connector=smallrye-kafka
mp.messaging.incoming.level-up.topic=level-up
mp.messaging.incoming.level-up.value.deserializer=org.apache.kafka.common.serialization.StringDeserializer
mp.messaging.incoming.level-up.bootstrap.servers=localhost:9092

# Enable debug logging (optional, useful for troubleshooting)
quarkus.log.level=INFO
```

Wir starten dieses Quarkus Projekt auf einem anderen Port um konflikte zu vermeiden.

### Update random Attribute

es soll bei einem Level up ein zufälliges Attribut verbessert werden.

Dazu wird ein Listener erstellt der auf level up reagiert.

```
package com.dnd.listener;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Random;

@ApplicationScoped
public class LevelUpListener {

    private final Random random = new Random();

    @Incoming("level-up")
    public void onCharacterLevelUp(String message) {
        System.out.println("Received Kafka message: " + message);

        // Simulierte Attribut-Erhöhung
        String[] attributes = { "Strength", "Dexterity", "Intelligence" };
        String boostedAttribute = attributes[random.nextInt(attributes.length)];

        System.out.println("Boosting " + boostedAttribute);
    }
}
```

## Überprüfen von Kafka

Service A macht einen Level up mit 
```
curl -X PUT "http://localhost:8080/characters/levelup/Thalindor"
```

Mit Service B empfangen wir die änderung

 - **Received Kafka message: Thalindor leveled** 
 - **up to 3**
 - **Boosting Strength**