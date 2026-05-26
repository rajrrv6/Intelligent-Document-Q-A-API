# Checkpoint: Configuration Format Migration (YAML to Properties)

## Completed Tasks
- [x] Converted standard `application.yml` configurations to [application.properties](file:///c:/Users/rajrr/OneDrive/Desktop/Intelligent-Document-Q-A-API/src/main/resources/application.properties).
- [x] Converted `application-dev.yml` overrides to [application-dev.properties](file:///c:/Users/rajrr/OneDrive/Desktop/Intelligent-Document-Q-A-API/src/main/resources/application-dev.properties), adding local `ragdb` datasource parameters and model parameters (`phi3` and `nomic-embed-text`).
- [x] Converted `application-prod.yml` to [application-prod.properties](file:///c:/Users/rajrr/OneDrive/Desktop/Intelligent-Document-Q-A-API/src/main/resources/application-prod.properties).
- [x] Converted the test-specific configuration `src/test/resources/application.yml` to [src/test/resources/application.properties](file:///c:/Users/rajrr/OneDrive/Desktop/Intelligent-Document-Q-A-API/src/test/resources/application.properties).
- [x] Removed all four old `.yml` files from the project.
- [x] Updated all references of `application.yml` to `application.properties` in [README.md](file:///c:/Users/rajrr/OneDrive/Desktop/Intelligent-Document-Q-A-API/README.md).
- [x] Verified via `./mvnw clean test` that all 29 tests successfully pass (including database mock replacements).
- [x] Verified via `./mvnw spring-boot:run` that the application correctly bootstraps the properties files and attempts database connection.
