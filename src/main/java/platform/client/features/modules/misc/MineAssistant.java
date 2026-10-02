package platform.client.features.modules.misc;

import java.util.Arrays;
import static platform.api.module.Interface.aM_;
import platform.api.module.InterfaceC0020Opcode;
import platform.api.module.Module;
import platform.client.utils.render.ColorUtil;
import platform.client.utils.player.ServerUtil;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Interface;
import platform.api.module.ModuleRegister;
import platform.api.event.events.render.DrawEvent;
import platform.api.event.events.client.TickEvent;
import platform.api.module.setting.BooleanSetting;

import platform.api.module.setting.MultiModeSetting;
import java.awt.Color;
import java.util.List;
import lombok.Generated;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

@ModuleRegister(a = "Mine Assistant", b = "Помощник, упрощающий добычу ресурсов в шахте под FunTime/SpookyTime", c = Category.Misc)
public class MineAssistant extends Module implements Interface {
    private final MultiModeSetting b = new MultiModeSetting("Выберите подсвечиваемые руды", new BooleanSetting("Алмазная", true), new BooleanSetting("Редстоуновая", false), new BooleanSetting("Железная", false), new BooleanSetting("Лазуритовая", false), new BooleanSetting("Золотая", true), new BooleanSetting("Древние", true), new BooleanSetting("Угольная", false));
    private final List<a> c = java.util.Arrays.asList(new a[]{new a(Blocks.DIAMOND_ORE, Color.CYAN.getRGB(), "Алмазная"), new a(Blocks.DEEPSLATE_DIAMOND_ORE, Color.CYAN.getRGB(), "Алмазная"), new a(Blocks.REDSTONE_ORE, Color.RED.getRGB(), "Редстоуновая"), new a(Blocks.DEEPSLATE_REDSTONE_ORE, Color.RED.getRGB(), "Редстоуновая"), new a(Blocks.IRON_ORE, Color.LIGHT_GRAY.getRGB(), "Железная"), new a(Blocks.DEEPSLATE_IRON_ORE, Color.LIGHT_GRAY.getRGB(), "Железная"), new a(Blocks.LAPIS_ORE, Color.BLUE.getRGB(), "Лазуритовая"), new a(Blocks.DEEPSLATE_LAPIS_ORE, Color.BLUE.getRGB(), "Лазуритовая"), new a(Blocks.GOLD_ORE, Color.YELLOW.getRGB(), "Золотая"), new a(Blocks.DEEPSLATE_GOLD_ORE, Color.YELLOW.getRGB(), "Золотая"), new a(Blocks.ANCIENT_DEBRIS, new Color(InterfaceC0020Opcode.aJ, 51, 0).getRGB(), "Древние"), new a(Blocks.COAL_ORE, Color.DARK_GRAY.getRGB(), "Угольная"), new a(Blocks.DEEPSLATE_COAL_ORE, Color.DARK_GRAY.getRGB(), "Угольная"), new a(Blocks.AIR, -1, null), new a(Blocks.STONE, -1, null), new a(Blocks.GRANITE, -1, null), new a(Blocks.COBBLESTONE, -1, null)});
    private AABB d;

    @Generated
    public AABB r() {
        return this.d;
    }

    public MineAssistant() {
        a(this.b);
    }

    @EventTarget
    public void a(TickEvent event) {
        if (ServerUtil.a.a() || ServerUtil.d.a()) {
            q();
        }
    }

    public void q() {
        for (net.minecraft.world.entity.Entity e2 : aM_.level.getEntities(aM_.player, aM_.player.getBoundingBox().inflate(256.0), e -> e instanceof ArmorStand)) {
            ArmorStand stand = (ArmorStand) e2;
            if (stand.getName().getString().contains("Авто-Шахта")) {
                if (this.d == null || this.d.getSize() <= 15.0d) {
                    int scanY = ((int) Math.floor(stand.getY())) - 2;
                    int startX = (int) Math.floor(stand.getX());
                    int startZ = (int) Math.floor(stand.getZ());
                    int minX = startX;
                    int maxX = startX;
                    int minZ = startZ;
                    int maxZ = startZ;
                        while (a(aM_.level.getBlockState(new BlockPos(minX - 2, scanY, startZ)).getBlock()) != null) {
                            minX--;
                        }
                        while (a(aM_.level.getBlockState(new BlockPos(maxX + 2, scanY, startZ)).getBlock()) != null) {
                            maxX++;
                        }
                        while (a(aM_.level.getBlockState(new BlockPos(startX, scanY, minZ - 1)).getBlock()) != null) {
                            minZ--;
                        }
                        while (a(aM_.level.getBlockState(new BlockPos(startX, scanY, maxZ + 1)).getBlock()) != null) {
                            maxZ++;
                        }
                        this.d = new AABB(minX, scanY + 1, minZ, maxX + 1, scanY - 8, maxZ + 1);
                        return;
                    }
                    return;
                }
        }
        this.d = null;
    }

    @EventTarget
    public void a(DrawEvent event) {
        if (event.c() && this.d != null) {
            for (int x = (int) this.d.minX; x <= ((int) this.d.maxX); x++) {
                for (int y = (int) this.d.minY; y <= ((int) this.d.maxY); y++) {
                    for (int z = (int) this.d.minZ; z <= ((int) this.d.maxZ); z++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        a info = a(aM_.level.getBlockState(pos).getBlock());
                        if (info != null && info.b() != -1 && this.b.a(info.c()).c().booleanValue()) {
                            event.e().a(event.h(), new AABB(pos), ColorUtil.a(info.b(), InterfaceC0020Opcode.ap), 1.0f);
                        }
                    }
                }
            }
        }
    }

    public a a(Block block) {
        for (a info : this.c) {
            if (info.a == block) {
                return info;
            }
        }
        return null;
    }

    public static class a {
        final Block a;
        final int b;
        final String c;

        @Generated
        public Block a() {
            return this.a;
        }

        @Generated
        public int b() {
            return this.b;
        }

        @Generated
        public String c() {
            return this.c;
        }

        a(Block block, int color, String name) {
            this.a = block;
            this.b = color;
            this.c = name;
        }
    }
}
