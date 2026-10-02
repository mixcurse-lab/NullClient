package platform.api.utils.auction;

import platform.client.utils.lib.javassist.TokenId;
import platform.api.system.interfaces.NativeMethodLookup;
import platform.client.utils.lib.json.JSONArray;
import platform.client.utils.lib.json.JSONObject;
import platform.api.module.InterfaceC0020Opcode;

import platform.api.system.configs.AttributeCondition;
import platform.api.system.configs.ConfigProcessor;
import platform.api.system.configs.PotionCondition;

import platform.client.utils.render.AnimationUtil;
import platform.api.system.configs.AttributeProcessor;
import platform.api.system.configs.DescriptionProcessor;
import platform.api.system.configs.EnchantmentProcessor;
import platform.api.system.configs.NBTProcessor;
import platform.api.system.configs.PotionProcessor;
import platform.api.annotation.Compile;
import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import lombok.Generated;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;

public class AutoBuyProcessor extends ConfigProcessor<AutoBuyEntry> {
    @Override
    @Compile
    protected List<AutoBuyEntry> a(String str) {
        if (this.d.isEmpty()) {
            this.d.addAll(Arrays.asList(AutoBuyEntry.values()));
        }
        JSONArray jSONArray = new JSONArray(str);
        for (int i = 0; i < jSONArray.a(); i++) {
            JSONObject jSONObjectJ = jSONArray.j(i);
            String strL = jSONObjectJ.l("name");
            for (Object obj : this.d) {
                if (!(obj instanceof AutoBuyEntry)) {
                    throw new ClassCastException();
                }
                AutoBuyEntry aVar = (AutoBuyEntry) obj;
                if (aVar.b().equals(strL)) {
                    if (jSONObjectJ.m("status")) {
                        aVar.a(jSONObjectJ.b("status"));
                    }
                    if (jSONObjectJ.m("price")) {
                        aVar.a(jSONObjectJ.e("price"));
                    }
                }
            }
        }
        return new ArrayList(this.d);
    }

    @Override
    @Compile
    protected String a(List<AutoBuyEntry> data) {
        JSONArray jSONArray = new JSONArray();
        for (AutoBuyEntry aVar : data) {
            JSONObject jSONObject = new JSONObject();
            if (!(aVar instanceof AutoBuyEntry)) {
                throw new ClassCastException();
            }
            AutoBuyEntry aVar2 = aVar;
            jSONObject.c("name", aVar2.b());
            jSONObject.b("status", aVar2.l());
            jSONObject.b("price", aVar2.k());
            jSONArray.a(jSONObject);
        }
        return jSONArray.E(2);
    }

    static {
        NativeMethodLookup.lookup(AutoBuyProcessor.class, 20);
    }

    public AutoBuyProcessor() {
        this.d.addAll(Arrays.asList(AutoBuyEntry.values()));
    }

    @Override
    protected String b() {
        return "autobuy.json";
    }
}




