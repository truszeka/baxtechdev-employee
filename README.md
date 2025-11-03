# Employee Service

Ez a projekt egy önállóan futtatható Spring Boot (3.2.5) alapú REST alkalmazás, amely az `employee.xml` fájlból olvassa be a dolgozók adatait, majd három különböző végpont segítségével teszi elérhetővé azokat.

## Követelmények
- Java 17
- Maven 3.9+

A fejlesztéshez és a tesztek futtatásához Java 17-et használtam.

## Fordítás és futtatás
```bash
mvn clean package
java -jar target/employee-service-1.0.0.jar
```

Fejlesztés közbeni futtatáshoz használható a Spring Boot Maven plugin is:
```bash
mvn spring-boot:run
```

## Elérhető végpontok
- `GET /rest/employees` – egyedileg, keresztnév + vezetéknév szerint rendezve adja vissza a dolgozók neveit.
- `GET /rest/employees?department={department}` – az adott osztályhoz tartozó dolgozókat listázza, rendezve.
- `GET /rest/employees/groupby/department` – osztályonként csoportosítva adja vissza a dolgozókat.

A szolgáltatás JSON formátumban válaszol.

## Swagger / OpenAPI felület
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI dokumentum: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

A Swagger UI segítségével böngészőből is kipróbálhatók a REST végpontok.

## Tesztek futtatása
```bash
mvn test
```
