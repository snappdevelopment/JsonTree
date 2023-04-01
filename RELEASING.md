## Releasing a new version

1. Update version in `gradle.properties`
2. Open and merge PR
3. On main: `git tag -a vX.Y.Z -m "Version X.Y.Z"`
4. `git push --tags`
5. Go to Sonatype repository and release artifact

## Releasing the local Maven repository

1. Update version in `gradle.properties` with `-SNAPSHOT`
2. `./gradlew publishToMavenLocal`
3. Add the `mavenLocal()` repository and the library to the consumer project