# BlurpAPI

[📖 View the full documentation on GitBook](https://blurp-1.gitbook.io/blurp-docs)

BlurpAPI is a utility library for Minecraft plugins, providing useful features such as cooldown management, sound handling, item creation, raycasting, scheduling, and region utilities.

## Features
- Cooldown management
- Sound utilities
- Item utilities
- Raycast utilities
- Task scheduling
- Region management

## Installation
Add the following repository to your `pom.xml`:

```xml
<repository>
    <id>github</id>
    <name>BlurpAPI</name>
    <url>https://maven.pkg.github.com/bloupyi/BlurpAPI</url>
</repository>
```

And add BlurpAPI as a dependency:

```xml
<dependency>
    <groupId>fr.bloup</groupId>
    <artifactId>blurpapi</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage
Import the utilities from the `fr.bloup.blurpapi.utils` package in your plugin.

## License
MIT