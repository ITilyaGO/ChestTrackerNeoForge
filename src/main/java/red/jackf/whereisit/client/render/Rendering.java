package red.jackf.whereisit.client.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Matrix4f;
import red.jackf.chesttracker.impl.ChestTracker;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.api.SearchResult;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.lang.reflect.Method;

/** Search result state and world highlights used by Chest Tracker. */
public final class Rendering {
    private static final RenderPipeline HIGHLIGHT_FILL_PIPELINE = RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(ChestTracker.id("pipeline/highlight_fill_see_through"))
            .withDepthStencilState(Optional.empty())
            .build();
    private static final RenderPipeline HIGHLIGHT_LINE_PIPELINE = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withLocation(ChestTracker.id("pipeline/highlight_line_see_through"))
            .withDepthStencilState(Optional.empty())
            .build();
    private static final RenderType HIGHLIGHT_FILL = RenderType.create(
            "chesttracker_highlight_fill_see_through",
            RenderSetup.builder(HIGHLIGHT_FILL_PIPELINE).sortOnUpload().createRenderSetup()
    );
    private static final RenderType HIGHLIGHT_LINES = RenderType.create(
            "chesttracker_highlight_line_see_through",
            RenderSetup.builder(HIGHLIGHT_LINE_PIPELINE)
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .createRenderSetup()
    );
    private static final int HIGHLIGHT_LIFETIME_TICKS = 200;
    private static final float BOX_SCALE = 1.02F;
    private static final float LINE_WIDTH = 3.0F;
    private static final int RED = 255;
    private static final int GREEN = 72;
    private static final int BLUE = 72;
    private static final Map<BlockPos, SearchResult> RESULTS = new HashMap<>();
    private static final Map<BlockPos, SearchResult> NAMED_RESULTS = new HashMap<>();
    private static int ticksSinceSearch = HIGHLIGHT_LIFETIME_TICKS + 1;
    private static boolean initialized;
    private static boolean irisApiChecked;
    private static Method irisGetInstance;
    private static Method irisShaderPackInUse;

    private Rendering() {}

    public static void setup() {
        if (initialized) return;
        initialized = true;
        NeoForge.EVENT_BUS.register(Rendering.class);
    }

