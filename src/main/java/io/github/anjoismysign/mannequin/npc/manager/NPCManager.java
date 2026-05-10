package io.github.anjoismysign.mannequin.npc.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.github.anjoismysign.mannequin.npc.MannequinAPI;
import io.github.anjoismysign.mannequin.npc.entity.NPCData;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mannequin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;

public class NPCManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type LIST_TYPE = new TypeToken<List<NPCData>>() {
    }.getType();

    private final Path dataDirectory;
    private final Map<String, List<NPCData>> worldNPCs = new ConcurrentHashMap<>();
    private final Map<UUID, NPCData> entityNpcData = new ConcurrentHashMap<>();

    public NPCManager(Path dataDirectory) {
        this.dataDirectory = dataDirectory.resolve("npcs").toAbsolutePath();
        try {
            Files.createDirectories(this.dataDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadWorld(World world) {
        Path file = dataDirectory.resolve(world.getName() + ".json");
        if (!Files.exists(file)) {
            worldNPCs.put(world.getName(), new ArrayList<>());
            return;
        }
        try {
            String content = Files.readString(file);
            List<NPCData> npcs = GSON.fromJson(content, LIST_TYPE);
            worldNPCs.put(world.getName(), npcs != null ? npcs : new ArrayList<>());
        } catch (IOException e) {
            e.printStackTrace();
            worldNPCs.put(world.getName(), new ArrayList<>());
        }
    }

    public void saveWorld(String worldName) {
        List<NPCData> npcs = worldNPCs.getOrDefault(worldName, new ArrayList<>());
        Path file = dataDirectory.resolve(worldName + ".json");
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, GSON.toJson(npcs));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void spawnNPC(NPCData data, World world) {
        world.spawn(data.toLocation(world), Mannequin.class, mannequin -> {
            applyDataToEntity(mannequin, data);
            entityNpcData.put(mannequin.getUniqueId(), data);
        });
    }

    private void applyDataToEntity(Mannequin mannequin, NPCData data) {
        var serializer = LegacyComponentSerializer.legacyAmpersand();
        mannequin.customName(data.customName() == null ? null : serializer.deserialize(data.customName()));
        mannequin.setCustomNameVisible(data.customNameVisible());
        mannequin.setDescription(data.description() == null ? null : serializer.deserialize(data.description()));
        mannequin.setProfile(MannequinAPI.resolvableProfile(data.profileUrl()));
        mannequin.setPersistent(false);
        mannequin.setImmovable(true);
    }

    public boolean exists(String name, String worldName) {
        return worldNPCs.getOrDefault(worldName, Collections.emptyList())
                .stream().anyMatch(n -> n.name().equalsIgnoreCase(name));
    }

    public void addNPC(NPCData data) {
        String worldName = data.worldName();
        List<NPCData> npcs = worldNPCs.computeIfAbsent(worldName, k -> new ArrayList<>());
        npcs.add(data);
        saveWorld(worldName);
    }

    public void removeNPC(String name, String worldName) {
        List<NPCData> npcs = worldNPCs.get(worldName);
        if (npcs != null) {
            npcs.removeIf(npc -> npc.name().equalsIgnoreCase(name));
            saveWorld(worldName);
            entityNpcData.entrySet().removeIf(entry -> {
                if (entry.getValue().name().equalsIgnoreCase(name) && entry.getValue().worldName().equals(worldName)) {
                    Entity entity = Bukkit.getEntity(entry.getKey());
                    if (entity != null) entity.remove();
                    return true;
                }
                return false;
            });
        }
    }

    public void moveNPC(String name, String currentWorldName, Location newLoc) {
        Optional<NPCData> dataOpt = getNPCByName(name, currentWorldName);
        if (dataOpt.isEmpty()) return;

        NPCData old = dataOpt.get();
        String newWorldName = newLoc.getWorld().getName();

        NPCData updated = new NPCData(
                old.name(), newLoc.getX(), newLoc.getY(), newLoc.getZ(),
                newLoc.getPitch(), newLoc.getYaw(), newWorldName,
                old.profileUrl(), old.command(), old.customName(),
                old.customNameVisible(), old.description()
        );

        if (!currentWorldName.equals(newWorldName)) {
            removeNPC(name, currentWorldName);
            addNPC(updated);
            spawnNPC(updated, newLoc.getWorld());
        } else {
            List<NPCData> npcs = worldNPCs.get(currentWorldName);
            if (npcs != null) {
                for (int i = 0; i < npcs.size(); i++) {
                    if (npcs.get(i).name().equalsIgnoreCase(name)) {
                        npcs.set(i, updated);
                        break;
                    }
                }
            }
            entityNpcData.entrySet().forEach(entry -> {
                if (entry.getValue().name().equalsIgnoreCase(name) && entry.getValue().worldName().equals(currentWorldName)) {
                    entry.setValue(updated);
                    Entity entity = Bukkit.getEntity(entry.getKey());
                    if (entity != null) entity.teleport(newLoc);
                }
            });
            saveWorld(currentWorldName);
        }
    }

    public Optional<NPCData> getNPCByName(String name, String worldName) {
        return worldNPCs.getOrDefault(worldName, Collections.emptyList())
                .stream().filter(n -> n.name().equalsIgnoreCase(name)).findFirst();
    }

    public Map<String, List<NPCData>> getAllNPCs() {
        return Collections.unmodifiableMap(worldNPCs);
    }

    public @NotNull List<NPCData> getNPCs(String worldName) {
        return Collections.unmodifiableList(worldNPCs.getOrDefault(worldName, new ArrayList<>()));
    }

    public Optional<NPCData> getNPCData(UUID entityId) {
        return Optional.ofNullable(entityNpcData.get(entityId));
    }

    public void killNPCs() {
        entityNpcData.keySet().forEach(uuid -> {
            var entity = Bukkit.getEntity(uuid);
            if (entity != null) entity.remove();
        });
        entityNpcData.clear();
    }

    public void updateNPC(String name, String worldName, UnaryOperator<NPCData> updater) {
        List<NPCData> npcs = worldNPCs.get(worldName);
        if (npcs == null) return;

        for (int i = 0; i < npcs.size(); i++) {
            NPCData old = npcs.get(i);
            if (old.name().equalsIgnoreCase(name)) {
                NPCData updated = updater.apply(old);
                npcs.set(i, updated);

                for (var entry : entityNpcData.entrySet()) {
                    if (entry.getValue().name().equalsIgnoreCase(name) && entry.getValue().worldName().equals(worldName)) {
                        entry.setValue(updated);
                        Entity entity = Bukkit.getEntity(entry.getKey());
                        if (entity instanceof Mannequin mannequin) {
                            applyDataToEntity(mannequin, updated);
                            mannequin.teleport(updated.toLocation(mannequin.getWorld()));
                        }
                    }
                }
                saveWorld(worldName);
                break;
            }
        }
    }

    public void reload() {
        killNPCs();
        worldNPCs.clear();
        entityNpcData.clear();
    }
}