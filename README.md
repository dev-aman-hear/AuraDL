# 🎵 AuraDL — Premium Apple Music Desktop Downloader

![AuraDL Banner](https://img.shields.io/badge/AuraDL-v1.0.0-gold?style=for-the-badge&logo=applemusic&logoColor=white)
![Java](https://img.shields.io/badge/Java-21%2B-blue?style=for-the-badge&logo=openjdk)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

**AuraDL** is a high-performance, modern desktop application for searching and downloading high-fidelity audio and music videos from Apple Music. Built with a sleek **True Dark Glassmorphism UI**, AuraDL features Hi-Res Lossless ALAC audio support, Dolby Atmos, 4K Music Videos, real-time download logs, and an interactive Album Details Viewer.

---

## ✨ Features

- **🎨 Modern Glassmorphism UI**: True Dark AMOLED theme with metallic gold accents, glow effects, smooth card layouts, and responsive tabbed navigation.
- **🔍 Multi-Category iTunes Search**: Instant search tabs for **Songs**, **Albums**, and **Music Videos** with accurate entity filtering and audio/video quality badge indicators.
- **💽 Interactive Album Tracklist Viewer**: Click or double-click any album to open a dedicated Album Details window showing high-res artwork, full tracklist, song audio specs (`ALAC 24-bit/96kHz`), individual track queuing, and a **Queue Entire Album** action button.
- **📊 Real-Time Download Queue Console**: Split-pane queue view featuring animated progress bars, active status pills (`⬇ DOWNLOADING`, `🔓 DECRYPTING`, `✔ DONE`), summary statistics (`3 active · 5 done · 0 failed`), and live stdout terminal logs.
- **🎧 Hi-Res Audio & Video Specs**:
  - **Songs**: ALAC Lossless (up to 24-bit/192kHz), Dolby Atmos spatial audio, and AAC.
  - **Music Videos**: 4K UHD (2160p HDR) and 1080p Full HD with AAC audio remuxing.
  - **Metadata & Lyrics**: Embedded high-res artwork (up to 3000px) and synced lyrics (`.lrc`).
- **🔑 Flexible Authentication**: Supports Netscape `cookies.txt`, Apple Music `Media-User-Token`, or local `Wrapper Server` proxies.

---

## 🚀 Prerequisites

Before using AuraDL, make sure you have the following installed on your system:

1. **Java Runtime Environment (JRE 21 or later)**:
   - Download from [Eclipse Adoptium / Temurin](https://adoptium.net/) or [Oracle Java](https://www.oracle.com/java/).
2. **FFmpeg**:
   - Required for remuxing and embedding metadata/artwork.
   - Add `ffmpeg.exe` to your System `PATH` or specify its path in the **Settings** tab.
3. **gamdl / yt-dlp**:
   - Download engine backend. Ensure `gamdl` or `yt-dlp` is installed and accessible.

---

## 🔑 Authentication & Setup Guide

AuraDL requires Apple Music authentication credentials to download full-length audio tracks. You can configure your credentials in the **Settings** tab using one of the 3 supported authentication methods:

---

### Method 1: Netscape `cookies.txt` File (Recommended)

Using a `cookies.txt` file is the most reliable way to authenticate with Apple Music.

#### Steps to Export `cookies.txt`:
1. Open your web browser (Chrome, Edge, Firefox, or Brave) and navigate to **[music.apple.com](https://music.apple.com)**.
2. Sign in with your active **Apple Music account**.
3. Install a browser extension for exporting cookies in Netscape format:
   - **Chrome / Edge**: [Get cookies.txt LOCALLY](https://chromewebstore.google.com/detail/get-cookiestxt-locally/cclelndahbckbenkjhflpdbgdldlbecc)
   - **Firefox**: [cookies.txt](https://addons.mozilla.org/en-US/firefox/addon/cookies-txt/)
4. Click the extension icon while on `music.apple.com` and export/download the file as `cookies.txt`.
5. Move the downloaded `cookies.txt` file into your AuraDL directory, or specify its exact file path under **Settings ➔ Cookies File Path**.
6. Set **API Method** to **`cookies-file`**.

---

### Method 2: Media User Token (`media-user-token`)

If you prefer using an authentication token header directly from your browser session:

#### Steps to Extract `media-user-token`:
1. Open **[music.apple.com](https://music.apple.com)** in your browser and ensure you are logged in.
2. Press `F12` (or Right-Click ➔ **Inspect**) to open Browser Developer Tools.
3. Go to the **Application** tab (in Chrome/Edge) or **Storage** tab (in Firefox).
4. Expand **Cookies** on the left sidebar and select `https://music.apple.com`.
5. Locate the cookie named **`media-user-token`**.
6. Double-click its **Value** column and copy the long token string.
7. Open AuraDL, go to the **Settings** tab, paste the token into **Media User Token**, and set **API Method** to **`media-user-token`**.

---

### Method 3: Wrapper Server (`wrapper-server`)

For advanced setups utilizing a local wrapper server or proxy server (such as an Apple Music API wrapper):

1. Start your local wrapper server on your machine (e.g., `http://localhost:8080`).
2. Open AuraDL, go to **Settings**, set **Wrapper Server Base URL** to your server address (e.g., `http://localhost:8080`).
3. Set **API Method** to **`wrapper-server`**.

---

## ⚙️ Configuration Options

In the **Settings** tab, you can customize all aspects of download behavior and file naming:

| Setting | Default Value | Description |
| :--- | :--- | :--- |
| **Download Directory** | `./Apple Music` | Destination folder for downloaded files |
| **FFmpeg Path** | Auto-detected | Path to `ffmpeg.exe` binary |
| **Max Concurrent Downloads** | `3` | Number of simultaneous download threads (1–10) |
| **Download Engine** | `ytdlp` | Engine mode (`gamdl` / `ytdlp`) |
| **Codec Priority** | `alac,atmos,aac` | Order of preferred audio formats |
| **Cover Art Size** | `1200px` | Resolution for downloaded artwork (600–3000px) |
| **Cover Art Format** | `jpg` | Image format (`jpg` / `png`) |
| **Lyrics Format** | `lrc` | Synced lyrics format (`lrc` / `txt`) |
| **Folder Template** | `{album_artist}/{album}` | Custom directory structure for albums |
| **Track Template** | `{track_number} {title}` | File naming convention for tracks |

---

## 🛠️ Building from Source

To compile and package AuraDL manually from source code:

```powershell
# 1. Clone the repository
git clone https://github.com/your-username/AuraDL.git
cd AuraDL

# 2. Compile Java source files
$srcFiles = Get-ChildItem -Path "src-java" -Recurse -Filter "*.java" | Select-Object -ExpandProperty FullName
javac -encoding UTF-8 -d "build\java_classes" $srcFiles

# 3. Create standalone executable JAR
jar cfm "dist-java\AuraDL.jar" "build\MANIFEST.MF" -C "build\java_classes" .

# 4. Run AuraDL
java -cp "dist-java\AuraDL.jar" com.auradl.Main
```

---

## 📄 License & Disclaimer

This project is licensed under the **MIT License**.

> **[!IMPORTANT]**  
> **AuraDL** is developed for educational and personal archival purposes only. Users must possess a valid, active Apple Music subscription to download content. Respect copyright laws and artist rights in your jurisdiction.
