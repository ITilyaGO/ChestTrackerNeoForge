package red.jackf.chesttracker.impl.gui.invbutton.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import org.apache.logging.log4j.Logger;
import com.google.gson.JsonParseException;
import red.jackf.chesttracker.impl.ChestTracker;
import red.jackf.chesttracker.impl.gui.invbutton.ButtonPositionMap;
import red.jackf.chesttracker.impl.gui.invbutton.position.ButtonPosition;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class InventoryButtonPositionLoader extends SimplePreparableReloadListener<Map<String, ButtonPosition>> {
    private static final Logger LOGGER = ChestTracker.getLogger("Button Position Loader");

    private static final FileToIdConverter LISTER = FileToIdConverter.json("chesttracker_button_positions");

    @Override
    protected Map<String, ButtonPosition> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<String, ButtonPosition> positions = new HashMap<>();

        for (var entry : LISTER.listMatchingResources(manager).entrySet()) {
            Identifier file = entry.getKey();
            Identifier id = LISTER.fileToId(file);

            Resource resource = entry.getValue();
            try (Reader reader = resource.openAsReader()) {
                JsonElement jsonElement = JsonParser.parseReader(reader);
                ButtonPositionDataFile result = ButtonPositionDataFile.CODEC
                        .parse(JsonOps.INSTANCE, jsonElement)
                        .getOrThrow(JsonParseException::new);

                for (String className : result.classNames()) {
                    positions.put(className, result.position());
                }
            } catch (Exception ex) {
                LOGGER.error("Couldn't read button positions {} from {} in data pack {}", id, file, resource.sourcePackId(), ex);
            }
        }

        return positions;
    }

    @Override
    protected void apply(Map<String, ButtonPosition> data, ResourceManager manager, ProfilerFiller profiler) {
        ButtonPositionMap.loadDatapackPositions(data);
    }

    public static void registerReloadListener(AddClientReloadListenersEvent event) {
        event.addListener(ChestTracker.id("inventory_button_positions"), new InventoryButtonPositionLoader());
    }
}
