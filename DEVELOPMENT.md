## Development

Instructions on how to develop in this project.

### Detekt

Install pre-commit hook for auto formatting:

Run the following command from the project root.
```
.github/hooks/install-pre-commit.sh
```

Run Detekt on all library modules:

```
./gradlew detektAll
```

Run Detekt on all library modules with auto-formatting enabled

```
./gradlew detektAll -PdetektAutoFix=true
```

Generate Detekt baseline

```
./gradlew detektGenerateBaseline
```

### Api validation

Create or overwrite the api validation file

```
./gradlew apiDump
```

Check the api validation file

```
./gradlew apiCheck
```