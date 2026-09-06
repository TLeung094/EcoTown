package me.tleung.ecoTown.data;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Town {
    private final String name;
    private final UUID mayor;
    private final Set<String> claimedChunks;
    private final Set<UUID> members;
    private String spawnLocation; // 新增：傳送點座標 (字串格式)

    public Town(String name, UUID mayor) {
        this.name = name;
        this.mayor = mayor;
        this.claimedChunks = new HashSet<>();
        this.members = new HashSet<>();
    }

    public String getName() { return name; }
    public UUID getMayor() { return mayor; }
    public Set<String> getClaimedChunks() { return claimedChunks; }
    public Set<UUID> getMembers() { return members; }
    public String getSpawnLocation() { return spawnLocation; }

    public void addChunk(String chunkKey) { claimedChunks.add(chunkKey); }
    public void removeChunk(String chunkKey) { claimedChunks.remove(chunkKey); }

    public void addMember(UUID uuid) { members.add(uuid); }
    public void removeMember(UUID uuid) { members.remove(uuid); }

    public void setSpawnLocation(String spawnLocation) { this.spawnLocation = spawnLocation; }

    public boolean isMember(UUID uuid) {
        return mayor.equals(uuid) || members.contains(uuid);
    }
}