package platform.api.event.events.client;

import platform.api.event.Event;
import platform.api.event.interfaces.IEvent;

import lombok.Generated;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class BlockChangeEvent extends Event implements IEvent {
    private final BlockPos a;
    private final BlockState b;
    private final BlockState c;

    @Generated
    public BlockChangeEvent(BlockPos pos, BlockState oldState, BlockState state) {
        this.a = pos;
        this.b = oldState;
        this.c = state;
    }

    @Generated
    public BlockPos b() {
        return this.a;
    }

    @Generated
    public BlockState c() {
        return this.b;
    }

    @Generated
    public BlockState d() {
        return this.c;
    }
}



