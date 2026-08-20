package red.jackf.whereisit.api;

import net.minecraft.resources.Identifier;
import red.jackf.chesttracker.impl.event.Event;

public interface EventPhases {
    Identifier PRIORITY = Identifier.fromNamespaceAndPath("whereisit", "priority");
    Identifier DEFAULT = Event.DEFAULT_PHASE;
    Identifier FALLBACK = Identifier.fromNamespaceAndPath("whereisit", "fallback");
}
