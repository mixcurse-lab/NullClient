package platform.client.features.modules.misc;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.module.InterfaceC0020Opcode;
import platform.api.module.Module;
import platform.client.utils.text.ChatUtil;
import platform.client.utils.render.ColorUtil;
import platform.client.utils.player.InventoryUtil;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.api.event.events.render.DrawEvent;
import platform.api.event.events.client.TickEvent;
import platform.api.handlers.InteractHandler;
import platform.inject.invokers.MinecraftInvoker;

import platform.client.features.modules.misc.XRay;
import platform.client.utils.rotation.Rotation;

import platform.client.utils.timer.CounterUtil;
import platform.api.module.setting.ModeSetting;
import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalRunAway;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;
import lombok.Generated;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.component.DataComponents;

@ModuleRegister(a = "Ancient Farmer", b = "Автоматически фармит древние обломки в режиме полета", c = Category.Misc)
public class AncientFarmer extends Module {
    private final ModeSetting b = new ModeSetting("Режим поиска территории", "Поиск сверху", "Поиск сверху", "Поиск снизу");
    private final ExecutorService c = Executors.newSingleThreadExecutor();
    private final CounterUtil d = new CounterUtil();
    private Phase e = Phase.SEARCH;
    private BlockPos f;
    private AABB g;

    enum Phase {
        SEARCH,
        TNT,
        RETREAT,
        MINE
    }

    @Override
    public void b() {
        super.b();
        d(true);
    }

    @Override
    public void c() {
        super.c();
        d(false);
    }

    public AncientFarmer() {
        a(this.b);
    }

