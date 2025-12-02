package com.queazified.velocity2fa;
import java.util.Map;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.command.CommandManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Plugin(
    id = "velocity2fa",
    name = "Velocity2FA",
    version = "1.0.0",
    description = "Two-Factor Authentication for staff members",
    authors = {"Queazified"}
)
public class Velocity2FA {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    
    private TwoFactorManager twoFactorManager;
    private ConfigManager configManager;
    // Session cache: username -> expiry timestamp
    private final Map<String, Long> authenticatedPlayers = new ConcurrentHashMap<>();
    private final Set<String> pendingAuthentication = ConcurrentHashMap.newKeySet();

    @Inject
    public Velocity2FA(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Velocity2FA is starting up...");
        
        // Initialize managers
        this.configManager = new ConfigManager(dataDirectory);
        this.twoFactorManager = new TwoFactorManager(dataDirectory, logger);
        
        // Register commands
        CommandManager commandManager = server.getCommandManager();
        commandManager.register("2fa", new TwoFactorCommand(this));
        commandManager.register("2fa-admin", new AdminCommand(this));
        
        logger.info("Velocity2FA has been enabled successfully!");
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        Player player = event.getPlayer();
        
        try {
            // Check if player has staff permission and 2FA enabled
            if (hasStaffPermission(player) && twoFactorManager.hasSecretKey(player.getUniqueId())) {
                pendingAuthentication.add(player.getUsername());
                // Use scheduler to send messages after a longer delay to ensure connection is stable
                server.getScheduler().buildTask(this, () -> {
                    try {
                        if (player.isActive()) {
                            player.sendMessage(Component.text("=== 2FA AUTHENTICATION REQUIRED ===")
                                .color(NamedTextColor.RED));
                            player.sendMessage(Component.text("Please enter your 2FA code using: /2fa <code>")
                                .color(NamedTextColor.YELLOW));
                            player.sendMessage(Component.text("You cannot join servers until authenticated.")
                                .color(NamedTextColor.RED));
                        }
                    } catch (Exception msgEx) {
                        logger.warn("Failed to send 2FA login message to player {}: {}", player.getUsername(), msgEx.getMessage());
                    }
                }).delay(2, java.util.concurrent.TimeUnit.SECONDS).schedule();
            }
        } catch (Exception e) {
            logger.error("Error in PostLogin event for player {}: {}", player.getUsername(), e.getMessage(), e);
        }
    }

    @Subscribe
    public void onServerPreConnect(ServerPreConnectEvent event) {
        Player player = event.getPlayer();
        try {
            String limboServer = configManager.getConfig().limboServer;
            boolean isStaff = hasStaffPermission(player);
            boolean has2FA = twoFactorManager.hasSecretKey(player.getUniqueId());
            boolean isAuthenticated = authenticatedPlayers.containsKey(player.getUsername());
            String targetServer = event.getOriginalServer().getServerInfo().getName();

            // If staff, has 2FA, and not authenticated
            if (isStaff && has2FA && !isAuthenticated) {
                if (!targetServer.equalsIgnoreCase(limboServer)) {
                    event.setResult(ServerPreConnectEvent.ServerResult.denied());
                    try {
                        player.sendMessage(Component.text("You must authenticate with 2FA first! Use /2fa <code>")
                            .color(NamedTextColor.RED));
                        player.sendMessage(Component.text("You may only join the lobby/limbo server until authenticated.")
                            .color(NamedTextColor.YELLOW));
                    } catch (Exception msgEx) {
                        logger.warn("Failed to send 2FA message to player {}: {}", player.getUsername(), msgEx.getMessage());
                    }
                }
                // If joining limbo, allow
            }
        } catch (Exception e) {
            logger.error("Error in ServerPreConnect event for player {}: {}", player.getUsername(), e.getMessage(), e);
            // Don't block the connection if there's an error in our plugin
        }
    }

    @Subscribe
    public void onCommandExecute(CommandExecuteEvent event) {
        if (!(event.getCommandSource() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getCommandSource();
        
        try {
            boolean isStaff = hasStaffPermission(player);
            boolean has2FA = twoFactorManager.hasSecretKey(player.getUniqueId());
            boolean isAuthenticated = authenticatedPlayers.containsKey(player.getUsername());

            // If staff, has 2FA, and not authenticated, block commands except /2fa
            if (isStaff && has2FA && !isAuthenticated) {
                String command = event.getCommand().toLowerCase();
                
                // Allow only /2fa commands for authentication
                if (!command.startsWith("2fa")) {
                    event.setResult(CommandExecuteEvent.CommandResult.denied());
                    try {
                        player.sendMessage(Component.text("You must authenticate with 2FA first! Use /2fa <code>")
                            .color(NamedTextColor.RED));
                        player.sendMessage(Component.text("Commands are blocked until you complete 2FA authentication.")
                            .color(NamedTextColor.YELLOW));
                    } catch (Exception msgEx) {
                        logger.warn("Failed to send 2FA command block message to player {}: {}", player.getUsername(), msgEx.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error in CommandExecute event for player {}: {}", player.getUsername(), e.getMessage(), e);
            // Don't block the command if there's an error in our plugin
        }
    }

    private boolean hasStaffPermission(Player player) {
        // Check for any staff permission - you can customize this logic
        return player.hasPermission("staff") || 
               player.hasPermission("moderator") || 
               player.hasPermission("admin") ||
               player.hasPermission("helper") ||
               player.hasPermission("velocity2fa.staff");
    }

    // Getters for other classes
    public ProxyServer getServer() { return server; }
    public Logger getLogger() { return logger; }
    public Path getDataDirectory() { return dataDirectory; }
    public TwoFactorManager getTwoFactorManager() { return twoFactorManager; }
    public ConfigManager getConfigManager() { return configManager; }
    public Map<String, Long> getAuthenticatedPlayers() { return authenticatedPlayers; }
    public Set<String> getPendingAuthentication() { return pendingAuthentication; }
}
