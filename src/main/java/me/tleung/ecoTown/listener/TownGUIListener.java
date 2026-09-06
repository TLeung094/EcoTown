package me.tleung.ecoTown.listener;

import me.tleung.ecoTown.data.Town;
import me.tleung.ecoTown.gui.TownGUI;
import me.tleung.ecoTown.manager.TownManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class TownGUIListener implements Listener {
    private final TownManager townManager;

    public TownGUIListener(TownManager townManager) {
        this.townManager = townManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof TownGUI gui)) {
            return;
        }

        // 取消事件，防止玩家把 GUI 裡的東西拿走
        event.setCancelled(true);
        if (event.getCurrentItem() == null) return;

        Player player = (Player) event.getWhoClicked();
        Town town = gui.getTown();

        switch (event.getRawSlot()) {
            case 12: // 傳送回城鎮
                player.closeInventory();
                if (town == null) player.sendMessage("§c❌ 你還沒有加入任何城鎮！");
                else townManager.teleportToSpawn(player, town.getName());
                break;

            case 13: // 佔領區塊
                player.closeInventory();
                if (town == null) player.sendMessage("§c❌ 你還沒有加入任何城鎮！");
                else townManager.claimChunk(player, town.getName());
                break;

            case 14: // 解除領地
                player.closeInventory();
                if (town == null) player.sendMessage("§c❌ 你還沒有加入任何城鎮！");
                else townManager.unclaimChunk(player, town.getName());
                break;

            case 22: // 關閉面板
                player.closeInventory();
                break;

            case 26: // 解散城鎮 (僅限鎮長看得到並點擊)
                player.closeInventory();
                if (town == null) player.sendMessage("§c❌ 你還沒有加入任何城鎮！");
                else townManager.deleteTown(player, town.getName());
                break;
        }
    }
}