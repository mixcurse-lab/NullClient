package platform.client.features.modules.player;

import static platform.api.module.Interface.aM_;
import platform.api.module.Module;
import platform.client.utils.text.ChatUtil;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Interface;
import platform.api.module.ModuleRegister;
import platform.api.event.events.render.HotbarEvent;
import platform.api.event.events.client.PacketEvent;
import platform.api.event.events.client.TickEvent;

import platform.client.utils.timer.CounterUtil;
import lombok.Generated;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

@ModuleRegister(a = "Auto Fish", b = "Автоматически ловит рыбу в AFK-режиме", c = Category.Player)
public class AutoFish extends Module implements Interface {
    private final CounterUtil b = new CounterUtil();
    private boolean c;

    @Generated
    public CounterUtil q() {
        return this.b;
    }

    @Generated
    public boolean r() {
        return this.c;
    }

    @Override
    public void b() {
        super.b();
        if (aM_.player != null && aM_.player.getInventory().getItem(aM_.player.getInventory().getSelectedSlot()).getItem() == Items.FISHING_ROD) {
            if (aM_.player.fishing == null) {
                d(false);
            }
            ChatUtil.a((Object) (j() + " активирован, удачной рыбалки!"));
        }
    }

    @EventTarget
    public void a(PacketEvent event) {
        if (event.d() instanceof ClientboundSoundPacket packet) {
            if (packet.getSound().value().location().equals(SoundEvents.FISHING_BOBBER_SPLASH.location()) && aM_.player.fishing != null && aM_.player.fishing.distanceToSqr(packet.getX(), packet.getY(), packet.getZ()) <= 0.48999979194765847d) {
                d(true);
                this.b.b();
            }
        }
    }

    @EventTarget
    public void a(TickEvent event) {
        if (this.b.a(450L) && this.c) {
            d(false);
        }
    }

    @EventTarget
    public void a(HotbarEvent event) {
        if (this.c) {
            event.a(true);
        }
    }

    public void d(boolean cast) {
        aM_.gameMode.useItem(aM_.player, InteractionHand.MAIN_HAND);
        aM_.player.swing(InteractionHand.MAIN_HAND);
        this.c = cast;
    }
}





