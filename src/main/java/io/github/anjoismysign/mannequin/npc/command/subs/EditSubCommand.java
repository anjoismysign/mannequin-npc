package io.github.anjoismysign.mannequin.npc.command.subs;

import io.github.anjoismysign.mannequin.npc.entity.NPCData;
import io.github.anjoismysign.mannequin.npc.manager.NPCManager;
import io.github.anjoismysign.mannequin.npc.command.SubCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class EditSubCommand implements SubCommand {
    private static final List<String> FIELDS = List.of("profileUrl", "command", "customName", "customNameVisible", "description");
    private final NPCManager npcManager;

    public EditSubCommand(NPCManager npcManager) {
        this.npcManager = npcManager;
    }

    @Override
    public String arg() {
        return "edit";
    }

    @Override
    public boolean execute(Player player, String[] args) {
        if (args.length < 5) {
            player.sendMessage(Component.text("Usage: /npc edit <name> <world> <field> <value...>", NamedTextColor.RED));
            return true;
        }

        String name = args[1];
        String worldName = args[2];
        String field = args[3].toLowerCase();
        String rawValue = String.join(" ", Arrays.copyOfRange(args, 4, args.length));
        String value = (rawValue.equalsIgnoreCase("none") || rawValue.equalsIgnoreCase("null")) ? null : rawValue;

        if (!npcManager.exists(name, worldName)) {
            player.sendMessage(Component.text("NPC not found.", NamedTextColor.RED));
            return true;
        }

        npcManager.updateNPC(name, worldName, old -> switch (field) {
            case "profileurl" -> new NPCData(old.name(), old.x(), old.y(), old.z(), old.pitch(), old.yaw(), old.worldName(), rawValue, old.command(), old.customName(), old.customNameVisible(), old.description());
            case "command" -> new NPCData(old.name(), old.x(), old.y(), old.z(), old.pitch(), old.yaw(), old.worldName(), old.profileUrl(), value, old.customName(), old.customNameVisible(), old.description());
            case "customname" -> new NPCData(old.name(), old.x(), old.y(), old.z(), old.pitch(), old.yaw(), old.worldName(), old.profileUrl(), old.command(), value, old.customNameVisible(), old.description());
            case "customnamevisible" -> new NPCData(old.name(), old.x(), old.y(), old.z(), old.pitch(), old.yaw(), old.worldName(), old.profileUrl(), old.command(), old.customName(), Boolean.parseBoolean(rawValue), old.description());
            case "description" -> new NPCData(old.name(), old.x(), old.y(), old.z(), old.pitch(), old.yaw(), old.worldName(), old.profileUrl(), old.command(), old.customName(), old.customNameVisible(), value);
            default -> old;
        });

        player.sendMessage(Component.text("NPC '" + name + "' updated field '" + field + "'", NamedTextColor.GREEN));
        return true;
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        return switch (args.length) {
            case 2 -> npcManager.getNPCs(player.getWorld().getName()).stream().map(NPCData::name).toList();
            case 3 -> Bukkit.getWorlds().stream().map(World::getName).toList();
            case 4 -> FIELDS;
            case 5 -> {
                String field = args[3].toLowerCase();
                if (field.equals("customnamevisible")) yield List.of("true", "false");
                if (List.of("customname", "description", "command").contains(field)) yield List.of("none");
                yield List.of("<value>");
            }
            default -> List.of();
        };
    }
}