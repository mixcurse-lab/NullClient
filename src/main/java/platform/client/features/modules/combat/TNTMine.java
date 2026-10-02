package platform.client.features.modules.combat;

import static platform.api.module.Interface.aM_;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import platform.api.event.events.client.TickEvent;
import platform.api.event.events.player.ClickEvent;
import platform.api.event.events.player.KeyEvent;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Category;
import platform.api.module.Module;
import platform.api.module.ModuleRegister;
import platform.api.module.setting.BindSetting;
import platform.api.module.setting.BooleanSetting;
import platform.api.module.setting.ModeSetting;
import platform.api.module.setting.SliderSetting;
import platform.client.Delta;
import platform.client.utils.rotation.Rotation;

@ModuleRegister(a = "Динамит", b = "Рельса + вагонетка с ТНТ под заранее выпущенную горящую стрелу", c = Category.Combat)
public class TNTMine extends Module {

    private enum State {
        IDLE,
        PLACE_RAIL,
        DRAW_BOW,
        PLACE_CART,
        DONE
    }

    private final BindSetting activateKey = new BindSetting("Клавиша активации", -1);
    private final ModeSetting mode = new ModeSetting("Режим", "Сначала рельсы", "Сначала рельсы", "Мгновенно");
    private final BooleanSetting silentRotation = new BooleanSetting("Сайлент", true);
    private final BooleanSetting aimAtCart = new BooleanSetting("Наводка на точку", true);
    private final SliderSetting chargeTicks = new SliderSetting("Тиков натяжки", 5.0f, 3.0f, 20.0f, 1.0f);

    private State state = State.IDLE;
    private int actionTimer;
    private int originalSlot = -1;
    private int railSlot = -1;
    private int bowSlot = -1;
    private int tntSlot = -1;
    private BlockHitResult targetHit;
    private float originalYaw;
    private float originalPitch;
    private boolean bowSent;
    private boolean railFirst;
    private boolean railDone;
    private boolean shotDone;
    private boolean pendingActivate;

    public TNTMine() {
        a(this.activateKey, this.mode, this.silentRotation, this.aimAtCart, this.chargeTicks);
    }

    @Override
    public void c() {
        reset();
        super.c();
    }

    @EventTarget
    public void a(KeyEvent event) {
        if (event.d() != 1 || aM_.gui.screen() != null) return;
        int key = activateKey.c().intValue();
        if (key == -1 || event.b() != key) return;
        pendingActivate = true;
    }

    @EventTarget
    public void a(ClickEvent event) {
        if (!event.b() || aM_.gui.screen() != null) return;
        int key = activateKey.c().intValue();
        if (key == -1 || event.h() != key) return;
        pendingActivate = true;
    }

    @EventTarget
    public void a(TickEvent event) {
        if (aM_.player == null || aM_.level == null || aM_.gameMode == null) {
            reset();
            return;
        }
        if (pendingActivate && this.state == State.IDLE) {
            this.pendingActivate = false;
            execute();
        }
        processTick();
    }


