package platform.client.ui.widget;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.event.GlobalEvent;
import platform.api.module.Interface;
import platform.api.utils.notification.Notification;
import platform.client.utils.render.EasingList;
import platform.client.utils.render.Fonts;
import platform.client.utils.render.ColorUtil;
import platform.api.system.configs.ThemeInfo;
import platform.api.event.events.render.DrawEvent;
import platform.api.event.events.client.PacketEvent;
import platform.api.module.setting.BooleanSetting;
import platform.client.ui.element.DragInfo;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class NotificationWidget extends Widget implements Interface {
    private final BooleanSetting f;
    private final BooleanSetting g;

    public NotificationWidget() {
        super(new DragInfo("Уведомления", 0.0f, 0.0f, 0.0f, 0.0f));
        this.f = new BooleanSetting("Оповещать о поднятии донат-предметов", true);
        this.g = new BooleanSetting("Обновления и уведомления друзей", true);
        j().a(this);
        j().a(1);
        addSettings(this.g, this.f);
    }

    @Override
    public void a(GlobalEvent event) {
        d().a((aM_.gui.screen() instanceof ChatScreen) || !Delta.h().d().m().b().isEmpty());
        super.a(event);
    }

    @Override
    public void a(DrawEvent event) {
        GuiGraphicsExtractor context = event.i();
        if (context == null) {
            super.a(event);
            return;
        }
        d().a(0.0f, 1.0f, 0.3f, EasingList.g, event.g());
        float contentY = j().b();
        for (Notification notification : Delta.h().d().m().b()) {
            float animation = notification.a().c() * a();
            if (animation > 0.0f) {
                Object message = notification.c();
                float fA;
                if (message instanceof Component) {
                    fA = Fonts.e.a(((Component) message).getString(), this.e);
                } else {
                    fA = Fonts.e.a(String.valueOf(message), this.e);
                }
                float width = 17.5f + fA + 4.0f;
                float x = (aM_.getWindow().getGuiScaledWidth() - width) / 2.0f;
                int color = notification.e() == -1 ? Delta.h().d().o().a(ThemeInfo.PRIMARY).a() : notification.e();
                Object objD = notification.d();
                if (objD instanceof ItemStack) {
                    a(event, context, x, contentY, (ItemStack) objD, message, width, animation, color);
                } else {
                    a(event, context, x, contentY, String.valueOf(objD), message, width, animation, color);
                }
                j().a(x);
                j().c(width);
                j().d(this.d);
                contentY += (this.d + 3.5f) * animation;
            }
        }
        super.a(event);
    }

    private void a(DrawEvent event, GuiGraphicsExtractor context, float x, float y, ItemStack icon, Object message, float width, float animation, int color) {
        if (animation > 0.0f) {
            drawBackground(context, x, y, width, this.d, true, animation);
            event.e().a(context, icon, x + 3.0f, (y + ((this.d - 8.0f) / 2.0f)) - 0.25f, 0, animation, 0.5f, false);
            drawSeparator(context, x + 13.5f, y, this.d, animation);
            if (message instanceof Component component) {
                Fonts.e.a(context, component, x + 17.5f, (y + ((this.d - Fonts.e.a(this.e)) / 2.0f)) - 0.5f, this.e, animation);
            } else {
                Fonts.e.a(context, String.valueOf(message), x + 17.5f, (y + ((this.d - Fonts.e.a(this.e)) / 2.0f)) - 0.5f, this.e, ColorUtil.a(-1, animation));
            }
        }
    }

    private void a(DrawEvent event, GuiGraphicsExtractor context, float x, float y, String icon, Object message, float width, float animation, int color) {
        if (animation > 0.0f) {
            drawBackground(context, x, y, width, this.d, true, animation);
            Fonts.a.a(context, icon, x + 3.0f, y + ((this.d - Fonts.a.a(this.e + 1.0f)) / 2.0f), this.e + 1.0f, ColorUtil.a(color, animation));
            drawSeparator(context, x + 13.5f, y, this.d, animation);
            if (message instanceof Component component) {
                Fonts.e.a(context, component, x + 17.5f, (y + ((this.d - Fonts.e.a(this.e)) / 2.0f)) - 0.5f, this.e, animation);
            } else {
                Fonts.e.a(context, String.valueOf(message), x + 17.5f, (y + ((this.d - Fonts.e.a(this.e)) / 2.0f)) - 0.5f, this.e, ColorUtil.a(-1, animation));
            }
        }
    }

    @Override
    public void a(PacketEvent event) {
        if (this.f.c().booleanValue() && event.c() && aM_.level != null && event.d() instanceof ClientboundTakeItemEntityPacket packet) {
            Entity collector = aM_.level.getEntity(packet.getPlayerId());
            Entity itemEntity = aM_.level.getEntity(packet.getItemId());
            if (collector instanceof Player player && itemEntity instanceof ItemEntity item) {
                if (player != aM_.player && !item.getItem().getHoverName().getString().contains("Упс.") && ((item.getItem().has(DataComponents.CUSTOM_NAME) && item.getItem().has(DataComponents.LORE)) || item.getItem().is(it -> it.value() == Items.ENCHANTED_GOLDEN_APPLE))) {
                    Component message = player.getName().copy().append(" подобрал ").append(item.getItem().getHoverName()).append(packet.getAmount() > 1 ? " x" + packet.getAmount() : "");
                    Delta.h().d().m().a(new Notification(item.getItem().copy(), message, 1500));
                }
            }
        }
        super.a(event);
    }
}


