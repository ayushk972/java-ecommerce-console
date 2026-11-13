
# Build Notes

## Build Environment

- Java: 17 (LTS)
- Maven: 3.9.16
- Encoding: UTF-8
- Packaging: JAR
- Main Class: `com.flipkart.app.Main`

## Dependencies

| Dependency | Version | Scope |
|---|---|---|
| JUnit Jupiter | 6.1.3 | test |

## Maven Plugins

| Plugin | Version | Purpose |
|---|---|---|
| Maven Compiler Plugin | 3.13.0 | Compiles source code using Java 17 |
| Maven Surefire Plugin | 3.5.2 | Executes JUnit tests |
| Maven JAR Plugin | 3.4.2 | Packages the executable JAR |
| JaCoCo Maven Plugin | 0.8.14 | Generates code coverage reports |

## Build Commands

- Full build: `mvn -B clean verify`
- Integration tests: `mvn -B test -Dgroups=integration`
- Exclude integration tests: `mvn -B test -DexcludedGroups=integration`

## Reports

- JUnit reports: `target/surefire-reports/`
- JaCoCo report: `target/site/jacoco/index.html`

## Version Selection

The dependency and plugin versions above are the versions selected for this project.

Verify that each selected version was a stable release at least 14 days before the project version-selection date, and record the release dates or verification sources here.
