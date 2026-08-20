package red.jackf.chesttracker.impl.gui.invbutton;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import red.jackf.chesttracker.impl.config.ChestTrackerConfig;
import red.jackf.chesttracker.impl.gui.invbutton.ui.InventoryButton;

import java.util.Optional;

/**
 * Handles data loading and screen events for the button.
 */
public class InventoryButtonFeature {
    public static void setup() {
    }

    public static Optional<InventoryButton> onScreenOpen(Minecraft client, Screen screen, int scaledWidth, int scaledHeight) {
        if (!ChestTrackerConfig.INSTANCE.instance().gui.inventoryButton.enabled) return Optional.empty();
        if (screen instanceof AbstractContainerScreen<?> menuScreen) {
            var position = ButtonPositionMap.getPositionFor(menuScreen);

            var context = Optional.ofNullable(((CTButtonScreenDuck) menuScreen).chesttracker$getContext());

            var target = context.flatMap(ctx -> Optional.ofNullable(ctx.getTarget()));

            InventoryButton button = new InventoryButton(menuScreen, position, target);

            ((CTButtonScreenDuck) menuScreen).chesttracker$setButton(button);

            return Optional.of(button);
        }
        return Optional.empty();
    }
}
