# Chest Tracker — NeoForge 26.1.2

Unofficial NeoForge port of [Chest Tracker](https://github.com/ponuing/ChestTracker) for Minecraft 26.1.2.

## Requirements

- Minecraft 26.1.2
- NeoForge 26.1.2.95 or newer
- Java 25
- [YetAnotherConfigLib (YACL) 3.9.1+26.1 for NeoForge](https://modrinth.com/mod/yacl)

## Build

```shell
./gradlew build
```

The built JAR is written to `build/libs/`.

## Port status

Core container memory, storage, search, inventory controls and configuration are ported. World highlights and labels are currently disabled. Optional Litematica, Jade, WTHIT, ShulkerBoxTooltip and Searchables integrations are not included in this build.

This branch also contains NeoForge lifecycle fixes for item components used by the Shared Ender Chest integration.

## Credits and license

Chest Tracker was created by JackFred and is maintained by ponuing. This repository is an unofficial port. The Chest Tracker source and assets remain licensed under LGPL-3.0; see `src/main/resources/LICENSE_chesttracker`.
