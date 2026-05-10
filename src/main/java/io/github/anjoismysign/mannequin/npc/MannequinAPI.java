package io.github.anjoismysign.mannequin.npc;

import com.destroystokyo.paper.profile.ProfileProperty;
import io.github.anjoismysign.mannequinbrain.BukkitMannequinBrain;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import org.bukkit.entity.Mannequin;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class MannequinAPI {
    public static ResolvableProfile resolvableProfile(@NotNull String url) {
        var json = "{\"textures\":{\"SKIN\":{\"url\":\"%s\"}}}".formatted(url);
        var base64 = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        return ResolvableProfile.resolvableProfile()
                .addProperty(new ProfileProperty("textures", base64))
                .build();
    }

    public static BukkitMannequinBrain getBrain(@NotNull Mannequin mannequin) {
        return new BukkitMannequinBrain(mannequin);
    }
}
