package fr.citedesiles.inscriptionplugin.command;

import fr.citedesiles.coreplugin.CoreCDI;
import fr.citedesiles.inscriptionplugin.config.PluginConfig;
import fr.citedesiles.inscriptionplugin.util.MessageUtil;
import fr.citedesiles.inscriptionplugin.util.TeamDisplayManager;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Commande /unlink — se délier de son compte Discord.
 *
 * Fonctionne en deux étapes :
 * 1. /unlink → demande de confirmation
 * 2. /unlink confirm → exécute la suppression
 */
public class UnlinkCommand implements CommandExecutor {

    private final CoreCDI api;
    private final PluginConfig config;

    // Demandes de confirmation en attente : UUID → expiration timestamp
    private final Map<UUID, Long> pendingConfirmations = new ConcurrentHashMap<>();
    private static final long CONFIRM_TIMEOUT_MS = 30_000; // 30 secondes

    public UnlinkCommand(CoreCDI api, PluginConfig config) {
        this.api = api;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.send(sender, config.getPlayerOnly());
            return true;
        }

        UUID uuid = player.getUniqueId();
        String prefix = config.getPrefix();

        // Pré-vérification : empêcher le déliage si le joueur est dans une équipe
        try {
            fr.citedesiles.coreplugin.Player p = api.getPlayer(uuid.toString());
            if (p.team() != -1) {
                MessageUtil.sendPrefixed(player, prefix, config.getUnlinkInTeam());
                return true;
            }
        } catch (CoreCDI.ApiException e) {
            if (e.getStatusCode() == 404) {
                MessageUtil.sendPrefixed(player, prefix, config.getUnlinkNotLinked());
                return true;
            }
            // API injoignable : on bloque par sécurité (impossible de vérifier l'équipe)
            MessageUtil.sendPrefixed(player, prefix, config.getUnlinkError());
            return true;
        }

        // /unlink confirm
        if (args.length > 0 && args[0].equalsIgnoreCase("confirm")) {
            Long pending = pendingConfirmations.remove(uuid);
            if (pending == null || System.currentTimeMillis() > pending) {
                MessageUtil.sendPrefixed(player, prefix, config.getUnlinkNoPending());
                return true;
            }

            try {
                api.deletePlayer(uuid.toString());
                MessageUtil.sendPrefixed(player, prefix, config.getUnlinkSuccess());
                TeamDisplayManager.updateDisplay(player, api);
            } catch (CoreCDI.ApiException e) {
                if (e.getStatusCode() == 404) {
                    MessageUtil.sendPrefixed(player, prefix, config.getUnlinkNotLinked());
                } else {
                    MessageUtil.sendPrefixed(player, prefix, config.getUnlinkError());
                }
            }
            return true;
        }

        // /unlink seul → demande de confirmation
        pendingConfirmations.put(uuid, System.currentTimeMillis() + CONFIRM_TIMEOUT_MS);
        MessageUtil.sendPrefixed(player, prefix, config.getUnlinkConfirmNeeded());
        return true;
    }
}
