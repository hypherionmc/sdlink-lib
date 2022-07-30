### SDLink Library

This is the library with common code used by my Simple Discord Link bot mod. This is only here as a backup and so others can see what I do. Not intended to be used by other mods

***

#### Building Instructions (For contributors)

For 1.16.5:

```gradle
// Pack OSHI into jar, since MC uses an incompatible version
gradlew build -Poshi_hack=true

// To Publish
gradlew publish -Poshi_hack=true

// Publish to mavenLocal()
gradlew publishToMavenLocal -Poshi_hack=true
```

For 1.17+:

```gradle
// Pack OSHI into jar, since MC uses an incompatible version
gradlew build

// To Publish
gradlew publish

// Publish to mavenLocal()
gradlew publishToMavenLocal
```
