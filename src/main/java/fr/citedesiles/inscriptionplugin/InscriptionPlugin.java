package fr.citedesiles.inscriptionplugin;

import fr.citedesiles.coreplugin.CoreCDI;
import fr.citedesiles.inscriptionplugin.command.LinkCommand;
import fr.citedesiles.inscriptionplugin.command.UnlinkCommand;
import fr.citedesiles.inscriptionplugin.config.PluginConfig;
import fr.citedesiles.inscriptionplugin.listener.ChatListener;
import fr.citedesiles.inscriptionplugin.listener.PlayerJoinListener;
import fr.citedesiles.inscriptionplugin.listener.ProtectionListener;
import fr.citedesiles.inscriptionplugin.util.TeamDisplayManager;

import org.bukkit.plugin.java.JavaPlugin;

public class InscriptionPlugin extends JavaPlugin {

    private PluginConfig config;
    private CoreCDI api;

    @Override
    public void onEnable() {
        // Charger la configuration
        config = new PluginConfig(this);

        // Enregistrer les listeners de protection (indépendants de l'API)
        getServer().getPluginManager().registerEvents(new ProtectionListener(), this);

        String apiUrl = config.getApiUrl();
        String apiToken = config.getApiToken();

        if (!apiToken.isEmpty()) {
            // Initialiser le client API
            api = new CoreCDI(apiUrl, apiToken);

            // Tester la connexion à l'API
            try {
                if (api.ping()) {
                    getLogger().info("Connecté à l'API CDI2 : " + apiUrl);
                    TeamDisplayManager.orderTeamsInScoreboard(api, this);
                }
            } catch (CoreCDI.ApiException e) {
                getLogger().warning("Impossible de contacter l'API CDI2 : " + e.getMessage());
                getLogger().warning("Les commandes /link et /unlink risquent de ne pas fonctionner.");
            }
        } else {
            getLogger().warning("Le token API est vide ! Configure 'api.token' dans config.yml");
            getLogger().warning("Les commandes /link et /unlink ne fonctionneront pas.");
        }

        // Enregistrer le listener de connexion avec l'API
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(api, config), this);
        getServer().getPluginManager().registerEvents(new ChatListener(), this);

        if (api != null) {
            // Enregistrer les commandes
            getCommand("link").setExecutor(new LinkCommand(api, config));
            getCommand("unlink").setExecutor(new UnlinkCommand(api, config));
        }

        getLogger().info("InscriptionPlugin activé !");
    }

    @Override
    public void onDisable() {
        getLogger().info("InscriptionPlugin désactivé !");
    }

    public PluginConfig getPluginConfig() {
        return config;
    }

    public CoreCDI getApi() {
        return api;
    }
}