package red.jackf.chesttracker.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import red.jackf.chesttracker.api.gui.ScreenBlacklist;
import red.jackf.chesttracker.impl.config.ChestTrackerConfig;
import red.jackf.chesttracker.impl.gui.invbutton.CTButtonScreenDuck;
import red.jackf.chesttracker.impl.gui.invbutton.InventoryButtonFeature;
import red.jackf.chesttracker.impl.gui.util.CTTitleOverrideDuck;
import red.jackf.chesttracker.impl.memory.MemoryBankAccessImpl;
import red.jackf.chesttracker.impl.memory.MemoryKeyImpl;
import red.jackf.chesttracker.impl.memory.key.OverrideInfo;
import red.jackf.chesttracker.impl.providers.InteractionTrackerImpl;
import red.jackf.chesttracker.impl.providers.ProviderHandler;
import red.jackf.chesttracker.impl.providers.ScreenCloseContextImpl;
import red.jackf.chesttracker.impl.providers.ScreenOpenContextImpl;
import red.jackf.whereisit.client.api.events.ShouldIgnoreKey;

import java.util.Optional;

public final class ScreenEventHandler {
    private ScreenEventHandler() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onScreenInit(ScreenEvent.Init.Pre event) {
        Screen screen = event.getScreen();
        if (screen instanceof AbstractContainerScreen<?> containerScreen) {
            ProviderHandler.INSTANCE.getCurrentProvider().ifPresent(provider -> {
                ScreenOpenContextImpl openContext = ScreenOpenContextImpl.createFor(containerScreen);
                provider.onScreenOpen(openContext);
                ((CTButtonScreenDuck) containerScreen).chesttracker$setContext(openContext);

                if (ChestTrackerConfig.INSTANCE.instance().gui.useCustomNameInGUIs) {
                    MemoryBankAccessImpl.INSTANCE.getLoadedInternal().ifPresent(bank -> {
                        if (openContext.getTarget() != null) {
                            Optional<MemoryKeyImpl> key = bank.getKeyInternal(openContext.getTarget().memoryKey());
                            if (key.isPresent()) {
                                OverrideInfo override = key.get().overrides().get(openContext.getTarget().position());
                                if (override != null && override.getCustomName() != null) {
                                    ((CTTitleOverrideDuck) containerScreen).chesttracker$setTitleOverride(Component.literal(override.getCustomName()));
                                } else {
                                    ((CTTitleOverrideDuck) containerScreen).chesttracker$clearTitleOverride();
                                }
                            }
                        }
                    });
                }
            });
        }
    }

    @SubscribeEvent
    public static void onScreenInitPost(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        if (screen instanceof PauseScreen) MemoryBankAccessImpl.INSTANCE.save();
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return;
        if (screen instanceof AbstractContainerScreen<?>) {
            InventoryButtonFeature.onScreenOpen(client, screen, event.getScreen().width, event.getScreen().height)
                    .ifPresent(event::addListener);
        }
    }

    @SubscribeEvent
    public static void onScreenKeyPressed(ScreenEvent.KeyPressed.Post event) {
        Screen screen = event.getScreen();
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return;
        if (screen instanceof AbstractContainerScreen<?>) {
            if (ShouldIgnoreKey.EVENT.invoker().shouldIgnoreKey()) return;
            if (ChestTracker.OPEN_GUI != null && ChestTracker.OPEN_GUI.matches(event.getKeyEvent())) {
                ChestTracker.openInGame(client, screen);
            }
        }
    }

    @SubscribeEvent
    public static void onScreenClose(ScreenEvent.Closing event) {
        Screen screen = event.getScreen();
        if (screen instanceof AbstractContainerScreen<?> containerScreen) {
            if (!ScreenBlacklist.isBlacklisted(screen.getClass())) {
                if (!ChestTracker.shouldSkipProvider()) {
                    ProviderHandler.INSTANCE.getCurrentProvider().ifPresent(provider ->
                            provider.onScreenClose(ScreenCloseContextImpl.createFor(containerScreen)));
                    InteractionTrackerImpl.INSTANCE.clear();
                } else {
                    ChestTracker.clearSkipProvider();
                }
            } else {
                ChestTracker.LOGGER.debug("Blacklisted screen class, ignoring");
            }
        }
    }
}
