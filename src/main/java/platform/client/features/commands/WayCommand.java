package platform.client.features.commands;

import platform.api.module.Interface;
import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.client.utils.render.Fonts;
import platform.client.utils.text.ChatUtil;
import platform.client.utils.render.ColorUtil;
import platform.client.utils.math.ProjectUtil;
import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket;

import platform.api.command.BaseCommand;
import platform.api.command.Command;
import platform.api.system.configs.ThemeInfo;
import platform.api.event.interfaces.EventTarget;
import platform.api.event.events.render.DrawEvent;
import platform.api.event.events.client.PacketEvent;
import platform.api.event.events.client.TickEvent;
import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Generated;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket;

@Command(a = "way")
public class WayCommand extends BaseCommand {
    private final List<b> c = new ArrayList();
    private final List<String> d = new ArrayList();
    private a e = a.NONE;

    public enum a {
        NONE,
        WAY,
        GPS
    }

    @Generated
    public List<b> c() {
        return this.c;
    }

    @Generated
    public void a(a eventMode) {
        this.e = eventMode;
    }

    @Override
    public void a(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.then(a("add").executes(context -> {
            ChatUtil.a((Object) "Использование: .way add <название> <x> <z> или .way add <название> <x> <y> <z>\"");
            return 1;
        }).then(d("название").executes(context2 -> {
            ChatUtil.a((Object) "Использование: .way add <название> <x> <z> или .way add <название> <x> <y> <z>\"");
            return 1;
        }).then(e("x").executes(context3 -> {
            ChatUtil.a((Object) "Использование: .way add <название> <x> <z> или .way add <название> <x> <y> <z>\"");
            return 1;
        }).then(e("y или z").executes(context4 -> {
            a(a((CommandContext<CommandSourceStack>) context4, "название"), new Vec3(b((CommandContext<CommandSourceStack>) context4, "x"), aM_.player.getY(), b((CommandContext<CommandSourceStack>) context4, "y или z")));
            return 1;
        }).then(e("z").executes(context5 -> {
            a(a((CommandContext<CommandSourceStack>) context5, "название"), new Vec3(b((CommandContext<CommandSourceStack>) context5, "x"), b((CommandContext<CommandSourceStack>) context5, "y или z"), b((CommandContext<CommandSourceStack>) context5, "z")));
            return 1;
        })))))).then(a("me").executes(context6 -> {
            a("me", aM_.player.position());
            return 1;
        })).then(a("remove").executes(context7 -> {
            ChatUtil.a((Object) "Использование: .way remove <название>");
            return 1;
        }).then(d("название").suggests(a(() -> {
            return this.c;
        }, (v0) -> {
            return v0.a();
        })).executes(context8 -> {
            String name = a((CommandContext<CommandSourceStack>) context8, "название");
            if (!g(name)) {
                ChatUtil.a((Object) ("Метка с именем &c" + name + " &7отсутствует"));
                return 1;
            }
            this.c.removeIf(way -> {
                return way.a().equalsIgnoreCase(name);
            });
            ChatUtil.a((Object) ("Метка с именем &c" + name + " &7успешно удалена"));
            return 1;
        }))).then(a("list").executes(context9 -> {
            if (this.c.isEmpty()) {
                ChatUtil.a((Object) "Список меток не содержит элементов");
                return 1;
            }
            ChatUtil.a((Object) ("Список всех меток (" + this.c.size() + "):"));
            for (b way : this.c) {
                ChatUtil.a((Object) ("— &c" + way.a() + " &7[&f" + a(way.b()) + "&7]&f"));
            }
            return 1;
        })).then(a("event").executes(context10 -> {
            this.e = a.WAY;
            aM_.player.connection.sendCommand("event delay");
            return 1;
        })).then(a("clear").executes(context11 -> {
            ChatUtil.a((Object) ("Количество удалённых меток: " + this.c.size()));
            this.c.clear();
            return 1;
        })).executes(context12 -> {
            ChatUtil.a((Object) "Использование: .way <add|me|remove|list|clear|event>");
            return 1;
        });
    }

    @EventTarget
    public void a(PacketEvent event) {
        if (this.e == a.NONE || !event.c()) {
            return;
        }
        ClientboundDisguisedChatPacket class_7439VarD = (ClientboundDisguisedChatPacket) event.d();
        if (class_7439VarD instanceof ClientboundDisguisedChatPacket) {
            ClientboundDisguisedChatPacket s2CPacket = class_7439VarD;
            String message = s2CPacket.message().getString();
            if (message.contains("[Ивенты]") || message.contains("Аир-дроп:") || message.contains("|| /warp portal") || message.contains("|| Координаты:") || message.contains("Призван игроком:") || message.contains("Уровень лута:") || message.contains("Статус:") || message.contains("[1]") || message.contains("[2]")) {
                event.a(true);
                this.d.add(message);
            }
        }
    }

