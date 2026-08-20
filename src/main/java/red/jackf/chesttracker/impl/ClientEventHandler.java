package red.jackf.chesttracker.impl;

import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import red.jackf.chesttracker.impl.compat.mods.ShareEnderChestIntegration;
import red.jackf.chesttracker.impl.memory.MemoryBankAccessImpl;

public final class ClientEventHandler {
    private static boolean lateIntegrationsInitialized;

    private ClientEventHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft client = Minecraft.getInstance();

        // NeoForge can start ticking while the initial resource reload is still
        // running. Item component prototypes are not bound until a client world
        // is ready, so integrations that create ItemStacks must wait for it.
        if (!lateIntegrationsInitialized && client.level != null) {
            lateIntegrationsInitialized = true;
            ShareEnderChestIntegration.setup();
        }

        if (client.screen == null && client.getOverlay() == null) {
            while (ChestTracker.OPEN_GUI != null && ChestTracker.OPEN_GUI.consumeClick()) {
                ChestTracker.openInGame(client, null);
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Pre event) {
        if (!event.getLevel().isClientSide()) return;
        MemoryBankAccessImpl.INSTANCE.getLoadedInternal().ifPresent(bank -> bank.getMetadata().incrementLoadedTime());
    }
}
