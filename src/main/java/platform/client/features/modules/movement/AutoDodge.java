package platform.client.features.modules.movement;

import platform.api.module.Interface;

import net.minecraft.client.player.AbstractClientPlayer;
import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.module.Module;
import platform.client.utils.player.InventoryUtil;
import platform.client.utils.math.MathUtil;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.api.event.events.client.PacketEvent;
import platform.api.event.events.client.TickEvent;
import platform.client.utils.rotation.Rotation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.entity.EntityTypeTest;

@ModuleRegister(a = "Auto Dodge", b = "Автоматически уклоняется от выбранных целей", c = Category.Movement)
public class AutoDodge extends Module {
    private final Map<Integer, b> c = new HashMap();
    int b = 0;

    @Override
    public void c() {
        this.c.clear();
        super.c();
    }

    @EventTarget
    public void a(TickEvent tickEvent) {
        Iterator<Map.Entry<Integer, b>> iterator = this.c.entrySet().iterator();
        while (iterator.hasNext()) {
            if (aM_.level.getEntity(iterator.next().getKey().intValue()) == null) {
                iterator.remove();
            }
        }
        AABB playerHitboxExpanded = aM_.player.getBoundingBox().inflate(2.0d);
        for (ThrownSplashPotion potionEntity : aM_.level.getEntities(EntityTypeTest.forClass(ThrownSplashPotion.class), aM_.player.getBoundingBox().inflate(((Integer) aM_.options.renderDistance().get()).intValue() * 16), p -> {
            return true;
        })) {
            b matched = this.c.get(Integer.valueOf(potionEntity.getId()));
            if (matched != null) {
                boolean trace = a(potionEntity, playerHitboxExpanded);
                int rgba = (-16777216) | (matched.a & 16777215);
                if (!trace || Delta.h().d().e().d(matched.b)) {
                    return;
                }
                if ((rgba == -13447886 || rgba == -16776961) && aM_.player.distanceTo(potionEntity) > 2.300000381469741d && this.b >= 0) {
                    ItemStack kelp = new ItemStack(Items.DRIED_KELP);
                    if (!aM_.player.getCooldowns().isOnCooldown(kelp) && InventoryUtil.b(Items.DRIED_KELP) != -1) {
                        if (Delta.h().d().v().b().a().isEmpty()) {
                            if (Rotation.b().a(new Rotation(aM_.player.getYRot(), aM_.player.getXRot())) < 20.0d) {
                                this.b++;
                            }
                            if (this.b >= 2) {
                                this.b = 5;
                                Delta.h().d().v().b().a(new ItemStack(Items.DRIED_KELP));
                            }
                        }
                        Rotation aimRotation = Rotation.a(aM_.player.getEyePosition(), potionEntity.getEyePosition());
                        Delta.h().d().k().a(new Rotation(aimRotation.c() + MathUtil.a(-3.0f, 3.0f), aimRotation.d() + MathUtil.a(-3.0f, 3.0f)), 180.0f, 1, 1);
                        break;
                    }
                } else {
                    return;
                }
            }
        }
        this.b++;
    }

    @EventTarget
    public void a(PacketEvent packetEvent) {
        if (!packetEvent.c() || aM_.player == null || aM_.level == null) {
            return;
        }
        a(packetEvent.d());
        if (packetEvent.c() && packetEvent.d() instanceof ClientboundSystemChatPacket gameMsg) {
            if (gameMsg.content().getString().equals("На этой анархии этот предмет не работает")) {
                this.b = -50;
            }
        }
    }

