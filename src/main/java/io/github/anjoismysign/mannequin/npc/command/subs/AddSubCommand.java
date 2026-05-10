package io.github.anjoismysign.mannequin.npc.command.subs;

import io.github.anjoismysign.mannequin.npc.entity.NPCData;
import io.github.anjoismysign.mannequin.npc.manager.NPCManager;
import io.github.anjoismysign.mannequin.npc.command.SubCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.List;

public class AddSubCommand implements SubCommand {
    private final NPCManager npcManager;

    public AddSubCommand(NPCManager npcManager) {
        this.npcManager = npcManager;
    }

    @Override
    public String arg() {
        return "add";
    }

    @Override
    public boolean execute(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Component.text("Usage: /npc add <name> <profileUrl>", NamedTextColor.RED));
            return true;
        }
        String name = args[1];
        String worldName = player.getWorld().getName();

        if (npcManager.exists(name, worldName)) {
            player.sendMessage(Component.text("An NPC with that name already exists in this world!", NamedTextColor.RED));
            return true;
        }

        String url = args[2];
        NPCData data = NPCData.fromLocation(player.getLocation(), name, url);
        npcManager.addNPC(data);
        npcManager.spawnNPC(data, player.getWorld());
        player.sendMessage(Component.text("NPC '" + name + "' created!", NamedTextColor.GREEN));
        return true;
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        return switch (args.length) {
            case 2 -> List.of("<name>");
            case 3 -> List.of("<profileUrl>");
            default -> List.of();
        };
    }
}