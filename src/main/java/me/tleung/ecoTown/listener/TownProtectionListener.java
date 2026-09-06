package me.tleung.ecoTown.listener;

import me.tleung.ecoTown.data.Town;
import me.tleung.ecoTown.manager.TownManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class TownProtectionListener implements Listener {
    private final TownManager townManager;

    public TownProtectionListener(TownManager townManager) {
        this.townManager = townManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Town town = townManager.getTownAt(event.getBlock().getChunk());
        if (town != null) {
            Player player = event.getPlayer();
            if (!town.isMember(player.getUniqueId())) {
                event.setCancelled(true);
                player.sendMessage("§c❌ 這是「" + town.getName() + "」的領地，你沒有權限破壞方塊！");
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Town town = townManager.getTownAt(event.getBlock().getChunk());
        if (town != null) {
            Player player = event.getPlayer();
            if (!town.isMember(player.getUniqueId())) {
                event.setCancelled(true);
                player.sendMessage("§c❌ 這是「" + town.getName() + "」的領地，你沒有權限放置方塊！");
            }
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player victim && event.getDamager() instanceof Player attacker) {
            Town town = townManager.getTownAt(victim.getLocation().getChunk());
            if (town != null) {
                event.setCancelled(true);
                attacker.sendMessage("§c🕊️ 「" + town.getName() + "」是受自然庇護的和平城鎮，禁止 PVP！");
            }
        }
    }
}