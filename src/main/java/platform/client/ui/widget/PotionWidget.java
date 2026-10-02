package platform.client.ui.widget;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.event.GlobalEvent;
import platform.api.module.Interface;
import platform.api.module.InterfaceC0020Opcode;
import platform.client.utils.inject.IStatusEffectInstance;
import platform.api.utils.notification.Notification;
import platform.client.utils.render.EasingList;
import platform.client.utils.render.Fonts;
import platform.client.utils.render.ColorUtil;
import platform.client.utils.render.pipeline.DeltaRenderUtil;
import platform.client.utils.math.MathUtil;
import platform.api.system.configs.ThemeInfo;
import platform.api.event.events.render.DrawEvent;
import platform.api.module.setting.BooleanSetting;
import platform.client.ui.element.DragInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class PotionWidget extends Widget implements Interface {
    private final BooleanSetting f;
    private final MobEffectInstance g;

    public PotionWidget() {
        super(new DragInfo("Зелья", 0.0f, 0.0f, 0.0f, 0.0f));
        this.f = new BooleanSetting("Боковое отображение", false);
        this.g = new MobEffectInstance(MobEffects.SPEED, 1200, 0);
        j().a(this);
        addSettings(this.f);
    }

    @Override
    public void a(DrawEvent event) {
        GuiGraphicsExtractor context = event.i();
        if (context == null) {
            super.a(event);
            return;
        }
        d().a(0.0f, 1.0f, 0.3f, EasingList.g, event.g());
        Iterator<MobEffectInstance> it = k().iterator();
        while (it.hasNext()) {
            ((IStatusEffectInstance) (Object) it.next()).getAnimation().a(0.0f, 1.0f, 0.3f, EasingList.g, event.g());
        }
        if (this.f.c().booleanValue()) {
            d(event);
        } else {
            c(event);
        }
        j().a(this.f.c().booleanValue() ? 2 : 0);
    }

    private void c(DrawEvent event) {
        GuiGraphicsExtractor context = event.i();
        float x = j().a();
        float y = j().b();
        float targetWidth = 14.5f + Fonts.e.a("Potion-list", this.e) + 5.0f + 2.0f;
        float contentY = y + this.d + 3.0f;
        boolean active = false;
        Iterator<MobEffectInstance> it = k().iterator();
        while (it.hasNext()) {
            IStatusEffectInstance effect = (IStatusEffectInstance) (Object) it.next();
            if (effect.getAnimation().c() > 0.0f) {
                active = true;
                String name = Component.translatable(((MobEffect) effect.getEffectType().value()).getDescriptionId()).getString() + " " + (effect.getAmplifier() + 1);
                targetWidth = Math.max(targetWidth, 19.0f + Fonts.e.a(name, 6.5f) + 8.0f + Fonts.e.a(((MobEffectInstance) (Object) effect).isInfiniteDuration() || effect.getDuration() > 1000000 ? "∞" : ((effect.getDuration() / 20) / 60) + ":" + String.format("%02d", Integer.valueOf((effect.getDuration() / 20) % 60)), 6.5f) + 5.0f + 2.0f);
            }
        }
        float width = MathUtil.c(j().f(), targetWidth, 0.5f);
        j().c(width);
        if (a() > 0.0f) {
            drawHeader(context, "E", "Potion-list", width, a());
        }
        Iterator<MobEffectInstance> it2 = k().iterator();
        while (it2.hasNext()) {
            IStatusEffectInstance effect2 = (IStatusEffectInstance) (Object) it2.next();
            float animation = effect2.getAnimation().c() * a();
            if (animation > 0.0f) {
                String name2 = Component.translatable(((MobEffect) effect2.getEffectType().value()).getDescriptionId()).getString() + " " + (effect2.getAmplifier() + 1);
                int seconds = effect2.getDuration() / 20;
                String duration = effect2.getDuration() > 1000000 ? "∞" : (seconds / 60) + ":" + String.format("%02d", Integer.valueOf(seconds % 60));
                float offsetX = (-8.0f) * (1.0f - animation);
                float offsetY = -(1.0f - animation);
                float drawY = contentY + offsetY;
                float durationWidth = Fonts.e.a(duration, 6.5f);
                float textY = (drawY + ((11.5f - Fonts.e.a(6.5f)) / 2.0f)) - 0.5f;
                drawBackground(context, x + offsetX, drawY, width, 11.5f, false, animation);
                drawSeparator(context, x + offsetX + 15.0f, drawY, 11.5f, animation);
                a(event, context, effect2.getEffectType(), x + offsetX + 5.0f, drawY + 2.0f, 8, animation);
                boolean harmful = ((MobEffect) effect2.getEffectType().value()).getCategory() == MobEffectCategory.HARMFUL;
                Fonts.e.a(context, name2, x + offsetX + 19.0f, textY, 6.5f, ColorUtil.a(harmful ? ColorUtil.a(255, InterfaceC0020Opcode.cG, InterfaceC0020Opcode.cG, 255) : -1, animation));
                Fonts.e.a(context, duration, ((((x + offsetX) + width) - 5.0f) - durationWidth) - 1.0f, textY, 6.5f, ColorUtil.a(-1, 0.55f * animation));
                contentY += 13.5f * animation;
            }
        }
        j().d(active ? (contentY - y) - 2.0f : this.d);
        super.a(event);
    }

    private void d(DrawEvent event) {
        GuiGraphicsExtractor context = event.i();
        int primary = Delta.h().d().o().a(ThemeInfo.PRIMARY).a();
        int visibleCount = 0;
        Iterator<MobEffectInstance> it = k().iterator();
        while (it.hasNext()) {
            if (((IStatusEffectInstance) (Object) it.next()).getAnimation().c() > 0.0f) {
                visibleCount++;
            }
        }
        float posY = (aM_.getWindow().getGuiScaledHeight() - ((visibleCount * 26.0f) + ((visibleCount - 1) * 2.0f))) / 2.0f;
        float contentY = posY;
        float maxWidth = 0.0f;
        Iterator<MobEffectInstance> it2 = k().iterator();
        while (it2.hasNext()) {
            IStatusEffectInstance effect = (IStatusEffectInstance) (Object) it2.next();
            float animation = effect.getAnimation().c() * a();
            if (animation > 0.0f) {
                boolean harmful = ((MobEffect) effect.getEffectType().value()).getCategory() == MobEffectCategory.HARMFUL;
                String name = Component.translatable(((MobEffect) effect.getEffectType().value()).getDescriptionId()).getString() + " " + (effect.getAmplifier() + 1);
                int seconds = effect.getDuration() / 20;
                String duration = ((MobEffectInstance) (Object) effect).isInfiniteDuration() || effect.getDuration() > 1000000 ? "∞" : (seconds / 60) + ":" + String.format("%02d", Integer.valueOf(seconds % 60));
                float textWidth = Math.max(Fonts.e.a(name, 7.0f), Fonts.e.a(duration, 6.0f));
                float width = 18.5f + textWidth + 8.0f;
                float drawX = 3.0f - (width * (1.0f - animation));
                float textX = drawX + 13.5f + 6.0f;
                drawBackground(context, drawX, contentY, width, 24.0f, true, animation);
                a(event, context, effect.getEffectType(), drawX + 3.5f, contentY + 5.75f, 12, animation);
                Fonts.e.a(context, name, textX, contentY + 3.5f, 7.0f, ColorUtil.a(harmful ? ColorUtil.a(215, 76, 76, 255) : -1, animation));
                Fonts.e.a(context, duration, textX, contentY + 13.0f, 6.0f, ColorUtil.a(-1, 0.55f * animation));
                int initialDuration = effect.getInitialDuration();
                float progress = initialDuration <= 0 ? 1.0f : Math.min(1.0f, effect.getDuration() / initialDuration);
                int accent = harmful ? ColorUtil.a(215, 76, 76, 255) : primary;
                event.d().a(context, drawX + 2.0f, (contentY + 24.0f) - 1.5f, width - 4.0f, 1.5f, 0.0f, ColorUtil.a(accent, 0.15f * animation));
                event.d().a(context, drawX + 2.0f, (contentY + 24.0f) - 1.5f, (width - 4.0f) * progress, 1.5f, 0.0f, ColorUtil.a(accent, animation));
                maxWidth = Math.max(maxWidth, width);
                contentY += 28.0f * animation;
            }
        }
        j().a(3.0f);
        j().b(posY);
        j().c(maxWidth);
        j().d((contentY - posY) - 2.0f);
        super.a(event);
    }

    private void a(DrawEvent event, GuiGraphicsExtractor context, Holder<MobEffect> effect, float x, float y, int size, float animation) {
        if (context == null) {
            return;
        }
        Identifier sprite = Hud.getMobEffectSprite(effect);
        if (sprite != null) {
            DeltaRenderUtil.flush(context);
            context.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, (int) x, (int) y, size, size, ColorUtil.a(255, 255, 255, (int) (animation * 255.0f)));
        }
    }

    @Override
    public void a(GlobalEvent event) {
        boolean visible = aM_.gui.screen() instanceof ChatScreen;
        if (aM_.player == null) {
            d().a(visible);
            super.a(event);
            return;
        }
        for (MobEffectInstance effect : k()) {
            if (!effect.is(MobEffects.NIGHT_VISION)) {
                ((IStatusEffectInstance) effect).getAnimation().a(effect == this.g ? aM_.gui.screen() instanceof ChatScreen : (effect.getDuration() > 20 || effect.isInfiniteDuration()));
                if (((IStatusEffectInstance) effect).getAnimation().c() > 0.0d) {
                    visible = true;
                }
                if (effect != this.g && effect.getDuration() == 100 && (effect.is(MobEffects.STRENGTH) || effect.is(MobEffects.SPEED) || effect.is(MobEffects.HEALTH_BOOST) || effect.is(MobEffects.INVISIBILITY))) {
                    Delta.h().d().m().a(new Notification("E", Component.literal("Эффект ").append(Component.translatable(((MobEffect) effect.getEffect().value()).getDescriptionId()).append(" " + (effect.getAmplifier() + 1)).withStyle(style -> {
                        return style.withColor(Delta.h().d().o().a(ThemeInfo.PRIMARY).a());
                    })).append(Component.literal(" заканчивается")), 2500));
                }
            }
        }
        d().a(visible);
        super.a(event);
    }

    private List<MobEffectInstance> k() {
        if (aM_.player == null) {
            return new ArrayList<>();
        }
        List<MobEffectInstance> effects = new ArrayList<>((Collection<? extends MobEffectInstance>) aM_.player.getActiveEffects());
        boolean empty = effects.stream().allMatch(effect -> {
            return effect.is(MobEffects.NIGHT_VISION);
        });
        if (this.f.c().booleanValue() && empty && ((aM_.gui.screen() instanceof ChatScreen) || ((IStatusEffectInstance) (Object) this.g).getAnimation().c() > 0.0f)) {
            effects.add(this.g);
        }
        effects.sort(Comparator.comparingInt(effect2 -> {
            if (effect2.is(MobEffects.STRENGTH)) {
                return 0;
            }
            return effect2.is(MobEffects.WEAKNESS) ? 1 : 2;
        }));
        return effects;
    }
}


