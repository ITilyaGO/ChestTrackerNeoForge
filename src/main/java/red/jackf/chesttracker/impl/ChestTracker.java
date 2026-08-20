package red.jackf.chesttracker.impl;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.api.ChestTrackerPlugin;
import red.jackf.chesttracker.impl.config.ChestTrackerConfig;
import red.jackf.chesttracker.impl.gui.DeveloperOverlay;
import red.jackf.chesttracker.impl.gui.invbutton.ButtonPositionMap;
import red.jackf.chesttracker.impl.gui.invbutton.InventoryButtonFeature;
import red.jackf.chesttracker.impl.gui.invbutton.data.InventoryButtonPositionLoader;
import red.jackf.chesttracker.impl.gui.screen.ChestTrackerScreen;
import red.jackf.chesttracker.impl.gui.util.ImagePixelReader;
import red.jackf.chesttracker.impl.memory.MemoryIntegrity;
import red.jackf.chesttracker.impl.providers.InteractionTrackerImpl;
import red.jackf.chesttracker.impl.providers.ProviderHandler;
import red.jackf.chesttracker.impl.rendering.NameRenderer;
import red.jackf.chesttracker.impl.storage.ConnectionSettings;
import red.jackf.chesttracker.impl.storage.Storage;
import red.jackf.whereisit.client.render.Rendering;

import java.util.ServiceLoader;

@Mod(value = ChestTracker.ID, dist = Dist.CLIENT)
public class ChestTracker {
    public static final String ID = "chesttracker";
    public static final Logger LOGGER = LogManager.getLogger();
    public static KeyMapping OPEN_GUI;
    public static final KeyMapping.Category CHESTTRACKER_CATEGORY =
            new KeyMapping.Category(Identifier.fromNamespaceAndPath(ID, "title"));
    private static boolean shouldSkipProviderForNextGuiClose = false;

    public ChestTracker(IEventBus modEventBus) {
        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(this::onRegisterReloadListeners);
        modEventBus.addListener(ChestTracker::registerKeyMappings);
        modEventBus.addListener(Rendering::registerPipelines);
        NeoForge.EVENT_BUS.register(ClientEventHandler.class);
        NeoForge.EVENT_BUS.register(ScreenEventHandler.class);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(ID, path);
    }

    public static Logger getLogger(String suffix) {
        return LogManager.getLogger(ChestTracker.class.getCanonicalName() + "/" + suffix);
    }

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        OPEN_GUI = new KeyMapping("key.chesttracker.open_gui", InputConstants.Type.KEYSYM, InputConstants.KEY_GRAVE, CHESTTRACKER_CATEGORY);
        event.register(OPEN_GUI);
    }

    private void onRegisterReloadListeners(AddClientReloadListenersEvent event) {
        ImagePixelReader.registerReloadListener(event);
        InventoryButtonPositionLoader.registerReloadListener(event);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            Rendering.setup();
            ChestTrackerConfig.init();
            LOGGER.debug("Loading ChestTracker (NeoForge)");
            InventoryButtonFeature.setup();
            ProviderHandler.INSTANCE.setupEvents();
            NameRenderer.setup();
            InteractionTrackerImpl.setup();
            MemoryIntegrity.setup();
            ImagePixelReader.setup();
            Storage.setup();
            DeveloperOverlay.setup();
            ConnectionSettings.load();
            ButtonPositionMap.loadUserPositions();
            loadPlugins();
        });
    }

    private void loadPlugins() {
        for (ChestTrackerPlugin plugin : ServiceLoader.load(ChestTrackerPlugin.class)) {
            LOGGER.debug("Loading ChestTracker plugin: {}", plugin.getClass().getName());
            plugin.load();
        }
        new DefaultChestTrackerPlugin().load();
        new red.jackf.chesttracker.impl.compat.mods.ChestTrackerWhereIsItPlugin().load();
    }

    public static void openInGame(Minecraft client, @Nullable Screen parent) {
        client.setScreen(new ChestTrackerScreen(parent));
    }

    public static void skipProviderForNextGuiClose() {
        shouldSkipProviderForNextGuiClose = true;
    }

    public static boolean shouldSkipProvider() {
        return shouldSkipProviderForNextGuiClose;
    }

    public static void clearSkipProvider() {
        shouldSkipProviderForNextGuiClose = false;
    }
}
