package io.github.anjoismysign.mannequin.npc.command;

import org.bukkit.entity.Player;

import java.util.List;

public interface SubCommand {
    String arg();
    boolean execute(Player player, String[] args);
    List<String> tabComplete(Player player, String[] args);
}