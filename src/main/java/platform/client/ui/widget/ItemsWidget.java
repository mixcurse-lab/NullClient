package platform.client.ui.widget;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.event.GlobalEvent;
import platform.api.module.Interface;
import platform.client.features.modules.misc.ServerAssistant;
import platform.client.utils.input.KeyUtil;
import platform.client.utils.render.EasingList;
import platform.client.utils.render.Fonts;
import platform.client.utils.render.ColorUtil;
import platform.client.utils.math.MathUtil;
import platform.api.event.events.render.DrawEvent;
import platform.client.ui.element.DragInfo;

import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemCooldowns;
import platform.inject.accessors.ItemCooldownsAccessor;
import platform.inject.accessors.ItemCooldownsEntryAccessor;

public class ItemsWidget extends Widget implements Interface {
    public ItemsWidget() {
        super(new DragInfo("Предметы", 0.0f, 0.0f, 0.0f, 0.0f));
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
        ServerAssistant assistant = Delta.h().d().t().serverAssistant();
        List<ServerAssistant.b> providers = assistant.q();
        boolean active = false;
        int selectedBinds = 0;
        for (ServerAssistant.b provider : providers) {
            provider.a().a(0.0f, 1.0f, 0.3f, EasingList.g, event.g());
            active |= provider.a().c() > 0.0f;
            if (provider.b().c().intValue() != -1) {
                selectedBinds++;
            }
        }
        boolean example = !active && a() > 0.0f;
        int exampleCount = Math.max(3, selectedBinds);
        float y = j().b();
        float x = j().a();
        float contentX = x;
        ItemCooldowns cooldowns = aM_.player.getCooldowns();
        ItemCooldownsAccessor accessor = (ItemCooldownsAccessor) cooldowns;
        int tick = accessor.getTickCount();
        int i = 0;
        while (i < providers.size()) {
            ServerAssistant.b provider = providers.get(i);
            float animation = example ? (i < exampleCount ? a() : 0.0f) : provider.a().c() * a();
            if (animation > 0.0f) {
                Identifier itemId = BuiltInRegistries.ITEM.getKey(provider.c());
                Object cooldownEntry = accessor.getCooldowns().get(itemId);
                int remaining = cooldownEntry != null ? Math.max(((ItemCooldownsEntryAccessor) cooldownEntry).getEndTime() - tick, 0) : 0;
                String label = remaining > 0 ? String.format(Locale.US, "%.1f", Float.valueOf(remaining / 20.0f)) : KeyUtil.b(provider.b().c().intValue());
                float width = 19.5f + Fonts.e.a(label, 6.5f) + 5.0f;
                float textY = (y + ((this.d - Fonts.e.a(6.5f)) / 2.0f)) - 0.5f;
                drawBackground(context, contentX, y, width, this.d, true, animation);
                Delta.h().d().j().a(event.i(), provider.c().getDefaultInstance(), contentX + 3.0f, y + ((this.d - 16.0f) / 2.0f) + 3.0f, 0, animation, 0.6f, false);
                drawSeparator(context, contentX + 15.5f, y, this.d, animation);
                Fonts.e.a(context, label, contentX + 19.5f, textY, 6.5f, ColorUtil.a(-1, animation));
                contentX += (width + 2.0f) * animation;
            }
            i++;
        }
        j().c(MathUtil.c(j().f(), Math.max(0.0f, (contentX - x) - 2.0f), 0.5f));
        j().d((active || example) ? this.d : 0.0f);
        super.a(event);
    }

    @Override
    public void a(GlobalEvent event) {
        ServerAssistant assistant = Delta.h().d().t().serverAssistant();
        boolean visible = aM_.gui.screen() instanceof ChatScreen;
        if (aM_.player != null) {
            ItemCooldownsAccessor accessor = (ItemCooldownsAccessor) aM_.player.getCooldowns();
            int tick = accessor.getTickCount();
            for (ServerAssistant.b provider : assistant.q()) {
                provider.a().a(assistant.m() && provider.b().c().intValue() != -1 && provider.b().e().get().booleanValue() && platform.client.utils.player.InventoryUtil.b(provider.c()) != -1);
                if (provider.a().c() > 0.0f) {
                    Identifier itemId = BuiltInRegistries.ITEM.getKey(provider.c());
                    Object cooldownEntry = accessor.getCooldowns().get(itemId);
                    if (cooldownEntry != null && ((ItemCooldownsEntryAccessor) cooldownEntry).getEndTime() - tick > 0) {
                        visible = true;
                    }
                }
            }
        }
        d().a(visible);
        super.a(event);
    }
}
