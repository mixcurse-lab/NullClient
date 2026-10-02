package platform.api.system.configs;

import platform.api.utils.auction.ItemFilter;
import platform.api.system.configs.AttributeCondition;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public class AttributeProcessor implements ItemFilter {
    private final List<AttributeCondition> a = new ArrayList();

    public AttributeProcessor a(AttributeCondition condition) {
        this.a.add(condition);
        return this;
    }

    @Override
    public boolean a(ItemStack stack) {
        return this.a.stream().allMatch(condition -> {
            return condition.a(stack);
        });
    }
}



