package red.jackf.whereisit.client.api.events;

import red.jackf.chesttracker.impl.event.Event;
import red.jackf.chesttracker.impl.event.EventFactory;

import java.util.Arrays;

public interface ShouldIgnoreKey {
    Event<ShouldIgnoreKey> EVENT = EventFactory.createArrayBacked(ShouldIgnoreKey.class, listeners -> () ->
            Arrays.stream(listeners).anyMatch(ShouldIgnoreKey::shouldIgnoreKey)
    );

    boolean shouldIgnoreKey();
}
