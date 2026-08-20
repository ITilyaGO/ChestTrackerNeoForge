package red.jackf.chesttracker.impl.mixinplugin;

import net.neoforged.fml.ModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Conditionally loads mixins used for mod compatibility based on whether the mod is loaded. Prevents log spam.
 */
public class ChestTrackerMixinPlugin implements IMixinConfigPlugin {
    private String prefix = "";

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        String trimmed = mixinClassName.substring(prefix.length());

        if (trimmed.startsWith("compat.")) {
            String modid = trimmed.split("\\.")[1];
            return ModList.get().isLoaded(modid);
        } else {
            return true;
        }
    }

    @Override
    public void onLoad(String mixinPackage) {
        this.prefix = mixinPackage + ".";
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
