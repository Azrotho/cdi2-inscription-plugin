package fr.citedesiles.inscriptionplugin.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Wrapper pour la configuration config.yml du plugin d'inscription.
 */
public class PluginConfig {

    private final JavaPlugin plugin;
    private FileConfiguration config;

    public PluginConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        this.config = plugin.getConfig();
    }

    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    // --- API ---

    public String getApiUrl() {
        return config.getString("api.url", "http://localhost:3000");
    }

    public String getApiToken() {
        return config.getString("api.token", "");
    }

    // --- Messages ---

    public String getPrefix() {
        return col(config.getString("messages.prefix", "&8[&bCDI2&8]&r"));
    }

    public String getLinkSuccess(String code) {
        return msg("messages.link.success").replace("%code%", code);
    }

    public String getLinkInstructions() {
        return msg("messages.link.instructions");
    }

    public String getLinkAlreadyLinked() {
        return msg("messages.link.already-linked");
    }

    public String getLinkError() {
        return msg("messages.link.error");
    }

    public String getLinkNetworkError() {
        return msg("messages.link.network-error");
    }

    public String getUnlinkConfirmNeeded() {
        return msg("messages.unlink.confirm-needed");
    }

    public String getUnlinkSuccess() {
        return msg("messages.unlink.success");
    }

    public String getUnlinkNotLinked() {
        return msg("messages.unlink.not-linked");
    }

    public String getUnlinkError() {
        return msg("messages.unlink.error");
    }

    public String getUnlinkNoPending() {
        return msg("messages.unlink.no-pending");
    }

    public String getNoPermission() {
        return msg("messages.no-permission");
    }

    public String getPlayerOnly() {
        return msg("messages.player-only");
    }

    // --- Spawn ---

    public double getSpawnX() {
        return config.getDouble("spawn.x", 0.0);
    }

    public double getSpawnY() {
        return config.getDouble("spawn.y", 90.0);
    }

    public double getSpawnZ() {
        return config.getDouble("spawn.z", 0.0);
    }

    public float getSpawnYaw() {
        return (float) config.getDouble("spawn.yaw", 0.0);
    }

    public float getSpawnPitch() {
        return (float) config.getDouble("spawn.pitch", 0.0);
    }

    // --- Protection ---

    public String getProtectionWelcome() {
        return msg("messages.protection.welcome");
    }

    // --- Interne ---

    private String msg(String path) {
        return col(config.getString(path, ""));
    }

    /**
     * Traduit les codes couleur & en vraies couleurs Minecraft.
     */
    private static String col(String s) {
        if (s == null) return "";
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', s);
    }
}
