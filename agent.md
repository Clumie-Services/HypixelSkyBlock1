# HypixelSkyBlock - Agent Documentation

This document provides AI agents with comprehensive instructions for navigating and understanding the HypixelSkyBlock codebase.

## Project Overview

**HypixelSkyBlock** is a Minestom-based recreation of Hypixel's SkyBlock gamemode.

| Aspect | Details |
|--------|---------|
| Framework | Java 25, Minestom server framework |
| Architecture | Microservices with scalable multi-server system |
| Database | MongoDB for persistence, Redis for inter-service communication |
| Proxy | Velocity proxy for load balancing and player routing |
| Deployment | Docker Compose with containerized services |
| Build System | Gradle with Kotlin DSL |

---

## Directory Structure

```
HypixelSkyBlock/
├── commons/                          # Shared code & protocols
├── loader/                           # Main entry point (HypixelCore.jar)
├── type.*/                           # Server type implementations
│   ├── type.generic/                 # Base loader for all servers
│   ├── type.skyblockgeneric/         # Shared SkyBlock functionality
│   ├── type.island/                  # Private island server
│   ├── type.hub/                     # SkyBlock Hub
│   ├── type.lobby/                   # Generic lobby
│   └── [12 more type modules...]
├── service.*/                        # Independent microservices
│   ├── service.generic/              # Base service framework
│   ├── service.api/                  # REST API service
│   ├── service.auctionhouse/         # Auctions system
│   ├── service.bazaar/               # Market system
│   └── [5 more service modules...]
├── velocity.extension/               # Velocity proxy plugin
├── proxy.api/                        # Proxy communication API
├── pvp/                              # PvP mechanics
├── anticheat/                        # SwoftyAnticheat system
├── packer/                           # Resource pack builder
├── configuration/                    # Server configs & assets
└── docker-compose.yml                # Full deployment config
```

---

## Key Entry Points

### Main Entry Point
- **File**: `loader/src/main/java/net/swofty/loader/Hypixel.java`
- **Execution**: `java -jar HypixelCore.jar <SERVER_TYPE> [OPTIONS]`

### Startup Flow
1. Parse server type from command-line args
2. Initialize Minestom server with Velocity authentication
3. Load configuration from `configuration/config.yml`
4. Discover and instantiate `HypixelTypeLoader` for the server type
5. Initialize `HypixelGenericLoader` (base functionality)
6. Initialize `SkyBlockGenericLoader` (if SkyBlock server)
7. Call `typeLoader.onInitialize()`
8. Setup Redis communication (ProxyAPI)
9. Load event handlers via reflection
10. Register server with proxy

---

## Module Types

### Commons Module
- **Path**: `commons/src/main/java/net/swofty/commons/`
- **Purpose**: Shared enums, protocols, and utilities used across all modules
- **Key Files**:
  - `ServerType.java` - Enum of all server types
  - `ServiceType.java` - Enum of all services
  - `CustomWorlds.java` - World folder mappings
  - `ConfigProvider.java` - Configuration loading

### Type Modules (Server Implementations)
- **Pattern**: Each type implements `HypixelTypeLoader` interface
- **Location**: `type.*/src/main/java/net/swofty/type/*/`
- **Key Interface Methods**:
  - `getType()` - Returns the ServerType
  - `onInitialize()` - Runs before server starts
  - `afterInitialize()` - Runs after player support begins
  - `getTraditionalEvents()` - Minecraft event handlers
  - `getRequiredServices()` - Needed microservices

### Service Modules (Microservices)
- **Pattern**: Each service implements `SkyBlockService` interface
- **Location**: `service.*/src/main/java/net/swofty/service/*/`
- **Run independently** as separate JVM processes

---

## Server Types

### SkyBlock Servers (13 types)
| ServerType | Module | Description |
|------------|--------|-------------|
| SKYBLOCK_ISLAND | type.island | Private player island |
| SKYBLOCK_HUB | type.hub | Main social hub |
| SKYBLOCK_GOLD_MINE | type.goldmine | Mining zone |
| SKYBLOCK_DEEP_CAVERNS | type.deepcaverns | Deep mining area |
| SKYBLOCK_DWARVEN_MINES | type.dwarvenmines | Dwarven area |
| SKYBLOCK_SPIDERS_DEN | type.spidersden | Combat zone |
| SKYBLOCK_THE_END | type.theend | End dimension |
| SKYBLOCK_CRIMSON_ISLE | type.crimsonisle | Nether area |
| SKYBLOCK_THE_PARK | type.thepark | Forest zone |
| SKYBLOCK_GALATEA | type.galatea | Special zone |
| SKYBLOCK_BACKWATER_BAYOU | type.backwaterbayou | Swamp zone |
| SKYBLOCK_JERRYS_WORKSHOP | type.jerrysworkshop | Winter event |
| SKYBLOCK_THE_FARMING_ISLANDS | type.thefarmingislands | Farming zone |
| SKYBLOCK_DUNGEON_HUB | type.dungeon.hub | Dungeon entrance |

### Other
| ServerType | Module | Description |
|------------|--------|-------------|
| PROTOTYPE_LOBBY | type.prototypelobby | Testing lobby |

---

## Architecture Patterns

### 1. Loader Hierarchy
```
HypixelTypeLoader (Interface)
├── SkyBlockTypeLoader (extends HypixelTypeLoader)
│   └── Implemented by each SkyBlock server type
└── Standard loaders for non-SkyBlock types
```

