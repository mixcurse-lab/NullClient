package platform.client.features.commands;

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
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

@Command(a = "vclip")
public class VClipCommand extends BaseCommand {
    @Override
    public void a(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.then(a("up").executes(context -> {
            float offset = a(true);
            if (offset != 0.0f) {
                a(offset);
                ChatUtil.a((Object) "Вы были успешно подняты по Y");
                return 1;
            }
            return 1;
        })).then(a("down").executes(context2 -> {
            float offset = a(false);
            if (offset != 0.0f) {
                a(offset);
                ChatUtil.a((Object) "Вы были успешно опущены по Y");
                return 1;
            }
            return 1;
        })).then(f("число").executes(context3 -> {
            float offset = c(context3, "число");
            a(offset);
            ChatUtil.a((Object) ("Вы были успешно перемещены на " + offset + " по Y"));
            return 1;
        })).executes(context4 -> {
            ChatUtil.a((Object) "Использование: .vclip <число|up|down>");
            return 1;
        });
    }

    private void a(float yOffset) {
        double x = aM_.player.getX();
        double y = aM_.player.getY() + ((double) yOffset);
        double z = aM_.player.getZ();
        aM_.player.connection.send(new ServerboundMovePlayerPacket.PosRot(x, y, z, aM_.player.getYRot(), aM_.player.getXRot(), aM_.player.onGround(), aM_.player.horizontalCollision));
        aM_.player.setPos(x, y, z);
    }

    private float a(boolean up) {
        BlockPos playerPos = aM_.player.blockPosition();
        int startY = up ? 25 : -1;
        int endY = up ? 255 : -255;
        int step = up ? 1 : -1;
        int i = startY;
        while (true) {
            int offset = i;
            if (offset != endY) {
                BlockPos targetPos = playerPos.offset(0, offset, 0);
                BlockPos nextPos = playerPos.offset(0, offset + step, 0);
                if (aM_.level.getBlockState(targetPos).isAir() && aM_.level.getBlockState(nextPos).isAir()) {
                    return offset + (up ? 1.0f : -1.0f);
                }
                if (up || !aM_.level.getBlockState(targetPos).is(Blocks.BEDROCK)) {
                    i = offset + step;
                } else {
                    ChatUtil.a((Object) (String.valueOf(ChatFormatting.GRAY) + "Телепортация в данное место невозможно"));
                    return 0.0f;
                }
            } else {
                return 0.0f;
            }
        }
    }
}



