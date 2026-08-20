package red.jackf.whereisit.api.search;

import red.jackf.chesttracker.impl.event.Event;
import red.jackf.chesttracker.impl.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface ConnectedBlocksGrabber {
    Event<ConnectedBlocksGrabber> EVENT = EventFactory.createArrayBacked(ConnectedBlocksGrabber.class, listeners -> (positions, pos, level, state) -> {
        for (ConnectedBlocksGrabber listener : listeners) {
            listener.getConnected(positions, pos, level, state);
        }
    });

    void getConnected(Set<BlockPos> positions, BlockPos pos, Level level, BlockState state);

    static List<BlockPos> getConnected(Level level, BlockState state, BlockPos pos) {
        Set<BlockPos> positions = new HashSet<>();
        positions.add(pos.immutable());
        if (state.getBlock() instanceof ChestBlock && state.hasProperty(ChestBlock.TYPE) && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
            positions.add(pos.relative(ChestBlock.getConnectedDirection(state)));
        }
        EVENT.invoker().getConnected(positions, pos, level, state);
        return positions.stream().sorted(Comparator.comparingLong(BlockPos::asLong)).toList();
    }
}
