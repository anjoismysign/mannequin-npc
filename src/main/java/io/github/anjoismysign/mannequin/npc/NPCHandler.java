package io.github.anjoismysign.mannequin.npc;

import io.github.anjoismysign.mannequin.npc.entity.NPCData;
import io.github.anjoismysign.mannequin.npc.manager.NPCManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class NPCHandler implements Listener {
    private final NPCManager npcManager;

    public NPCHandler(NPCManager npcManager) {
        this.npcManager = npcManager;
    }

    @EventHandler
    public void onHit(@NotNull EntityDamageByEntityEvent event){
        Entity damaged = event.getEntity();
        if (damaged.getType() != EntityType.MANNEQUIN) {
            return;
        }
        Entity damager = event.getDamager();
        if (damager.getType() != EntityType.PLAYER){
            return;
        }
        Player player = (Player) damager;
        Optional<NPCData> npcData = npcManager.getNPCData(damaged.getUniqueId());
        if (npcData.isEmpty()) {
            return;
        }
        event.setCancelled(true);
        String command = npcData.get().command();
        if (command == null || command.isBlank()) {
            return;
        }
        String parsedCommand = command
                .replace("{player}", player.getName())
                .replace("{playerId}", player.getUniqueId().toString());
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand);
    }

    @EventHandler
    public void onPlayerInteractEntity(@NotNull PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND){
            return;
        }
        if (!(event.getRightClicked() instanceof Mannequin mannequin)) {
            return;
        }
        Optional<NPCData> npcData = npcManager.getNPCData(mannequin.getUniqueId());
        if (npcData.isEmpty()) {
            return;
        }
        String command = npcData.get().command();
        if (command == null || command.isBlank()) {
            return;
        }
        Player player = event.getPlayer();
        String parsedCommand = command
                .replace("{player}", player.getName())
                .replace("{playerId}", player.getUniqueId().toString());
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand);
    }
}
