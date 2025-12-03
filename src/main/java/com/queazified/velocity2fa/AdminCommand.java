package com.queazified.velocity2fa;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class AdminCommand implements SimpleCommand {

    private final Velocity2FA plugin;

    public AdminCommand(Velocity2FA plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!source.hasPermission("velocity2fa.admin")) {
            plugin.sendPrefixed(source, Component.text("No permission.")
                .color(NamedTextColor.RED));
            return;
        }

        if (args.length == 0) {
            showAdminHelp(source);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "disable":
                if (args.length < 2) {
                    plugin.sendPrefixed(source, Component.text("Usage: /2fa-admin disable <player> [code]")
                        .color(NamedTextColor.RED));
                    return;
                }
                disablePlayerTwoFactor(source, args[1], args.length > 2 ? args[2] : null);
                break;
            case "force-disable":
                if (args.length < 2) {
                    plugin.sendPrefixed(source, Component.text("Usage: /2fa-admin force-disable <player>")
                        .color(NamedTextColor.RED));
                    return;
                }
                forceDisablePlayerTwoFactor(source, args[1]);
                break;
            case "status":
                if (args.length < 2) {
                    plugin.sendPrefixed(source, Component.text("Usage: /2fa-admin status <player>")
                        .color(NamedTextColor.RED));
                    return;
                }
                showPlayerStatus(source, args[1]);
                break;
            case "list":
                listTwoFactorUsers(source);
                break;
            case "stats":
                showStats(source);
                break;
            case "reload":
                reloadPlugin(source);
                break;
            default:
                showAdminHelp(source);
                break;
        }
    }

    private void showAdminHelp(CommandSource source) {
        plugin.sendPrefixed(source, Component.text("=== Kr2FA Admin Commands ===")
            .color(NamedTextColor.GOLD));
        plugin.sendPrefixed(source, Component.text("/2fa-admin disable <player> [code] - Disable player's 2FA (with verification)")
            .color(NamedTextColor.YELLOW));
        plugin.sendPrefixed(source, Component.text("/2fa-admin force-disable <player> - Force disable without verification")
            .color(NamedTextColor.YELLOW));
        plugin.sendPrefixed(source, Component.text("/2fa-admin status <player> - Check player's 2FA status")
            .color(NamedTextColor.YELLOW));
        plugin.sendPrefixed(source, Component.text("/2fa-admin list - List all players with 2FA enabled")
            .color(NamedTextColor.YELLOW));
        plugin.sendPrefixed(source, Component.text("/2fa-admin stats - Show 2FA usage statistics")
            .color(NamedTextColor.YELLOW));
        plugin.sendPrefixed(source, Component.text("/2fa-admin reload - Reload plugin configuration")
            .color(NamedTextColor.YELLOW));
        plugin.sendPrefixed(source, Component.text("==================================")
            .color(NamedTextColor.GOLD));
    }

    private void disablePlayerTwoFactor(CommandSource source, String playerName, String code) {
        Optional<Player> playerOpt = plugin.getServer().getPlayer(playerName);
        if (!playerOpt.isPresent()) {
            plugin.sendPrefixed(source, Component.text("Player not found or not online!")
                .color(NamedTextColor.RED));
            return;
        }

        Player target = playerOpt.get();
        UUID targetUuid = target.getUniqueId();

        if (!plugin.getTwoFactorManager().hasSecretKey(targetUuid)) {
            plugin.sendPrefixed(source, Component.text("Player " + playerName + " doesn't have 2FA enabled!")
                .color(NamedTextColor.RED));
            return;
        }

        if (code != null) {
            // Verify the code before disabling
            boolean valid = plugin.getTwoFactorManager().verifyCode(targetUuid, code);
            if (!valid) {
                plugin.sendPrefixed(source, Component.text("Invalid 2FA code! Cannot disable 2FA for " + playerName)
                    .color(NamedTextColor.RED));
                return;
            }
        }

        plugin.getTwoFactorManager().removeSecretKey(targetUuid);
        plugin.getAuthenticatedPlayers().remove(target.getUsername());
        plugin.getPendingAuthentication().remove(target.getUsername());

        plugin.sendPrefixed(source, Component.text("Successfully disabled 2FA for " + playerName)
            .color(NamedTextColor.GREEN));
        target.sendMessage(plugin.withPrefix(Component.text("Your 2FA has been disabled by an administrator.")
            .color(NamedTextColor.YELLOW)));

        plugin.getLogger().info("Admin {} disabled 2FA for player {}", 
            source instanceof Player ? ((Player) source).getUsername() : "Console", playerName);
    }

    private void forceDisablePlayerTwoFactor(CommandSource source, String playerName) {
        Optional<Player> playerOpt = plugin.getServer().getPlayer(playerName);
        UUID targetUuid;
        String targetName;

        if (playerOpt.isPresent()) {
            targetUuid = playerOpt.get().getUniqueId();
            targetName = playerOpt.get().getUsername();
        } else {
            // Try to find by stored data (offline removal)
            // Search secrets.json for UUID by name
            targetUuid = null;
            targetName = playerName;
            for (UUID uuid : plugin.getTwoFactorManager().getAllSecretUUIDs()) {
                if (uuid.toString().equalsIgnoreCase(playerName)) {
                    targetUuid = uuid;
                    break;
                }
            }
            if (targetUuid == null) {
                plugin.sendPrefixed(source, Component.text("Player must be online for force-disable. Use regular disable with verification instead.")
                    .color(NamedTextColor.RED));
                return;
            }
        }

        if (!plugin.getTwoFactorManager().hasSecretKey(targetUuid)) {
            plugin.sendPrefixed(source, Component.text("Player " + targetName + " doesn't have 2FA enabled!")
                .color(NamedTextColor.RED));
            return;
        }

        plugin.getTwoFactorManager().removeSecretKey(targetUuid);
        plugin.getAuthenticatedPlayers().remove(targetName);
        plugin.getPendingAuthentication().remove(targetName);

        plugin.sendPrefixed(source, Component.text("Force-disabled 2FA for " + targetName + " (no verification required)")
            .color(NamedTextColor.GREEN));
        
        if (playerOpt.isPresent()) {
            playerOpt.get().sendMessage(plugin.withPrefix(Component.text("Your 2FA has been force-disabled by an administrator.")
                .color(NamedTextColor.RED)));
        }

        plugin.getLogger().warn("Admin {} force-disabled 2FA for player {} without verification", 
            source instanceof Player ? ((Player) source).getUsername() : "Console", targetName);
    }

    private void showPlayerStatus(CommandSource source, String playerName) {
        Optional<Player> playerOpt = plugin.getServer().getPlayer(playerName);
        if (!playerOpt.isPresent()) {
            plugin.sendPrefixed(source, Component.text("Player not found or not online!")
                .color(NamedTextColor.RED));
            return;
        }

        Player target = playerOpt.get();
        boolean has2FA = plugin.getTwoFactorManager().hasSecretKey(target.getUniqueId());
        boolean isAuthenticated = plugin.getAuthenticatedPlayers().containsKey(target.getUsername());
        boolean isPending = plugin.getPendingAuthentication().contains(target.getUsername());
        boolean hasStaffPerm = hasStaffPermission(target);

        plugin.sendPrefixed(source, Component.text("=== 2FA Status for " + playerName + " ===")
            .color(NamedTextColor.GOLD));
        plugin.sendPrefixed(source, Component.text("2FA Enabled: " + (has2FA ? "✓ Yes" : "✗ No"))
            .color(has2FA ? NamedTextColor.GREEN : NamedTextColor.RED));
        plugin.sendPrefixed(source, Component.text("Staff Permission: " + (hasStaffPerm ? "✓ Yes" : "✗ No"))
            .color(hasStaffPerm ? NamedTextColor.GREEN : NamedTextColor.RED));
        
        if (has2FA) {
            plugin.sendPrefixed(source, Component.text("Authenticated This Session: " + (isAuthenticated ? "✓ Yes" : "✗ No"))
                .color(isAuthenticated ? NamedTextColor.GREEN : NamedTextColor.RED));
            plugin.sendPrefixed(source, Component.text("Pending Authentication: " + (isPending ? "⚠ Yes" : "✓ No"))
                .color(isPending ? NamedTextColor.YELLOW : NamedTextColor.GREEN));
        }
    }

    private void listTwoFactorUsers(CommandSource source) {
        List<String> onlineUsers = plugin.getServer().getAllPlayers().stream()
            .filter(player -> plugin.getTwoFactorManager().hasSecretKey(player.getUniqueId()))
            .map(Player::getUsername)
            .collect(Collectors.toList());

        int totalUsers = plugin.getTwoFactorManager().getTotalEnabledUsers();

        plugin.sendPrefixed(source, Component.text("=== Players with 2FA Enabled ===")
            .color(NamedTextColor.GOLD));
        plugin.sendPrefixed(source, Component.text("Total: " + totalUsers + " | Online: " + onlineUsers.size())
            .color(NamedTextColor.YELLOW));

        if (!onlineUsers.isEmpty()) {
            plugin.sendPrefixed(source, Component.text("Online users with 2FA:")
                .color(NamedTextColor.AQUA));
            for (String username : onlineUsers) {
                Long expiry = plugin.getAuthenticatedPlayers().get(username);
                boolean authenticated = expiry != null && expiry > System.currentTimeMillis();
                boolean pending = plugin.getPendingAuthentication().contains(username);
                
                String status = authenticated ? " §a✓" : (pending ? " §e⚠" : " §c✗");
                plugin.sendPrefixed(source, Component.text("- " + username + status)
                    .color(NamedTextColor.WHITE));
            }
        }
    }

    private void showStats(CommandSource source) {
        int totalEnabled = plugin.getTwoFactorManager().getTotalEnabledUsers();
        int currentlyAuthenticated = (int) plugin.getAuthenticatedPlayers().values().stream()
            .filter(expiry -> expiry > System.currentTimeMillis()).count();
        int pendingAuth = plugin.getPendingAuthentication().size();
        int totalOnlineStaff = (int) plugin.getServer().getAllPlayers().stream()
            .filter(this::hasStaffPermission)
            .count();

        plugin.sendPrefixed(source, Component.text("=== Kr2FA Statistics ===")
            .color(NamedTextColor.GOLD));
        plugin.sendPrefixed(source, Component.text("Total 2FA Enabled: " + totalEnabled)
            .color(NamedTextColor.YELLOW));
        plugin.sendPrefixed(source, Component.text("Currently Authenticated: " + currentlyAuthenticated)
            .color(NamedTextColor.GREEN));
        plugin.sendPrefixed(source, Component.text("Pending Authentication: " + pendingAuth)
            .color(NamedTextColor.YELLOW));
        plugin.sendPrefixed(source, Component.text("Total Online Staff: " + totalOnlineStaff)
            .color(NamedTextColor.AQUA));
    }

    private void reloadPlugin(CommandSource source) {
        plugin.getConfigManager().reload();
        plugin.sendPrefixed(source, Component.text("Velocity2FA configuration reloaded!")
            .color(NamedTextColor.GREEN));
    }

    private boolean hasStaffPermission(Player player) {
        return player.hasPermission("staff") || 
               player.hasPermission("moderator") || 
               player.hasPermission("admin") ||
               player.hasPermission("helper") ||
               player.hasPermission("velocity2fa.staff");
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        String[] args = invocation.arguments();
        
        if (args.length <= 1) {
            return CompletableFuture.completedFuture(
                List.of("disable", "force-disable", "status", "list", "stats", "reload"));
        }
        
        if (args.length == 2 && (args[0].equalsIgnoreCase("disable") || 
                                args[0].equalsIgnoreCase("force-disable") || 
                                args[0].equalsIgnoreCase("status"))) {
            return CompletableFuture.completedFuture(
                plugin.getServer().getAllPlayers().stream()
                    .map(Player::getUsername)
                    .collect(Collectors.toList()));
        }
        
        return CompletableFuture.completedFuture(List.of());
    }
}
