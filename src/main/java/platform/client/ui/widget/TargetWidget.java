package platform.client.ui.widget;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.event.GlobalEvent;
import platform.api.module.Interface;
import platform.client.utils.render.EasingList;
import platform.client.utils.render.Fonts;
import platform.client.utils.render.ColorUtil;
import platform.client.utils.render.AnimationUtil;
import platform.client.utils.math.MathUtil;
import platform.api.system.configs.ThemeInfo;
import platform.api.event.events.render.DrawEvent;
import platform.api.module.setting.BooleanSetting;
import platform.client.ui.element.DragInfo;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import platform.client.utils.render.ScissorUtil;

public class TargetWidget extends Widget {
    private final BooleanSetting f;
    private final BooleanSetting g;
    private final AnimationUtil h;
    private final AnimationUtil i;
    private LivingEntity j;
    private String k;

    public TargetWidget() {
        super(new DragInfo("Таргет-худ", 0.0f, 0.0f, 0.0f, 0.0f));
        this.f = new BooleanSetting("Визуализация предметов", true);
        this.g = new BooleanSetting("Отображать при наводке", false);
        this.h = new AnimationUtil();
        this.i = new AnimationUtil();
        this.k = "";
        j().a(this);
        addSettings(this.g, this.f);
    }

    @Override
    public void a(DrawEvent event) {
        GuiGraphicsExtractor context = event.i();
        if (context == null) {
            super.a(event);
            return;
        }
        d().a(0.0f, 1.0f, 0.3f, EasingList.g, event.g());
        if (a() > 0.0f && this.j != null) {
            j().c(100.0f);
            j().d(24.0f);
            float x = j().a();
            float y = j().b();
            drawBackground(context, x, y, j().f(), j().g(), true, a());
            float headSize = j().g() / 1.35f;
            float headY = y + ((j().g() - headSize) / 2.0f);
            if (this.j instanceof AbstractClientPlayer player) {
                event.d().a(context, player.getSkin().body().texturePath(), this.j, x + 5.0f, headY, headSize, headSize, 2.0f, a());
            } else {
                Fonts.a.a(context, "B", x + 6.5f + ((headSize - 24.0f) / 2.0f), headY + ((headSize - 24.0f) / 2.0f), 24.0f, ColorUtil.a(-1, a()));
            }
            float textX = x + 5.0f + headSize + 5.0f;
            String name = this.j.getName().getString();
            if (name.length() > 12) {
                Fonts.e.c(context, name, textX, headY, 7.5f, ColorUtil.a(-1, a()), Fonts.e.a(name.substring(0, 12), 7.5f));
            } else {
                Fonts.e.a(context, name, textX, headY, 7.5f, ColorUtil.a(-1, a()));
            }
            if (this.f.c().booleanValue()) {
                int idx = 0;
                for (ItemStack stack : new ItemStack[]{this.j.getItemBySlot(EquipmentSlot.FEET), this.j.getItemBySlot(EquipmentSlot.LEGS), this.j.getItemBySlot(EquipmentSlot.CHEST), this.j.getItemBySlot(EquipmentSlot.HEAD), this.j.getOffhandItem(), this.j.getMainHandItem()}) {
                    if (!stack.isEmpty()) {
                        event.e().a(context, stack, ((x + j().f()) - 10.0f) - (idx * 9), y + j().g(), 0, a(), 0.55f, true);
                        idx++;
                    }
                }
            }
            int primary = Delta.h().d().o().a(ThemeInfo.PRIMARY).a();
            float health = this.j.getHealth() + this.j.getAbsorptionAmount();
            String hpValue = String.valueOf((int) health);
            if (this.k.isEmpty()) {
                this.k = hpValue;
            }
            float progress = hpValue.equals(this.k) ? 1.0f : this.i.a(0.0f, 1.0f, 0.75f);
            a(context, hpValue, this.k, ((x + j().f()) - 5.0f) - Fonts.e.a(hpValue, 7.0f), headY + 0.5f, 7.0f, primary, progress);
            if (progress >= 0.99f) {
                this.k = hpValue;
                this.i.c(0.0f);
            }
            float maxHealth = this.j.getMaxHealth();
            float targetHP = MathUtil.b(MathUtil.b(health, 0.0f, maxHealth) / maxHealth, 0.0f, 1.0f);
            float lineHP = this.h.a(targetHP, targetHP, 0.5f);
            float alpha = Delta.h().d().o().a(ThemeInfo.BACKGROUND_HUD).b() * a();
            event.d().a(context, textX, headY + 12.5f, 54.0f, 3.0f, 0.5f, ColorUtil.a(ColorUtil.b(primary, 0.3f), a()));
            event.d().a(context, textX, headY + 12.5f, 54.0f * lineHP, 3.0f, 0.5f, ColorUtil.a(primary, alpha));
        }
        super.a(event);
    }

    private void a(GuiGraphicsExtractor context, String current, String previous, float right, float y, float size, int color, float progress) {
        float height = Fonts.e.a(size);
        float cursor = right + Fonts.e.a(current, size);
        int i = 0;
        while (i < current.length()) {
            char digit = current.charAt((current.length() - 1) - i);
            char old = i < previous.length() ? previous.charAt((previous.length() - 1) - i) : ' ';
            String value = String.valueOf(digit);
            cursor -= Fonts.e.a(value, size);
            if (digit == old || progress >= 1.0f) {
                Fonts.e.a(context, value, cursor, y, size, ColorUtil.a(color, a()));
            } else {
                context.enableScissor((int) (cursor - 0.5f), (int) y, (int) (cursor - 0.5f + Fonts.e.a(value, size) + 1.0f), (int) (y + height + 1.0f));
                Fonts.e.a(context, String.valueOf(old), cursor, y - (height * progress), size, ColorUtil.a(color, (1.0f - progress) * a()));
                Fonts.e.a(context, value, cursor, y + (height * (1.0f - progress)), size, ColorUtil.a(color, progress * a()));
                context.disableScissor();
            }
            i++;
        }
    }

    @Override
    public void a(GlobalEvent event) {
        LivingEntity target;
        LivingEntity targets = Delta.h().d().t().B().s() != null ? Delta.h().d().t().B().s() : Delta.h().d().t().X().s();
        LivingEntity crosshair = null;
        if (this.g.c().booleanValue() && aM_.hitResult instanceof EntityHitResult hit) {
            if (hit.getEntity() instanceof LivingEntity entity && entity != aM_.player) {
                crosshair = entity;
            }
        }
        if (targets != null) {
            target = targets;
        } else if (crosshair != null) {
            target = crosshair;
        } else {
            target = aM_.gui.screen() instanceof ChatScreen ? aM_.player : null;
        }
        boolean visible = target != null;
        if (target != null) {
            this.j = target;
        }
        d().a(visible);
        if (!visible && d().a() <= 0.0f) {
            this.j = null;
        }
        super.a(event);
    }
}


