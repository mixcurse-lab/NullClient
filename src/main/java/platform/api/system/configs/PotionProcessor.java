package platform.api.system.configs;

import platform.api.utils.auction.ItemFilter;
import platform.api.system.configs.PotionCondition;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.minecraft.world.item.ItemStack;

public class PotionProcessor implements ItemFilter {
    private final List<PotionCondition> a = new ArrayList();

    @Generated
    public List<PotionCondition> a() {
        return this.a;
    }

    public PotionProcessor a(PotionCondition condition) {
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



