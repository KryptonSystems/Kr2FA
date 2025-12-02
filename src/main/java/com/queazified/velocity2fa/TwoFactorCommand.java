package com.queazified.velocity2fa;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TwoFactorCommand implements SimpleCommand {

    private final Velocity2FA plugin;

    public TwoFactorCommand(Velocity2FA plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!(source instanceof Player)) {
            source.sendMessage(Component.text("Only players can use this command.")
                .color(NamedTextColor.RED));
            return;
        }

        Player player = (Player) source;

        if (args.length == 0) {
            showHelp(player);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "setup":
                setupTwoFactor(player);
                break;
            case "verify":
            case "auth":
                if (args.length < 2) {
                    player.sendMessage(Component.text("Usage: /2fa verify <code>")
                        .color(NamedTextColor.RED));
                    return;
                }
                verifyCode(player, args[1]);
                break;
            case "disable":
                disableTwoFactor(player);
                break;
            case "status":
                showStatus(player);
                break;
            default:
                // Assume it's a verification code
                verifyCode(player, args[0]);
                break;
        }
    }

    private void showHelp(Player player) {
        player.sendMessage(Component.text("---------- 2FA Commands ----------")
            .color(NamedTextColor.GOLD));
        player.sendMessage(Component.text("/2fa setup - Set up 2FA for your account")
            .color(NamedTextColor.YELLOW));
        player.sendMessage(Component.text("/2fa <code> - Verify your 2FA code")
            .color(NamedTextColor.YELLOW));
        player.sendMessage(Component.text("/2fa verify <code> - Verify your 2FA code")
            .color(NamedTextColor.YELLOW));
        player.sendMessage(Component.text("/2fa disable - Disable 2FA (requires current code)")
            .color(NamedTextColor.YELLOW));
        player.sendMessage(Component.text("/2fa status - Check your 2FA status")
            .color(NamedTextColor.YELLOW));
        player.sendMessage(Component.text("----------    Kr2FA     ----------")
            .color(NamedTextColor.GOLD));
    }

    private void setupTwoFactor(Player player) {
        // Check if player has staff permission
        if (!hasStaffPermission(player)) {
            try {
                player.sendMessage(Component.text("You don't have permission to use 2FA!")
                    .color(NamedTextColor.RED));
            } catch (Exception e) {
                // Ignore system chat errors
            }
            return;
        }

        if (plugin.getTwoFactorManager().hasSecretKey(player.getUniqueId())) {
            try {
                player.sendMessage(Component.text("You already have 2FA enabled! Use /2fa disable to remove it.")
                    .color(NamedTextColor.RED));
            } catch (Exception e) {
                // Ignore system chat errors
            }
            return;
        }

        String secretKey = plugin.getTwoFactorManager().generateSecretKey(player.getUniqueId());
        String qrUrl = plugin.getTwoFactorManager().generateQRUrl(player.getUsername(), secretKey);

        if (player.isActive()) {
            try {
                player.sendMessage(Component.text("---------- 2FA Setup ----------")
                    .color(NamedTextColor.GOLD));

                player.sendMessage(Component.text("1. Install an authenticator app (Google Authenticator, Microsoft Authenticator, etc.)")
                    .color(NamedTextColor.YELLOW));

                player.sendMessage(Component.text("2. Enter the secret manually into your Authenticator:")
                    .color(NamedTextColor.YELLOW));

                player.sendMessage(Component.text("Secret Key: " + secretKey)
                    .color(NamedTextColor.GREEN));

                // Convert the otpauth URI to an HTTPS QR image URL so clicking opens in a browser.
                try {
                    String qrImageUrl = "https://api.qrserver.com/v1/create-qr-code/?data=" +
                        java.net.URLEncoder.encode(qrUrl, "UTF-8") + "&size=300x300";

                    Component qrComponent = Component.text("QR Code: ")
                        .color(NamedTextColor.AQUA)
                        .append(Component.text("Click here")
                            .color(NamedTextColor.AQUA)
                            .clickEvent(ClickEvent.openUrl(qrImageUrl)));
                    player.sendMessage(qrComponent);
                } catch (Exception e) {
                    // Fallback: send the raw otpauth URI as plain text (manual entry still possible)
                    player.sendMessage(Component.text("QR: " + qrUrl)
                        .color(NamedTextColor.AQUA));
                }

                player.sendMessage(Component.text("3. After setup, use /2fa <code> to verify and complete setup")
                    .color(NamedTextColor.YELLOW));
            } catch (Exception e) {
                // Ignore system chat errors
            }
        }
    }

    private void verifyCode(Player player, String code) {
        if (!plugin.getTwoFactorManager().hasSecretKey(player.getUniqueId())) {
            player.sendMessage(Component.text("You don't have 2FA set up! Use /2fa setup first.")
                .color(NamedTextColor.RED));
            return;
        }

        boolean valid = plugin.getTwoFactorManager().verifyCode(player.getUniqueId(), code);
        
        if (valid) {
            // Session expiry: 12h (can be made configurable)
            long expiry = System.currentTimeMillis() + 12 * 60 * 60 * 1000L;
            plugin.getAuthenticatedPlayers().put(player.getUsername(), expiry);
            plugin.getPendingAuthentication().remove(player.getUsername());
            
            player.sendMessage(Component.text("2FA verification successful! You can now access servers.")
                .color(NamedTextColor.GREEN));
            
            plugin.getLogger().info("Player {} successfully authenticated with 2FA", player.getUsername());
        } else {
            player.sendMessage(Component.text("Invalid 2FA code! Please try again.")
                .color(NamedTextColor.RED));
            
            plugin.getLogger().warn("Player {} failed 2FA authentication", player.getUsername());
        }
    }

    private void disableTwoFactor(Player player) {
        if (!plugin.getTwoFactorManager().hasSecretKey(player.getUniqueId())) {
            player.sendMessage(Component.text("You don't have 2FA enabled.")
                .color(NamedTextColor.RED));
            return;
        }

        player.sendMessage(Component.text("To disable 2FA, please provide your current 2FA code:")
            .color(NamedTextColor.YELLOW));
        player.sendMessage(Component.text("Use: /2fa-admin disable " + player.getUsername() + " <code>")
            .color(NamedTextColor.YELLOW));
    }

    private void showStatus(Player player) {
        boolean has2FA = plugin.getTwoFactorManager().hasSecretKey(player.getUniqueId());
        Long expiry = plugin.getAuthenticatedPlayers().get(player.getUsername());
        boolean isAuthenticated = expiry != null && expiry > System.currentTimeMillis();
        boolean isPending = plugin.getPendingAuthentication().contains(player.getUsername());

        player.sendMessage(Component.text("=== Your 2FA Status ===")
            .color(NamedTextColor.GOLD));
        player.sendMessage(Component.text("2FA Enabled: " + (has2FA ? "✓ Yes" : "✗ No"))
            .color(has2FA ? NamedTextColor.GREEN : NamedTextColor.RED));
        player.sendMessage(Component.text("Staff Permission: " + (hasStaffPermission(player) ? "✓ Yes" : "✗ No"))
            .color(hasStaffPermission(player) ? NamedTextColor.GREEN : NamedTextColor.RED));
        
        if (has2FA) {
            player.sendMessage(Component.text("Authenticated This Session: " + (isAuthenticated ? "✓ Yes" : "✗ No"))
                .color(isAuthenticated ? NamedTextColor.GREEN : NamedTextColor.RED));
            player.sendMessage(Component.text("Pending Authentication: " + (isPending ? "⚠ Yes" : "✓ No"))
                .color(isPending ? NamedTextColor.YELLOW : NamedTextColor.GREEN));
            if (isAuthenticated && expiry != null) {
                long minsLeft = (expiry - System.currentTimeMillis()) / 60000L;
                player.sendMessage(Component.text("Session expires in: " + minsLeft + " min")
                    .color(NamedTextColor.AQUA));
            }
        }
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
        return CompletableFuture.completedFuture(List.of("setup", "verify", "disable", "status"));
    }
}
