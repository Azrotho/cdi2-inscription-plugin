package fr.citedesiles.inscriptionplugin.listener;

import fr.citedesiles.coreplugin.CoreCDI;
import fr.citedesiles.inscriptionplugin.InscriptionPlugin;
import fr.citedesiles.inscriptionplugin.config.PluginConfig;
import fr.citedesiles.inscriptionplugin.util.TeamDisplayManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

/**
 * À la connexion, les joueurs non-OP sont téléportés au spawn
 * et passés en mode aventure avec vie/faim au max.
 * Affiche également le statut de leur inscription (liaison Discord, équipe complète).
 */
public class PlayerJoinListener implements Listener {

    private final CoreCDI api;
    private final PluginConfig config;

    public PlayerJoinListener(CoreCDI api, PluginConfig config) {
        this.api = api;
        this.config = config;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Annuler le message de join par défaut (pour le remplacer plus tard de manière asynchrone et propre)
        event.joinMessage(null);

        InscriptionPlugin plugin = JavaPlugin.getPlugin(InscriptionPlugin.class);

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

        // Mettre à jour l'affichage en jeu (Tab & Nametag)
        TeamDisplayManager.updateDisplay(player, api);

        // Message d'accueil dynamique selon le statut d'inscription
        Component prefix = LegacyComponentSerializer.legacySection().deserialize(config.getPrefix());

        if (api != null) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                Component joinMsg = null;
                try {
                    fr.citedesiles.coreplugin.Player apiPlayer = api.getPlayer(player.getUniqueId().toString());
                    
                    if (apiPlayer.team() != -1) {
                        try {
                            fr.citedesiles.coreplugin.Team team = api.getTeam(apiPlayer.team());
                            TextColor teamColor = TextColor.fromHexString(team.color());
                            Component tagComp = Component.text("[" + team.tag() + "] ", teamColor);
                            Component nameComp = Component.text(player.getName(), teamColor);
                            if (team.staff() == 1) {
                                tagComp = tagComp.decorate(net.kyori.adventure.text.format.TextDecoration.BOLD);
                                nameComp = nameComp.decorate(net.kyori.adventure.text.format.TextDecoration.BOLD);
                            }
                            joinMsg = Component.text("(+) ", NamedTextColor.GREEN)
                                    .append(tagComp)
                                    .append(nameComp);
                        } catch (Exception e) {
                            // Si la team n'est pas trouvable, fallback joinMsg sans tag
                        }
                    }

                    if (joinMsg == null) {
                        joinMsg = Component.text("(+) ", NamedTextColor.GREEN)
                                .append(Component.text(player.getName(), NamedTextColor.YELLOW));
                    }
                    
                    final Component finalJoinMsg = joinMsg;
                    // Diffuser le message de join sur le thread principal
                    Bukkit.getScheduler().runTask(plugin, () -> Bukkit.broadcast(finalJoinMsg));

                    // Le joueur est lié
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        player.sendMessage(prefix.append(Component.text(" Ton compte est bien lié à ton profil Discord !", NamedTextColor.GREEN)));
                        
                        if (apiPlayer.team() == -1) {
                            player.sendMessage(prefix.append(Component.text(" Tu ne fais partie d'aucune équipe. Rejoins-en une ou crées-en une sur Discord !", NamedTextColor.YELLOW)));
                        } else {
                            // Charger le statut de l'équipe
                            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                                try {
                                    fr.citedesiles.coreplugin.Team team = api.getTeam(apiPlayer.team());
                                    java.util.List<fr.citedesiles.coreplugin.Player> members = team.players(api);
                                    
                                    Bukkit.getScheduler().runTask(plugin, () -> {
                                        if (team.staff() != 1 && members.size() < 4) {
                                            Component msgIncomplete = prefix
                                                    .append(Component.text(" Ton équipe ", NamedTextColor.YELLOW))
                                                    .append(Component.text(team.name(), NamedTextColor.GOLD))
                                                    .append(Component.text(" n'est pas complète (", NamedTextColor.YELLOW))
                                                    .append(Component.text(members.size() + "/4", NamedTextColor.GOLD))
                                                    .append(Component.text(" membres). Invite tes amis à te rejoindre sur Discord !", NamedTextColor.YELLOW));
                                            player.sendMessage(msgIncomplete);
                                        } else {
                                            if (team.verification() == 0) {
                                                Component msgNotVerified = prefix
                                                        .append(Component.text(" Ton équipe ", NamedTextColor.YELLOW))
                                                        .append(Component.text(team.name(), NamedTextColor.GOLD))
                                                        .append(Component.text(" est complète (", NamedTextColor.YELLOW))
                                                        .append(Component.text(members.size() + "/4", NamedTextColor.GOLD))
                                                        .append(Component.text(" membres) mais elle n'est pas vérifiée. Il faut aller ouvrir un ticket sur le discord du cripieclub pour faire vérifier ton équipe auprès d'un modérateur !", NamedTextColor.RED));
                                                player.sendMessage(msgNotVerified);
                                            } else {
                                                Component msgComplete = prefix
                                                        .append(Component.text(" Ton équipe ", NamedTextColor.GREEN))
                                                        .append(Component.text(team.name(), NamedTextColor.GOLD))
                                                        .append(Component.text(" est complète (", NamedTextColor.GREEN))
                                                        .append(Component.text(members.size() + "/4", NamedTextColor.GOLD))
                                                        .append(Component.text(" membres). Tout est bon pour participer !", NamedTextColor.GREEN));
                                                player.sendMessage(msgComplete);
                                            }
                                        }
                                    });
                                } catch (Exception e) {
                                    // Fallback silencieux si l'équipe n'est pas trouvée
                                }
                            });
                        }
                    });
                } catch (fr.citedesiles.coreplugin.CoreCDI.ApiException e) {
                    // Si pas lié ou erreur API : diffuser le joinMsg sans tag
                    joinMsg = Component.text("(+) ", NamedTextColor.GREEN)
                            .append(Component.text(player.getName(), NamedTextColor.YELLOW));
                    final Component finalJoinMsg = joinMsg;
                    Bukkit.getScheduler().runTask(plugin, () -> Bukkit.broadcast(finalJoinMsg));

                    if (e.getStatusCode() == 404) {
                        // Pas lié : message de bienvenue standard
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            Component welcome = LegacyComponentSerializer.legacySection().deserialize(config.getProtectionWelcome());
                            player.sendMessage(prefix.append(Component.text(" ")).append(welcome));
                        });
                    }
                } catch (Exception e) {
                    // Erreur réseau ou autre
                    joinMsg = Component.text("(+) ", NamedTextColor.GREEN)
                            .append(Component.text(player.getName(), NamedTextColor.YELLOW));
                    final Component finalJoinMsg = joinMsg;
                    Bukkit.getScheduler().runTask(plugin, () -> Bukkit.broadcast(finalJoinMsg));
                }
            });
        } else {
            Component joinMsg = Component.text("(+) ", NamedTextColor.GREEN)
                    .append(Component.text(player.getName(), NamedTextColor.YELLOW));
            Bukkit.broadcast(joinMsg);

            Component welcome = LegacyComponentSerializer.legacySection().deserialize(config.getProtectionWelcome());
            player.sendMessage(prefix.append(Component.text(" ")).append(welcome));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Message de déconnexion personnalisé : (-) [TAG] Joueur
        TeamDisplayManager.CachedTeam team = TeamDisplayManager.getCachedTeam(player.getUniqueId());
        
        Component quitPrefix = Component.text("(-) ", NamedTextColor.RED);
        Component tagComp = Component.empty();
        Component nameComp = Component.text(player.getName(), NamedTextColor.YELLOW);
        
        if (team != null) {
            TextColor teamColor = TextColor.fromHexString(team.color);
            tagComp = Component.text("[" + team.tag + "] ", teamColor);
            nameComp = Component.text(player.getName(), teamColor);
            if (team.isStaff) {
                tagComp = tagComp.decorate(net.kyori.adventure.text.format.TextDecoration.BOLD);
                nameComp = nameComp.decorate(net.kyori.adventure.text.format.TextDecoration.BOLD);
            }
        }
        
        Component quitMsg = quitPrefix.append(tagComp).append(nameComp);
        event.quitMessage(quitMsg);
        
        // Nettoyer du cache
        TeamDisplayManager.removeCachedTeam(player.getUniqueId());
    }
}
