package red.jackf.chesttracker.impl.gui.util;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import red.jackf.chesttracker.impl.ChestTracker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Reads colours from an image every reload. Used to get text colours .
 */
public class ImagePixelReader {
    private static final Identifier TEXTURE = ChestTracker.id("textures/gui/text_colours.png");

    private ImagePixelReader() {
    }

    private static final List<Consumer<Integer>> results = new ArrayList<>();
    private static final List<Function<NativeImage, Integer>> hooks = new ArrayList<>();

    @SuppressWarnings("SameParameterValue")
    private static void addPixelColourListener(int x, int y, int defaultColour, Consumer<Integer> result) {
        hooks.add(image -> {
            if (image.getWidth() > x && image.getHeight() > y) {
                return image.getPixel(x, y);
            } else {
                return defaultColour;
            }
        });

        results.add(result);
    }

    public static class TitleListener extends SimplePreparableReloadListener<List<Integer>> {
        @Override
        protected List<Integer> prepare(ResourceManager manager, ProfilerFiller profiler) {
            var resource = manager.getResource(TEXTURE);
            var list = new ArrayList<Integer>();
            if (resource.isEmpty()) {
                ChestTracker.LOGGER.warn("Texture {} not found", TEXTURE);
            } else {
                try (var image = NativeImage.read(resource.get().open())) {
                    for (var hook : hooks)
                        list.add(hook.apply(image));
                } catch (IOException e) {
                    ChestTracker.LOGGER.warn("Error loading %s: ".formatted(TEXTURE), e);
                }
            }
            return list;
        }

        @Override
        protected void apply(List<Integer> data, ResourceManager manager, ProfilerFiller profiler) {
            for (int i = 0; i < data.size(); i++) {
                results.get(i).accept(data.get(i));
            }
        }
    }

    public static void setup() {
        addPixelColourListener(2, 5, 0x404040, TextColours::setLabelColour);
        addPixelColourListener(2, 14, 0xFFFFFF, TextColours::setTextColour);
        addPixelColourListener(2, 23, 0x808080, TextColours::setHintColour);
        addPixelColourListener(2, 31, 0x669BBC, TextColours::setSearchKeyColour);
        addPixelColourListener(2, 41, 0xEECC77, TextColours::setSearchTermColour);
        addPixelColourListener(2, 49, 0xFF0000, TextColours::setErrorColour);
    }

    public static void registerReloadListener(AddClientReloadListenersEvent event) {
        event.addListener(ChestTracker.id("pixel_colour_listener"), new ImagePixelReader.TitleListener());
    }
}
