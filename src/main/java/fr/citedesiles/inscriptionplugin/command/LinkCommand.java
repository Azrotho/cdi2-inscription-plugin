package fr.citedesiles.inscriptionplugin.command;

import fr.citedesiles.coreplugin.CoreCDI;
import fr.citedesiles.inscriptionplugin.config.PluginConfig;
import fr.citedesiles.inscriptionplugin.util.MessageUtil;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Commande /link — obtient un code de vérification pour lier son compte Discord.
 * Le code est valable 10 minutes. Si un code existe déjà, le même est renvoyé.
 */
public class LinkCommand implements CommandExecutor {

    private final CoreCDI api;
    private final PluginConfig config;

    public LinkCommand(CoreCDI api, PluginConfig config) {
        this.api = api;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Vérifier que c'est un joueur
        if (!(sender instanceof Player player)) {
            MessageUtil.send(sender, config.getPlayerOnly());
            return true;
        }

        // Appeler l'API
        try {
            String code = api.requestVerification(player.getUniqueId().toString(), player.getName());
            
            // Envoyer le message avec le code cliquable (copie dans le presse-papiers)
            String prefixStr = config.getPrefix();
            String successMsg = config.getLinkSuccess(code);
            int index = successMsg.indexOf(code);
            
            if (index != -1) {
                String beforeCode = successMsg.substring(0, index);
                String afterCode = successMsg.substring(index + code.length());

                Component prefixComp = LegacyComponentSerializer.legacySection().deserialize(prefixStr + " ");
                Component beforeComp = LegacyComponentSerializer.legacySection().deserialize(beforeCode);
                Component codeComp = LegacyComponentSerializer.legacySection().deserialize(code)
                        .clickEvent(ClickEvent.copyToClipboard(code))
                        .hoverEvent(HoverEvent.showText(Component.text("§aClique pour copier")));
                Component afterComp = LegacyComponentSerializer.legacySection().deserialize(afterCode);

                Component finalMessage = prefixComp.append(beforeComp).append(codeComp).append(afterComp);
                player.sendMessage(finalMessage);
            } else {
                MessageUtil.sendPrefixed(player, config.getPrefix(), successMsg);
            }
            
            MessageUtil.sendPrefixed(player, config.getPrefix(), config.getLinkInstructions());
        } catch (CoreCDI.ApiException e) {
            if (e.getStatusCode() == 409) {
                // Déjà lié
                MessageUtil.sendPrefixed(player, config.getPrefix(), config.getLinkAlreadyLinked());
            } else if (e.getStatusCode() == 0) {
                // Erreur réseau
                MessageUtil.sendPrefixed(player, config.getPrefix(), config.getLinkNetworkError());
            } else {
                MessageUtil.sendPrefixed(player, config.getPrefix(), config.getLinkError());
            }
        }

        return true;
    }
}
