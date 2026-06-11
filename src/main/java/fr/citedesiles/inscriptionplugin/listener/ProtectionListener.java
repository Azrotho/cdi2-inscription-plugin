package fr.citedesiles.inscriptionplugin.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.*;

/**
 * Bloque toutes les interactions des joueurs non-OP :
 * build, dégâts, faim, interact, drop, craft, buckets, etc.
 */
public class ProtectionListener implements Listener {

    private static boolean isProtected(Player player) {
        return player != null && !player.isOp();
    }

    // --- Blocks ---

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (isProtected(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (isProtected(e.getPlayer())) e.setCancelled(true);
    }

    // --- Interactions ---

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (isProtected(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent e) {
        if (isProtected(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent e) {
        if (isProtected(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler
    public void onShearEntity(PlayerShearEntityEvent e) {
        if (isProtected(e.getPlayer())) e.setCancelled(true);
    }

    // --- Dégâts ---

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player player && isProtected(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player player && isProtected(player)) {
            e.setCancelled(true);
        }
    }

    // --- Faim ---

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent e) {
        if (e.getEntity() instanceof Player player && isProtected(player)) {
            e.setCancelled(true);
            player.setFoodLevel(20);
        }
    }

    // --- Items ---

    @EventHandler
    public void onPickup(EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player player && isProtected(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (isProtected(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler
    public void onSwapHands(PlayerSwapHandItemsEvent e) {
        if (isProtected(e.getPlayer())) e.setCancelled(true);
    }

    // --- Buckets ---

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        if (isProtected(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent e) {
        if (isProtected(e.getPlayer())) e.setCancelled(true);
    }

    // --- Craft ---

    @EventHandler
    public void onCraft(CraftItemEvent e) {
        if (e.getWhoClicked() instanceof Player player && isProtected(player)) {
            e.setCancelled(true);
        }
    }
}
