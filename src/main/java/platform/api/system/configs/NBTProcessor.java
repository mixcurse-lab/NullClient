package platform.api.system.configs;

import platform.api.utils.auction.ItemFilter;
import platform.api.system.configs.NBTCondition;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.minecraft.world.item.ItemStack;

public class NBTProcessor implements ItemFilter {
    private final List<NBTCondition> a = new ArrayList();

    @Generated
    public List<NBTCondition> a() {
        return this.a;
    }

    public NBTProcessor a(NBTCondition condition) {
        this.a.add(condition);
        return this;
    }

    public NBTProcessor a(String value) {
        return a(new NBTCondition(value));
    }

    @Override
    public boolean a(ItemStack stack) {
        return this.a.stream().allMatch(condition -> {
            return condition.a(stack);
        });
    }
}



