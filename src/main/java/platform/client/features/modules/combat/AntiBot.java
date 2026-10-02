package platform.client.features.modules.combat;

import platform.api.module.Interface;

import static platform.api.module.Interface.aM_;
import platform.api.module.Module;
import platform.client.utils.text.ChatUtil;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.api.event.events.client.PacketEvent;
import platform.api.event.events.client.TickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.client.multiplayer.PlayerInfo;

@ModuleRegister(a = "Anti Bot", b = "Скрывает фальшивых игроков, появляющихся в мире", c = Category.Combat)
public class AntiBot extends Module {
    private final List<UUID> b = new ArrayList();

    @Override
    public void b() {
        super.b();
        this.b.clear();
    }

    @Override
    public void c() {
        super.c();
        this.b.clear();
    }

    @EventTarget
    public void a(PacketEvent event) {
        if (event.c()) {
            if (event.d() instanceof ClientboundAddEntityPacket spawn) {
                if (spawn.getType() == EntityTypes.PLAYER) {
                    PlayerInfo entry = aM_.getConnection().getPlayerInfo(spawn.getUUID());
                    boolean skin = (entry == null || entry.getSkin() == null || entry.getSkin().body() == null) ? false : true;
                    boolean texture = (entry == null || entry.getProfile().properties().get("textures").isEmpty()) ? false : true;
                    boolean ping = entry == null || entry.getLatency() == 0;
                    if (!skin && !texture && ping) {
                        this.b.add(spawn.getUUID());
                    }
                }
            }
        }
    }

    @EventTarget
    public void a(TickEvent event) {
        List<UUID> checked = new ArrayList<>();
        for (UUID uuid : this.b) {
            for (Player player : aM_.level.players()) {
                if (player.getUUID().equals(uuid)) {
                    boolean armor = (player.getItemBySlot(EquipmentSlot.HEAD).isEmpty() || player.getItemBySlot(EquipmentSlot.CHEST).isEmpty() || player.getItemBySlot(EquipmentSlot.LEGS).isEmpty() || player.getItemBySlot(EquipmentSlot.FEET).isEmpty()) ? false : true;
                    if (armor) {
                        ChatUtil.a((Object) "Фальшивый игрок был обнаружен, и удален из мира.");
                        aM_.level.removeEntity(player.getId(), Entity.RemovalReason.DISCARDED);
                    }
                    checked.add(uuid);
                    break;
                }
            }
        }
        this.b.removeAll(checked);
    }
}


