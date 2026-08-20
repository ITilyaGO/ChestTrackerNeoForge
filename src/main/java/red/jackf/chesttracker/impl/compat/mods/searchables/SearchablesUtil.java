package red.jackf.chesttracker.impl.compat.mods.searchables;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.world.item.ItemStack;
import red.jackf.chesttracker.impl.gui.widget.CustomEditBox;
import red.jackf.chesttracker.impl.util.ItemStacks;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class SearchablesUtil {
    public static final ItemStackSearch ITEM_STACK = new ItemStackSearch();

    public static EditBox getEditBox(
            Font font,
            int x,
            int y,
            int width,
            int height,
            EditBox previous,
            Supplier<List<ItemStack>> itemSupplier,
            Consumer<String> callback) {
        CustomEditBox box = new CustomEditBox(font, x, y, width, height, previous, CustomEditBox.SEARCH_MESSAGE);
        box.setHint(CustomEditBox.SEARCH_MESSAGE);
        box.setResponder(callback);
        return box;
    }

    public static boolean ifSearchables(EditBox box, Predicate<AbstractWidget> ifSearchablesBox) {
        return false;
    }

    public static AbstractWidget getWrappedAutocomplete(EditBox search) {
        throw new IllegalStateException("Searchables autocomplete is not available in the NeoForge port");
    }

    public static class ItemStackSearch {
        public List<ItemStack> filterEntries(List<ItemStack> items, String filter) {
            return items.stream()
                    .filter(stack -> ItemStacks.defaultPredicate(stack, filter.toLowerCase()))
                    .toList();
        }
    }
}
