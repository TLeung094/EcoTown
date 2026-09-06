package me.tleung.ecoTown.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.tleung.ecoTown.data.Town;
import me.tleung.ecoTown.manager.TownManager;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class EcoTownExpansion extends PlaceholderExpansion {
    private final TownManager townManager;

    public EcoTownExpansion(TownManager townManager) {
        this.townManager = townManager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "ecotown";
    }

    @Override
    public @NotNull String getAuthor() {
        return "TLeung";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        Town town = townManager.getPlayerTown(player.getUniqueId());

        if (params.equalsIgnoreCase("name")) {
            return town != null ? town.getName() : "";
        }

        if (params.equalsIgnoreCase("name_formatted")) {
            return town != null ? "<dark_green>[" + town.getName() + "]</dark_green>" : "";
        }

        if (params.equalsIgnoreCase("role")) {
            if (town == null) return "流浪者";
            // ✅ 正名為「鎮長」
            return town.getMayor().equals(player.getUniqueId()) ? "鎮長" : "鎮民";
        }

        return null;
    }
}