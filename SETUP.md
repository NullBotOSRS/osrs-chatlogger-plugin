# Setup Instructions

## Quick Start: Push to GitHub

### 1. Create a New GitHub Repository

1. Go to [github.com/new](https://github.com/new)
2. Repository name: `osrs-chatlogger-plugin`
3. Description: `RuneLite plugin for syncing chat to osrschatlogger.com`
4. Set to **Public** (required for Plugin Hub)
5. Do NOT add README, .gitignore, or license (they're included)
6. Click **Create repository**

### 2. Push the Code

Extract the zip and run these commands:

```bash
cd osrs-chatlogger-plugin

# Initialize git
git init

# Add all files
git add .

# Initial commit
git commit -m "Initial release v1.0.0"

# Add your remote (replace YOUR_USERNAME)
git remote add origin https://github.com/YOUR_USERNAME/osrs-chatlogger-plugin.git

# Push to GitHub
git push -u origin main
```

### 3. Generate Gradle Wrapper JAR

The gradle-wrapper.jar file isn't included (it's 60KB and usually generated). To add it:

```bash
# If you have Gradle installed:
gradle wrapper

# Or download manually from a trusted source and place in:
# gradle/wrapper/gradle-wrapper.jar
```

Alternatively, users can use their system Gradle directly.

---

## Submit to Plugin Hub (Optional)

To make your plugin available to all RuneLite users:

### 1. Fork Plugin Hub

1. Go to [github.com/runelite/plugin-hub](https://github.com/runelite/plugin-hub)
2. Click **Fork** (top right)

### 2. Add Your Plugin

1. In your fork, edit `plugins` file
2. Add a new line with your repository:
   ```
   NullBotOSRS/osrs-chatlogger-plugin
   ```
3. Commit the change

### 3. Create a Release

In your plugin repository:

1. Go to **Releases** → **Create a new release**
2. Tag: `v1.0.0`
3. Title: `OSRS Chatlogger v1.0.0`
4. Description: Initial release
5. Click **Publish release**

### 4. Submit Pull Request

1. Go to your plugin-hub fork
2. Click **Contribute** → **Open pull request**
3. Title: `Add OSRS Chatlogger plugin`
4. Description: Brief description of what it does
5. Submit and wait for review

---

## Test Locally Before Submitting

### Method 1: Using IntelliJ IDEA

1. Install [IntelliJ IDEA](https://www.jetbrains.com/idea/download/) (Community is free)
2. Install [Java JDK 11](https://adoptium.net/temurin/releases/?version=11)
3. Open IntelliJ → **Open** → Select the plugin folder
4. Wait for Gradle to sync
5. Run → Edit Configurations → Add **Application**
   - Main class: `net.runelite.client.RuneLite`
   - VM options: `-ea`
   - Working directory: Your plugin folder
6. Run it!

### Method 2: Command Line

```bash
cd osrs-chatlogger-plugin

# Build the plugin
./gradlew build

# The JAR will be in build/libs/
```

Then load the JAR as an external plugin in RuneLite.

---

## File Checklist

Make sure these files are present before pushing:

```
✅ .gitignore
✅ LICENSE
✅ README.md
✅ build.gradle
✅ settings.gradle
✅ runelite-plugin.properties
✅ gradlew
✅ gradlew.bat
✅ gradle/wrapper/gradle-wrapper.properties
✅ icon.png (48x48)
✅ src/main/java/com/osrschatlogger/
   ├── OsrsChatloggerPlugin.java
   ├── OsrsChatloggerConfig.java
   ├── ChatSender.java
   ├── ChatData.java
   └── OsrsChatloggerOverlay.java
```

---

## Updating the Plugin

To release a new version:

1. Update version in `build.gradle`
2. Commit changes
3. Create a new release tag (e.g., `v1.1.0`)
4. Plugin Hub will automatically pick up the new version

---

## Need Help?

- [RuneLite Discord](https://runelite.net/discord)
- [Plugin Hub Documentation](https://github.com/runelite/plugin-hub/blob/master/README.md)
- [OSRS Chatlogger Discord](https://discord.gg/xAa3JXaVb5)
