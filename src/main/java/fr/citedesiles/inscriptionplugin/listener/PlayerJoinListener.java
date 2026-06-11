package fr.citedesiles.inscriptionplugin.listener;

import fr.citedesiles.inscriptionplugin.config.PluginConfig;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;

/**
 * À la connexion, les joueurs non-OP sont téléportés au spawn
 * et passés en mode aventure avec vie/faim au max.
 */
public class PlayerJoinListener implements Listener {

    private final PluginConfig config;

    public PlayerJoinListener(PluginConfig config) {
        this.config = config;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Téléporter au spawn
        Location spawn = new Location(
                player.getWorld(),
                config.getSpawnX(),
                config.getSpawnY(),
                config.getSpawnZ(),
                config.getSpawnYaw(),
                config.getSpawnPitch()
        );
        player.teleport(spawn);

        // Mode aventure (empêche de casser/placer sans event)
        player.setGameMode(GameMode.ADVENTURE);

        // Vie et faim au max
        player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getValue());
        player.setFoodLevel(20);
        player.setSaturation(10f);

        // Retirer les effets de potion
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        // Message d'accueil
        player.sendMessage(config.getPrefix() + " " + config.getProtectionWelcome());

        if (player.isOp()) {
            return;
        }
    }
}
