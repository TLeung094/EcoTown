package me.tleung.ecoTown.gui;

import me.tleung.ecoTown.data.Town;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TownGUI implements InventoryHolder {
    private final Inventory inventory;
    private final Town town;
    private final Player player;

    public TownGUI(Town town, Player player) {
        this.town = town;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 27, "§2§lEcoTown 生態城鎮管理");
        setupItems();
    }

    private void setupItems() {
        // 1. 先用黑色玻璃板填滿背景，提升視覺質感
        ItemStack bg = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta bgMeta = bg.getItemMeta();
        bgMeta.setDisplayName(" ");
        bg.setItemMeta(bgMeta);
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, bg);
        }

        // 如果玩家有城鎮，判斷其身份
        boolean isMayor = town != null && town.getMayor().equals(player.getUniqueId());

        // 2. [第 11 格] 城鎮資訊書本
        ItemStack infoItem = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (town != null) {
            infoMeta.setDisplayName("§a§l城鎮: §f" + town.getName());
            infoMeta.setLore(List.of(
                    "§7--------------------",
                    "§7你的身份: §e" + (isMayor ? "鎮長" : "鎮民"),
                    "§7領地數量: §f" + town.getClaimedChunks().size() + " 區塊",
                    "§7總人口數: §f" + (town.getMembers().size() + 1) + " 人",
                    "§7--------------------"
            ));
        } else {
            infoMeta.setDisplayName("§c§l尚未加入城鎮");
            infoMeta.setLore(List.of(
                    "§7--------------------",
                    "§7你目前是個流浪者！",
                    "§7使用 §e/town create <名稱>",
                    "§7來建立屬於你的生態家園。",
                    "§7--------------------"
            ));
        }
        infoItem.setItemMeta(infoMeta);
        inventory.setItem(11, infoItem);

        // 3. [第 12 格] 傳送回城鎮中心
        ItemStack spawnItem = new ItemStack(Material.COMPASS);
        ItemMeta spawnMeta = spawnItem.getItemMeta();
        spawnMeta.setDisplayName("§b§l🌀 傳送回城鎮");
        spawnMeta.setLore(List.of(
                "§7--------------------",
                "§7點擊傳送回城鎮中心點。",
                "§7⚠️ 必須為鎮民或鎮長",
                "§7--------------------"
        ));
        spawnItem.setItemMeta(spawnMeta);
        inventory.setItem(12, spawnItem);

        // 4. [第 13 格] 佔領當前區塊
        ItemStack claimItem = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta claimMeta = claimItem.getItemMeta();
        claimMeta.setDisplayName("§a§l🌿 佔領當前區塊");
        claimMeta.setLore(List.of(
                "§7--------------------",
                "§7將腳下的土地納入城鎮領地。",
                "§7⚠️ 必須為鎮長，且生態值 >= 0",
                "§7--------------------"
        ));
        claimItem.setItemMeta(claimMeta);
        inventory.setItem(13, claimItem);

        // 5. [第 14 格] 解除當前領地
        ItemStack unclaimItem = new ItemStack(Material.DIRT);
        ItemMeta unclaimMeta = unclaimItem.getItemMeta();
        unclaimMeta.setDisplayName("§e§l⛏️ 解除當前領地");
        unclaimMeta.setLore(List.of(
                "§7--------------------",
                "§7放棄腳下區塊的領地保護。",
                "§7⚠️ 必須為鎮長",
                "§7--------------------"
        ));
        unclaimItem.setItemMeta(unclaimMeta);
        inventory.setItem(14, unclaimItem);

        // 6. [第 15 格] 邀請玩家按鈕
        ItemStack inviteItem = new ItemStack(Material.PAPER);
        ItemMeta inviteMeta = inviteItem.getItemMeta();
        inviteMeta.setDisplayName("§d§l💌 邀請指引");
        inviteMeta.setLore(List.of(
                "§7--------------------",
                "§7在聊天欄輸入指令來邀請好友：",
                "§e/town invite <玩家ID>",
                "§7--------------------"
        ));
        inviteItem.setItemMeta(inviteMeta);
        inventory.setItem(15, inviteItem);

        // 7. [第 22 格] 關閉面板
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.setDisplayName("§c§l❌ 關閉面板");
        closeItem.setItemMeta(closeMeta);
        inventory.setItem(22, closeItem);

        // 8. [第 26 格] 解散城鎮 (如果是鎮長才顯示，避免誤觸)
        if (isMayor) {
            ItemStack disbandItem = new ItemStack(Material.TNT);
            ItemMeta disbandMeta = disbandItem.getItemMeta();
            disbandMeta.setDisplayName("§4§l💣 解散城鎮");
            disbandMeta.setLore(List.of(
                    "§7--------------------",
                    "§c⚠️ 警告：點擊將永久解散城鎮！",
                    "§c所有領地與鎮民關係將被清除。",
                    "§7--------------------"
            ));
            disbandItem.setItemMeta(disbandMeta);
            inventory.setItem(26, disbandItem);
        }
    }

    public Town getTown() {
        return town;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}