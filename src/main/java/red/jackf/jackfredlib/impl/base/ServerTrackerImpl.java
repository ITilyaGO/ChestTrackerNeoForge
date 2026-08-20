package red.jackf.jackfredlib.impl.base;

import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.jetbrains.annotations.Nullable;
import red.jackf.jackfredlib.api.base.ServerTracker;

public enum ServerTrackerImpl implements ServerTracker {
    INSTANCE;

    private MinecraftServer server;

    static {
        NeoForge.EVENT_BUS.register(INSTANCE);
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        this.server = event.getServer();
    }

    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent event) {
        this.server = null;
    }

    @Override
    public @Nullable MinecraftServer getServer() {
        return this.server;
    }
}
