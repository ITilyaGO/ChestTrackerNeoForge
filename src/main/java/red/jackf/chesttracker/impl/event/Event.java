package red.jackf.chesttracker.impl.event;

import net.minecraft.resources.Identifier;

@SuppressWarnings("unused")
public interface Event<T> {
    Identifier DEFAULT_PHASE = Identifier.withDefaultNamespace("default");

    T invoker();

    void register(T listener);

    void register(Identifier phase, T listener);
}
