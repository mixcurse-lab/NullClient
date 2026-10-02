package platform.api.utils.notification;

import platform.api.system.interfaces.NativeMethodLookup;
import static platform.api.module.Interface.aM_;
import platform.client.utils.render.EasingList;

import platform.api.system.configs.BaseProcessor;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Interface;
import platform.api.event.events.render.DrawEvent;
import platform.api.event.events.client.TickEvent;
import platform.api.utils.notification.Notification;

import platform.api.annotation.Compile;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.minecraft.client.gui.screens.ChatScreen;

public class NotificationProcessor extends BaseProcessor implements Interface {
    private final Notification b = new Notification("o", "Пример отображения уведомления", 0);
    private final List<Notification> c = new ArrayList<>();

    @Override
    @Compile
    public void setup() {
    }

    static {
        NativeMethodLookup.lookup(NotificationProcessor.class, 34);
    }

    @Generated
    public Notification a() {
        return this.b;
    }

    @Generated
    public List<Notification> b() {
        return this.c;
    }

    @Override
    public void unSetup() {
    }

    @EventTarget
    public void a(DrawEvent event) {
        if (event.b() && !b().isEmpty()) {
            for (Notification notification : b()) {
                notification.a().a(0.0f, 1.0f, 0.3f, EasingList.g, event.g());
            }
        }
    }

    @EventTarget
    public void a(TickEvent event) {
        List<Notification> notifications = new ArrayList<>(b());
        notifications.remove(this.b);
        boolean preview = (aM_.gui.screen() instanceof ChatScreen) && notifications.isEmpty();
        if (!b().contains(this.b)) {
            b().add(this.b);
        }
        for (Notification notification : new ArrayList<>(b())) {
            if (notification == this.b) {
                notification.a().a(preview);
                if (!preview && notification.a().c() == 0.0f) {
                    b().remove(this.b);
                }
            } else {
                boolean finished = notification.b().a(notification.f() - 100);
                notification.a().a(!finished);
                if (finished && notification.a().c() == 0.0f) {
                    b().remove(notification);
                }
            }
        }
    }

    public void a(Notification notification) {
        int time = notification.f();
        notification.a(time + (((int) b().stream().filter(current -> {
            return current.f() >= time;
        }).filter(current2 -> {
            return (current2.f() - time) % 50 == 0;
        }).count()) * 50));
        b().add(notification);
    }
}



