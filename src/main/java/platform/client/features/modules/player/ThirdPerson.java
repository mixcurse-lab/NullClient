package platform.client.features.modules.player;

import platform.api.module.Interface;
import platform.client.utils.lib.javassist.TokenId;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.module.Module;
import platform.client.utils.rotation.Look;
import platform.client.utils.math.MathUtil;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.api.event.events.player.KeyEvent;
import platform.api.event.events.client.TickEvent;

import platform.api.module.setting.BindSetting;
import platform.api.module.setting.ModeSetting;
import platform.client.utils.rotation.Rotation;
import net.minecraft.client.CameraType;

@ModuleRegister(a = "Third Person", b = "Свободный обзор от третьего лица без изменения направления движения", c = Category.Player)
public class ThirdPerson extends Module {
    private boolean c;
    private Rotation e;
    private final ModeSetting b = new ModeSetting("Режим активации осмотра", "По нажатию", "По нажатию", "По зажатию");
    private final BindSetting d = new BindSetting("Кнопка осмотра", Integer.valueOf(TokenId.Q_), 0).a(() -> {
        if (this.b.l("По зажатию")) {
            d(true);
        } else {
            d(!this.c);
        }
    }).b(() -> {
        if (this.c && this.b.l("По зажатию")) {
            d(false);
        }
    });

    public ThirdPerson() {
        a(this.b, this.d);
    }

    @EventTarget
    public void a(KeyEvent event) {
        if (this.c && event.b() == 294) {
            event.a(true);
        }
    }

    @EventTarget
    public void a(TickEvent event) {
        if (this.c) {
            if (aM_.gui.screen() != null) {
                d(false);
            } else {
                Delta.h().d().k().a(new Rotation(aM_.player.getYRot(), MathUtil.b(aM_.player.getXRot(), -89.0f, 89.0f)), 360.0f, 0, 1);
            }
        }
    }

    private void d(boolean active) {
        if (active) {
            this.e = new Rotation(Look.b(), Look.c());
        } else {
            Look.a(this.e.c());
            Look.b(this.e.d());
        }
        aM_.options.setCameraType(active ? CameraType.THIRD_PERSON_BACK : CameraType.FIRST_PERSON);
        this.c = active;
    }
}





