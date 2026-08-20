package red.jackf.whereisit.client.render;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.api.SearchResult;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Search result state used by Chest Tracker. World overlays are temporarily
 * disabled in this NeoForge port until the 26.1 render-state API is supported.
 */
public final class Rendering {
    private static final Map<BlockPos, SearchResult> RESULTS = new HashMap<>();
    private static final Map<BlockPos, SearchResult> NAMED_RESULTS = new HashMap<>();

    private Rendering() {}

    public static void setup() {}

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

    public static void resetSearchTime() {}

    public static Map<BlockPos, SearchResult> getResults() {
        return Collections.unmodifiableMap(RESULTS);
    }

    public static Map<BlockPos, SearchResult> getNamedResults() {
        return Collections.unmodifiableMap(NAMED_RESULTS);
    }

    public static void scheduleLabel(Vec3 pos, Component name, boolean seeThrough) {}
}
