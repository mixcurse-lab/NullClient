package platform.client.features.modules.player;

import platform.api.module.Interface;

import static platform.api.module.Interface.aM_;
import platform.api.module.Module;
import platform.client.utils.player.ServerUtil;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.client.utils.bridge.network.Packet;
import platform.api.event.events.client.BackendEvent;
import platform.api.event.events.client.TickEvent;

import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.AABB;

@ModuleRegister(a = "Captcha Solver", b = "Автоматически решает капчу при входе на сервер FunTime", c = Category.Player)
public class CaptchaSolver extends Module {

    @EventTarget
    public void a(TickEvent event) {
        if (ServerUtil.a.c() && aM_.hitResult instanceof BlockHitResult hit) {
            try {
                aM_.level.getEntitiesOfClass(ItemFrame.class, new AABB(hit.getBlockPos()).inflate(0.5d), frame -> frame.getItem().getItem() instanceof net.minecraft.world.item.MapItem)
                    .stream().findFirst().ifPresent(frame -> {
                    });
            } catch (Exception ignored) {}
        }
    }

    @EventTarget
    public void a(BackendEvent event) {
        String code;
        Packet packet = event.d();
        if (event.b() && "captcha".equals(packet.b()) && (code = packet.a().a(packet.c(), "code")) != null) {
            if (aM_.player != null && aM_.player.connection != null) {
                aM_.player.connection.sendChat(code);
            }
        }
    }
}



