package platform.client.features.modules.misc;

import platform.client.utils.text.StringUtils;
import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.module.Module;
import platform.client.utils.render.Fonts;
import platform.client.utils.text.ChatUtil;
import platform.client.utils.render.ColorUtil;
import platform.client.utils.math.ProjectUtil;

import platform.api.system.configs.ThemeInfo;
import platform.api.system.configs.ThemeProcessor;
import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Interface;
import platform.api.module.ModuleRegister;
import platform.api.event.events.client.BackendEvent;
import platform.api.event.events.render.DrawEvent;
import platform.api.event.events.client.PacketEvent;
import platform.api.event.events.client.TickEvent;
import platform.client.utils.render.Draw2DProcessor;

import platform.api.module.setting.BindSetting;
import platform.api.module.setting.BooleanSetting;
import platform.client.utils.timer.CounterUtil;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.MutableComponent;
import org.joml.Vector2f;

@ModuleRegister(a = "Communication", b = "Связывает вас с другими игроками через групповые и глобальные сообщения (party, IRC и др.)", c = Category.Misc)
public class Communication extends Module implements Interface {
    private final BindSetting b = new BindSetting("Отправление метки друзьям", -1).a(() -> {
        if (aM_.player == null) return;
        JsonObject posObject = new JsonObject();
        posObject.addProperty("x", Double.valueOf(aM_.player.position().x));
        posObject.addProperty("y", Double.valueOf(aM_.player.position().y));
        posObject.addProperty("z", Double.valueOf(aM_.player.position().z));

    });
    private final BooleanSetting c = new BooleanSetting("Клиентский чат", false);
    private final List<a> d = new ArrayList();

    public Communication() {
        a(this.b, this.c);
    }

    @EventTarget
    public void a(TickEvent event) {
        this.d.removeIf(mark -> {
            return mark.a().a(5000L);
        });
    }

    @EventTarget
    public void a(PacketEvent event) {
        if (this.c.c().booleanValue() && event.b()) {
            if (event.d() instanceof ServerboundChatPacket packet) {
                String content = packet.message();
                if (content.startsWith("@")) {

                    event.a(true);
                }
            }
        }
    }

    @EventTarget
    public void a(DrawEvent event) {
        if (event.b() && aM_.player != null) {
            for (a mark : this.d) {
                a(event, mark, aM_.player.getEyePosition());
            }
        }
    }

    private void a(DrawEvent event, a mark, Vec3 eyes) {

        if (aM_.player == null) return;
    }

    @EventTarget
    public void a(BackendEvent event) {
        String payload = event.d().c();
        String type = event.d().a().a(payload, "type");
        String user = event.d().a().a(payload, "user");
        String message = event.d().a().a(payload, "message");
        String priority = event.d().a().a(payload, "priority");
        if ("irc".equals(event.d().b()) && this.c.c().booleanValue()) {
            Prefix prefix = Prefix.a(priority);
            MutableComponent line = Component.empty();
            if (prefix != null) {
                line.append(Component.literal(prefix.a())).append(Component.literal(StringUtils.a));
            }
            line.append(ChatUtil.b("[" + user + "] → " + message));
            ChatUtil.a((Object) "[IRC]", line);
        }
        if ("friend".equals(event.d().b()) && "mark".equals(type)) {
            JsonObject pos = event.d().a().b(payload, "pos").getAsJsonObject();
            String position = String.format("%.0f, %.0f, %.0f", Double.valueOf(pos.get("x").getAsDouble()), Double.valueOf(pos.get("y").getAsDouble()), Double.valueOf(pos.get("z").getAsDouble()));
            String login = event.d().a().a(payload, "minecraft");
            this.d.removeIf(mark -> mark.b().equalsIgnoreCase(login));
            this.d.add(new a(new CounterUtil(), login, position));
        }
    }

    public enum Prefix {
        ADMIN("Администратор", "\ue100"),
        STAFF("Сотрудник", "\ue101"),
        YOUTUBER("Ютубер", "\ue102"),
        SHADE("shade", "\ue103"),
        DANGEROUS("dangerous", "\ue104"),
        DEVSTVENIK("девственник", "\ue105"),
        DRUN("друн", "\ue106"),
        QCOLD("qcold", "\ue107"),
        WIN("win", "\ue108"),
        BURMALDA("бурмалда", "\ue109"),
        VOZDUXAN("воздухан", "\ue110");

        private final String l;
        private final String m;

        Prefix(String role, String glyph) {
            this.l = role;
            this.m = glyph;
        }

        public String a() {
            return this.m;
        }

        public static Prefix a(String role) {
            return (Prefix) Arrays.stream(values()).filter(prefix -> {
                return prefix.l.equalsIgnoreCase(role);
            }).findFirst().orElse(null);
        }
    }

    public static final class a {
        private final CounterUtil a;
        private final String b;
        private final String c;

        public a(CounterUtil counterUtil, String login, String position) {
            this.a = counterUtil;
            this.b = login;
            this.c = position;
        }
public CounterUtil a() {
            return this.a;
        }

        public String b() {
            return this.b;
        }

        public String c() {
            return this.c;
        }
    }
}
