package com.queazified.velocity2fa;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class ConfigManager {
    
    private final Path dataDirectory;
    private final File configFile;
    private final Gson gson;
    private Config config;

    public ConfigManager(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
        this.configFile = dataDirectory.resolve("config.json").toFile();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        
        // Create data directory if it doesn't exist
        try {
            if (!dataDirectory.toFile().exists()) {
                dataDirectory.toFile().mkdirs();
            }
        } catch (Exception e) {
            System.err.println("Failed to create data directory: " + e.getMessage());
        }
        
        loadConfig();
    }

    /**
     * Load configuration from file
     */
    private void loadConfig() {
        if (!configFile.exists()) {
            // Create default config
            config = new Config();
            saveConfig();
            return;
        }

        try (FileReader reader = new FileReader(configFile)) {
            config = gson.fromJson(reader, Config.class);
            if (config == null) {
                config = new Config();
                saveConfig();
            }
        } catch (IOException e) {
            System.err.println("Failed to load config, using defaults: " + e.getMessage());
            config = new Config();
        }
    }

    /**
     * Save configuration to file
     */
    private void saveConfig() {
        try (FileWriter writer = new FileWriter(configFile)) {
            gson.toJson(config, writer);
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());
        }
    }

    /**
     * Get current configuration
     */
    public Config getConfig() {
        return config;
    }

    /**
     * Reload configuration from file
     */
    public void reload() {
        loadConfig();
    }

    /**
     * Configuration class
     */
    public static class Config {
    public String serverName = "Proxy-01";
    public String limboServer = "Hub"; // Server name for unauthenticated staff
        public String messagePrefix = "[Kr2FA]";
        public String issuerName = "Kr2FA";
        public boolean enforceFor2FA = true;
        public boolean requireCodeOnJoin = true;
        public List<String> staffPermissions = Arrays.asList(
            "velocity2fa.staff"
        );
        public int codeWindow = 3; // Number of 30-second windows to allow
        public boolean logAuthAttempts = true;
        public boolean kickOnFailedAuth = false;
        public int maxAuthAttempts = 3;
        public Messages messages = new Messages();

        public static class Messages {
            public String authRequired = "§c=== 2FA AUTHENTICATION REQUIRED ===";
            public String enterCode = "§ePlease enter your 2FA code using: /2fa <code>";
            public String cannotJoinServers = "§cYou cannot join servers until authenticated.";
            public String authSuccess = "§a✓ 2FA verification successful! You can now access servers.";
            public String authFailed = "§c✗ Invalid 2FA code! Please try again.";
            public String noPermission = "§cYou don't have permission to use 2FA!";
            public String alreadyEnabled = "§cYou already have 2FA enabled! Use /2fa disable to remove it.";
            public String not2FA = "§cYou don't have 2FA set up! Use /2fa setup first.";
            public String setupInstructions = "§e1. Install an authenticator app (Google Authenticator, Authy, etc.)";
            public String scanQR = "§e2. Scan this QR code or enter the secret manually:";
            public String completeSetup = "§e3. After setup, use /2fa <code> to verify and complete setup";
        }
    }
}
