package platform.api.event.events.render;

import platform.api.event.Event;
import platform.api.event.interfaces.IEvent;

import lombok.Generated;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.model.HeadedModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

public class HeadFeatureEvent extends Event implements IEvent {
    private PoseStack a;
    private MultiBufferSource b;
    private Player c;
    private HeadedModel d;

    @Generated
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof HeadFeatureEvent)) {
            return false;
        }
        HeadFeatureEvent other = (HeadFeatureEvent) o;
        if (!other.a(this) || !super.equals(o)) {
            return false;
        }
        Object this$matrix = b();
        Object other$matrix = other.b();
        if (this$matrix == null) {
            if (other$matrix != null) {
                return false;
            }
        } else if (!this$matrix.equals(other$matrix)) {
            return false;
        }
        Object this$vertexConsumerProvider = c();
        Object other$vertexConsumerProvider = other.c();
        if (this$vertexConsumerProvider == null) {
            if (other$vertexConsumerProvider != null) {
                return false;
            }
        } else if (!this$vertexConsumerProvider.equals(other$vertexConsumerProvider)) {
            return false;
        }
        Object this$player = d();
        Object other$player = other.d();
        if (this$player == null) {
            if (other$player != null) {
                return false;
            }
        } else if (!this$player.equals(other$player)) {
            return false;
        }
        Object this$model = e();
        Object other$model = other.e();
        if (this$model == null) {
            return other$model == null;
        }
        return this$model.equals(other$model);
    }

    @Generated
    protected boolean a(Object other) {
        return other instanceof HeadFeatureEvent;
    }

    @Generated
    public int hashCode() {
        int result = super.hashCode();
        Object $matrix = b();
        int result2 = (result * 59) + ($matrix == null ? 43 : $matrix.hashCode());
        Object $vertexConsumerProvider = c();
        int result3 = (result2 * 59) + ($vertexConsumerProvider == null ? 43 : $vertexConsumerProvider.hashCode());
        Object $player = d();
        int result4 = (result3 * 59) + ($player == null ? 43 : $player.hashCode());
        Object $model = e();
        return (result4 * 59) + ($model == null ? 43 : $model.hashCode());
    }

    @Generated
    public HeadFeatureEvent(PoseStack matrix, MultiBufferSource vertexConsumerProvider, Player player, HeadedModel model) {
        this.a = matrix;
        this.b = vertexConsumerProvider;
        this.c = player;
        this.d = model;
    }

    @Generated
    public void a(PoseStack matrix) {
        this.a = matrix;
    }

    @Generated
    public void a(MultiBufferSource vertexConsumerProvider) {
        this.b = vertexConsumerProvider;
    }

    @Generated
    public void a(Player player) {
        this.c = player;
    }

    @Generated
    public void a(HeadedModel model) {
        this.d = model;
    }

    @Generated
    public String toString() {
        return "HeadFeatureEvent(matrix=" + String.valueOf(b()) + ", vertexConsumerProvider=" + String.valueOf(c()) + ", player=" + String.valueOf(d()) + ", model=" + String.valueOf(e()) + ")";
    }

    @Generated
    public PoseStack b() {
        return this.a;
    }

    @Generated
    public MultiBufferSource c() {
        return this.b;
    }

    @Generated
    public Player d() {
        return this.c;
    }

    @Generated
    public HeadedModel e() {
        return this.d;
    }
}



