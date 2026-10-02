package platform.api.system.configs;


import platform.client.utils.text.StringUtils;
import static platform.api.module.Interface.aM_;
import platform.api.module.Interface;

import lombok.Generated;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;

public class NBTCondition {
    private final String a;

    @Generated
    public String a() {
        return this.a;
    }

    public NBTCondition(String value) {
        this.a = value;
    }

    public boolean a(ItemStack stack) {
        Tag nbt = ItemStack.CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE, Interface.aM_.level.registryAccess()), stack).result().orElse(null);
        return nbt != null && nbt.toString().replaceAll("§.", "").toLowerCase().replaceAll("\\s+", StringUtils.a).trim().contains(this.a.replaceAll("§.", "").toLowerCase().replaceAll("\\s+", StringUtils.a).trim());
    }
}



