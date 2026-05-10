package io.github.anjoismysign.mannequin.npc.command;

import io.github.anjoismysign.mannequin.npc.MannequinNPC;
import io.github.anjoismysign.mannequin.npc.manager.NPCManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.security.CodeSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class NPCCommand implements CommandExecutor, TabCompleter {
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public NPCCommand(MannequinNPC plugin, NPCManager npcManager) {
        loadSubCommands(plugin, npcManager);
    }

    private void loadSubCommands(MannequinNPC plugin, NPCManager npcManager) {
        String packageName = "io.github.anjoismysign.mannequin.npc.command.subs";
        String path = packageName.replace('.', '/');

        CodeSource src = plugin.getClass().getProtectionDomain().getCodeSource();
        if (src == null) return;

        URL jar = src.getLocation();
        try (ZipInputStream zip = new ZipInputStream(jar.openStream())) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                String name = entry.getName();
                if (name.startsWith(path) && name.endsWith(".class") && !name.contains("$")) {
                    String className = name.replace('/', '.').substring(0, name.length() - 6);
                    try {
                        Class<?> clazz = Class.forName(className);
                        if (SubCommand.class.isAssignableFrom(clazz) && !clazz.isInterface()) {
                            instantiateSubCommand((Class<? extends SubCommand>) clazz, plugin, npcManager);
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to load subcommand class: " + className);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void instantiateSubCommand(Class<? extends SubCommand> clazz, MannequinNPC plugin, NPCManager npcManager) {
        try {
            for (Constructor<?> constructor : clazz.getConstructors()) {
                Class<?>[] params = constructor.getParameterTypes();

                Object instance = null;
                if (params.length == 1) {
                    if (params[0].equals(NPCManager.class)) {
                        instance = constructor.newInstance(npcManager);
                    } else if (params[0].equals(MannequinNPC.class)) {
                        instance = constructor.newInstance(plugin);
                    }
                } else if (params.length == 0) {
                    instance = constructor.newInstance();
                }

                if (instance instanceof SubCommand sub) {
                    subCommands.put(sub.arg().toLowerCase(), sub);
                    return;
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Could not instantiate subcommand: " + clazz.getSimpleName());
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length == 0 || !subCommands.containsKey(args[0].toLowerCase())) {
            player.sendMessage(Component.text("Usage: /npc <" + String.join("|", subCommands.keySet()) + ">", NamedTextColor.RED));
            return true;
        }

        return subCommands.get(args[0].toLowerCase()).execute(player, args);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) return null;

        if (args.length == 1) {
            return subCommands.keySet().stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }

        SubCommand sub = subCommands.get(args[0].toLowerCase());
        if (sub != null) {
            return sub.tabComplete(player, args).stream()
                    .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                    .toList();
        }

        return List.of();
    }
}