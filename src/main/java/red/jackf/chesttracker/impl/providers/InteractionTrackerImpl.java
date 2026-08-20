package red.jackf.chesttracker.impl.providers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.InteractionHand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.impl.ChestTracker;
import red.jackf.chesttracker.api.ClientBlockSource;
import red.jackf.chesttracker.api.providers.InteractionTracker;
import red.jackf.chesttracker.impl.util.CachedClientBlockSource;

import java.util.Optional;

public class InteractionTrackerImpl implements InteractionTracker {
    public static final InteractionTrackerImpl INSTANCE = new InteractionTrackerImpl();
    private static final Logger LOGGER = ChestTracker.getLogger("InteractionTracker");

    private @Nullable ClientBlockSource lastSource = null;
    private @Nullable EntityInteraction lastEntity = null;
    private @Nullable InteractionType lastType = null;

    public static void setup() {
        NeoForge.EVENT_BUS.register(EventHandler.class);
    }

    @Override
    public Optional<ClientLevel> getPlayerLevel() {
        if (Minecraft.getInstance().level == null) return Optional.empty();
        return Optional.of(Minecraft.getInstance().level);
    }

    @Override
    public Optional<ClientBlockSource> getLastBlockSource() {
        return Optional.ofNullable(lastSource);
    }

    @Override
    public Optional<EntityInteraction> getLastEntity() {
        return Optional.ofNullable(lastEntity);
    }

    @Override
    public Optional<InteractionType> getLastInteractionType() {
        return Optional.ofNullable(lastType);
    }

    public void clear() {
        this.lastSource = null;
        this.lastEntity = null;
        this.lastType = null;
        LOGGER.debug("[Clear] block & entity cleared");
    }

    private void clearLastBlockSource() {
        this.lastSource = null;
    }

    private void clearLastEntity() {
        this.lastEntity = null;
    }

    public void setLastBlockSource(ClientBlockSource source) {
        this.lastSource = source;
        this.lastEntity = null;
        this.lastType = InteractionType.BLOCK;
        LOGGER.debug("[SetBlock] pos={} dim={}", source.pos().toShortString(), source.level().dimension().identifier());
    }

    public void setLastEntity(EntityInteraction entity) {
        this.lastEntity = entity;
        this.lastSource = null;
        this.lastType = InteractionType.ENTITY;
        LOGGER.debug("[SetEntity] id={} uuid={} pos={}", entity.entityId(), entity.entityUuid(), entity.pos().toShortString());
    }

    private static final class EventHandler {
        @SubscribeEvent
        public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
            if (event.getHand() == InteractionHand.MAIN_HAND && event.getLevel() instanceof ClientLevel clientLevel) {
                INSTANCE.setLastBlockSource(new CachedClientBlockSource(clientLevel, event.getPos()));
            }
        }

        @SubscribeEvent
        public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
            if (event.getHand() != InteractionHand.MAIN_HAND || !(event.getLevel() instanceof ClientLevel)) return;
            var entity = event.getTarget();
            if (entity instanceof net.minecraft.world.Container) {
                INSTANCE.setLastEntity(new EntityInteraction(entity.getId(), entity.getUUID(), entity.blockPosition()));
            } else {
                INSTANCE.clear();
            }
        }

        @SubscribeEvent
        public static void onPlayerLogout(ClientPlayerNetworkEvent.LoggingOut event) {
            INSTANCE.clear();
        }
    }
}
