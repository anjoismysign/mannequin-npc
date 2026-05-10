package io.github.anjoismysign.mannequin.npc.command.subs;

import io.github.anjoismysign.mannequin.npc.entity.NPCData;
import io.github.anjoismysign.mannequin.npc.manager.NPCManager;
import io.github.anjoismysign.mannequin.npc.command.SubCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;

public class RemoveSubCommand implements SubCommand {
    private final NPCManager npcManager;

    public RemoveSubCommand(NPCManager npcManager) {
        this.npcManager = npcManager;
    }

    @Override
    public String arg() {
        return "remove";
    }

    @Override
    public boolean execute(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Component.text("Usage: /npc remove <name> [world]", NamedTextColor.RED));
            return true;
        }
        String name = args[1];
        String worldName = args.length > 2 ? args[2] : player.getWorld().getName();

        if (!npcManager.exists(name, worldName)) {
            player.sendMessage(Component.text("NPC not found.", NamedTextColor.RED));
            return true;
        }

        npcManager.removeNPC(name, worldName);
        player.sendMessage(Component.text("NPC '" + name + "' removed from " + worldName, NamedTextColor.YELLOW));
        return true;
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        return switch (args.length) {
            case 2 -> npcManager.getNPCs(player.getWorld().getName()).stream().map(NPCData::name).toList();
            case 3 -> Bukkit.getWorlds().stream().map(World::getName).toList();
            default -> List.of();
        };
    }
}