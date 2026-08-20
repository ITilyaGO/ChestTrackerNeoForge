package red.jackf.chesttracker.impl.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;

/** Container-name world rendering is disabled in the initial NeoForge 26.1.2 port. */
public final class NameRenderer {
    private NameRenderer() {}

    public static void setup() {}

    public static void scheduleLabels() {}

    public static boolean hasScheduledLabels() { return false; }

    public static void renderLabels(PoseStack poseStack, Camera camera, MultiBufferSource.BufferSource buffers) {}

    public static void clearScheduledLabels() {}
}
