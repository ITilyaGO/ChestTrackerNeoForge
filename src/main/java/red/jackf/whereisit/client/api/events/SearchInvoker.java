package red.jackf.whereisit.client.api.events;

import red.jackf.chesttracker.impl.event.Event;
import red.jackf.chesttracker.impl.event.EventFactory;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.api.SearchResult;
import red.jackf.whereisit.client.WhereIsItClient;

import java.util.Collection;
import java.util.function.Consumer;

public interface SearchInvoker {
    Event<SearchInvoker> EVENT = EventFactory.createArrayBacked(SearchInvoker.class, listeners -> (request, resultConsumer) -> {
        boolean started = false;
        for (SearchInvoker listener : listeners) {
            started |= listener.search(request, resultConsumer);
        }
        return started;
    });

    static boolean doSearch(SearchRequest request) {
        return WhereIsItClient.doSearch(request);
    }

    boolean search(SearchRequest request, Consumer<Collection<SearchResult>> resultConsumer);
}