    // связка: рельса и стрела летят одновременно — вагонетка ставится под уже
    // выпущенную горящую стрелу, поэтому поджог происходит моментально
    private void execute() {
        tntSlot = findItem(Items.TNT_MINECART);
        railSlot = findRail();
        bowSlot = findFlameBow();

        if (tntSlot == -1 || railSlot == -1 || bowSlot == -1) return;

        if (!(aM_.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) return;

        this.targetHit = hit;
        this.originalSlot = aM_.player.getInventory().getSelectedSlot();
        this.originalYaw = aM_.player.getYRot();
        this.originalPitch = aM_.player.getXRot();
        this.railFirst = mode.l("Сначала рельсы");
        this.railDone = false;
        this.shotDone = false;
        this.bowSent = false;
        this.actionTimer = 0;
        this.state = railFirst ? State.PLACE_RAIL : State.DRAW_BOW;
    }

    private void processTick() {
        switch (this.state) {
            case IDLE -> {
            }
            case PLACE_RAIL -> {
                selectSlot(railSlot);
                InteractionResult result = aM_.gameMode.useItemOn(aM_.player, InteractionHand.MAIN_HAND, targetHit);
                if (result.consumesAction()) {
                    aM_.player.swing(InteractionHand.MAIN_HAND);
                }
                this.railDone = true;
                this.state = this.shotDone ? State.PLACE_CART : State.DRAW_BOW;
                this.actionTimer = 0;
            }
            case DRAW_BOW -> {
                if (!bowSent) {
                    selectSlot(bowSlot);
                    aM_.player.connection.send(new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, 0, aM_.player.getYRot(), aM_.player.getXRot()));
                    this.bowSent = true;
                    this.actionTimer = 0;
                }
                aimAtTarget();
                if (++this.actionTimer >= chargeTicks.c().intValue()) {
                    aM_.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN));
                    this.bowSent = false;
                    this.shotDone = true;
                    if (!silentRotation.c().booleanValue()) {
                        aM_.player.setYRot(originalYaw);
                        aM_.player.setXRot(originalPitch);
                    }
                    this.state = railDone ? State.PLACE_CART : State.PLACE_RAIL;
                    this.actionTimer = 0;
                }
            }
            case PLACE_CART -> {
                BlockPos railPos = targetHit.getBlockPos().relative(targetHit.getDirection());
                selectSlot(tntSlot);
                BlockHitResult hit = new BlockHitResult(
                        new Vec3(railPos.getX() + 0.5d, railPos.getY() + 0.15d, railPos.getZ() + 0.5d),
                        Direction.UP,
                        railPos,
                        false
                );
                InteractionResult result = aM_.gameMode.useItemOn(aM_.player, InteractionHand.MAIN_HAND, hit);
                if (result.consumesAction()) {
                    aM_.player.swing(InteractionHand.MAIN_HAND);
                }
                this.state = State.DONE;
                this.actionTimer = 0;
            }
            case DONE -> {
                if (++this.actionTimer >= 1) {
                    selectSlot(originalSlot);
                    reset();
                }
            }
        }
    }

    private void aimAtTarget() {
        if (!aimAtCart.c().booleanValue()) return;
        Rotation angle = getBestAimAngle();
        if (angle == null) return;
        double dist = aM_.player.getEyePosition().distanceTo(targetHit.getLocation());
        // компенсация падения стрелы: на дистанции смотрим чуть выше точки
        float pitchOffset = dist > 3.0d ? (float) Math.min(10.0d, dist * 0.8d) : 0.0f;
        Rotation aimed = new Rotation(angle.c(), net.minecraft.util.Mth.clamp(angle.d() - pitchOffset, -90.0f, 90.0f));
        if (silentRotation.c().booleanValue()) {
            Delta.h().d().k().a(aimed, 220.0f, 1, 1);
        } else {
            aM_.player.setYRot(aimed.c());
            aM_.player.setXRot(aimed.d());
        }
    }


    private Rotation getBestAimAngle() {
        if (targetHit == null || aM_.level == null) return null;

        BlockPos railPos = targetHit.getBlockPos().relative(targetHit.getDirection());
        Vec3 targetPos = Vec3.atCenterOf(railPos);

        AABB checkBox = new AABB(railPos).inflate(0.5d, 1.5d, 0.5d);
        for (Entity entity : aM_.level.getEntities((Entity) null, checkBox)) {
            if (entity instanceof net.minecraft.world.entity.vehicle.minecart.MinecartTNT) {
                targetPos = entity.getBoundingBox().getCenter();
                break;
            }
        }

        return Rotation.a(aM_.player.getEyePosition(), targetPos);
    }


    private void reset() {
        if (this.bowSent && aM_.player != null && aM_.player.connection != null) {
            aM_.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN));
        }
        this.state = State.IDLE;
        this.actionTimer = 0;
        this.bowSent = false;
        this.targetHit = null;
        this.originalSlot = -1;
        this.originalYaw = 0.0f;
        this.originalPitch = 0.0f;
    }


    private void selectSlot(int slot) {
        aM_.player.getInventory().setSelectedSlot(slot);
        aM_.player.connection.send(new ServerboundSetCarriedItemPacket(slot));
    }

    private int findItem(Item item) {
        for (int i = 0; i < 9; i++) {
            if (aM_.player.getInventory().getItem(i).is(item)) {
                return i;
            }
        }
        return -1;
    }


    private int findRail() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = aM_.player.getInventory().getItem(i);
            if (stack.getItem() instanceof BlockItem blockItem
                    && blockItem.getBlock().defaultBlockState().is(BlockTags.RAILS)) {
                return i;
            }
        }
        return -1;
    }


    private int findFlameBow() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = aM_.player.getInventory().getItem(i);
            if (!stack.is(Items.BOW)) continue;

            ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            for (Holder<Enchantment> holder : enchantments.keySet()) {
                if (holder.is(Enchantments.FLAME) && enchantments.getLevel(holder) > 0) {
                    return i;
                }
            }
        }
        return -1;
    }
}
