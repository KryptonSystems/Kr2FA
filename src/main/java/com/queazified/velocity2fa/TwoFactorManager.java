package com.queazified.velocity2fa;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class TwoFactorManager {
    private final Path storagePath;
    private final Logger logger;
    private final Map<UUID, String> secretKeys = new HashMap<>();
    private final File secretsFile;
    private final GoogleAuthenticator authenticator = new GoogleAuthenticator();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public TwoFactorManager(Path storagePath, Logger logger) {
        this.storagePath = storagePath;
        this.logger = logger;
        this.secretsFile = storagePath.resolve("secrets.json").toFile();
        
        // Ensure directory exists
        try {
            if (!storagePath.toFile().exists()) {
                storagePath.toFile().mkdirs();
            }
        } catch (Exception e) {
            logger.error("Failed to create storage directory: {}", e.getMessage());
        }
        
        loadSecrets();
    }

    public Set<UUID> getAllSecretUUIDs() {
        return secretKeys.keySet();
    }

    public boolean hasSecretKey(UUID uuid) {
        return secretKeys.containsKey(uuid);
    }

    public void removeSecretKey(UUID uuid) {
        secretKeys.remove(uuid);
        saveSecrets();
    }

    public boolean verifyCode(UUID uuid, String code) {
        String secret = secretKeys.get(uuid);
        if (secret == null) return false;
        
        try {
            int codeInt = Integer.parseInt(code);
            return authenticator.authorize(secret, codeInt);
        } catch (NumberFormatException e) {
            logger.debug("Invalid code format from player {}: {}", uuid, code);
            return false;
        } catch (Exception e) {
            logger.error("Error verifying 2FA code for {}: {}", uuid, e.getMessage());
            return false;
        }
    }

    public String generateSecretKey(UUID uuid) {
        try {
            GoogleAuthenticatorKey key = authenticator.createCredentials();
            String secret = key.getKey();
            secretKeys.put(uuid, secret);
            saveSecrets();
            return secret;
        } catch (Exception e) {
            logger.error("Failed to generate secret key for {}: {}", uuid, e.getMessage());
            throw new RuntimeException("Failed to generate 2FA secret", e);
        }
    }

    public String generateQRUrl(String username, String secret) {
        try {
            String issuer = "Kr2FA";
            return "otpauth://totp/" + java.net.URLEncoder.encode(issuer + ":" + username, "UTF-8") + 
                   "?secret=" + secret + "&issuer=" + java.net.URLEncoder.encode(issuer, "UTF-8");
        } catch (Exception e) {
            logger.error("Failed to generate QR URL for {}: {}", username, e.getMessage());
            return "otpauth://totp/Kr2FA:" + username + "?secret=" + secret + "&issuer=Kr2FA";
        }
    }

    public int getTotalEnabledUsers() {
        return secretKeys.size();
    }

    private void loadSecrets() {
        if (!secretsFile.exists()) {
            logger.info("No secrets file found, starting with empty 2FA database");
            return;
        }
        
        try (java.io.FileReader reader = new java.io.FileReader(secretsFile)) {
            TypeToken<Map<String, String>> typeToken = new TypeToken<Map<String, String>>() {};
            Map<String, String> map = gson.fromJson(reader, typeToken.getType());
            
            if (map != null) {
                secretKeys.clear();
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    try {
                        UUID uuid = UUID.fromString(entry.getKey());
                        secretKeys.put(uuid, entry.getValue());
                    } catch (IllegalArgumentException e) {
                        logger.warn("Invalid UUID in secrets file: {}", entry.getKey());
                    }
                }
                logger.info("Loaded {} 2FA secrets", secretKeys.size());
            }
        } catch (Exception e) {
            logger.error("Failed to load secrets.json: {}", e.getMessage());
        }
    }

    private void saveSecrets() {
        try {
            // Ensure parent directory exists
            if (!secretsFile.getParentFile().exists()) {
                secretsFile.getParentFile().mkdirs();
            }
            
            try (java.io.FileWriter writer = new java.io.FileWriter(secretsFile)) {
                Map<String, String> map = new HashMap<>();
                for (Map.Entry<UUID, String> entry : secretKeys.entrySet()) {
                    map.put(entry.getKey().toString(), entry.getValue());
                }
                gson.toJson(map, writer);
                logger.debug("Saved {} 2FA secrets to file", secretKeys.size());
            }
        } catch (Exception e) {
            logger.error("Failed to save secrets.json: {}", e.getMessage());
        }
    }
}