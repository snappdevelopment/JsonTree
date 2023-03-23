## Releasing a new version

1. Update version in `gradle.properties`
2. Open and merge PR
3. On main: `git tag -a vX.Y.Z -m "Version X.Y.Z"`
4. `git push --tags`
5. Go to Sonatype repository and release artifact