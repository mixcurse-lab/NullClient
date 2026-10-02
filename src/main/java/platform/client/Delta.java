package platform.client;


import platform.api.system.interfaces.NativeMethodLookup;
import static platform.api.module.Interface.aM_;
import platform.api.event.EventManager;
import platform.api.module.Interface;
import platform.api.module.Module;
import platform.client.utils.render.EasingList;


import platform.api.event.interfaces.EventTarget;
import platform.client.services.Processor_2;
import platform.api.auth.User;
import platform.api.event.events.render.DrawEvent;
import platform.api.event.events.player.KeyEvent;
import platform.client.ui.screen.GUIScreen;

import platform.api.annotation.Compile;
import platform.api.annotation.Ultra;
import java.io.File;
import lombok.Generated;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class Delta {
    private static Delta a;
    private Processor_2 b;
    private GUIScreen c;
    private Object d;
    private User e;
    private boolean fontsPreloaded = false;
    private static volatile User jc$unifiedPendingUser$;
    private static volatile Delta jc$unifiedClient$;

    @Compile
    @Ultra
    protected void a() {
        this.e = new User("1", "User", "User", "Владелец", "01.01.2099 00:00", "");
        a = this;
        jc$bindUnifiedClient$(this);
        this.b = new Processor_2();
        this.d = new Object();
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            platform.client.utils.player.PendingBlockEntities.clear();
            this.a(client);
        });
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
            try {
                platform.client.utils.player.PendingBlockEntities.apply(world, chunk.getPos());
            } catch (Throwable ignored) {
            }
        });
        EventManager.a(this);
        this.b.a();
        try {
            baritone.api.BaritoneAPI.getSettings().allowBreak.value = true;
            baritone.api.BaritoneAPI.getSettings().allowPlace.value = true;
            baritone.api.BaritoneAPI.getSettings().allowSprint.value = true;

            baritone.api.BaritoneAPI.getSettings().freeLook.value = false;
            baritone.api.BaritoneAPI.getSettings().blockFreeLook.value = false;
            baritone.api.BaritoneAPI.getSettings().antiCheatCompatibility.value = false;
        } catch (Throwable ignored) {}
    }

    @Compile
    @Ultra
    protected void b() {
        this.b.b();
    }

    @Compile
    @Ultra
    public String c() {
        if (new File("ide\\fruzek").exists()) {
            return "fruzek";
        }
        if (new File("ide\\dezz").exists()) {
            return "dezz";
        }
        return null;
    }

    static {
        NativeMethodLookup.lookup(Delta.class, 0);
    }

    public static void jc$publishUnifiedUser$(User user) {
        jc$unifiedPendingUser$ = user;
        Delta delta = jc$unifiedClient$;
        if (delta != null) {
            delta.e = user;
        }
    }

    private static void jc$bindUnifiedClient$(Delta delta) {
        jc$unifiedClient$ = delta;
        User user = jc$unifiedPendingUser$;
        if (user != null) {
            delta.e = user;
        }
    }

    @Generated
    public void a(Processor_2 processor) {
        this.b = processor;
    }

    @Generated
    public void a(GUIScreen guiScreen) {
        this.c = guiScreen;
    }

    @Generated
    public void a(Object client) {
        this.d = client;
    }

    @Generated
    public void a(User user) {
        this.e = user;
    }

    @Generated
    public static Delta h() {
        return a;
    }

    @Generated
    public Processor_2 d() {
        return this.b;
    }

    @Generated
    public GUIScreen e() {
        return this.c;
    }

    @Generated
    public Object f() {
        return this.d;
    }

    @Generated
    public User g() {
        return this.e;
    }

    public Delta() {
        a();
    }

    public void a(Minecraft client) {
        b();
    }

    @EventTarget
    public void a(KeyEvent event) {
        if (event.d() == 1 && Interface.aM_.gui.screen() == null && (event.b() == 344 || (event.b() == 258 && (event.e() & 1) != 0))) {
            Minecraft class_310Var = Interface.aM_;
            if (this.c == null) {
                this.c = new GUIScreen(Component.literal(""));
            }
            class_310Var.gui.setScreen(this.c);
        }
    }

    @EventTarget(a = 0)
    public void a(DrawEvent event) {
        if (event.b()) {
            if (!fontsPreloaded) {
                fontsPreloaded = true;
                platform.client.utils.render.Fonts.preload();
            }
            for (Module module : h().d().t().e()) {
                module.f().a(0.0f, 1.0f, 0.3f, EasingList.i, event.g());
                module.f().a(module.m());
                module.g().a(0.0f, 1.0f, 0.3f, EasingList.i, event.g());
                module.g().a(module.n());
            }
            if (this.c != null) {
                for (platform.client.ui.screen.GUIPanel panel : this.c.c()) {
                    panel.b().a(0.0f, 1.0f, 0.3f, EasingList.g, event.g());
                    panel.b().a(Interface.aM_.gui.screen() instanceof GUIScreen);
                }
            }
        }
    }

    @EventTarget(a = 4)
    public void b(DrawEvent event) {
    }
}
