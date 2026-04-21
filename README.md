# ServiceDeck — Professional Microservices Manager

ServiceDeck is a modern, high-performance desktop orchestrator designed to streamline the management of local microservices. Built with Kotlin Multiplatform and Compose Multiplatform, it provides a unified control center for Spring Boot, Quarkus, Micronaut, and other JVM-based services.

![App Screenshot](APP-UI.png)

## 📥 Download

ServiceDeck is available as a native application for **Windows**, **macOS**, and **Linux**. You can download the latest installers from the [Releases](https://github.com/YOUR_USERNAME/YOUR_REPO/releases) page:

- **Windows:** `.msi` installer
- **macOS:** `.dmg` disk image
- **Linux:** `.deb` package

## 🚀 Key Features

- **Automated Service Discovery:** Instantly scan project roots to discover runnable microservices (Maven/Gradle).
- **Smart Metadata Detection:** Automatically extracts service names, ports, and frameworks from `application.yml/properties`, including support for Spring Cloud profiles.
- **Runtime Port Intelligence:** Real-time monitoring of service logs to detect actual binding ports and handle dynamic assignments.
- **Modern Log Console:** High-performance, IntelliJ-inspired log tailing with ANSI code stripping, colored level badges, and instant search.
- **Conflict Resolution:** Detects port conflicts at both OS-level and runtime, providing one-click "Kill & Start" resolution.
- **Background Process Recovery:** Automatically identifies and manages services already running in the background, even after an app restart.
- **System Health Dashboard:** Aggregate project metrics including total CPU, Heap memory, and active port count.
- **Platform Native:** Native look-and-feel across Windows, macOS, and Linux.

## 🛠 Tech Stack

- **UI:** Compose Multiplatform (Desktop)
- **Language:** Kotlin
- **Build System:** Gradle
- **State Management:** MVI-inspired Architecture
- **Process Management:** JVM ProcessHandle & ProcessBuilder

## 👨‍💻 Development

### Prerequisites
- JDK 17 or higher

### Running from Source
```bash
./gradlew :composeApp:run
```

### Building Native Distribution
```bash
./gradlew :composeApp:packageDeb   # Linux
./gradlew :composeApp:packageMsi   # Windows
./gradlew :composeApp:packageDmg   # macOS
```

---
Built with ❤️ for microservice developers.
