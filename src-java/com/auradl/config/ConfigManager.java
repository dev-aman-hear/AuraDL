package com.auradl.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ConfigManager {
    private final Path configPath;
    private final Config config;

    public ConfigManager() {
        String userHome = System.getProperty("user.home");
        this.configPath = Paths.get(userHome, ".auradl", "config.yml");
        this.config = new Config();
        initConfigFile();
    }

    private void initConfigFile() {
        try {
            if (!Files.exists(configPath.getParent())) {
                Files.createDirectories(configPath.getParent());
            }
            if (Files.exists(configPath)) {
                loadConfig();
            } else {
                saveConfig();
            }
        } catch (IOException e) {
            System.err.println("[ConfigManager] Error initializing config file: " + e.getMessage());
        }
    }

    public Config getConfig() {
        return config;
    }

    public boolean exists() {
        return Files.exists(configPath);
    }

    public void loadConfig() {
        File file = configPath.toFile();
        if (!file.exists()) return;

        try (FileReader reader = new FileReader(file)) {
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[1024];
            int numRead;
            while ((numRead = reader.read(buf)) != -1) {
                sb.append(buf, 0, numRead);
            }
            String content = sb.toString();
            parseSimpleYaml(content);
        } catch (IOException e) {
            System.err.println("[ConfigManager] Failed to read config: " + e.getMessage());
        }
    }

    private void parseSimpleYaml(String content) {
        String[] lines = content.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            int colonIdx = line.indexOf(':');
            if (colonIdx > 0) {
                String key = line.substring(0, colonIdx).trim();
                String val = line.substring(colonIdx + 1).trim();
                if (val.startsWith("\"") && val.endsWith("\"")) {
                    val = val.substring(1, val.length() - 1);
                } else if (val.startsWith("'") && val.endsWith("'")) {
                    val = val.substring(1, val.length() - 1);
                }

                switch (key) {
                    case "api_method":
                        config.setApiMethod(ApiMethod.fromString(val));
                        break;
                    case "browser_name":
                        config.setBrowserName(val);
                        break;
                    case "browser_profile":
                        config.setBrowserProfile(val);
                        break;
                    case "max_concurrent_downloads":
                        try {
                            config.setMaxConcurrentDownloads(Integer.parseInt(val));
                        } catch (NumberFormatException ignored) {}
                        break;
                    case "cookies":
                        config.setCookies(val);
                        break;
                    case "wrapper_base_url":
                        config.setWrapperBaseUrl(val);
                        break;
                    case "media_user_token":
                        config.setMediaUserToken(val);
                        break;
                    case "language":
                        config.setLanguage(val);
                        break;
                    case "output":
                        config.setOutputDir(val);
                        break;
                    case "temp":
                        config.setTempDir(val);
                        break;
                    case "download_mode":
                        config.setDownloadMode(val);
                        break;
                    case "nm3u8dlre_path":
                        config.setNm3u8dlrePath(val);
                        break;
                    case "ffmpeg_path":
                        config.setFfmpegPath(val);
                        break;
                    case "album_folder_template":
                        config.setAlbumFolderTemplate(val);
                        break;
                    case "song_file_template":
                        config.setSongFileTemplate(val);
                        break;
                    case "song_codec_priority":
                        config.setSongCodecPriority(val);
                        break;
                    case "cover_format":
                        config.setCoverFormat(val);
                        break;
                    case "cover_size":
                        try {
                            config.setCoverSize(Integer.parseInt(val));
                        } catch (NumberFormatException ignored) {}
                        break;
                    case "synced_lyrics_format":
                        config.setSyncedLyricsFormat(val);
                        break;
                    case "save_cover":
                        config.setSaveCover(Boolean.parseBoolean(val));
                        break;
                    case "save_playlist":
                        config.setSavePlaylist(Boolean.parseBoolean(val));
                        break;
                    case "overwrite":
                        config.setOverwrite(Boolean.parseBoolean(val));
                        break;
                    case "music_video_resolution":
                        config.setMusicVideoResolution(val);
                        break;
                    case "music_video_codec_priority":
                        config.setMusicVideoCodecPriority(val);
                        break;
                    case "music_video_remux_format":
                        config.setMusicVideoRemuxFormat(val);
                        break;
                }
            }
        }
    }

    public void saveConfig() {
        try (FileWriter writer = new FileWriter(configPath.toFile())) {
            writer.write("api_method: " + config.getApiMethod().getValue() + "\n");
            writer.write("browser_name: \"" + config.getBrowserName() + "\"\n");
            writer.write("browser_profile: \"" + config.getBrowserProfile() + "\"\n");
            writer.write("max_concurrent_downloads: " + config.getMaxConcurrentDownloads() + "\n");
            writer.write("cookies: \"" + config.getCookies() + "\"\n");
            writer.write("wrapper_base_url: \"" + config.getWrapperBaseUrl() + "\"\n");
            writer.write("media_user_token: \"" + config.getMediaUserToken() + "\"\n");
            writer.write("language: \"" + config.getLanguage() + "\"\n");
            writer.write("output: \"" + config.getOutputDir() + "\"\n");
            writer.write("temp: \"" + config.getTempDir() + "\"\n");
            writer.write("download_mode: \"" + config.getDownloadMode() + "\"\n");
            writer.write("nm3u8dlre_path: \"" + config.getNm3u8dlrePath() + "\"\n");
            writer.write("ffmpeg_path: \"" + config.getFfmpegPath() + "\"\n");
            writer.write("album_folder_template: \"" + config.getAlbumFolderTemplate() + "\"\n");
            writer.write("song_file_template: \"" + config.getSongFileTemplate() + "\"\n");
            writer.write("song_codec_priority: \"" + config.getSongCodecPriority() + "\"\n");
            writer.write("cover_format: \"" + config.getCoverFormat() + "\"\n");
            writer.write("cover_size: " + config.getCoverSize() + "\n");
            writer.write("synced_lyrics_format: \"" + config.getSyncedLyricsFormat() + "\"\n");
            writer.write("save_cover: " + config.isSaveCover() + "\n");
            writer.write("save_playlist: " + config.isSavePlaylist() + "\n");
            writer.write("overwrite: " + config.isOverwrite() + "\n");
            writer.write("music_video_resolution: \"" + config.getMusicVideoResolution() + "\"\n");
            writer.write("music_video_codec_priority: \"" + config.getMusicVideoCodecPriority() + "\"\n");
            writer.write("music_video_remux_format: \"" + config.getMusicVideoRemuxFormat() + "\"\n");
        } catch (IOException e) {
            System.err.println("[ConfigManager] Failed to save config: " + e.getMessage());
        }
    }
}
