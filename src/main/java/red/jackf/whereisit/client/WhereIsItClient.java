package red.jackf.whereisit.client;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.api.SearchResult;
import red.jackf.whereisit.client.api.events.SearchInvoker;
import red.jackf.whereisit.client.render.Rendering;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class WhereIsItClient {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static boolean closedScreenThisSearch = false;
    private static final List<SearchResult> RESULTS = new ArrayList<>();

    private WhereIsItClient() {
    }

    public static boolean doSearch(SearchRequest request) {
        closedScreenThisSearch = false;
        Rendering.resetSearchTime();
        Rendering.clearResults();
        Rendering.setLastRequest(request);
        List<SearchResult> results = new ArrayList<>();
        boolean started = SearchInvoker.EVENT.invoker().search(request, newResults -> {
            results.addAll(newResults);
            Rendering.addResults(newResults);
        });
        recieveResults(results);
        if (!results.isEmpty() && Minecraft.getInstance().player != null && Minecraft.getInstance().screen != null) {
            closedScreenThisSearch = true;
            Minecraft.getInstance().player.closeContainer();
        }
        return started;
    }

    public static void clearResults() {
        RESULTS.clear();
        Rendering.clearResults();
    }

    public static void recieveResults(Collection<SearchResult> newResults) {
        RESULTS.clear();
        RESULTS.addAll(newResults);
        Rendering.addResults(newResults);
    }

    public static List<SearchResult> getResults() {
        return List.copyOf(RESULTS);
    }
}
