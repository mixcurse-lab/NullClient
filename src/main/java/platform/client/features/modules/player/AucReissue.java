package platform.client.features.modules.player;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.module.Module;
import platform.client.utils.text.ChatUtil;
import platform.client.utils.player.ServerUtil;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Interface;
import platform.api.module.ModuleRegister;
import platform.api.event.events.client.PacketEvent;
import platform.api.event.events.client.TickEvent;

import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Items;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;

@ModuleRegister(a = "Auc Reissue", b = "Автоматически перевыставляет предметы на аукционе", c = Category.Player)
public class AucReissue extends Module implements Interface {
    private boolean b;

    @Override
    public void b() {
        super.b();
        this.b = false;
    }

    @Override
    public void c() {
        super.c();
        this.b = false;
    }

    @EventTarget
    public void a(TickEvent event) {
        if (!ServerUtil.e() && ((ServerUtil.a.d() != -1 || ServerUtil.d.b() != -1) && aM_.player.tickCount >= 220 && !Delta.h().d().v().g().a() && !aM_.player.getCooldowns().isOnCooldown(Items.CLOCK.getDefaultInstance()))) {
            AbstractContainerScreen<?> class_465Var = (AbstractContainerScreen<?>) aM_.gui.screen();
            if (class_465Var instanceof AbstractContainerScreen) {
                AbstractContainerScreen<?> handledScreen = class_465Var;
                if (handledScreen instanceof ContainerScreen) {
                    String title = handledScreen.getTitle().getString();
                    if (aM_.player.tickCount % 5 == 0) {
                        if (title.matches(".*А.*у.*к.*ц.*и.*о.*н.*")) {
                            aM_.gameMode.handleContainerInput(handledScreen.getMenu().containerId, 46, 1, ContainerInput.PICKUP, aM_.player);
                        } else if (title.matches(".*Х.*р.*а.*н.*и.*л.*и.*щ.*е.*")) {
                            aM_.gameMode.handleContainerInput(handledScreen.getMenu().containerId, 52, 1, ContainerInput.PICKUP, aM_.player);
                        }
                    }
                } else if (aM_.player.tickCount % 20 == 0) {
                    aM_.player.connection.sendCommand("ah");
                }
            } else if (aM_.player.tickCount % 20 == 0) {
                aM_.player.connection.sendCommand("ah");
            }
        }
        if (this.b && (aM_.gui.screen() instanceof ContainerScreen)) {
            aM_.player.clientSideCloseContainer();
            this.b = false;
        }
    }

    @EventTarget
    public void a(PacketEvent eventPacket) {
        if (!ServerUtil.e()) {
            if ((ServerUtil.a.d() != -1 || ServerUtil.d.b() != -1) && aM_.player.tickCount >= 220 && eventPacket.c() && eventPacket.d() instanceof ClientboundSystemChatPacket packet) {
                String msg = packet.content().getString();
                    if (msg.equals("Данная команда недоступна в режиме AFK")) {
                        Delta.h().d().v().g().a(10);
                    }
                    if (msg.equals("[☃] В хранилище отсутствуют предметы для перевыставления.")) {
                        ChatUtil.a((Object) "Авто-выключение: в хранилище отсутствуют предметы для перевыставления");
                        a();
                    }
                    if (msg.contains("[☃] Предметы успешно перевыставлены ") || msg.contains("[✔] Предметы успешно перевыставлены!")) {
                        aM_.player.getCooldowns().addCooldown(Items.CLOCK.getDefaultInstance(), 1200);
                        this.b = true;
                    }
                    if (msg.contains("[☃] Вы можете переставлять предметы раз в минуту! Подождите ")) {
                        int seconds = Integer.parseInt(msg.replaceAll(".*Подождите (\\d+) сек\\..*", "$1"));
                        aM_.player.getCooldowns().addCooldown(Items.CLOCK.getDefaultInstance(), (seconds * 20) + 20);
                        this.b = true;
                    }
            }
        }
    }
}





