package red.jackf.chesttracker.impl.event;

import net.minecraft.resources.Identifier;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public final class EventFactory {
    private EventFactory() {
    }

    public static <T> Event<T> createArrayBacked(Class<T> type, Function<T[], T> invokerFactory) {
        return new SimpleEvent<>(type, invokerFactory);
    }

    public static <T> Event<T> createWithPhases(Class<T> type, Function<T[], T> invokerFactory, Identifier... phases) {
        return new SimpleEvent<>(type, invokerFactory);
    }

    private static final class SimpleEvent<T> implements Event<T> {
        private final Class<T> type;
        private final Function<T[], T> invokerFactory;
        private final List<Entry<T>> listeners = new ArrayList<>();

        private SimpleEvent(Class<T> type, Function<T[], T> invokerFactory) {
            this.type = type;
            this.invokerFactory = invokerFactory;
        }

        @Override
        public T invoker() {
            listeners.sort(Comparator.comparingInt(entry -> phaseOrder(entry.phase)));
            @SuppressWarnings("unchecked")
            T[] array = listeners.stream()
                    .map(entry -> entry.listener)
                    .toArray(size -> (T[]) Array.newInstance(type, size));
            return invokerFactory.apply(array);
        }

        @Override
        public void register(T listener) {
            register(Event.DEFAULT_PHASE, listener);
        }

        @Override
        public void register(Identifier phase, T listener) {
            listeners.add(new Entry<>(phase, listener));
        }

        private static int phaseOrder(Identifier phase) {
            String path = phase.getPath();
            if ("priority".equals(path)) return 0;
            if ("default".equals(path)) return 1;
            if ("fallback".equals(path)) return 2;
            return 1;
        }
    }

    private record Entry<T>(Identifier phase, T listener) {
    }
}
