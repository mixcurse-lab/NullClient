package platform.api.module;

import static platform.api.module.Interface.aM_;

import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

public interface Interface {
    public static final Minecraft aM_ = Minecraft.getInstance();
    static boolean isHumanoidArmor(ItemStack stack) {
        if (stack.has(DataComponents.EQUIPPABLE)) {
            return stack.get(DataComponents.EQUIPPABLE).slot().isArmor();
        }
        return false;
    }
}



