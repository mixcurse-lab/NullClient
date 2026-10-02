package platform.client.features.modules.player;

import platform.api.module.Interface;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.module.Module;
import platform.client.utils.text.ChatUtil;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.api.event.events.client.PacketEvent;
import platform.api.event.events.client.TickEvent;
import platform.api.module.setting.BooleanSetting;
import platform.api.module.setting.MultiModeSetting;

import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.core.component.DataComponents;

@ModuleRegister(a = "Use Tracker", b = "Отслеживает выбранные использования и уведомляет о них", c = Category.Player)
public class UseTracker extends Module {
    private final MultiModeSetting b = new MultiModeSetting("Отслеживать использования", new BooleanSetting("Тотема", true), new BooleanSetting("Зелья", false), new BooleanSetting("Предмета", true));

    public UseTracker() {
        a(this.b);
    }

    @EventTarget
    public void a(TickEvent event) {
        if (this.b.a("Предмета").c().booleanValue() && aM_.level != null && aM_.player != null) {
            for (Player player : aM_.level.players()) {
                if (player == aM_.player) continue;
                ItemStack active = player.getUseItem();
                if (active.isEmpty()) continue;
                boolean isPotion = active.has(DataComponents.POTION_CONTENTS);
                boolean isFood = active.has(DataComponents.FOOD);
                boolean isMilk = active.is(Items.MILK_BUCKET);
                if (isPotion || isFood || isMilk) {
                    if (player.getUseItemRemainingTicks() == 1) {
                        String color = isPotion ? "&a" : "&c";
                        ChatUtil.a("[" + j() + "]", player.getName().getString() + " использовал \"" + color + active.getHoverName().getString() + "&7\"");
                    }
                }
            }
        }
    }

    @EventTarget
    public void a(PacketEvent event) {
        if (!event.c()) return;
        if (event.d() instanceof ClientboundEntityEventPacket packet) {
            if (packet.getEventId() == 35) {
                Entity ent = packet.getEntity(aM_.level);
                if (ent instanceof LivingEntity living) {
                    if (this.b.a("Тотема").c().booleanValue()) {

                        ItemStack main = living.getMainHandItem();
                        ItemStack off = living.getOffhandItem();
                        ItemStack totem = null;
                        if (main.is(Items.TOTEM_OF_UNDYING)) totem = main;
                        else if (off.is(Items.TOTEM_OF_UNDYING)) totem = off;
                        if (totem != null) {
                            String name = totem.getHoverName().getString();
                            if (living == aM_.player) {
                                ChatUtil.a("[" + j() + "]", "Вы потеряли " + name);
                            } else {
                                ChatUtil.a("[" + j() + "]", living.getName().getString() + " потерял " + name);
                            }
                        }
                    }
                }
            }
        }
    }
}



