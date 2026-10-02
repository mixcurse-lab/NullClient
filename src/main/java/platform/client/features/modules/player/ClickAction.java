package platform.client.features.modules.player;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.module.Module;
import platform.client.utils.text.ChatUtil;

import platform.api.module.Category;
import platform.api.module.Interface;
import platform.api.module.ModuleRegister;

import platform.api.module.setting.BindSetting;
import lombok.Generated;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.client.player.LocalPlayer;

@ModuleRegister(a = "Click Action", b = "Выполняет действие, привязанное к выбранной клавише", c = Category.Player)
public class ClickAction extends Module implements Interface {
    private final BindSetting b = new BindSetting("Эндер-жемчуг", -1).a(() -> {
        Delta.h().d().v().b().a(Items.ENDER_PEARL.getDefaultInstance());
    });
    private final BindSetting c = new BindSetting("Добавление друга", -1).a(() -> {
        AbstractClientPlayer class_746Var;
        EntityHitResult class_3966Var = aM_.hitResult instanceof EntityHitResult ? (EntityHitResult) aM_.hitResult : null;
        if (class_3966Var instanceof EntityHitResult) {
            EntityHitResult hit = class_3966Var;
            if (hit.getEntity() instanceof AbstractClientPlayer class_746VarMethod_17782) {
                class_746Var = class_746VarMethod_17782;
                if (class_746Var != aM_.player) {
                    String name = class_746Var.getName().getString();
                    if (Delta.h().d().e().d(name)) {
                        Delta.h().d().e().c(name);
                        Delta.h().d().e().unSetup();
                        ChatUtil.a((Object) ("Товарищ " + name + " был успешно удален из списка друзей."));
                    } else {
                        Delta.h().d().e().b(name);
                        Delta.h().d().e().unSetup();
                        ChatUtil.a((Object) ("Товарищ " + name + " был успешно добавлен в список друзей."));
                    }
                }
            }
        }
    });

    @Generated
    public BindSetting q() {
        return this.b;
    }

    @Generated
    public BindSetting r() {
        return this.c;
    }

    public ClickAction() {
        a(this.b, this.c);
    }
}





