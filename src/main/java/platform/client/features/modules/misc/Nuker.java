package platform.client.features.modules.misc;

import platform.api.module.Interface;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.module.InterfaceC0020Opcode;
import platform.api.module.Module;
import platform.client.utils.text.ChatUtil;
import platform.client.utils.render.ColorUtil;
import platform.client.utils.player.InventoryUtil;
import platform.client.utils.rotation.Look;
import platform.client.utils.math.MathUtil;
import platform.client.utils.player.MoveUtil;
import platform.client.utils.player.ServerUtil;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.api.event.events.render.DrawEvent;
import platform.api.event.events.player.InputEvent;
import platform.api.event.events.client.TickEvent;
import platform.client.features.modules.misc.MineAssistant;
import platform.client.utils.rotation.Rotation;

import platform.api.module.setting.BooleanSetting;
import platform.api.module.setting.ModeSetting;
import platform.api.module.setting.SliderSetting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;

@ModuleRegister(a = "Nuker", b = "Автоматически разрушает блоки в радиусе досягаемости", c = Category.Misc)
public class Nuker extends Module {
    private final ModeSetting b = new ModeSetting("Режим копания территории", "Шахта ФанТайм", "Шахта ФанТайм", "Общий");
    private final SliderSetting c = new SliderSetting("Дистанция копания", 4.0f, 1.0f, 6.0f, 0.5f);
    private final SliderSetting d = new SliderSetting("Скорость копания", 1.0f, 1.0f, 5.0f, 1.0f);
    private final BooleanSetting e = new BooleanSetting("Не копать под себя", true);
    private BlockPos f;

    public Nuker() {
        a(this.b, this.c, this.d, this.e);
    }

    @Override
    public void b() {
        super.b();
        this.f = null;
    }

    @Override
    public void c() {
        super.c();
        this.f = null;
    }

    @EventTarget
    public void a(TickEvent event) {
        MineAssistant assistant = Delta.h().d().t().mineAssistant();
        this.f = null;
        ItemStack tool = aM_.player.getMainHandItem();
        if (tool.isDamageableItem() && tool.getMaxDamage() - tool.getDamageValue() < 50) {
            ChatUtil.a((Object) "Работа прекращена во избежание поломки кирки.");
            a();
            return;
        }
        if (this.b.l("Шахта ФанТайм") && ServerUtil.a.a()) {
            assistant.q();
        }
        boolean pickaxe = tool.is(ItemTags.PICKAXES);
        int minY = this.e.c().booleanValue() ? aM_.player.blockPosition().getY() + ((pickaxe && InventoryUtil.a(tool, "Бульдозер")) ? 1 : 0) : Integer.MIN_VALUE;
        float range = this.c.h().floatValue();
        AABB scan = aM_.player.getBoundingBox().inflate(range + 1.0f);
        Vec3 eye = aM_.player.getEyePosition();
        Vec3 look = aM_.player.getViewVector(1.0f);
        double bestScore = 1.7976922776554427E308d;
        Direction face = null;
        for (BlockPos pos : BlockPos.betweenClosed(BlockPos.containing(scan.minX, scan.minY, scan.minZ), BlockPos.containing(scan.maxX, scan.maxY, scan.maxZ))) {
            if (pos.getY() >= minY && a(pos, pickaxe, assistant.r())) {
                Vec3 center = Vec3.atCenterOf(pos);
                Vec3 diff = center.subtract(eye);
                double along = diff.dot(look);
                Vec3 hit = along > 0.0d ? (Vec3) new AABB(pos).clip(eye, center).orElse(null) : null;
                if (hit != null && eye.distanceToSqr(hit) <= range * range && a(eye, pos)) {
                    double score = diff.subtract(look.scale(along)).lengthSqr();
                    if (score < bestScore) {
                        bestScore = score;
                        this.f = pos.immutable();
                        face = Direction.getApproximateNearest(hit.subtract(center));
                    }
                }
            }
        }
        if (this.f != null) {
            Rotation base = Rotation.a(eye, Vec3.atCenterOf(this.f));
            Delta.h().d().k().a(new Rotation(Mth.wrapDegrees(base.c() + MathUtil.a(-3.0f, 3.0f)), Mth.clamp(base.d() + MathUtil.a(-3.0f, 3.0f), -90.0f, 90.0f)), 180.0f, 1, 1);
            if (Rotation.b().a(base) <= 20.0d) {
                for (int i = 0; i < this.d.h().intValue(); i++) {
                    aM_.gameMode.continueDestroyBlock(this.f, face);
                }
                aM_.player.swing(InteractionHand.MAIN_HAND);
            }
        }
    }

    @EventTarget
    public void a(InputEvent event) {
        if (this.f != null) {
            MoveUtil.a(event, Look.b(), 5);
        }
    }

    @EventTarget
    public void a(DrawEvent event) {
        if (event.c() && this.f != null) {
            event.e().a(event.h(), new AABB(this.f), ColorUtil.a(255, 0, 0, InterfaceC0020Opcode.aN), 2.0f);
        }
    }

    private boolean a(BlockPos pos, boolean pickaxe, AABB mineBox) {
        BlockState state = aM_.level.getBlockState(pos);
        if (state.isAir()) {
            return false;
        }
        return !this.b.l("Шахта ФанТайм") || (pickaxe && mineBox.contains(Vec3.atCenterOf(pos)) && state.getDestroyProgress(aM_.player, aM_.level, pos) >= 1.0f);
    }

    private boolean a(Vec3 eye, BlockPos pos) {
        AABB box = new AABB(pos).deflate(0.05000000385685581d);
        for (int i = -1; i < 8; i++) {
            Vec3 point = i < 0 ? box.getCenter() : new Vec3((i & 1) == 0 ? box.minX : box.maxX, (i & 2) == 0 ? box.minY : box.maxY, (i & 4) == 0 ? box.minZ : box.maxZ);
            if (aM_.level.clip(new ClipContext(eye, point, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, aM_.player)).getBlockPos().equals(pos)) {
                return true;
            }
        }
        return false;
    }
}

