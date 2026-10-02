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
import platform.api.utils.staff.StaffConstructor;
import platform.client.ui.element.DragInfo;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.multiplayer.PlayerInfo;

public class StaffWidget extends Widget implements Interface {
    public StaffWidget() {
        super(new DragInfo("Стафф", 0.0f, 0.0f, 0.0f, 0.0f));
        j().a(this);
    }

    @Override
    public void a(DrawEvent event) {
        GuiGraphicsExtractor context = event.i();
        if (context == null) {
            super.a(event);
            return;
        }
        d().a(0.0f, 1.0f, 0.3f, EasingList.g, event.g());
        float x = j().a();
        float y = j().b();
        float targetWidth = 14.5f + Fonts.e.a("Staff-list", this.e) + 5.0f + 2.0f;
        float contentY = y + this.d + 3.0f;
        boolean active = false;
        for (StaffConstructor staff : Delta.h().d().f().e()) {
            if (staff.b().c() > 0.0f) {
                targetWidth = Math.max(targetWidth, 19.0f + Fonts.e.a(staff.a(), 6.5f) + 8.0f + Fonts.e.a(a(staff.a()) ? "Near" : "Online", 6.5f) + 5.0f + 2.0f);
                active = true;
            }
        }
        float width = MathUtil.c(j().f(), targetWidth, 0.5f);
        j().c(width);
        if (a() > 0.0f) {
            drawHeader(context, "i", "Staff-list", width, a());
        }
        for (StaffConstructor staff : Delta.h().d().f().e()) {
            AnimationUtil animationUtil = staff.b();
            animationUtil.a(0.0f, 1.0f, 0.3f, EasingList.g, event.g());
            float animation = animationUtil.c() * a();
            if (animation > 0.0f) {
                float offsetX = (-8.0f) * (1.0f - animation);
                float offsetY = -(1.0f - animation);
                float drawY = contentY + offsetY;
                float textY = (drawY + ((11.5f - Fonts.e.a(6.5f)) / 2.0f)) - 0.5f;
                drawBackground(context, x + offsetX, drawY, width, 11.5f, false, animation);
                drawSeparator(context, x + offsetX + 15.0f, drawY, 11.5f, animation);
                PlayerInfo entry = aM_.getConnection() == null ? null : (PlayerInfo) aM_.getConnection().getListedOnlinePlayers().stream().filter(e -> {
                    return e.getProfile().name().equalsIgnoreCase(staff.a());
                }).findFirst().orElse(null);
                if (entry != null) {
                    event.d().a(context, entry.getSkin().body().texturePath(), null, x + offsetX + 5.0f, drawY + 2.0f, 7.5f, 7.5f, 2.0f, animation);
                } else {
                    Fonts.a.a(context, "y", x + offsetX + 5.0f, drawY + ((11.5f - Fonts.a.a(8.0f)) / 2.0f), 8.0f, ColorUtil.a(Delta.h().d().o().a(ThemeInfo.PRIMARY).a(), animation));
                }
                Fonts.e.a(context, staff.a(), x + offsetX + 19.0f, textY, 6.5f, ColorUtil.a(-1, animation));
                boolean near = a(staff.a());
                Fonts.e.a(context, near ? "Near" : "Online", ((((x + offsetX) + width) - 5.0f) - Fonts.e.a(near ? "Near" : "Online", 6.5f)) - 1.0f, textY, 6.5f, ColorUtil.a(near ? -1529792 : -9711765, animation));
                contentY += 13.5f * animation;
            }
        }
        j().d(active ? (contentY - y) - 2.0f : this.d);
        super.a(event);
    }

    @Override
    public void a(GlobalEvent event) {
        boolean visible = aM_.gui.screen() instanceof ChatScreen;
        for (StaffConstructor staff : Delta.h().d().f().e()) {
            staff.b().a((aM_.getConnection() != null && aM_.getConnection().getListedOnlinePlayers().stream().anyMatch(e -> {
                return e.getProfile().name().equalsIgnoreCase(staff.a());
            })) || a(staff.a()));
            if (staff.b().c() > 0.0f) {
                visible = true;
            }
        }
        d().a(visible);
        super.a(event);
    }

    private boolean a(String name) {
        return aM_.level != null && aM_.level.players().stream().anyMatch(player -> {
            return player.getName().getString().equalsIgnoreCase(name);
        });
    }
}


