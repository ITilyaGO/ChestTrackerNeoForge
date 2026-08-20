package red.jackf.jackfredlib.client.impl.toasts;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;
import red.jackf.jackfredlib.client.api.toasts.ToastIcon;

import java.util.HashMap;
import java.util.Map;

public class ModUtils {
    private static final Map<String, ToastIcon> ICON_CACHE = new HashMap<>();

    private ModUtils() {
    }

    public static Component getModTitle(String modid) {
        return ModList.get().getModContainerById(modid)
                .map(container -> Component.literal(container.getModInfo().getDisplayName()))
                .orElse(Component.literal(modid));
    }

    public static @Nullable ToastIcon iconFromModId(String modid) {
        return ICON_CACHE.computeIfAbsent(modid, id -> ToastIcon.image(Identifier.fromNamespaceAndPath(id, "icon.png"), 32, 32));
    }
}
