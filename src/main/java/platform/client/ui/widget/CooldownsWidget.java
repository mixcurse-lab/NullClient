package platform.client.ui.widget;

import static platform.api.module.Interface.aM_;
import platform.api.event.GlobalEvent;
import platform.api.module.Interface;
import platform.client.utils.render.EasingList;
import platform.client.utils.render.Fonts;
import platform.client.utils.render.ColorUtil;
import platform.client.utils.math.MathUtil;
import platform.api.event.events.render.DrawEvent;
import platform.client.ui.element.DragInfo;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import platform.inject.accessors.ItemCooldownsAccessor;
import platform.inject.accessors.ItemCooldownsEntryAccessor;

public class CooldownsWidget extends Widget implements Interface {
    public CooldownsWidget() {
        super(new DragInfo("Задержки", 0.0f, 0.0f, 0.0f, 0.0f));
        j().a(this);
    }

    @Override
    public void a(DrawEvent event) {
        GuiGraphicsExtractor context = event.i();
        if (context == null || aM_.player == null) {
            super.a(event);
            return;
        }
        d().a(0.0f, 1.0f, 0.3f, EasingList.g, event.g());
        float x = j().a();
        float y = j().b();
        float targetWidth = 14.5f + Fonts.e.a("Cooldowns", this.e) + 5.0f + 2.0f;
        float contentY = y + this.d + 3.0f;
        ItemCooldownsAccessor accessor = (ItemCooldownsAccessor) aM_.player.getCooldowns();
        Map<Identifier, ?> entries = accessor.getCooldowns();
        int tick = accessor.getTickCount();
        boolean active = false;
        for (Map.Entry<Identifier, ?> e : entries.entrySet()) {
            int remaining = Math.max(((ItemCooldownsEntryAccessor) e.getValue()).getEndTime() - tick, 0);
            if (remaining > 0) {
                active = true;
                Item item = BuiltInRegistries.ITEM.getValue(e.getKey());
                targetWidth = Math.max(targetWidth, 19.0f + Fonts.e.a(item.getName(item.getDefaultInstance()).getString(), 6.5f) + 8.0f + Fonts.e.a(String.format(Locale.US, "%.1f", Float.valueOf(remaining / 20.0f)), 6.5f) + 5.0f + 2.0f);
            }
        }
        float width = MathUtil.c(j().f(), targetWidth, 0.5f);
        j().c(width);
        if (a() > 0.0f) {
            drawHeader(context, "d", "Cooldowns", width, a());
        }
        for (Map.Entry<Identifier, ?> e : entries.entrySet()) {
            int remaining = Math.max(((ItemCooldownsEntryAccessor) e.getValue()).getEndTime() - tick, 0);
            if (remaining <= 0) {
                continue;
            }
            float animation = a();
            if (animation > 0.0f) {
                Item item = BuiltInRegistries.ITEM.getValue(e.getKey());
                String time = String.format(Locale.US, "%.1f", Float.valueOf(remaining / 20.0f));
                float offsetX = (-8.0f) * (1.0f - animation);
                float offsetY = -(1.0f - animation);
                float drawY = contentY + offsetY;
                float timeWidth = Fonts.e.a(time, 6.5f);
                float textY = (drawY + ((11.5f - Fonts.e.a(6.5f)) / 2.0f)) - 0.5f;
                ItemStack stack = item.getDefaultInstance();
                drawBackground(context, x + offsetX, drawY, width, 11.5f, false, animation);
                drawSeparator(context, x + offsetX + 15.0f, drawY, 11.5f, animation);
                event.e().a(context, stack, x + offsetX + 5.0f, drawY + 2.0f, 0, animation, 0.45f, false);
                Fonts.e.a(context, item.getName(stack).getString(), x + offsetX + 19.0f, textY, 6.5f, ColorUtil.a(-1, animation));
                Fonts.e.a(context, time, ((((x + offsetX) + width) - 5.0f) - timeWidth) - 1.0f, textY, 6.5f, ColorUtil.a(-1, 0.55f * animation));
                contentY += 13.5f * animation;
            }
        }
        j().d(active ? (contentY - y) - 2.0f : this.d);
        super.a(event);
    }

    @Override
    public void a(GlobalEvent event) {
        boolean visible = aM_.gui.screen() instanceof ChatScreen;
        if (aM_.player != null) {
            ItemCooldownsAccessor accessor = (ItemCooldownsAccessor) aM_.player.getCooldowns();
            int tick = accessor.getTickCount();
            for (Object instance : accessor.getCooldowns().values()) {
                if (((ItemCooldownsEntryAccessor) instance).getEndTime() - tick > 0) {
                    visible = true;
                    break;
                }
            }
        }
        d().a(visible);
        super.a(event);
    }
}


