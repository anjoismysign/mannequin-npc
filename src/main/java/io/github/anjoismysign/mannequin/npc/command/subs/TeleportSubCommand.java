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

public class TeleportSubCommand implements SubCommand {
    private final NPCManager npcManager;

    public TeleportSubCommand(NPCManager npcManager) {
        this.npcManager = npcManager;
    }

    @Override
    public String arg() {
        return "tp";
    }

    @Override
    public boolean execute(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Component.text("Usage: /npc tp <name> [world]", NamedTextColor.RED));
            return true;
        }
        String name = args[1];
        String worldName = args.length > 2 ? args[2] : player.getWorld().getName();

        var dataOpt = npcManager.getNPCByName(name, worldName);
        if (dataOpt.isEmpty()) {
            player.sendMessage(Component.text("NPC not found.", NamedTextColor.RED));
            return true;
        }

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            player.sendMessage(Component.text("World not loaded.", NamedTextColor.RED));
            return true;
        }

        player.teleport(dataOpt.get().toLocation(world));
        player.sendMessage(Component.text("Teleported to " + name, NamedTextColor.GREEN));
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