    @EventTarget
    public void a(TickEvent event) {
        if (aM_.player == null || aM_.level == null) return;
        AncientFarmer.a missing = Arrays.stream(AncientFarmer.a.values()).filter(requirement -> !requirement.a()).findFirst().orElse(null);
        XRay xray = Delta.h().d().t().xRay();
        if (xray == null) return;
        boolean work = missing == AncientFarmer.a.TNT && this.e != Phase.SEARCH;
        if (missing != null && !work) {
            ChatUtil.a("Для работы модуля " + missing.c() + "!");
            a();
            return;
        }
        InteractHandler eat = Delta.h().d().v().k();
        IBaritone baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
        int foodSlot = IntStream.range(0, 9).filter(slot -> aM_.player.getInventory().getItem(slot).has(DataComponents.FOOD)).findFirst().orElse(-1);
        if (!eat.a() && aM_.player.getFoodData().getFoodLevel() <= 17 && foodSlot != -1) {
            baritone.getPathingBehavior().cancelEverything();
            eat.a(foodSlot);
        }
        int potionSlot = IntStream.range(0, 9).filter(slot -> StreamSupport.stream(((PotionContents) aM_.player.getInventory().getItem(slot).getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY)).getAllEffects().spliterator(), false).anyMatch(effect -> effect.getEffect().is(MobEffects.FIRE_RESISTANCE))).findFirst().orElse(-1);
        if (!eat.a() && potionSlot != -1 && (!aM_.player.hasEffect(MobEffects.FIRE_RESISTANCE) || aM_.player.getEffect(MobEffects.FIRE_RESISTANCE).getDuration() <= 100)) {
            baritone.getPathingBehavior().cancelEverything();
            eat.a(potionSlot);
        }
        if (eat.a()) {
            return;
        }
        if (!xray.m()) {
            xray.a();
            return;
        }
        boolean near = this.g != null && aM_.player.blockPosition().distSqr(BlockPos.containing(this.g.getCenter())) < 6.25d;
        boolean primed = !aM_.level.getEntitiesOfClass(PrimedTnt.class, aM_.player.getBoundingBox().inflate(8.0d), t -> true).isEmpty();
        this.f = null;
        switch (this.e) {
            case SEARCH:
                if (!xray.s().isEmpty()) {
                    ChatUtil.a("Вскапываем обломки найденные по пути");
                    this.e = Phase.MINE;
                } else if (this.g == null) {
                    this.c.execute(() -> {
                        if (this.g == null && aM_.player != null && aM_.level != null && aM_.player.tickCount > 20) {
                            aM_.execute(() -> ChatUtil.a("Переходим к поиску новой территории."));
                            this.g = q();
                        }
                    });
                } else if (near) {
                    baritone.getPathingBehavior().cancelEverything();
                    this.e = Phase.TNT;
                } else if (!baritone.getPathingBehavior().hasPath()) {
                    baritone.getCustomGoalProcess().setGoalAndPath(new GoalBlock(BlockPos.containing(this.g.getCenter())));
                }
                break;
            case TNT:
                double reach = aM_.player.blockInteractionRange();
                BlockPos tnt = StreamSupport.stream(BlockPos.withinManhattan(aM_.player.blockPosition(), (int) reach, (int) reach, (int) reach).spliterator(), false).filter(pos -> aM_.level.getBlockState(pos).is(Blocks.TNT)).map(BlockPos::immutable).findFirst().orElse(null);
                if (primed) {
                    this.e = Phase.RETREAT;
                    break;
                } else if (tnt != null) {
                    this.f = tnt;
                    int flint = InventoryUtil.a(Items.FLINT_AND_STEEL, true);
                    if (flint != -1) {
                        Vec3 eye = aM_.player.getEyePosition();
                        Vec3 center = Vec3.atCenterOf(tnt);
                        Vec3 aim = Arrays.stream(Direction.values()).map(side -> center.add(((double) side.getStepX()) * 0.4499999185117763d, ((double) side.getStepY()) * 0.4499999185117763d, ((double) side.getStepZ()) * 0.4499999185117763d)).filter(point -> eye.distanceTo(point) <= reach).filter(point -> {
                            BlockHitResult trace = aM_.level.clip(new ClipContext(eye, point, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, aM_.player));
                            return trace.getType() == HitResult.Type.BLOCK && trace.getBlockPos().equals(tnt);
                        }).min((a1, b1) -> Double.compare(eye.distanceToSqr(a1), eye.distanceToSqr(b1))).orElse(null);
                        if (aim != null) {
                            Delta.h().d().k().a(Rotation.a(eye, aim), 180.0f, 0, 1);
                            if (new Rotation(aM_.player).a(Rotation.b()) < 1.0d && aM_.player.tickCount % 5 == 0) {
                                aM_.player.getInventory().setSelectedSlot(flint);
                                if (aM_.hitResult instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK && hit.getBlockPos().equals(tnt)) {
                                    ((MinecraftInvoker) aM_).invokeDoItemUse();
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    Vec3 eye = aM_.player.getEyePosition();
                    BlockPos target = StreamSupport.stream(BlockPos.withinManhattan(aM_.player.blockPosition(), (int) reach, (int) reach, (int) reach).spliterator(), false).filter(pos -> aM_.level.getBlockState(pos).canBeReplaced()).filter(pos -> aM_.level.getBlockState(pos.below()).isSolidRender()).filter(pos -> !aM_.player.getBoundingBox().expandTowards(aM_.player.getDeltaMovement()).inflate(0.10000001882007822d).intersects(new AABB(pos))).filter(pos -> {
                        Vec3 hitVec = new Vec3(((double) pos.getX()) + 0.5d, pos.getY(), ((double) pos.getZ()) + 0.5d);
                        if (eye.distanceTo(hitVec) > reach || eye.subtract(hitVec).normalize().y <= 0.0d) {
                            return false;
                        }
                        BlockHitResult hit = aM_.level.clip(new ClipContext(eye, hitVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, aM_.player));
                        return hit.getType() != HitResult.Type.BLOCK || hit.getBlockPos().equals(pos.below());
                    }).map(BlockPos::immutable).min((t1, t2) -> Double.compare(eye.distanceToSqr(Vec3.atCenterOf(t1)), eye.distanceToSqr(Vec3.atCenterOf(t2)))).orElse(null);
                    this.f = target;
                    int slot = InventoryUtil.a(Items.TNT, true);
                    if (target != null) {
                        if (slot != -1) {
                            BlockPos support = target.below();
                            Delta.h().d().k().a(Rotation.a(eye, new Vec3(support.getX() + 0.5d, support.getY() + 1, support.getZ() + 0.5d)), 180.0f, 0, 1);
                            if (new Rotation(aM_.player).a(Rotation.b()) < 1.0d && aM_.player.tickCount % 5 == 0) {
                                if (aM_.player.getInventory().getSelectedSlot() != slot) {
                                    aM_.player.getInventory().setSelectedSlot(slot);
                                }
                                if (aM_.hitResult instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK && hit.getBlockPos().equals(support) && hit.getDirection() == Direction.UP) {
                                    ((MinecraftInvoker) aM_).invokeDoItemUse();
                                    break;
                                }
                            }
                        }
                    } else {
                        if (!baritone.getPathingBehavior().hasPath()) {
                            BlockPos feet = aM_.player.blockPosition();
                            BlockPos stand = StreamSupport.stream(BlockPos.withinManhattan(feet, 16, 8, 16).spliterator(), false).filter(pos -> !pos.equals(feet)).filter(pos -> aM_.level.getBlockState(pos.below()).isSolidRender()).filter(pos -> aM_.level.getBlockState(pos).canBeReplaced() && aM_.level.getBlockState(pos.above()).canBeReplaced()).filter(pos -> aM_.level.getBlockState(pos).getFluidState().isEmpty() && aM_.level.getBlockState(pos.above()).getFluidState().isEmpty()).map(BlockPos::immutable).min((p1, p2) -> Double.compare(feet.distSqr(p1), feet.distSqr(p2))).orElse(null);
                            if (stand != null) {
                                baritone.getCustomGoalProcess().setGoalAndPath(new GoalBlock(stand));
                            } else {
                                this.e = Phase.SEARCH;
                            }
                        }
                        break;
                    }
                }
                break;
            case RETREAT:
                List<PrimedTnt> burning = aM_.level.getEntitiesOfClass(PrimedTnt.class, aM_.player.getBoundingBox().inflate(32.0d), t -> true);
                if (burning.isEmpty()) {
                    ChatUtil.a("Ожидаем обломки, и начинаем вскапывать");
                    baritone.getPathingBehavior().cancelEverything();
                    this.g = null;
                    this.e = Phase.MINE;
                    this.d.b();
                } else if (!baritone.getPathingBehavior().hasPath()) {
                    baritone.getCustomGoalProcess().setGoalAndPath(new GoalRunAway(20.0d, burning.stream().map(PrimedTnt::blockPosition).toArray(BlockPos[]::new)));
                }
                break;
            case MINE:
                if (this.d.a(1000L)) {
                    if (!xray.s().isEmpty()) {
                        if (!baritone.getMineProcess().isActive()) {
                            baritone.getMineProcess().mine(Blocks.ANCIENT_DEBRIS);

                        }
                    } else if (!baritone.getMineProcess().isActive()) {
                        this.e = Phase.SEARCH;
                    }
                }
                break;
        }
    }

    @EventTarget
    public void a(DrawEvent draw) {
        if (draw.c() && this.f != null) {
            draw.e().a(draw.h(), new AABB(this.f), ColorUtil.a(230, 90, 70, InterfaceC0020Opcode.ap), 1.0f);
        }
    }

    private AABB q() {
        if (aM_.player == null || aM_.level == null) return null;
        BlockPos anchor;
        int reach = ((int) ((Math.sqrt(aM_.level.getChunkSource().getLoadedChunksCount()) - 1.0d) / 2.0d)) * 16;
        BlockPos feet = aM_.player.blockPosition();
        WorldBorder border = aM_.level.getWorldBorder();
        int bottom = this.b.l("Поиск сверху") ? 90 : 20;
        int top = this.b.l("Поиск сверху") ? InterfaceC0020Opcode.bN : 60;
        AABB best = null;
        double bestScore = -1.0d;
        double bestFill = 0.0d;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = feet.getX() - reach; x <= feet.getX() + reach; x += 8) {
            for (int z = feet.getZ() - reach; z <= feet.getZ() + reach; z += 8) {
                if (aM_.level.getChunkSource().hasChunk(x >> 4, z >> 4) && border.isWithinBounds((double) (x - 20), (double) (z - 20)) && border.isWithinBounds((double) (x + 20), (double) (z + 20))) {
                    for (int y = bottom; y <= top; y += 8) {
                        int solid = 0;
                        int total = 0;
                        boolean badBiome = false;
                        for (int dx = -20; dx <= 20 && !badBiome; dx += 4) {
                            for (int dy = -20; dy <= 20 && !badBiome; dy += 4) {
                                for (int dz = -20; dz <= 20; dz += 4) {
                                    pos.set(x + dx, y + dy, z + dz);
                                    if (aM_.level.getBiome(pos).is(Biomes.BASALT_DELTAS) || aM_.level.getBiome(pos).is(Biomes.WARPED_FOREST)) {
                                        badBiome = true;
                                        break;
                                    }
                                    total++;
                                    if (!aM_.level.getBlockState(pos).isAir() && aM_.level.getBlockState(pos).getFluidState().isEmpty()) {
                                        solid++;
                                    }
                                }
                            }
                        }
                        if (!badBiome && total > 0) {
                            double score = (((double) solid) / ((double) total)) - (feet.distSqr(new BlockPos(x, y, z)) * 9.99999555911002E-10d);
                            if (score > bestScore && (anchor = StreamSupport.stream(BlockPos.withinManhattan(new BlockPos(x, y, z), 20, 20, 20).spliterator(), false).filter(candidate -> !aM_.level.getBlockState(candidate).isAir() && aM_.level.getBlockState(candidate).getFluidState().isEmpty()).map(BlockPos::immutable).findFirst().orElse(null)) != null) {
                                bestScore = score;
                                bestFill = ((double) solid) / ((double) total);
                                best = new AABB(anchor.getX() - 20, anchor.getY() - 20, anchor.getZ() - 20, anchor.getX() + 20, anchor.getY() + 20, anchor.getZ() + 20);
                            }
                        }
                    }
                }
            }
        }
        if (best != null) {
            long jRound = Math.round(bestFill * 100.0d);
            Math.round(Math.sqrt(best.getCenter().distanceToSqr(feet.getX(), feet.getY(), feet.getZ())));
            aM_.execute(() -> ChatUtil.a("Успешность: &c" + jRound + "%&7, до неё &c" + jRound + "&7 блоков"));
        }
        return best;
    }

    public void d(boolean status) {
        BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
        BaritoneAPI.getSettings().blockFreeLook.value = true;
        BaritoneAPI.getSettings().allowBreak.value = true;
        BaritoneAPI.getSettings().randomLooking.value = Double.valueOf(1.0d);
        BaritoneAPI.getSettings().randomLooking113.value = Double.valueOf(1.0d);
        this.g = null;
        this.e = Phase.SEARCH;
    }

    enum a {
        FLY("необходимо включить режим полёта (/fly)", () -> aM_.player != null && aM_.player.getAbilities().mayfly),
        NETHER("необходимо находиться в Незере", () -> aM_.level != null && aM_.level.dimension() == Level.NETHER),
        FOOD("в хотбаре должна быть еда", stack -> stack.has(DataComponents.FOOD)),
        TNT("в хотбаре должен быть динамит", stack2 -> stack2.is(Items.TNT)),
        FLINT_AND_STEEL("в хотбаре должно быть огниво", stack3 -> stack3.is(Items.FLINT_AND_STEEL)),
        PICKAXE("в хотбаре должна быть кирка с прочностью больше 5%", stack4 -> stack4.is(ItemTags.PICKAXES) && ((double) (stack4.getMaxDamage() - stack4.getDamageValue())) > ((double) stack4.getMaxDamage()) * 0.05d),
        FIRE_RESISTANCE("в хотбаре должна быть огнестойкость", stack5 -> {
            PotionContents pc = stack5.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            return StreamSupport.stream(pc.getAllEffects().spliterator(), false).anyMatch(effect -> effect.getEffect().is(MobEffects.FIRE_RESISTANCE));
        });

        private final BooleanSupplier h;
        private final String i;

        @Generated
        public BooleanSupplier b() {
            return this.h;
        }

        @Generated
        public String c() {
            return this.i;
        }

        a(String description, BooleanSupplier condition) {
            this.i = description;
            this.h = condition;
        }

        a(String description, Predicate<ItemStack> predicate) {
            this(description, () -> {
                if (aM_.player == null || aM_.player.getInventory() == null) return false;
                return IntStream.range(0, 9).anyMatch(slot -> predicate.test(aM_.player.getInventory().getItem(slot)));
            });
        }

        public boolean a() {
            return this.h.getAsBoolean();
        }
    }
}
