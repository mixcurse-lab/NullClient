package platform.inject.mixin;

import platform.client.Delta;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.User;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.UUID;

@Mixin(User.class)
public class UserMixin {
    @ModifyReturnValue(method = "getName", at = @At("RETURN"))
    private String delta$getName(String original) {
        if (Delta.h() != null && Delta.h().d().h().a() != null) {
            String name = Delta.h().d().h().a().b();
            if (name != null && !name.isEmpty()) {
                return name;
            }
        }
        return original;
    }

    @ModifyReturnValue(method = "getProfileId", at = @At("RETURN"))
    private UUID delta$getProfileId(UUID original) {
        if (Delta.h() != null && Delta.h().d().h().a() != null) {
            UUID uuid = Delta.h().d().h().a().e();
            if (uuid != null) {
                return uuid;
            }
        }
        return original;
    }
}
