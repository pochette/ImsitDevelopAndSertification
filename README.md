# Учебные проекты ИМСИТ

Архитектура лабораторной №04 (агентная система перевозки грузов): [cargo/docs/architecture.md](cargo/docs/architecture.md). Пока подготовлен проект архитектуры; выбрана Java 17, реализация Maven-модуля cargo ещё не добавлена.

Репозиторий организован как многомодульный Maven-проект. Корневой `pom.xml` объединяет модули и задаёт общие версии. Зависимости приложения, Spring Boot plugin и проверка покрытия находятся в модуле гостиницы.

```text
pom.xml             — родительский POM, packaging=pom
mvnw / mvnw.cmd     — общий Maven Wrapper
hotel/
  pom.xml           — приложение гостиницы
  src/main/         — код и ресурсы
  src/test/         — тесты
postman/            — коллекция и окружение для API гостиницы
```

Команды выполняются из корня репозитория, требуется JDK 17 или новее и настроенный `JAVA_HOME`.

```powershell
# Сборка всех модулей, тесты и проверка покрытия
.\mvnw.cmd clean verify

# Только гостиница
.\mvnw.cmd -pl hotel clean verify

# Запуск гостиницы
.\mvnw.cmd -pl hotel spring-boot:run
```

На Linux/macOS используйте `./mvnw` вместо `.\mvnw.cmd`.

Исполняемый JAR: `hotel/target/hotel-0.0.1-SNAPSHOT.jar`.
Отчёт покрытия: `hotel/target/site/jacoco/index.html`.
Инструкция Postman и описание тестов: [postman/README.md](postman/README.md).

Чтобы добавить проект, создайте рядом с `hotel` новую папку с `pom.xml`, укажите родителя `ru.burdak:ImsitDevelopAndSertification:0.0.1-SNAPSHOT` с `relativePath` равным `../pom.xml` и добавьте имя папки в `<modules>` корневого POM. Зависимости и плагины конкретного приложения объявляйте в его модуле. В IntelliJ IDEA импортируйте корневой `pom.xml` или выполните Reload All Maven Projects.
