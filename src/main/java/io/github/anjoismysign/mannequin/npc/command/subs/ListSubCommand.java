package io.github.anjoismysign.mannequin.npc.command.subs;

import io.github.anjoismysign.mannequin.npc.entity.NPCData;
import io.github.anjoismysign.mannequin.npc.manager.NPCManager;
import io.github.anjoismysign.mannequin.npc.command.SubCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;

public class ListSubCommand implements SubCommand {
    private final NPCManager npcManager;

    public ListSubCommand(NPCManager npcManager) {
        this.npcManager = npcManager;
    }

    @Override
    public String arg() {
        return "list";
    }

    @Override
    public boolean execute(Player player, String[] args) {
        if (args.length > 1) {
            String worldName = args[1];
            sendWorldNpcList(player, worldName, npcManager.getNPCs(worldName));
        } else {
            npcManager.getAllNPCs().forEach((worldName, npcs) -> {
                if (!npcs.isEmpty()) sendWorldNpcList(player, worldName, npcs);
            });
        }
        return true;
    }

    private void sendWorldNpcList(Player player, String worldName, List<NPCData> npcs) {
        player.sendMessage(Component.text("--- World: " + worldName + " ---", NamedTextColor.AQUA));
        for (NPCData npc : npcs) {
            var tpBtn = Component.text("[TP]").color(NamedTextColor.GOLD)
                    .hoverEvent(HoverEvent.showText(Component.text("Teleport to " + npc.name())))
                    .clickEvent(ClickEvent.runCommand("/npc tp " + npc.name() + " " + worldName));
            player.sendMessage(Component.text("- " + npc.name() + " ").append(tpBtn));
        }
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        return args.length == 2 ? Bukkit.getWorlds().stream().map(World::getName).toList() : List.of();
    }
}