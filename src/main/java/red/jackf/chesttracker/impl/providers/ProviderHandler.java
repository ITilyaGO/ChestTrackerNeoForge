package red.jackf.chesttracker.impl.providers;

import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.api.ClientBlockSource;
import red.jackf.chesttracker.api.providers.InteractionTracker;
import red.jackf.chesttracker.api.providers.ServerProvider;
import red.jackf.chesttracker.api.providers.context.BlockPlacedContext;
import red.jackf.chesttracker.impl.events.AfterPlayerPlaceBlock;
import red.jackf.chesttracker.impl.memory.MemoryBankAccessImpl;
import red.jackf.chesttracker.impl.util.CachedClientBlockSource;
import red.jackf.jackfredlib.client.api.gps.Coordinate;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

public class ProviderHandler {
    public static final ProviderHandler INSTANCE = new ProviderHandler();
    private final Set<ServerProvider> REGISTERED_PROVIDERS = Sets.newHashSet();
    private ServerProvider currentProvider = null;
    private @Nullable Coordinate lastCoordinate = null;

    private ProviderHandler() {
    }

    public <T extends ServerProvider> T register(T provider) {
        this.REGISTERED_PROVIDERS.add(provider);
        return provider;
    }

    private void load(Coordinate coordinate) {
        if (this.currentProvider != null) {
            this.unload();
        }

        REGISTERED_PROVIDERS.stream()
                .sorted(Comparator.comparingInt(ServerProvider::getPriority).reversed())
                .filter(provider -> provider.appliesTo(coordinate))
                .findFirst()
                .ifPresent(serverProvider -> {
                    this.currentProvider = serverProvider;
                    serverProvider.onConnect(coordinate);
                });
    }

    private void unload() {
        if (this.currentProvider == null) return;
        this.currentProvider.onDisconnect();
        this.currentProvider = null;
        MemoryBankAccessImpl.INSTANCE.unload();
    }

    public Optional<ServerProvider> getCurrentProvider() {
        return Optional.ofNullable(currentProvider);
    }

    public void setupEvents() {
        NeoForge.EVENT_BUS.register(new ForgeEventHandler());
        AfterPlayerPlaceBlock.EVENT.register((clientLevel, pos, state, placementStack) ->
                getCurrentProvider().ifPresent(provider ->
                        MemoryBankAccessImpl.INSTANCE.getLoadedInternal().ifPresent(bank -> {
                            if (!bank.getMetadata().getFilteringSettings().autoAddPlacedBlocks.blockPredicate.test(state))
                                return;

                            ClientBlockSource cbs = new CachedClientBlockSource(clientLevel, pos, state);
                            provider.onBlockPlaced(BlockPlacedContext.create(cbs, placementStack));
                        })));
    }

    private class ForgeEventHandler {
        @SubscribeEvent
        public void onPlayerLoggedIn(ClientPlayerNetworkEvent.LoggingIn event) {
            Minecraft.getInstance().execute(() -> {
                Optional<Coordinate> coord = Coordinate.getCurrent();
                if (coord.isPresent()) {
                    if (!coord.get().equals(ProviderHandler.this.lastCoordinate)) {
                        ProviderHandler.this.lastCoordinate = coord.get();
                        ProviderHandler.this.load(coord.get());
                    }
                } else {
                    ProviderHandler.this.unload();
                }
            });
        }

        @SubscribeEvent
        public void onPlayerLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
            ProviderHandler.this.lastCoordinate = null;
            Minecraft.getInstance().execute(ProviderHandler.this::unload);
        }

        @SubscribeEvent
        public void onChatReceived(ClientChatReceivedEvent event) {
            boolean overlay = event instanceof ClientChatReceivedEvent.System system && system.isOverlay();
            ProviderHandler.this.getCurrentProvider().ifPresent(provider -> provider.onGameMessageReceived(event.getMessage(), overlay));
        }
    }
}
