package platform.client.features.commands;

import platform.client.utils.rotation.Rotation;
import platform.api.module.Interface;

import static platform.api.module.Interface.aM_;
import platform.client.utils.text.ChatUtil;

import platform.api.command.BaseCommand;
import platform.api.command.Command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

@Command(a = "hclip")
public class HClipCommand extends BaseCommand {
    @Override
    public void a(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.then(a("forward").executes(context -> {
            Vec3 dir = c().normalize();
            float distance = a(dir);
            if (distance != 0.0f) {
                a(dir, distance);
                ChatUtil.a((Object) "Вы были успешно перемещены вперёд по горизонтали взгляда");
                return 1;
            }
            return 1;
        })).then(a("back").executes(context2 -> {
            Vec3 dir = c().normalize().reverse();
            float distance = a(dir);
            if (distance != 0.0f) {
                a(dir, distance);
                ChatUtil.a((Object) "Вы были успешно перемещены назад по горизонтали взгляда");
                return 1;
            }
            return 1;
        })).then(f("число").executes(context3 -> {
            float distance = c(context3, "число");
            Vec3 forward = c().normalize();
            a(forward, distance);
            ChatUtil.a((Object) ("Вы успешно сдвинулись на " + distance + " блоков по горизонтали взгляда"));
            return 1;
        })).executes(context4 -> {
            ChatUtil.a((Object) "Использование: .hclip <число|forward|back>");
            return 1;
        });
    }

    private static Vec3 c() {
        return Vec3.directionFromRotation(Rotation.a().c(), 0.0f);
    }

    private void a(Vec3 horizontalDirection, float distance) {
        Vec3 delta = horizontalDirection.scale(distance);
        double x = aM_.player.getX() + delta.x;
        double y = aM_.player.getY();
        double z = aM_.player.getZ() + delta.z;
        aM_.player.connection.send(new ServerboundMovePlayerPacket.PosRot(x, y, z, aM_.player.getYRot(), aM_.player.getXRot(), aM_.player.onGround(), aM_.player.horizontalCollision));
        aM_.player.setPos(x, y, z);
    }

    private float a(Vec3 direction) {
        Vec3 unit = direction.normalize();
        Vec3 origin = aM_.player.position();
        for (int blocks = 1; blocks <= 255; blocks++) {
            BlockPos here = BlockPos.containing(origin.add(unit.scale(blocks)));
            BlockPos ahead = BlockPos.containing(origin.add(unit.scale(blocks + 1)));
            if (aM_.level.getBlockState(here).isAir() && aM_.level.getBlockState(ahead).isAir()) {
                return blocks + 1;
            }
            if (aM_.level.getBlockState(here).is(Blocks.BEDROCK)) {
                ChatUtil.a((Object) (String.valueOf(ChatFormatting.GRAY) + "Телепортация в данное место невозможно"));
                return 0.0f;
            }
        }
        return 0.0f;
    }
}



