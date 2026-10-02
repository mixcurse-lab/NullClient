package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.resources.Identifier;

public class ShaderKey {
    private final Identifier id;
    private final VertexFormat vertexFormat;

    public ShaderKey(Identifier id, VertexFormat vertexFormat, ShaderDefines defines) {
        this.id = id;
        this.vertexFormat = vertexFormat;
    }

    public Identifier getId() {
        return this.id;
    }

    public VertexFormat getVertexFormat() {
        return this.vertexFormat;
    }
}