    @EventTarget
    public void a(TickEvent event) {
        if (this.e != a.NONE && !this.d.isEmpty()) {
            boolean found = false;
            for (int i = 0; i < this.d.size(); i++) {
                String message = this.d.get(i);
                if (message.contains("[1] Маяк убийца") || message.contains("[1] Вулкан") || message.contains("[1] Метеоритный дождь") || message.contains("[1] Гейзер")) {
                    for (int j = i; j < this.d.size(); j++) {
                        String next = this.d.get(j);
                        if (next.contains("|| Координаты:")) {
                            Matcher matcher = Pattern.compile("\\[(-?\\d+) (-?\\d+) (-?\\d+)]").matcher(next);
                            if (!matcher.find()) {
                                break;
                            }
                            Vec3 pos = new Vec3(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3)));
                            if (this.e == a.GPS) {
                                Delta.h().d().u().d().a(pos);
                            } else {
                                a("Ивент", pos);
                            }
                            found = true;
                            break;
                        }
                    }
                    break;
                }
            }
            if (!found) {
                ChatUtil.a((Object) "Нет активного события с координатами.");
            }
            this.d.clear();
            this.e = a.NONE;
        }
    }

    @EventTarget
    public void a(DrawEvent event) {
        if (event.b()) {
            for (b way : this.c) {
                a(event, way, aM_.player.getEyePosition());
            }
            Vec3 gps = Delta.h().d().u().d().c();
            if (gps != null) {
                a(event, gps, aM_.player.getEyePosition());
            }
        }
    }

    public void a(String name, Vec3 pos) {
        String trimmed = name.length() > 6 ? name.substring(0, 6) : name;
        boolean replaced = this.c.removeIf(way -> {
            return way.a().equalsIgnoreCase(trimmed) || way.b().distanceTo(pos) <= 5.0d;
        });
        this.c.add(new b(trimmed, pos));
        ChatUtil.a((Object) ("Метка &c" + trimmed + (replaced ? " &7успешно переставлена: " : " &7успешно добавлена: ") + a(pos)));
    }

    private boolean g(String name) {
        return this.c.stream().anyMatch(way -> {
            return way.a().equalsIgnoreCase(name);
        });
    }

    private String a(Vec3 pos) {
        return ((int) pos.x) + ", " + ((int) pos.y) + ", " + ((int) pos.z);
    }

    private void a(DrawEvent event, b way, Vec3 eyes) {
        Vector2f screen = ProjectUtil.a(way.b().x, way.b().y, way.b().z);
        if (ProjectUtil.a(screen)) {
            int primary = Delta.h().d().o().a(ThemeInfo.PRIMARY).a();
            int background = Delta.h().d().o().a(ThemeInfo.BACKGROUND_HUD).a();
            Component text = Component.literal(way.a().toUpperCase(Locale.ROOT)).append(Component.literal("  /  ").setStyle(Style.EMPTY.withColor(primary))).append(Component.literal(String.format(Locale.US, "%.1fм", Double.valueOf(eyes.distanceTo(way.b())))));
            float width = 14.0f + Fonts.e.a(text.getString(), 6.25f);
            float x = screen.x() - (width / 2.0f);
            float y = screen.y() - 5.75f;
            event.d().a(event.i(), x, y, width, 11.5f, 3.5f, ColorUtil.a(background, Delta.h().d().o().a(ThemeInfo.BACKGROUND_HUD).b()), 1.0f, ColorUtil.a(background, Delta.h().d().o().a(ThemeInfo.BACKGROUND_HUD).b()), 6.0f);
            Fonts.a.a(event.i(), "F", x + 3.0f, y + 3.0f, 5.5f, primary);
            Fonts.e.a(event.i(), text, x + 3.0f + 5.5f + 2.5f, (y + ((11.5f - Fonts.e.a(6.25f)) / 2.0f)) - 0.25f, 6.25f);
        }
    }

    private void a(DrawEvent event, Vec3 gps, Vec3 eyes) {
        GuiGraphicsExtractor matrices = event.i();
        int primary = Delta.h().d().o().a(ThemeInfo.PRIMARY).a();
        double dx = gps.x - aM_.player.getX();
        double dz = gps.z - aM_.player.getZ();
        float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float angle = Mth.wrapDegrees(targetYaw - aM_.player.getYRot());
        float cx = aM_.getWindow().getGuiScaledWidth() / 2.0f;
        float cy = aM_.getWindow().getGuiScaledHeight() * 0.25f;
        matrices.pose().pushMatrix();
        matrices.pose().translate(cx, cy);
        matrices.pose().rotate((float) Math.toRadians(angle));
        event.d().a(matrices, Identifier.fromNamespaceAndPath("delta", "pictures/triangle.png"), -7.0f, -7.0f, 14.0f, 14.0f, 0.0f, primary);
        matrices.pose().popMatrix();
        Component text = Component.literal(String.format(Locale.US, "%.1fм", Double.valueOf(eyes.distanceTo(gps))));
        Fonts.d.a(event.i(), text, cx - (Fonts.d.a(text.getString(), 7.0f) / 2.0f), cy + 7.0f + 2.0f, 7.0f);
    }

    public static class b {
        private final String a;
        private final Vec3 b;

        @Generated
        public b(String name, Vec3 position) {
            this.a = name;
            this.b = position;
        }

        @Generated
        public String a() {
            return this.a;
        }

        @Generated
        public Vec3 b() {
            return this.b;
        }
    }
}



