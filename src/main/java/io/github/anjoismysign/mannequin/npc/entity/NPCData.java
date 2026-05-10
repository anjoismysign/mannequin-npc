package io.github.anjoismysign.mannequin.npc.entity;

import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

public record NPCData(
        String name,
        double x,
        double y,
        double z,
        float pitch,
        float yaw,
        String worldName,
        String profileUrl,
        String command,
        @Nullable String customName,
        boolean customNameVisible,
        @Nullable String description

) {
    public Location toLocation(World world) {
        return new Location(world, x, y, z, yaw, pitch);
    }

    public static NPCData fromLocation(Location location,
                                       String name,
                                       String profileUrl) {
        return new NPCData(
                name,
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getPitch(),
                location.getYaw(),
                location.getWorld().getName(),
                profileUrl,
                "",
                name,
                true,
                null
        );
    }
}
