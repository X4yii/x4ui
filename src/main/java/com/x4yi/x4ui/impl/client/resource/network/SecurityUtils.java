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
                try {
                    URI resolved = URI.create(currentUrl).resolve(location.trim());
                    currentUrl = resolved.toString();
                } catch (IllegalArgumentException e) {
                    throw new IOException("Malformed redirect Location: " + location, e);
                }
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
        if (url.getUserInfo() != null && !url.getUserInfo().isEmpty()) {
            throw new SecurityException("URL userinfo is not allowed");
        }
        String host = url.getHost();
        if (host == null || host.isEmpty()) {
            throw new IOException("URL has no host: " + urlStr);
        }
        if (!Boolean.getBoolean("x4ui.allowLocalMedia")) {
            InetAddress[] addresses = InetAddress.getAllByName(host);
            for (InetAddress addr : addresses) {
                if (isBlockedAddress(addr)) {
                    throw new SecurityException("Blocked local/private network address: " + host);
                }
            }
        }
        return url;
    }

    public static boolean isBlockedAddress(InetAddress addr) {
        if (addr == null) return true;
        if (addr.isAnyLocalAddress()
                || addr.isLoopbackAddress()
                || addr.isLinkLocalAddress()
                || addr.isSiteLocalAddress()
                || addr.isMulticastAddress()) {
            return true;
        }
        byte[] b = addr.getAddress();
        if (b.length == 4) {
            int b0 = b[0] & 0xFF;
            int b1 = b[1] & 0xFF;
            int b2 = b[2] & 0xFF;
            if (b0 == 0) return true; // 0.0.0.0/8
            if (b0 == 100 && (b1 & 0xC0) == 64) return true; // 100.64.0.0/10 CGNAT
            if (b0 == 169 && b1 == 254) return true; // 169.254.0.0/16 link-local
            if (b0 == 192 && b1 == 0 && b2 == 0) return true; // 192.0.0.0/24 IETF protocol
            if (b0 == 198 && (b1 == 18 || b1 == 19)) return true; // 198.18.0.0/15 benchmark
        } else if (b.length == 16) {
            int b0 = b[0] & 0xFF;
            int b1 = b[1] & 0xFF;
            if ((b0 & 0xFE) == 0xFC) return true; // fc00::/7 IPv6 ULA
            if (b0 == 0xFE && (b1 & 0xC0) == 0x80) return true; // fe80::/10 link-local
        }
        return false;
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

