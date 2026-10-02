package platform.client.features.modules.player;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.module.Module;
import platform.client.utils.player.InventoryUtil;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Interface;
import platform.api.module.ModuleRegister;
import platform.api.event.events.player.InputEvent;
import platform.api.event.events.client.PacketEvent;

import platform.api.module.setting.BindSetting;
import platform.api.module.setting.BooleanSetting;
import java.util.Objects;
import lombok.Generated;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;

@ModuleRegister(a = "Elytra Helper", b = "Выполняет действия с элитрой по нажатию назначенной клавиши", c = Category.Player)
public class ElytraHelper extends Module implements Interface {
    private final BooleanSetting b = new BooleanSetting("Автостарт после свапа", false);
    private final BooleanSetting c;
    private final BooleanSetting d;
    private final BindSetting e;
    private final BindSetting f;
    private boolean g;
    private boolean h;
    private int i;

    @Generated
    public BooleanSetting q() {
        return this.b;
    }

    @Generated
    public BooleanSetting r() {
        return this.c;
    }

    @Generated
    public BooleanSetting s() {
        return this.d;
    }

    @Generated
    public BindSetting t() {
        return this.e;
    }

    @Generated
    public BindSetting u() {
        return this.f;
    }

    @Generated
    public boolean v() {
        return this.g;
    }

    @Generated
    public boolean w() {
        return this.h;
    }

    @Generated
    public int x() {
        return this.i;
    }

    public ElytraHelper() {
        BooleanSetting booleanSetting = new BooleanSetting("Использовать /fly при свапе", false);
        BooleanSetting booleanSetting2 = this.b;
        Objects.requireNonNull(booleanSetting2);
        this.c = (BooleanSetting) booleanSetting.a(booleanSetting2::c);
        BooleanSetting booleanSetting3 = new BooleanSetting("Автофейерверк", false);
        BooleanSetting booleanSetting4 = this.b;
        Objects.requireNonNull(booleanSetting4);
        this.d = (BooleanSetting) booleanSetting3.a(booleanSetting4::c);
        this.e = new BindSetting("Кнопка фейерверка", -1).a(() -> {
            z();
        });
        this.f = new BindSetting("Кнопка переключения", -1).a(() -> {
            int slot = aM_.player.getItemBySlot(EquipmentSlot.CHEST).getItem() == Items.ELYTRA ? InventoryUtil.a() : InventoryUtil.b(Items.ELYTRA);
            Delta.h().d().v().a().b(slot, 1, 1);
            if (this.b.c().booleanValue() && InventoryUtil.b(Items.ELYTRA) == slot) {
                this.i = aM_.player.tickCount;
                this.g = true;
            }
        });
        a(this.f, this.e, this.b, this.c, this.d);
    }

    @EventTarget
    public void a(InputEvent event) {
        if (aM_.player.tickCount < 5) {
            this.g = false;
        } else if (this.g && y()) {
            b(event);
        }
    }

    private boolean y() {
        if (!this.c.c().booleanValue() || this.h) {
            return true;
        }
        if (this.i + 1 == aM_.player.tickCount) {
            aM_.player.connection.sendCommand("fly");
        }
        if (aM_.player.getAbilities().mayfly && this.i <= aM_.player.tickCount && aM_.player.onGround()) {
            double x = aM_.player.getX();
            double y = aM_.player.getY() + 0.19999997317790985d;
            double z = aM_.player.getZ();
            aM_.player.connection.send(new ServerboundMovePlayerPacket.Pos(x, y, z, false, aM_.player.horizontalCollision));
            aM_.player.setPos(x, y, z);
            this.i = aM_.player.tickCount + 9;
        }
        if (aM_.player.getAbilities().mayfly && !aM_.player.onGround()) {
            aM_.player.getAbilities().flying = true;
            aM_.player.setDeltaMovement(0.0d, 0.0d, 0.0d);
            aM_.player.onUpdateAbilities();
            this.g = false;
        }
        if (b(15) || (b(3) && aM_.player.getItemBySlot(EquipmentSlot.CHEST).getItem() != Items.ELYTRA)) {
            this.g = false;
            return false;
        }
        return false;
    }

    private void b(InputEvent event) {
        boolean wearingElytra = aM_.player.getItemBySlot(EquipmentSlot.CHEST).getItem() == Items.ELYTRA;
        if (!aM_.player.isFallFlying() && wearingElytra) {
            event.b(aM_.player.tickCount % 2 == 0);
            this.i = aM_.player.tickCount;
            if (!this.d.c().booleanValue()) {
                this.g = false;
            }
        }
        if (aM_.player.isFallFlying() && this.d.c().booleanValue() && !aM_.player.isInWater() && !aM_.player.onGround() && b(2)) {
            z();
            this.g = false;
        }
        if (b(10)) {
            this.g = false;
        }
    }

    private void z() {
        if (aM_.player.isFallFlying()) {
            Delta.h().d().v().b().a(Items.FIREWORK_ROCKET.getDefaultInstance());
        }
    }

    private boolean b(int delay) {
        return this.i + delay < aM_.player.tickCount;
    }

    @EventTarget
    public void a(PacketEvent event) {
        if (event.c() && event.d() instanceof ClientboundSystemChatPacket chat) {
            if (this.g && chat.content().getString().contains("Эту команду могут писать только донатеры выше рангом")) {
                this.h = true;
            }
        }
    }

    @Override
    public void c() {
        super.c();
        this.g = false;
    }
}