    private void a(Packet<?> packet) {
        if (packet instanceof ClientboundBundlePacket) {
            ClientboundBundlePacket bundlePacket = (ClientboundBundlePacket) packet;
            for (Packet<?> innerPacket : bundlePacket.subPackets()) {
                a(innerPacket);
            }
            return;
        }
        if (packet instanceof ClientboundAddEntityPacket) {
            ClientboundAddEntityPacket spawnPacket = (ClientboundAddEntityPacket) packet;
            if (spawnPacket.getType() != EntityTypes.SPLASH_POTION) {
                return;
            }
            Map<String, a> holders = q();
            Vec3 spawnPosition = new Vec3(spawnPacket.getX(), spawnPacket.getY(), spawnPacket.getZ());
            Vec3 spawnVelocity = spawnPacket.getMovement();
            double bestDistance = 1.7976922776554304E308d;
            String matchedNick = null;
            int matchedRgb = 0;
            for (Map.Entry<String, a> holderEntry : holders.entrySet()) {
                a holder = holderEntry.getValue();
                double distance = spawnPosition.distanceTo(holder.b);
                if (distance <= 25.0d) {
                    if (holder.b.y - spawnPosition.y > 2.0d) {
                        if (new Vec3(spawnPosition.x - holder.b.x, 0.0d, spawnPosition.z - holder.b.z).length() < 15.0d) {
                            if (distance < bestDistance) {
                                bestDistance = distance;
                                matchedRgb = holder.a;
                                matchedNick = holderEntry.getKey();
                            }
                        }
                    } else if (spawnVelocity.lengthSqr() <= 9.99999773128142E-7d || spawnVelocity.normalize().dot(holder.c.normalize()) > 0.10000000396251493d) {
                        if (distance < bestDistance) {
                            bestDistance = distance;
                            matchedRgb = holder.a;
                            matchedNick = holderEntry.getKey();
                        }
                    }
                }
            }
            if (matchedNick != null) {
                this.b = 0;
                this.c.put(Integer.valueOf(spawnPacket.getId()), new b(matchedRgb, matchedNick));
            }
        }
    }

    private Map<String, a> q() {
        HashMap<String, a> result = new HashMap<>();
        for (AbstractClientPlayer class_746Var : aM_.level.players()) {
            ItemStack mainHand = class_746Var.getMainHandItem();
            ItemStack offHand = class_746Var.getOffhandItem();
            int splashColor = mainHand.getItem() == Items.SPLASH_POTION ? a(mainHand) : offHand.getItem() == Items.SPLASH_POTION ? a(offHand) : -1;
            if (splashColor >= 0 && class_746Var != aM_.player && aM_.player.distanceToSqr(class_746Var) <= 400.0d) {
                result.put(class_746Var.getName().getString(), new a(splashColor, class_746Var.position(), class_746Var.getViewVector(1.0f)));
            }
        }
        return result;
    }

    private static boolean a(ThrownSplashPotion potionEntity, AABB expandedPlayer) {
        Vec3 velocity = potionEntity.getDeltaMovement();
        Vec3 position = potionEntity.position();
        for (int step = 0; step < 70 && velocity.lengthSqr() >= 9.99999773128142E-7d && position.y >= aM_.level.getMinY() && position.y <= aM_.level.getMinY() + aM_.level.getHeight(); step++) {
            double drag = aM_.level.getFluidState(BlockPos.containing(position)).is(FluidTags.WATER) ? 0.8000000016738433d : 0.9900000205305426d;
            velocity = new Vec3(velocity.x * drag, (velocity.y - 0.0500000024232657d) * drag, velocity.z * drag);
            Vec3 nextPosition = position.add(velocity);
            ClipContext ctx = new ClipContext(position, nextPosition, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, potionEntity);
            BlockHitResult blockHit = aM_.level.clip(ctx);
            if (blockHit.getType() == HitResult.Type.BLOCK) {
                return a(expandedPlayer, position, blockHit.getLocation());
            }
            if (a(expandedPlayer, position, nextPosition)) {
                return true;
            }
            position = nextPosition;
        }
        return false;
    }

    private static boolean a(AABB box, Vec3 first, Vec3 second) {
        return new AABB(Math.min(first.x, second.x), Math.min(first.y, second.y), Math.min(first.z, second.z), Math.max(first.x, second.x), Math.max(first.y, second.y), Math.max(first.z, second.z)).inflate(0.11999995180429479d).intersects(box);
    }

    private static int a(ItemStack itemStack) {
        PotionContents contents = (PotionContents) itemStack.get(DataComponents.POTION_CONTENTS);
        if (contents != null) {
            return contents.getColor() & 16777215;
        }
        return 0;
    }

    static final class a {
        final int a;
        final Vec3 b;
        final Vec3 c;

        a(int color, Vec3 position, Vec3 lookDirection) {
            this.a = color;
            this.b = position;
            this.c = lookDirection;
        }
    }

    static final class b {
        final int a;
        final String b;

        b(int color, String nick) {
            this.a = color;
            this.b = nick;
        }
    }
}