### 2. Event System
```java
@HypixelEvent(
    node = EventNodes.PLAYER_JOIN,
    requireDataLoaded = true,
    isAsync = false
)
public void onPlayerJoin(Player player) { ... }
```
- Events discovered via reflection in `net.swofty.type.*` packages
- Use `EventNodes` enum for event types

### 3. Redis Communication
- **ProxyToClient**: Proxy → Game server messages
- **ServiceToClient**: Service → Game server messages
- **ServerOutboundMessage**: Game server → Proxy/Services
- Messages are JSON-serialized via Redis pub/sub

### 4. Data Handlers
```java
public interface GameDataHandler {
    String getHandlerId();
    Map<UUID, ? extends DataHandler> getCache();
    DataHandler createFromDocument(UUID uuid, Document doc);
    DataHandler initWithDefaults(UUID uuid);
}
```
- Registered in `GameDataHandlerRegistry`
- Auto-loads on player join, saves on quit

---

## Database Layer

### MongoDB Collections
- **profiles** - SkyBlock player profiles
- **users** - Player account data
- **attributes** - Player statistics
- **authentication** - Login credentials

### Key Database Classes
- `MongoDB.java` - Base interface
- `UserDatabase.java` - Player accounts
- `ProfilesDatabase.java` - Island profiles
- `AttributeDatabase.java` - Player stats

---

## Configuration Files

| File | Purpose |
|------|---------|
| `configuration/config.yml` | Main server settings (MongoDB, Redis, etc.) |
| `configuration/settings.yml` | Game-specific settings |
| `configuration/velocity.toml` | Velocity proxy config |
| `configuration/achievements/*.yml` | Achievement definitions |
| `configuration/quests/*.yml` | Quest definitions |
| `configuration/worlds/` | Pre-generated world files |

---

## Common File Location Patterns

| Looking for... | Path Pattern |
|----------------|--------------|
| Server implementations | `type.*/src/main/java/net/swofty/type/*/Type*Loader.java` |
| Event handlers | `type.*/src/main/java/net/swofty/type/*/events/` |
| Service endpoints | `service.*/src/main/java/net/swofty/service/*/endpoints/` |
| Shared utilities | `commons/src/main/java/net/swofty/commons/` |
| Commands | `*/commands/` directories within type modules |
| GUIs | `*/gui/` directories within type modules |
| NPCs | `*/npcs/` directories within type modules |

---

## Important Classes Reference

### Core Framework
| Class | Location | Purpose |
|-------|----------|---------|
| `Hypixel` | loader/.../Hypixel.java | Main entry point |
| `HypixelTypeLoader` | type.generic/.../HypixelTypeLoader.java | Server type interface |
| `HypixelGenericLoader` | type.generic/.../HypixelGenericLoader.java | Base initialization |
| `SkyBlockGenericLoader` | type.skyblockgeneric/.../SkyBlockGenericLoader.java | SkyBlock base |

### Player Management
| Class | Location | Purpose |
|-------|----------|---------|
| `HypixelPlayer` | type.generic/.../user/HypixelPlayer.java | Base player class |
| `SkyBlockPlayer` | type.skyblockgeneric/.../user/SkyBlockPlayer.java | SkyBlock player |

### Data & Persistence
| Class | Location | Purpose |
|-------|----------|---------|
| `GameDataHandler` | type.generic/.../data/GameDataHandler.java | Data handler interface |
| `GameDataHandlerRegistry` | type.generic/.../data/GameDataHandlerRegistry.java | Handler registration |

### Communication
| Class | Location | Purpose |
|-------|----------|---------|
| `ProxyAPI` | proxy.api/.../ProxyAPI.java | Redis connection manager |
| `ServerOutboundMessage` | proxy.api/.../ServerOutboundMessage.java | Outbound messages |

---

## Common Development Tasks

### Adding a New Event Handler
1. Create class in `type.*/src/main/java/net/swofty/type/*/events/`
2. Add methods with `@HypixelEvent` annotation
3. Automatically discovered via reflection

### Adding a New Command
1. Create class in `type.*/src/main/java/net/swofty/type/*/commands/`
2. Extend appropriate command base class
3. Registered automatically

### Adding Player Data
1. Create `DataHandler` implementation
2. Create `GameDataHandler` implementation
3. Register in `GameDataHandlerRegistry`
4. Add to `TypeLoader.getAdditionalDataHandlers()`

### Adding a New Server Type
1. Create module `type.newtype/`
2. Implement `SkyBlockTypeLoader` in `TypeNewTypeLoader.java`
3. Add to `settings.gradle.kts`
4. Add world to `CustomWorlds` enum
5. Add Docker container to `docker-compose.yml`

---

## Build & Run Commands

```bash
# Build all modules
./gradlew build

# Run specific server type
java -jar loader/build/libs/HypixelCore.jar SKYBLOCK_ISLAND

# Docker deployment
docker-compose up --build

# Run with test flow
gradle -PtestFlow=flow_name runWithTestFlow
```

---

## Search Tips for Agents

```bash
# Find server type implementations
grep -r "class Type.*Loader implements"

# Find all events
grep -r "@HypixelEvent"

# Find service implementations
grep -r "implements SkyBlockService"

# Find database operations
grep -r "extends MongoDB"

# Find GUI implementations
grep -r "extends.*GUI"
```

---

## External Resources

- **Documentation**: https://opensource.swofty.net
- **Javadocs**: https://swofty-developments.github.io/HypixelSkyBlock
- **Discord**: discord.swofty.net
