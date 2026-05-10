package io.github.anjoismysign.mannequin.npc.command.subs;

import io.github.anjoismysign.mannequin.npc.MannequinNPC;
import io.github.anjoismysign.mannequin.npc.command.SubCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.List;

public class ReloadSubCommand implements SubCommand {
    private final MannequinNPC plugin;

    public ReloadSubCommand(MannequinNPC plugin) {
        this.plugin = plugin;
    }

    @Override
    public String arg() {
        return "reload";
    }

    @Override
    public boolean execute(Player player, String[] args) {
        if (!player.hasPermission("mannequinnpc.admin")) {
            player.sendMessage(Component.text("You do not have permission to reload NPCs.", NamedTextColor.RED));
            return true;
        }
        plugin.reloadPlugin();
        player.sendMessage(Component.text("MannequinNPC configuration and NPCs reloaded!", NamedTextColor.GREEN));
        return true;
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        return List.of();
    }
}