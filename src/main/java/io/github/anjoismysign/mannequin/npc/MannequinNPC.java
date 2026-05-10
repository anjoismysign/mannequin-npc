package io.github.anjoismysign.mannequin.npc;

import io.github.anjoismysign.mannequin.npc.command.NPCCommand;
import io.github.anjoismysign.mannequin.npc.entity.NPCData;
import io.github.anjoismysign.mannequin.npc.manager.NPCManager;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

public class MannequinNPC extends JavaPlugin implements Listener {
    private NPCManager npcManager;

    @Override
    public void onEnable() {
        Path dataDir = getDataFolder().toPath();
        npcManager = new NPCManager(dataDir);

        reloadPlugin(); // Use the new method here

        getServer().getPluginManager().registerEvents(new NPCHandler(npcManager), this);
        getServer().getPluginManager().registerEvents(this, this);

        NPCCommand npcCommand = new NPCCommand(this, npcManager); // Pass 'this'
        getCommand("npc").setExecutor(npcCommand);
        getCommand("npc").setTabCompleter(npcCommand);

        getLogger().info("MannequinNPC enabled!");
    }

    @Override
    public void onDisable() {
        npcManager.killNPCs();
    }

    public void reloadPlugin() {
        npcManager.reload();
        for (World world : getServer().getWorlds()) {
            npcManager.loadWorld(world);
            for (Chunk chunk : world.getLoadedChunks()) {
                spawnNPCsInChunk(chunk);
            }
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        spawnNPCsInChunk(event.getChunk());
    }

    private void spawnNPCsInChunk(Chunk chunk) {
        World world = chunk.getWorld();
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();

        for (NPCData npc : npcManager.getNPCs(world.getName())) {
            // Check if the NPC's coordinates fall within this chunk
            int npcChunkX = (int) Math.floor(npc.x()) >> 4;
            int npcChunkZ = (int) Math.floor(npc.z()) >> 4;

            if (npcChunkX == chunkX && npcChunkZ == chunkZ) {
                npcManager.spawnNPC(npc, world);
            }
        }
    }
}