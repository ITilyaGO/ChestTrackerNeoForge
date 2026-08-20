package red.jackf.whereisit.api;

import net.minecraft.world.item.ItemStack;
import red.jackf.whereisit.api.search.NestedItemsGrabber;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class SearchRequest implements Consumer<ItemStack> {
    private final List<Predicate<ItemStack>> predicates = new ArrayList<>();
    private ItemStack targetStack = ItemStack.EMPTY;

    public static boolean check(ItemStack stack, SearchRequest request) {
        if (request.test(stack)) return true;
        return NestedItemsGrabber.get(stack).anyMatch(request::test);
    }

    @Override
    public void accept(ItemStack stack) {
        setTargetStack(stack);
    }

    public SearchRequest setTargetStack(ItemStack stack) {
        this.targetStack = stack.copy();
        return this;
    }

    public SearchRequest addPredicate(Predicate<ItemStack> predicate) {
        this.predicates.add(predicate);
        return this;
    }

    public boolean hasCriteria() {
        return !targetStack.isEmpty() || !predicates.isEmpty();
    }

    private boolean test(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (!targetStack.isEmpty() && !ItemStack.isSameItemSameComponents(stack, targetStack)) return false;
        for (Predicate<ItemStack> predicate : predicates) {
            if (!predicate.test(stack)) return false;
        }
        return true;
    }
}
