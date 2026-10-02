package platform.inject.mixin;

import lombok.Generated;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import platform.interfaces.SeeInvisiblesMarker;

@Mixin({EntityRenderState.class})
public abstract class EntityRenderStateMixin implements SeeInvisiblesMarker {
    @Unique
    private boolean delta$seeInvisRevealed;

    @Override
    @Generated
    public boolean delta$isSeeInvisRevealed() {
        return this.delta$seeInvisRevealed;
    }

    @Override
    public void delta$setSeeInvisRevealed(boolean revealed) {
        this.delta$seeInvisRevealed = revealed;
    }
}
