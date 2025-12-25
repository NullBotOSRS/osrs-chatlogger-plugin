# OSRS Chatlogger - RuneLite Plugin

[![RuneLite Plugin Hub](https://img.shields.io/badge/RuneLite-Plugin%20Hub-orange)](https://runelite.net/plugin-hub)
[![License](https://img.shields.io/badge/License-BSD%202--Clause-blue.svg)](LICENSE)

Sync your Old School RuneScape chat messages directly to [osrschatlogger.com](https://osrschatlogger.com) in real-time. No more manual file uploads!

![Plugin Screenshot](https://osrschatlogger.com/static/images/plugin-screenshot.png)

## Features

- **🔄 Real-time sync** - Messages are sent to your Chatlogger account instantly
- **📦 Smart batching** - Efficient batching reduces network overhead
- **💾 Offline buffering** - Messages are queued if connection is lost (up to 1000 messages)
- **⚙️ Configurable filters** - Choose exactly which chat types to log
- **📊 Status overlay** - See connection status and message counts in-game
- **🔒 Secure** - API keys are stored securely, all data sent over HTTPS

## Supported Chat Types

| Chat Type | Default |
|-----------|---------|
| ✅ Public chat | Enabled |
| ✅ Private messages (sent & received) | Enabled |
| ✅ Clan chat | Enabled |
| ✅ Group Ironman chat | Enabled |
| ✅ Friends chat (FC) | Enabled |
| ✅ Game messages (drops, level ups, etc.) | Enabled |
| ⬜ Trade requests | Disabled |

## Installation

### From Plugin Hub (Recommended)

1. Open **RuneLite**
2. Click the **wrench icon** (Configuration)
3. Click **Plugin Hub** at the bottom
4. Search for **"OSRS Chatlogger"**
5. Click **Install**

### Manual Installation (Development)

1. Clone this repository
2. Build with Gradle: `./gradlew build`
3. Run RuneLite with the external plugin

## Setup

### Step 1: Get your API Key

1. Go to [osrschatlogger.com](https://osrschatlogger.com)
2. Log in or create an account
3. Navigate to **Settings → API Keys**
4. Click **"Generate New API Key"**
5. **Copy the key immediately** - it's only shown once!

### Step 2: Configure the Plugin

1. In RuneLite, click the **wrench icon**
2. Find **"OSRS Chatlogger"** in the plugin list
3. Paste your API key in the **"API Key"** field
4. Configure which chat types you want to log

### Step 3: Verify Connection

- The status overlay should show **"Connected"** (green)
- Visit [osrschatlogger.com](https://osrschatlogger.com) to see your messages appearing in real-time!

## Configuration Options

### API Settings

| Option | Description |
|--------|-------------|
| **API Key** | Your osrschatlogger.com API key (required) |
| **Server URL** | Server URL (default: https://osrschatlogger.com) |

### Chat Types

Toggle which types of messages to sync:

- **Public Chat** - Overhead public chat messages
- **Private Messages** - PMs sent and received
- **Clan Chat** - Clan chat messages
- **Group Ironman Chat** - GIM group messages
- **Friends Chat** - FC messages
- **Game Messages** - Drops, level ups, broadcasts, etc.
- **Trade Requests** - Trade request spam (disabled by default)

### Display

| Option | Default | Description |
|--------|---------|-------------|
| **Show Status Overlay** | ✅ | Show connection status in-game |
| **Show Message Count** | ✅ | Show sent/pending message counts |

### Advanced

| Option | Default | Description |
|--------|---------|-------------|
| **Batch Size** | 10 | Messages per batch (1-50) |
| **Batch Delay** | 2000ms | Max wait before sending batch |
| **Retry Attempts** | 3 | Retries on failure (0-5) |
| **Debug Mode** | ❌ | Enable debug logging |

## Troubleshooting

### "Disconnected" Status

1. ✅ Check your API key is correct (starts with `oscl_`)
2. ✅ Verify your internet connection
3. ✅ Check if [osrschatlogger.com](https://osrschatlogger.com) is accessible
4. ✅ Try regenerating your API key in settings

### Messages Not Appearing

1. ✅ Make sure the chat type is enabled in plugin settings
2. ✅ Check the "Pending" count in the overlay
3. ✅ Enable Debug Mode and check RuneLite logs

### Rate Limited

The plugin has built-in rate limiting protection:
- The plugin will automatically wait and retry
- Messages are buffered locally
- No messages will be lost

## Privacy & Security

- 🔒 Only chat messages you configure are sent
- 🔒 All data sent securely over HTTPS
- 🔒 Your API key is stored locally in RuneLite settings
- 🔒 You can revoke API keys instantly from the website
- 🔒 No passwords or sensitive data are ever transmitted

## Building from Source

Requirements:
- Java JDK 11+
- Gradle 7+

```bash
# Clone the repository
git clone https://github.com/NullBotOSRS/osrs-chatlogger-plugin.git
cd osrs-chatlogger-plugin

# Build
./gradlew build

# The JAR will be in build/libs/
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## Support

- **Website**: [osrschatlogger.com](https://osrschatlogger.com)
- **Discord**: [Join our Discord](https://discord.gg/xAa3JXaVb5)
- **Issues**: [GitHub Issues](https://github.com/NullBotOSRS/osrs-chatlogger-plugin/issues)

## License

This project is licensed under the BSD 2-Clause License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- [RuneLite](https://runelite.net/) - The amazing open source client
- [OSRS Chatlogger](https://osrschatlogger.com) - The chat logging platform
- All our users and contributors!

---

Made with ❤️ by [NullBot](https://osrschatlogger.com)
