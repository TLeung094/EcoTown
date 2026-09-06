package me.tleung.ecoTown.manager;

import me.tleung.ecoTown.EcoTown;
import me.tleung.ecoTown.data.Town;
import me.tleung.ecoTown.storage.TownDatabase;
import me.tleung.ecochain.data.EcoRegionData;
import me.tleung.ecochain.manager.ChunkManager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TownManager {
    private final EcoTown plugin;
    private final TownDatabase database;
    private final ChunkManager ecoChunkManager;

    private final Map<String, Town> towns = new ConcurrentHashMap<>();
    private final Map<String, String> chunkClaims = new ConcurrentHashMap<>();
    private final Map<UUID, String> pendingInvites = new ConcurrentHashMap<>();

    public TownManager(EcoTown plugin, TownDatabase database, ChunkManager ecoChunkManager) {
        this.plugin = plugin;
        this.database = database;
        this.ecoChunkManager = ecoChunkManager;
    }

    // 輔助工具：將 Location 轉換為字串存入資料庫
    public static String locationToString(Location loc) {
        if (loc == null || loc.getWorld() == null) return null;
        return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();
    }

    // 輔助工具：將字串轉回 Location
    public static Location stringToLocation(String str) {
        if (str == null || str.isEmpty()) return null;
        String[] parts = str.split(",");
        if (parts.length != 6) return null;
        World world = Bukkit.getWorld(parts[0]);
        if (world == null) return null;
        return new Location(world, Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]), Float.parseFloat(parts[4]), Float.parseFloat(parts[5]));
    }

    public void loadAllData() {
        Map<String, Town> loadedTowns = database.loadAllTownsSync();
        towns.clear();
        chunkClaims.clear();
        towns.putAll(loadedTowns);
        for (Town town : loadedTowns.values()) {
            for (String chunkKey : town.getClaimedChunks()) {
                chunkClaims.put(chunkKey, town.getName());
            }
        }
        plugin.getLogger().info("成功載入了 " + towns.size() + " 個城鎮與 " + chunkClaims.size() + " 個領地區塊！");
    }

    public boolean createTown(Player player, String townName) {
        if (towns.containsKey(townName)) {
            player.sendMessage("§c❌ 城鎮名稱已被使用！");
            return false;
        }
        Town newTown = new Town(townName, player.getUniqueId());
        towns.put(townName, newTown);
        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> database.saveTownSync(newTown));
        player.sendMessage("§a🎉 恭喜！你成功建立了生態城鎮「" + townName + "」！");
        return true;
    }

    public void claimChunk(Player player, String townName) {
        Town town = towns.get(townName);
        if (town == null) {
            player.sendMessage("§c❌ 找不到該城鎮！"); return;
        }
        if (!town.getMayor().equals(player.getUniqueId())) {
            player.sendMessage("§c❌ 只有鎮長可以擴張城鎮領地！"); return;
        }

        Chunk chunk = player.getLocation().getChunk();
        String chunkKey = ChunkManager.getChunkKey(chunk);

        if (chunkClaims.containsKey(chunkKey)) {
            player.sendMessage("§c❌ 這裡已經是其他城鎮的領地了！"); return;
        }

        EcoRegionData regionData = ecoChunkManager.getRegionData(chunk);
        if (regionData == null) {
            player.sendMessage("§c⏳ 生態系統尚未完全載入此區塊，請稍候再試..."); return;
        }

        int overallHealth = regionData.getOverallHealth();
        if (overallHealth < 0) {
            player.sendMessage("§c❌ 無法在此建立或擴張城鎮！");
            player.sendMessage("§7這裡的生態過於惡劣 (評分: " + overallHealth + ")。請先將環境提升至「🌾 平凡田野」以上！");
            return;
        }

        town.addChunk(chunkKey);
        chunkClaims.put(chunkKey, townName);
        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> database.claimChunkSync(chunkKey, townName));
        player.sendMessage("§a✅ 成功佔領區塊！優美的自然環境將庇護你的城鎮。");
    }

    public void unclaimChunk(Player player, String townName) {
        Town town = towns.get(townName);
        if (town == null) return;
        if (!town.getMayor().equals(player.getUniqueId())) {
            player.sendMessage("§c❌ 只有鎮長可以解除領地！"); return;
        }

        String chunkKey = ChunkManager.getChunkKey(player.getLocation().getChunk());
        if (!town.getClaimedChunks().contains(chunkKey)) {
            player.sendMessage("§c❌ 腳下這個區塊並非屬於你的城鎮！"); return;
        }

        town.removeChunk(chunkKey);
        chunkClaims.remove(chunkKey);
        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> database.unclaimChunkSync(chunkKey));
        player.sendMessage("§a✅ 成功解除該區塊的領地保護。");
    }

    public void deleteTown(Player player, String townName) {
        Town town = towns.get(townName);
        if (town == null) return;
        if (!town.getMayor().equals(player.getUniqueId())) {
            player.sendMessage("§c❌ 只有鎮長可以解散城鎮！"); return;
        }

        for (String chunkKey : town.getClaimedChunks()) {
            chunkClaims.remove(chunkKey);
        }
        towns.remove(townName);
        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> database.deleteTownSync(townName));
        player.sendMessage("§c⚠️ 你的城鎮「" + townName + "」已徹底解散，這片土地回歸了自然。");
    }

    public Town getTownAt(Chunk chunk) {
        String townName = chunkClaims.get(ChunkManager.getChunkKey(chunk));
        return townName != null ? towns.get(townName) : null;
    }

    public void invitePlayer(Player mayor, Player target, String townName) {
        Town town = towns.get(townName);
        if (town == null || !town.getMayor().equals(mayor.getUniqueId())) {
            mayor.sendMessage("§c❌ 你不是該城鎮的鎮長，無法邀請玩家！"); return;
        }
        if (town.isMember(target.getUniqueId())) {
            mayor.sendMessage("§c❌ 該玩家已經是你的鎮民了！"); return;
        }
        pendingInvites.put(target.getUniqueId(), townName);
        mayor.sendMessage("§a✅ 已向 " + target.getName() + " 發送了城鎮邀請！");
        target.sendMessage("§a💌 你收到了加入城鎮「" + townName + "」的邀請！");
        target.sendMessage("§a請輸入 §e/town accept §a來接受邀請。");
    }

    public void acceptInvite(Player player) {
        String townName = pendingInvites.remove(player.getUniqueId());
        if (townName == null) {
            player.sendMessage("§c❌ 你目前沒有收到任何城鎮邀請。"); return;
        }
        Town town = towns.get(townName);
        if (town == null) {
            player.sendMessage("§c❌ 該城鎮已解散或不存在。"); return;
        }
        town.addMember(player.getUniqueId());
        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> database.addMemberSync(townName, player.getUniqueId()));
        player.sendMessage("§a🎉 你已成功加入生態城鎮「" + townName + "」！");

        Player mayor = plugin.getServer().getPlayer(town.getMayor());
        if (mayor != null && mayor.isOnline()) {
            mayor.sendMessage("§a🎉 " + player.getName() + " 接受了邀請，成為了你的鎮民！");
        }
    }

    public Town getPlayerTown(UUID uuid) {
        for (Town town : towns.values()) {
            if (town.isMember(uuid)) {
                return town;
            }
        }
        return null;
    }

    // ====== 新增：傳送點相關邏輯 ======

    public void setTownSpawn(Player player, String townName) {
        Town town = towns.get(townName);
        if (town == null) {
            player.sendMessage("§c❌ 找不到該城鎮！"); return;
        }
        if (!town.getMayor().equals(player.getUniqueId())) {
            player.sendMessage("§c❌ 只有鎮長可以設定城鎮傳送點！"); return;
        }

        String chunkKey = ChunkManager.getChunkKey(player.getLocation().getChunk());
        if (!town.getClaimedChunks().contains(chunkKey)) {
            player.sendMessage("§c❌ 傳送點必須設定在城鎮的領地範圍內！"); return;
        }

        String locStr = locationToString(player.getLocation());
        town.setSpawnLocation(locStr);
        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> database.updateSpawnSync(townName, locStr));
        player.sendMessage("§a✅ 成功將你腳下的位置設定為城鎮傳送點！");
    }

    public void teleportToSpawn(Player player, String townName) {
        Town town = towns.get(townName);
        if (town == null) {
            player.sendMessage("§c❌ 找不到該城鎮！"); return;
        }
        if (!town.isMember(player.getUniqueId())) {
            player.sendMessage("§c❌ 你不是該城鎮的鎮民，無法使用傳送點！"); return;
        }
        if (town.getSpawnLocation() == null) {
            player.sendMessage("§c❌ 鎮長尚未設定城鎮傳送點！"); return;
        }

        Location loc = stringToLocation(town.getSpawnLocation());
        if (loc == null) {
            player.sendMessage("§c❌ 傳送點座標無效或世界不存在！"); return;
        }

        player.sendMessage("§a🌀 正在傳送至城鎮中心...");
        // 完美適應 Folia 的非同步傳送
        player.teleportAsync(loc).thenAccept(success -> {
            if (!success) {
                player.sendMessage("§c❌ 傳送失敗，可能是區塊尚未載入，請稍後再試。");
            }
        });
    }
}