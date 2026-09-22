package com.x4yi.x4ui.impl.client.resource.network;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Locale;

public final class SecurityUtils {
    public static final long MAX_DOWNLOAD_BYTES = 512L * 1024L * 1024L;
    private static final int MAX_REDIRECTS = 5;

    private SecurityUtils() {}

    public static String sanitizeModId(String modId) {
        if (modId == null || modId.trim().isEmpty()) {
            return "common";
        }
        String clean = modId.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.-]", "_");
        while (clean.contains("..")) {
            clean = clean.replace("..", "_");
        }
        if (clean.isEmpty() || ".".equals(clean)) {
            return "common";
        }
        return clean;
    }

    public static File resolveSafeChild(File rootDir, String childName) throws IOException {
        File rootCanonical = rootDir.getCanonicalFile();
        File target = new File(rootCanonical, childName).getCanonicalFile();
        String rootPath = rootCanonical.getPath();
        String targetPath = target.getPath();
        if (!targetPath.equals(rootPath) && !targetPath.startsWith(rootPath + File.separator)) {
            throw new SecurityException("Path traversal attempt blocked: " + childName);
        }
        return target;
    }

    public static String sanitizeExtension(String urlStr, String defaultExt) {
        if (urlStr == null) return defaultExt;
        String path = urlStr;
        int qIdx = path.indexOf('?');
        if (qIdx != -1) path = path.substring(0, qIdx);
        int hIdx = path.indexOf('#');
        if (hIdx != -1) path = path.substring(0, hIdx);
        int lastSlash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        String fileName = (lastSlash >= 0) ? path.substring(lastSlash + 1) : path;
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot >= 0) {
            String ext = fileName.substring(lastDot).toLowerCase(Locale.ROOT);
            if (ext.matches("\\.[a-z0-9]{1,8}")) {
                return ext;
            }
        }
        return defaultExt;
    }

    public static HttpURLConnection openValidatedHttpConnection(String urlStr) throws IOException {
        String currentUrl = urlStr;
        for (int redirects = 0; redirects <= MAX_REDIRECTS; redirects++) {
            URL url = validateHttpUrl(currentUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) X4UI/1.0");
            conn.setInstanceFollowRedirects(false);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(20000);

            int code = conn.getResponseCode();
            if (code == HttpURLConnection.HTTP_MOVED_PERM
                    || code == HttpURLConnection.HTTP_MOVED_TEMP
                    || code == HttpURLConnection.HTTP_SEE_OTHER
                    || code == 307 || code == 308) {
                String location = conn.getHeaderField("Location");
                conn.disconnect();
                if (location == null || location.trim().isEmpty()) {
                    throw new IOException("Redirect without Location header (HTTP " + code + ")");
                }
                URI resolved = URI.create(currentUrl).resolve(location.trim());
                currentUrl = resolved.toString();
                continue;
            }
            if (code >= 400) {
                conn.disconnect();
                throw new IOException("HTTP Error: " + code);
            }
            return conn;
        }
        throw new IOException("Too many HTTP redirects (> " + MAX_REDIRECTS + ")");
    }

    public static URL validateHttpUrl(String urlStr) throws IOException {
        if (urlStr == null || urlStr.trim().isEmpty()) {
            throw new IOException("Empty URL");
        }
        URL url = new URL(urlStr.trim());
        String protocol = url.getProtocol().toLowerCase(Locale.ROOT);
        if (!"http".equals(protocol) && !"https".equals(protocol)) {
            throw new SecurityException("Unsupported URL protocol: " + protocol);
        }
        String host = url.getHost();
        if (host == null || host.isEmpty()) {
            throw new IOException("URL has no host: " + urlStr);
        }
        if (!Boolean.getBoolean("x4ui.allowLocalMedia")) {
            InetAddress[] addresses = InetAddress.getAllByName(host);
            for (InetAddress addr : addresses) {
                if (addr.isAnyLocalAddress()
                        || addr.isLoopbackAddress()
                        || addr.isLinkLocalAddress()
                        || addr.isSiteLocalAddress()) {
                    throw new SecurityException("Blocked local/private network address: " + host);
                }
            }
        }
        return url;
    }

    public static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return Integer.toHexString(input.hashCode());
        }
    }
}

