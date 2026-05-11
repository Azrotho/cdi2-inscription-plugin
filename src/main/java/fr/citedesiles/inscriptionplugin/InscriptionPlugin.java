package fr.citedesiles.inscriptionplugin;

import org.bukkit.plugin.java.JavaPlugin;

public class InscriptionPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("InscriptionPlugin a été activé !");
    }

    @Override
    public void onDisable() {
        getLogger().info("InscriptionPlugin a été désactivé !");
    }
}