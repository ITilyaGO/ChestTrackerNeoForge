package red.jackf.whereisit.client.api;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import red.jackf.whereisit.client.render.Rendering;

import java.util.Set;

public interface RenderUtils {
    static Set<BlockPos> getCurrentlyRendered() {
        return Rendering.getResults().keySet();
    }

    static Set<BlockPos> getCurrentlyRenderedWithNames() {
        return Rendering.getNamedResults().keySet();
    }

    static void scheduleLabelRender(Vec3 pos, Component name) {
        scheduleLabelRender(pos, name, false);
    }

    static void scheduleLabelRender(Vec3 pos, Component name, boolean seeThrough) {
        Rendering.scheduleLabel(pos, name, seeThrough);
    }
}
