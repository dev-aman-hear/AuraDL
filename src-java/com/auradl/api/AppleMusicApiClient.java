package com.auradl.api;

import com.auradl.config.ApiMethod;
import com.auradl.config.Config;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppleMusicApiClient {
    private final HttpClient httpClient;
    private final Config config;
    private AccountInfo accountInfo;

    // Apple Music URL patterns
    private static final Pattern SONG_URL    = Pattern.compile("music\\.apple\\.com/.+/album/.+\\?i=(\\d+)");
    private static final Pattern ALBUM_URL   = Pattern.compile("music\\.apple\\.com/.+/album/[^?]+/(\\d+)(?:\\?.*)?$");
    private static final Pattern PLAYLIST_URL= Pattern.compile("music\\.apple\\.com/.+/playlist/.+/(pl\\.[a-z0-9]+)");
    private static final Pattern ARTIST_URL  = Pattern.compile("music\\.apple\\.com/.+/artist/.+/(\\d+)");
    private static final Pattern VIDEO_URL   = Pattern.compile("music\\.apple\\.com/.+/music-video/.+/(\\d+)");

    public AppleMusicApiClient(Config config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(12))
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
    }

    public CompletableFuture<Boolean> initialize() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("[AppleMusicApiClient] Initializing via method: " + config.getApiMethod());
                if (config.getApiMethod() == ApiMethod.WRAPPER) {
                    try {
                        HttpRequest req = HttpRequest.newBuilder()
                                .uri(URI.create(config.getWrapperBaseUrl() + "/me"))
                                .timeout(Duration.ofSeconds(5))
                                .GET().build();
                        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
                        if (resp.statusCode() == 200) {
                            this.accountInfo = new AccountInfo("wrapper-user", "us", true);
                            System.out.println("[AppleMusicApiClient] Wrapper connected.");
                            return true;
                        }
                    } catch (Exception e) {
                        System.out.println("[AppleMusicApiClient] Wrapper unavailable, using web API.");
                    }
                }
                this.accountInfo = new AccountInfo("web-user", "us", true);
                return true;
            } catch (Exception e) {
                System.err.println("[AppleMusicApiClient] Init error: " + e.getMessage());
                return false;
            }
        });
    }

    /**
     * Detect whether a query is an Apple Music URL.
     * Returns null if it is not a URL.
     */
    public static boolean isAppleMusicUrl(String query) {
        return query != null && query.contains("music.apple.com");
    }

    /**
     * Resolve a pasted Apple Music URL into a MediaItem by querying the
     * iTunes Lookup API using the ID extracted from the URL.
     */
    public CompletableFuture<MediaItem> resolveUrl(String url) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("[AppleMusicApiClient] Resolving URL: " + url);

                // ── Song (track) ──────────────────────────────────────────────
                Matcher m = SONG_URL.matcher(url);
                if (m.find()) {
                    String trackId = m.group(1);
                    return lookupById(trackId, "song", url);
                }

                // ── Playlist ──────────────────────────────────────────────────
                m = PLAYLIST_URL.matcher(url);
                if (m.find()) {
                    return new MediaItem(m.group(1), extractNameFromUrl(url, "playlist"),
                            "Apple Music", "", "playlist", 0, url);
                }

                // ── Music Video ───────────────────────────────────────────────
                m = VIDEO_URL.matcher(url);
                if (m.find()) {
                    return lookupById(m.group(1), "musicVideo", url);
                }

                // ── Album ─────────────────────────────────────────────────────
                m = ALBUM_URL.matcher(url);
                if (m.find()) {
                    return lookupById(m.group(1), "album", url);
                }

                // ── Artist ────────────────────────────────────────────────────
                m = ARTIST_URL.matcher(url);
                if (m.find()) {
                    return new MediaItem(m.group(1), extractNameFromUrl(url, "artist"),
                            "Apple Music", "", "artist", 0, url);
                }

                // Fallback — return minimal item with raw URL
                return new MediaItem(url, extractNameFromUrl(url, "track"), "Apple Music", "", "song", 0, url);

            } catch (Exception e) {
                System.err.println("[AppleMusicApiClient] resolveUrl error: " + e.getMessage());
                return new MediaItem(url, "Apple Music Track", "Unknown", "", "song", 0, url);
            }
        });
    }

    /**
     * iTunes Lookup API — public, no auth needed.
     * https://itunes.apple.com/lookup?id=<id>&entity=<entity>
     */
    private MediaItem lookupById(String id, String entity, String originalUrl) {
        try {
            String lookupUrl = "https://itunes.apple.com/lookup?id=" + id + "&entity=" + entity;
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(lookupUrl))
                    .timeout(Duration.ofSeconds(10))
                    .header("User-Agent", "AuraDL/1.0")
                    .GET().build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                String body = resp.body();
                return parseItunesLookupResult(body, entity, originalUrl, id);
            }
        } catch (Exception e) {
            System.err.println("[AppleMusicApiClient] Lookup error for id=" + id + ": " + e.getMessage());
        }
        return new MediaItem(id, "Apple Music Track #" + id, "Unknown", "", mapEntityToType(entity), 0, originalUrl);
    }

    /**
     * Simple JSON field extractor — avoids needing a JSON library.
     */
    private MediaItem parseItunesLookupResult(String json, String entity, String originalUrl, String id) {
        int resultCount = extractInt(json, "resultCount", 0);
        if (resultCount == 0) {
            return new MediaItem(id, extractNameFromUrl(originalUrl, "track"), "Unknown", "", mapEntityToType(entity), 0, originalUrl);
        }
        int firstResult = json.indexOf("\"results\"");
        String block = json.substring(firstResult);

        String title  = extractField(block, "trackName");
        if (title == null || title.isEmpty()) title = extractField(block, "collectionName");
        if (title == null || title.isEmpty()) title = extractField(block, "artistName");
        if (title == null || title.isEmpty()) title = extractNameFromUrl(originalUrl, "track");

        String artist = extractField(block, "artistName");
        if (artist == null) artist = "Apple Music";

        String artwork = extractField(block, "artworkUrl100");
        if (artwork == null) artwork = "";
        artwork = artwork.replace("100x100bb", "600x600bb");

        long duration = extractLong(block, "trackTimeMillis");
        String type   = mapEntityToType(entity);

        String trackUrl = extractField(block, "trackViewUrl");
        if (trackUrl == null || trackUrl.isEmpty()) trackUrl = extractField(block, "collectionViewUrl");
        if (trackUrl == null || trackUrl.isEmpty()) trackUrl = originalUrl;

        MediaItem item = new MediaItem(id, title, artist, artwork, type, duration, trackUrl);

        // Populate extended metadata
        String genre = extractField(block, "primaryGenreName");
        item.setGenre(genre != null ? genre : "");

        String col = extractField(block, "collectionName");
        item.setCollectionName(col != null ? col : "");

        long tn = extractLong(block, "trackNumber");
        item.setTrackNumber((int) tn);

        long tc = extractLong(block, "trackCount");
        item.setTrackCount((int) tc);

        String expl = extractField(block, "trackExplicitness");
        item.setExplicit("explicit".equalsIgnoreCase(expl));

        String country = extractField(block, "country");
        item.setCountry(country != null ? country : "US");

        // Quality: Apple Music supports ALAC lossless for songs; videos are typically HD
        if ("song".equals(type)) {
            item.setQuality("ALAC · Lossless · 44.1kHz · 16-bit");
        } else if ("video".equals(type)) {
            item.setQuality("1080p HD · AAC");
        } else if ("album".equals(type)) {
            long tracks = extractLong(block, "trackCount");
            item.setQuality((tracks > 0 ? tracks + " tracks · " : "") + "ALAC Lossless");
        } else {
            item.setQuality("");
        }

        System.out.println("[AppleMusicApiClient] Resolved: " + title + " by " + artist + " [" + type + "] genre=" + item.getGenre());
        return item;
    }

    /**
     * Real iTunes Search API: https://itunes.apple.com/search?term=<query>&entity=song&limit=25
     */
    public CompletableFuture<List<MediaItem>> search(String query) {
        return CompletableFuture.supplyAsync(() -> {
            List<MediaItem> items = new ArrayList<>();
            try {
                System.out.println("[AppleMusicApiClient] Searching iTunes API: " + query);
                String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
                String searchUrl = "https://itunes.apple.com/search?term=" + encoded
                        + "&media=music&entity=song,album,musicVideo&limit=25&country="
                        + (config.getLanguage() != null && config.getLanguage().contains("-")
                            ? config.getLanguage().split("-")[1].toLowerCase() : "us");

                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(searchUrl))
                        .timeout(Duration.ofSeconds(12))
                        .header("User-Agent", "AuraDL/1.0")
                        .GET().build();

                HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 200) {
                    items = parseSearchResults(resp.body(), null);
                    System.out.println("[AppleMusicApiClient] Found " + items.size() + " results.");
                } else {
                    System.err.println("[AppleMusicApiClient] Search HTTP " + resp.statusCode());
                    items = fallbackResults(query);
                }
            } catch (Exception e) {
                System.err.println("[AppleMusicApiClient] Search error: " + e.getMessage());
                items = fallbackResults(query);
            }
            return items;
        });
    }

    /**
     * Explicit typed search — fires a dedicated iTunes API call for a single entity type.
     * entity must be one of: "song", "album", "musicVideo"
     */
    public CompletableFuture<List<MediaItem>> searchByType(String query, String entity) {
        return CompletableFuture.supplyAsync(() -> {
            List<MediaItem> items = new ArrayList<>();
            try {
                String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
                String country = (config.getLanguage() != null && config.getLanguage().contains("-"))
                        ? config.getLanguage().split("-")[1].toLowerCase() : "us";
                String searchUrl = "https://itunes.apple.com/search?term=" + encoded
                        + "&media=music&entity=" + entity + "&limit=25&country=" + country;

                System.out.println("[AppleMusicApiClient] Explicit search [" + entity + "]: " + searchUrl);

                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(searchUrl))
                        .timeout(Duration.ofSeconds(12))
                        .header("User-Agent", "AuraDL/1.0")
                        .GET().build();

                HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 200) {
                    items = parseSearchResults(resp.body(), entity);
                    System.out.println("[AppleMusicApiClient] [" + entity + "] Found " + items.size() + " results.");
                } else {
                    System.err.println("[AppleMusicApiClient] [" + entity + "] HTTP " + resp.statusCode());
                }
            } catch (Exception e) {
                System.err.println("[AppleMusicApiClient] [" + entity + "] Error: " + e.getMessage());
            }
            return items;
        });
    }

    private List<MediaItem> parseSearchResults(String json, String requestedEntity) {
        List<MediaItem> items = new ArrayList<>();
        int idx = json.indexOf("\"results\"");
        if (idx < 0) return items;

        String results = json.substring(idx);
        String[] blocks = results.split("\\{\"wrapperType\"");
        if (blocks.length <= 1) {
            blocks = results.split("\\{\"collectionId\"|\\{\"trackId\"");
        }
        for (int i = 1; i < blocks.length && items.size() < 25; i++) {
            String block = blocks[i];

            String wrapperType = extractField(block, "wrapperType");
            String collectionType = extractField(block, "collectionType");
            String kind        = extractField(block, "kind");

            String title = extractField(block, "trackName");
            if (title == null || title.isEmpty()) title = extractField(block, "collectionName");
            if (title == null || title.isEmpty()) continue;

            String artist = extractField(block, "artistName");
            if (artist == null) artist = "Unknown";

            String artwork = extractField(block, "artworkUrl100");
            if (artwork == null) artwork = "";
            artwork = artwork.replace("100x100bb", "600x600bb");

            long duration = extractLong(block, "trackTimeMillis");

            String url = extractField(block, "trackViewUrl");
            if (url == null || url.isEmpty()) url = extractField(block, "collectionViewUrl");
            if (url == null || url.isEmpty()) continue;

            String type;
            if ("album".equalsIgnoreCase(requestedEntity) || "collection".equals(wrapperType) || "Album".equalsIgnoreCase(collectionType)) {
                type = "album";
            } else if ("musicVideo".equalsIgnoreCase(requestedEntity) || "music-video".equals(kind) || "musicVideo".equals(kind)) {
                type = "video";
            } else if ("song".equalsIgnoreCase(requestedEntity)) {
                type = "song";
            } else if ("collection".equals(wrapperType)) {
                type = "album";
            } else if ("musicVideo".equals(kind)) {
                type = "video";
            } else {
                type = "song";
            }

            String id;
            if ("album".equalsIgnoreCase(type)) {
                id = extractField(block, "collectionId");
                if (id == null) id = extractField(block, "trackId");
            } else {
                id = extractField(block, "trackId");
                if (id == null) id = extractField(block, "collectionId");
            }
            if (id == null) id = String.valueOf(items.size());

            MediaItem item = new MediaItem(id, title, artist, artwork, type, duration, url);

            // Extended metadata
            String genre = extractField(block, "primaryGenreName");
            item.setGenre(genre != null ? genre : "");

            String col = extractField(block, "collectionName");
            item.setCollectionName(col != null ? col : "");

            long tn = extractLong(block, "trackNumber");
            item.setTrackNumber((int) tn);

            long tc = extractLong(block, "trackCount");
            item.setTrackCount((int) tc);

            String expl = extractField(block, "trackExplicitness");
            item.setExplicit("explicit".equalsIgnoreCase(expl));

            // Quality determination
            if ("song".equals(type)) {
                item.setQuality("ALAC · Lossless · 44.1kHz / 24-bit");
            } else if ("video".equals(type)) {
                String hdPrice = extractField(block, "trackHdPrice");
                String hdCol = extractField(block, "collectionHdPrice");
                if (hdPrice != null || hdCol != null) {
                    item.setQuality("4K UHD · 2160p · HDR · AAC Audio");
                } else {
                    item.setQuality("1080p Full HD · AAC Audio");
                }
            } else if ("album".equals(type)) {
                item.setQuality((tc > 0 ? tc + " tracks · " : "") + "ALAC Lossless");
            }

            items.add(item);
        }
        return items;
    }

    private List<MediaItem> fallbackResults(String query) {
        List<MediaItem> items = new ArrayList<>();
        items.add(new MediaItem("offline-1", query + " (Offline — check internet)", "Apple Music Search",
                "", "song", 0, "https://music.apple.com/search?term=" + query.replace(" ", "+")));
        return items;
    }

    // ── Simple JSON field helpers (no external library) ─────────────────────

    private String extractField(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start < 0) {
            // Also check integer/numeric representation e.g. "collectionId":123456
            String searchNum = "\"" + key + "\":";
            int startNum = json.indexOf(searchNum);
            if (startNum < 0) return null;
            startNum += searchNum.length();
            int endNum = startNum;
            while (endNum < json.length() && (Character.isDigit(json.charAt(endNum)) || json.charAt(endNum) == '-')) endNum++;
            if (endNum > startNum) return json.substring(startNum, endNum);
            return null;
        }
        start += search.length();
        int end = json.indexOf("\"", start);
        if (end < 0) return null;
        return json.substring(start, end);
    }

    private long extractLong(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start < 0) return 0;
        start += search.length();
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) end++;
        try { return Long.parseLong(json.substring(start, end)); } catch (NumberFormatException e) { return 0; }
    }

    private int extractInt(String json, String key, int def) {
        long v = extractLong(json, key);
        return v == 0 ? def : (int) v;
    }

    private String extractNameFromUrl(String url, String fallback) {
        try {
            String path = url.split("\\?")[0];
            String[] parts = path.split("/");
            for (int i = parts.length - 1; i >= 0; i--) {
                String seg = parts[i];
                if (!seg.isEmpty() && !seg.matches("\\d+") && !seg.equals("us") && !seg.equals("album")
                        && !seg.equals("song") && !seg.equals("artist") && !seg.equals("playlist")) {
                    return seg.replace("-", " ");
                }
            }
        } catch (Exception ignored) {}
        return fallback;
    }

    private String mapEntityToType(String entity) {
        switch (entity) {
            case "musicVideo": return "video";
            case "album":      return "album";
            case "artist":     return "artist";
            default:           return "song";
        }
    }

    /**
     * Fetch album details and its complete tracklist from iTunes Lookup API.
     */
    public CompletableFuture<AlbumDetails> fetchAlbumDetails(String albumIdOrUrl) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String id = albumIdOrUrl;
                Matcher m = ALBUM_URL.matcher(albumIdOrUrl);
                if (m.find()) {
                    id = m.group(1);
                } else if (albumIdOrUrl.contains("/")) {
                    String[] parts = albumIdOrUrl.split("/");
                    for (int i = parts.length - 1; i >= 0; i--) {
                        if (parts[i].matches("\\d+")) {
                            id = parts[i];
                            break;
                        }
                    }
                }

                String lookupUrl = "https://itunes.apple.com/lookup?id=" + id + "&entity=song";
                System.out.println("[AppleMusicApiClient] Fetching album details for ID " + id + ": " + lookupUrl);

                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(lookupUrl))
                        .timeout(Duration.ofSeconds(10))
                        .header("User-Agent", "AuraDL/1.0")
                        .GET().build();

                HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 200) {
                    AlbumDetails details = parseAlbumDetailsResult(resp.body(), id, albumIdOrUrl);

                    // Fallback: If initial lookup returned 0 tracks or only 1 track, extract collectionId and re-lookup!
                    if (details == null || details.getTracks().isEmpty()) {
                        String colId = extractField(resp.body(), "collectionId");
                        if (colId != null && !colId.equals(id)) {
                            System.out.println("[AppleMusicApiClient] Re-lookup using extracted collectionId: " + colId);
                            String colLookupUrl = "https://itunes.apple.com/lookup?id=" + colId + "&entity=song";
                            HttpRequest colReq = HttpRequest.newBuilder()
                                    .uri(URI.create(colLookupUrl))
                                    .timeout(Duration.ofSeconds(10))
                                    .header("User-Agent", "AuraDL/1.0")
                                    .GET().build();
                            HttpResponse<String> colResp = httpClient.send(colReq, HttpResponse.BodyHandlers.ofString());
                            if (colResp.statusCode() == 200) {
                                details = parseAlbumDetailsResult(colResp.body(), colId, albumIdOrUrl);
                            }
                        }
                    }
                    return details;
                }
            } catch (Exception e) {
                System.err.println("[AppleMusicApiClient] fetchAlbumDetails error: " + e.getMessage());
            }
            return null;
        });
    }

    private AlbumDetails parseAlbumDetailsResult(String json, String id, String originalUrl) {
        List<MediaItem> tracks = new ArrayList<>();
        MediaItem albumItem = null;

        int idx = json.indexOf("\"results\"");
        if (idx < 0) return null;

        String results = json.substring(idx);
        String[] blocks = results.split("\\{\"wrapperType\"");
        if (blocks.length <= 1) {
            blocks = results.split("\\{\"collectionId\"|\\{\"trackId\"");
        }

        for (int i = 1; i < blocks.length; i++) {
            String block = blocks[i];
            String wrapperType = extractField(block, "wrapperType");
            String collectionType = extractField(block, "collectionType");

            String title = extractField(block, "trackName");
            if (title == null || title.isEmpty()) title = extractField(block, "collectionName");
            if (title == null || title.isEmpty()) continue;

            String artist = extractField(block, "artistName");
            if (artist == null) artist = "Unknown Artist";

            String artwork = extractField(block, "artworkUrl100");
            if (artwork == null) artwork = "";
            artwork = artwork.replace("100x100bb", "600x600bb");

            long duration = extractLong(block, "trackTimeMillis");

            String url = extractField(block, "trackViewUrl");
            if (url == null || url.isEmpty()) url = extractField(block, "collectionViewUrl");
            if (url == null || url.isEmpty()) url = originalUrl;

            String trackId = extractField(block, "trackId");
            if (trackId == null) trackId = extractField(block, "collectionId");
            if (trackId == null) trackId = id + "-" + i;

            String genre = extractField(block, "primaryGenreName");
            String col = extractField(block, "collectionName");
            long tn = extractLong(block, "trackNumber");
            long tc = extractLong(block, "trackCount");
            String expl = extractField(block, "trackExplicitness");

            if ("collection".equals(wrapperType) || "Album".equalsIgnoreCase(collectionType) || (i == 1 && albumItem == null && duration == 0)) {
                MediaItem album = new MediaItem(id, title, artist, artwork, "album", duration, url);
                album.setGenre(genre != null ? genre : "");
                album.setCollectionName(col != null ? col : title);
                album.setTrackCount((int) tc);
                album.setExplicit("explicit".equalsIgnoreCase(expl));
                album.setQuality((tc > 0 ? tc + " tracks · " : "") + "ALAC Lossless 24-bit/96kHz");
                albumItem = album;
            } else {
                MediaItem song = new MediaItem(trackId, title, artist, artwork, "song", duration, url);
                song.setGenre(genre != null ? genre : "");
                song.setCollectionName(col != null ? col : "");
                song.setTrackNumber((int) tn);
                song.setExplicit("explicit".equalsIgnoreCase(expl));
                song.setQuality("ALAC · Lossless · 44.1kHz / 24-bit");
                tracks.add(song);
            }
        }

        if (albumItem == null && !tracks.isEmpty()) {
            MediaItem first = tracks.get(0);
            albumItem = new MediaItem(id, first.getCollectionName().isEmpty() ? first.getTitle() : first.getCollectionName(),
                    first.getArtist(), first.getArtworkUrl(), "album", 0, originalUrl);
            albumItem.setGenre(first.getGenre());
            albumItem.setTrackCount(tracks.size());
            albumItem.setQuality(tracks.size() + " tracks · ALAC Lossless");
        }

        return new AlbumDetails(albumItem, tracks);
    }

    public AccountInfo getAccountInfo() { return accountInfo; }
}
