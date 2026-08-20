package red.jackf.whereisit.client.api.events;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import red.jackf.whereisit.api.SearchRequest;

import java.util.function.Consumer;

public interface SearchRequestPopulator {
    void grabStack(SearchRequest request, Screen screen, int mouseX, int mouseY);

    static void addItemStack(Consumer<ItemStack> consumer, ItemStack stack, Context context) {
        if (!stack.isEmpty()) {
            consumer.accept(stack);
        }
    }

    enum Context {
        INVENTORY,
        INVENTORY_PRECISE,
        RECIPE,
        OVERLAY,
        OVERLAY_ALTERNATE,
        FAVOURITE
    }
}
