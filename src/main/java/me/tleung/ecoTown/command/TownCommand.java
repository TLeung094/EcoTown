package me.tleung.ecoTown.command;

import me.tleung.ecoTown.data.Town;
import me.tleung.ecoTown.gui.TownGUI;
import me.tleung.ecoTown.manager.TownManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TownCommand implements CommandExecutor {
    private final TownManager townManager;

    public TownCommand(TownManager townManager) {
        this.townManager = townManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("只有玩家可以使用此指令！");
            return true;
        }

        // 🔥 自動讀取玩家所屬的城鎮
        Town playerTown = townManager.getPlayerTown(player.getUniqueId());

        if (args.length == 0) {
            TownGUI gui = new TownGUI(playerTown, player);
            player.openInventory(gui.getInventory());
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "create":
                if (args.length < 2) {
                    player.sendMessage("§c用法: /town create <城鎮名稱>"); return true;
                }
                townManager.createTown(player, args[1]);
                break;

            case "claim":
                if (playerTown == null) {
                    player.sendMessage("§c❌ 你還沒有加入任何城鎮！"); return true;
                }
                townManager.claimChunk(player, playerTown.getName());
                break;

            case "unclaim":
                if (playerTown == null) {
                    player.sendMessage("§c❌ 你還沒有加入任何城鎮！"); return true;
                }
                townManager.unclaimChunk(player, playerTown.getName());
                break;

            case "disband":
                if (playerTown == null) {
                    player.sendMessage("§c❌ 你還沒有加入任何城鎮！"); return true;
                }
                townManager.deleteTown(player, playerTown.getName());
                break;

            case "invite":
                // 因為不用打城鎮名了，長度 >= 2 即可，args[1] 就是玩家名稱
                if (args.length < 2) {
                    player.sendMessage("§c用法: /town invite <玩家ID>"); return true;
                }
                if (playerTown == null) {
                    player.sendMessage("§c❌ 你還沒有加入任何城鎮！"); return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null || !target.isOnline()) {
                    player.sendMessage("§c❌ 找不到該玩家，或玩家不在線上！"); return true;
                }
                townManager.invitePlayer(player, target, playerTown.getName());
                break;

            case "accept":
                townManager.acceptInvite(player);
                break;

            case "setspawn":
                if (playerTown == null) {
                    player.sendMessage("§c❌ 你還沒有加入任何城鎮！"); return true;
                }
                townManager.setTownSpawn(player, playerTown.getName());
                break;

            case "spawn":
                if (playerTown == null) {
                    player.sendMessage("§c❌ 你還沒有加入任何城鎮！"); return true;
                }
                townManager.teleportToSpawn(player, playerTown.getName());
                break;

            default:
                sendHelp(player);
                break;
        }
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(" ");
        player.sendMessage("§2=== EcoTown 生態城鎮系統 ===");
        player.sendMessage("§a/town create <名稱> §f- 建立一個新的城鎮");
        player.sendMessage("§a/town claim §f- 佔領你腳下的區塊");
        player.sendMessage("§a/town unclaim §f- 解除腳下區塊的領地");
        player.sendMessage("§a/town disband §f- 解散整個城鎮");
        player.sendMessage("§a/town invite <玩家> §f- 邀請玩家加入");
        player.sendMessage("§a/town accept §f- 接受城鎮邀請");
        player.sendMessage("§a/town setspawn §f- 將腳下設為城鎮傳送點");
        player.sendMessage("§a/town spawn §f- 傳送至城鎮中心");
        player.sendMessage(" ");
    }
}