    public static void registerPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(HIGHLIGHT_FILL_PIPELINE);
        event.registerPipeline(HIGHLIGHT_LINE_PIPELINE);
    }

    public static void addResults(Collection<SearchResult> results) {
        for (SearchResult result : results) {
            RESULTS.put(result.pos(), result);
            if (result.name() != null) NAMED_RESULTS.put(result.pos(), result);
        }
    }

    public static void setLastRequest(SearchRequest request) {}

    public static void clearResults() {
        RESULTS.clear();
        NAMED_RESULTS.clear();
    }

    public static void resetSearchTime() {
        ticksSinceSearch = 0;
    }

    public static Map<BlockPos, SearchResult> getResults() {
        return Collections.unmodifiableMap(RESULTS);
    }

    public static Map<BlockPos, SearchResult> getNamedResults() {
        return Collections.unmodifiableMap(NAMED_RESULTS);
    }

    public static void scheduleLabel(Vec3 pos, Component name, boolean seeThrough) {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (RESULTS.isEmpty()) return;
        if (ticksSinceSearch <= HIGHLIGHT_LIFETIME_TICKS) ticksSinceSearch++;
        if (ticksSinceSearch > HIGHLIGHT_LIFETIME_TICKS) clearResults();
    }

    @SubscribeEvent
    public static void onSubmitCustomGeometry(SubmitCustomGeometryEvent event) {
        if (RESULTS.isEmpty() || ticksSinceSearch > HIGHLIGHT_LIFETIME_TICKS || isIrisShaderPackInUse()) return;

        HighlightAlpha alpha = getHighlightAlpha();
        Vec3 cameraPos = event.getLevelRenderState().cameraRenderState.pos;
        PoseStack poseStack = event.getPoseStack();

        for (SearchResult result : RESULTS.values()) {
            submitBox(event, poseStack, cameraPos, result.pos(), alpha.fill(), alpha.line());
            for (BlockPos otherPos : result.otherPositions()) {
                submitBox(event, poseStack, cameraPos, otherPos, alpha.fill(), alpha.line());
            }
        }
    }

    /**
     * Iris performs the shader pack's final composite after normal submitted world geometry. Render the same 3D boxes
     * once more after the level and shader pipeline have finished so Complementary and other packs cannot cover them.
     * A fresh immediate buffer is required here; buffers from the earlier level stages have already been closed.
     */
    @SubscribeEvent
    public static void onRenderAfterShaders(RenderLevelStageEvent.AfterLevel event) {
        if (RESULTS.isEmpty() || ticksSinceSearch > HIGHLIGHT_LIFETIME_TICKS || !isIrisShaderPackInUse()) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;

        HighlightAlpha alpha = getHighlightAlpha();
        Vec3 cameraPos = event.getLevelRenderState().cameraRenderState.pos;
        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(new Matrix4f(event.getModelViewMatrix()));
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();

        VertexConsumer fillConsumer = buffers.getBuffer(HIGHLIGHT_FILL);
        for (SearchResult result : RESULTS.values()) {
            emitImmediateBox(poseStack, cameraPos, result.pos(), fillConsumer, null, alpha.fill(), alpha.line());
            for (BlockPos otherPos : result.otherPositions()) {
                emitImmediateBox(poseStack, cameraPos, otherPos, fillConsumer, null, alpha.fill(), alpha.line());
            }
        }
        buffers.endBatch(HIGHLIGHT_FILL);

        VertexConsumer lineConsumer = buffers.getBuffer(HIGHLIGHT_LINES);
        for (SearchResult result : RESULTS.values()) {
            emitImmediateBox(poseStack, cameraPos, result.pos(), null, lineConsumer, alpha.fill(), alpha.line());
            for (BlockPos otherPos : result.otherPositions()) {
                emitImmediateBox(poseStack, cameraPos, otherPos, null, lineConsumer, alpha.fill(), alpha.line());
            }
        }
        buffers.endBatch(HIGHLIGHT_LINES);
    }

    private static boolean isIrisShaderPackInUse() {
        if (!irisApiChecked) {
            irisApiChecked = true;
            try {
                Class<?> irisApi = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                irisGetInstance = irisApi.getMethod("getInstance");
                irisShaderPackInUse = irisApi.getMethod("isShaderPackInUse");
            } catch (ReflectiveOperationException ignored) {
                irisGetInstance = null;
                irisShaderPackInUse = null;
            }
        }
        if (irisGetInstance == null || irisShaderPackInUse == null) return false;
        try {
            return Boolean.TRUE.equals(irisShaderPackInUse.invoke(irisGetInstance.invoke(null)));
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private static HighlightAlpha getHighlightAlpha() {
        float progress = Mth.clamp(ticksSinceSearch / (float) HIGHLIGHT_LIFETIME_TICKS, 0.0F, 1.0F);
        float fade = progress <= 0.6F ? 1.0F : 1.0F - (progress - 0.6F) / 0.4F;
        float pulse = (Mth.sin(ticksSinceSearch * 0.4F) + 1.0F) * 0.5F;
        return new HighlightAlpha(
                (int) (70.0F * fade * (0.65F + pulse * 0.35F)),
                (int) (235.0F * fade)
        );
    }

    private static void emitImmediateBox(PoseStack poseStack, Vec3 cameraPos, BlockPos pos,
                                         VertexConsumer fillConsumer, VertexConsumer lineConsumer,
                                         int fillAlpha, int lineAlpha) {
        poseStack.pushPose();
        poseStack.translate(
                pos.getX() + 0.5D - cameraPos.x,
                pos.getY() + 0.5D - cameraPos.y,
                pos.getZ() + 0.5D - cameraPos.z
        );
        poseStack.scale(BOX_SCALE * 0.5F, BOX_SCALE * 0.5F, BOX_SCALE * 0.5F);
        if (fillConsumer != null) emitFilledCube(fillConsumer, poseStack.last().pose(), fillAlpha);
        if (lineConsumer != null) emitCubeOutline(lineConsumer, poseStack.last(), lineAlpha);
        poseStack.popPose();
    }

    private record HighlightAlpha(int fill, int line) {}

    private static void submitBox(SubmitCustomGeometryEvent event, PoseStack poseStack, Vec3 cameraPos,
                                  BlockPos pos, int fillAlpha, int lineAlpha) {
        poseStack.pushPose();
        poseStack.translate(
                pos.getX() + 0.5D - cameraPos.x,
                pos.getY() + 0.5D - cameraPos.y,
                pos.getZ() + 0.5D - cameraPos.z
        );
        poseStack.scale(BOX_SCALE * 0.5F, BOX_SCALE * 0.5F, BOX_SCALE * 0.5F);

        event.getSubmitNodeCollector().submitCustomGeometry(
                poseStack,
                HIGHLIGHT_FILL,
                (pose, consumer) -> emitFilledCube(consumer, pose.pose(), fillAlpha)
        );
        event.getSubmitNodeCollector().submitCustomGeometry(
                poseStack,
                HIGHLIGHT_LINES,
                (pose, consumer) -> emitCubeOutline(consumer, pose, lineAlpha)
        );

        poseStack.popPose();
    }

    private static void emitFilledCube(VertexConsumer consumer, Matrix4f matrix, int alpha) {
        quad(consumer, matrix, -1, -1, -1, -1, 1, -1, 1, 1, -1, 1, -1, -1, alpha);
        quad(consumer, matrix, -1, -1, 1, 1, -1, 1, 1, 1, 1, -1, 1, 1, alpha);
        quad(consumer, matrix, -1, -1, -1, 1, -1, -1, 1, -1, 1, -1, -1, 1, alpha);
        quad(consumer, matrix, -1, 1, -1, -1, 1, 1, 1, 1, 1, 1, 1, -1, alpha);
        quad(consumer, matrix, -1, -1, -1, -1, -1, 1, -1, 1, 1, -1, 1, -1, alpha);
        quad(consumer, matrix, 1, -1, -1, 1, 1, -1, 1, 1, 1, 1, -1, 1, alpha);
    }

    private static void quad(VertexConsumer consumer, Matrix4f matrix,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             float x3, float y3, float z3, float x4, float y4, float z4, int alpha) {
        consumer.addVertex(matrix, x1, y1, z1).setColor(RED, GREEN, BLUE, alpha);
        consumer.addVertex(matrix, x2, y2, z2).setColor(RED, GREEN, BLUE, alpha);
        consumer.addVertex(matrix, x3, y3, z3).setColor(RED, GREEN, BLUE, alpha);
        consumer.addVertex(matrix, x4, y4, z4).setColor(RED, GREEN, BLUE, alpha);
    }

    private static void emitCubeOutline(VertexConsumer consumer, PoseStack.Pose pose, int alpha) {
        line(consumer, pose, -1, -1, -1, 1, -1, -1, 1, 0, 0, alpha);
        line(consumer, pose, 1, -1, -1, 1, 1, -1, 0, 1, 0, alpha);
        line(consumer, pose, 1, 1, -1, -1, 1, -1, -1, 0, 0, alpha);
        line(consumer, pose, -1, 1, -1, -1, -1, -1, 0, -1, 0, alpha);
        line(consumer, pose, -1, -1, 1, 1, -1, 1, 1, 0, 0, alpha);
        line(consumer, pose, 1, -1, 1, 1, 1, 1, 0, 1, 0, alpha);
        line(consumer, pose, 1, 1, 1, -1, 1, 1, -1, 0, 0, alpha);
        line(consumer, pose, -1, 1, 1, -1, -1, 1, 0, -1, 0, alpha);
        line(consumer, pose, -1, -1, -1, -1, -1, 1, 0, 0, 1, alpha);
        line(consumer, pose, 1, -1, -1, 1, -1, 1, 0, 0, 1, alpha);
        line(consumer, pose, 1, 1, -1, 1, 1, 1, 0, 0, 1, alpha);
        line(consumer, pose, -1, 1, -1, -1, 1, 1, 0, 0, 1, alpha);
    }

    private static void line(VertexConsumer consumer, PoseStack.Pose pose,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             float nx, float ny, float nz, int alpha) {
        consumer.addVertex(pose, x1, y1, z1)
                .setColor(RED, GREEN, BLUE, alpha)
                .setNormal(pose, nx, ny, nz)
                .setLineWidth(LINE_WIDTH);
        consumer.addVertex(pose, x2, y2, z2)
                .setColor(RED, GREEN, BLUE, alpha)
                .setNormal(pose, nx, ny, nz)
                .setLineWidth(LINE_WIDTH);
    }
}
