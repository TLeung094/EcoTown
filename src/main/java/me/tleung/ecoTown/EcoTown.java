package me.tleung.ecoTown;

import me.tleung.ecoTown.command.TownCommand;
import me.tleung.ecoTown.listener.TownGUIListener;
import me.tleung.ecoTown.listener.TownProtectionListener;
import me.tleung.ecoTown.manager.TownManager;
import me.tleung.ecoTown.papi.EcoTownExpansion;
import me.tleung.ecoTown.storage.TownDatabase;
import me.tleung.ecochain.EcoChain;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class EcoTown extends JavaPlugin {
    private TownDatabase database;
    private TownManager townManager;

    @Override
    public void onEnable() {
        getLogger().info("EcoTown 正在啟動...");

        EcoChain ecoChain = (EcoChain) getServer().getPluginManager().getPlugin("EcoChain");
        if (ecoChain == null) {
            getLogger().severe("找不到 EcoChain 插件！EcoTown 將被停用。");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.database = new TownDatabase(this);
        this.townManager = new TownManager(this, database, ecoChain.getChunkManager());

        this.townManager.loadAllData();

        Objects.requireNonNull(getCommand("town")).setExecutor(new TownCommand(townManager));

        getServer().getPluginManager().registerEvents(new TownProtectionListener(this.townManager), this);
        getServer().getPluginManager().registerEvents(new TownGUIListener(this.townManager), this);

        // 註冊 PlaceholderAPI 擴充
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new EcoTownExpansion(this.townManager).register();
            getLogger().info("✅ 已成功掛載 PlaceholderAPI 變數支援！");
        } else {
            getLogger().warning("⚠️ 未偵測到 PlaceholderAPI，城鎮變數將無法使用。");
        }

        getLogger().info("EcoTown 啟動成功！已連動 EcoChain 生態系統與 GUI 面板。");
    }

    @Override
    public void onDisable() {
        getLogger().info("EcoTown 正在關閉...");
        if (database != null) {
            database.close();
        }
    }
}