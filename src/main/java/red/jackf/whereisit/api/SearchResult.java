package red.jackf.whereisit.api;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class SearchResult {
    private final BlockPos pos;
    private final @Nullable ItemStack item;
    private final @Nullable Component name;
    private final @Nullable Vec3 nameOffset;
    private final Set<BlockPos> otherPositions;

    private SearchResult(BlockPos pos, @Nullable ItemStack item, @Nullable Component name, @Nullable Vec3 nameOffset, Collection<BlockPos> otherPositions) {
        this.pos = pos.immutable();
        this.item = item == null ? null : item.copy();
        this.name = name;
        this.nameOffset = nameOffset;
        this.otherPositions = new HashSet<>(otherPositions);
        this.otherPositions.remove(this.pos);
    }

    public static Builder builder(BlockPos pos) {
        return new Builder(pos);
    }

    public BlockPos pos() {
        return pos;
    }

    public @Nullable ItemStack item() {
        return item;
    }

    public @Nullable Component name() {
        return name;
    }

    public Vec3 nameOffset() {
        return nameOffset == null ? new Vec3(0, 1, 0) : nameOffset;
    }

    public @Nullable Vec3 customNameOffset() {
        return nameOffset;
    }

    public Set<BlockPos> otherPositions() {
        return Set.copyOf(otherPositions);
    }

    public static final class Builder {
        private final BlockPos pos;
        private @Nullable ItemStack item;
        private @Nullable Component name;
        private @Nullable Vec3 nameOffset;
        private final Set<BlockPos> otherPositions = new HashSet<>();

        private Builder(BlockPos pos) {
            this.pos = pos;
        }

        public Builder item(ItemStack item) {
            this.item = item;
            return this;
        }

        public Builder name(Component name, @Nullable Vec3 offset) {
            this.name = name;
            this.nameOffset = offset;
            return this;
        }

        public Builder otherPositions(Collection<BlockPos> positions) {
            this.otherPositions.addAll(positions);
            return this;
        }

        /** Entity highlighting is not rendered by this reduced NeoForge port. */
        public Builder entityId(int entityId) {
            return this;
        }

        public SearchResult build() {
            return new SearchResult(pos, item, name, nameOffset, otherPositions);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SearchResult that)) return false;
        return Objects.equals(pos, that.pos) && Objects.equals(item, that.item) && Objects.equals(name, that.name) && Objects.equals(nameOffset, that.nameOffset) && Objects.equals(otherPositions, that.otherPositions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, item, name, nameOffset, otherPositions);
    }
}
