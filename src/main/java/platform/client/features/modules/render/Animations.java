package platform.client.features.modules.render;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Module;
import platform.api.module.ModuleRegister;
import platform.api.event.events.render.DrawEvent;
import platform.api.event.events.client.TickEvent;
import platform.client.utils.render.AnimationUtil;
import platform.client.utils.render.EasingList;
import platform.api.module.setting.BooleanSetting;
import platform.api.module.setting.MultiModeSetting;
import platform.client.utils.math.MathUtil;
import lombok.Generated;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

import static platform.api.module.Interface.aM_;

@ModuleRegister(a = "Animations", b = "Анимирует выбранные элементы игры", c = Category.Render)
public class Animations extends Module {
    private final MultiModeSetting b = new MultiModeSetting("Выберите что анимировать", new BooleanSetting("TAB", true), new BooleanSetting("Открытие инвентаря", true), new BooleanSetting("Смена перспективы", true), new BooleanSetting("Поднятие хотбара", true), new BooleanSetting("Слот хотбара", true), new BooleanSetting("Появление сообщений", true), new BooleanSetting("Предметы", true));
    private final AnimationUtil c = new AnimationUtil();
    private final AnimationUtil d = new AnimationUtil();
    private final AnimationUtil e = new AnimationUtil();
    private final AnimationUtil f = new AnimationUtil();
    private float g = -1.0f;

    @Generated
    public MultiModeSetting q() {
        return this.b;
    }

    @Generated
    public AnimationUtil r() {
        return this.c;
    }

    @Generated
    public AnimationUtil s() {
        return this.d;
    }

    @Generated
    public AnimationUtil t() {
        return this.e;
    }

    @Generated
    public AnimationUtil u() {
        return this.f;
    }

    @Generated
    public float v() {
        return this.g;
    }

    public Animations() {
        a(this.b);
    }

    @Override
    public void c() {
        super.c();
        this.g = -1.0f;
    }

    @EventTarget
    public void a(DrawEvent event) {
        this.c.a(0.0f, 1.0f, 0.5f, EasingList.g, event.g());
        this.d.a(0.0f, 1.0f, 0.45f, EasingList.g, event.g());
        this.e.a(0.0f, 1.0f, 0.4f, EasingList.g, event.g());
        this.f.a(0.0f, 1.0f, 0.35f, EasingList.g, event.g());
        this.g = this.g < 0.0f ? aM_.player.getInventory().getSelectedSlot() : MathUtil.c(this.g, aM_.player.getInventory().getSelectedSlot(), 1.25f);
    }

    @EventTarget
    public void a(TickEvent event) {
        this.e.a(aM_.gui.screen() instanceof InventoryScreen);
        this.f.a(aM_.options.getCameraType() != CameraType.FIRST_PERSON);
        this.d.a(aM_.gui.screen() instanceof ChatScreen);
    }
}


