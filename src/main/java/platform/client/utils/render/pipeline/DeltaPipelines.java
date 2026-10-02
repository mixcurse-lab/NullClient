package platform.client.utils.render.pipeline;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import java.lang.reflect.Field;
import java.util.Optional;

public final class DeltaPipelines {
    private static final BindGroupLayout BLUR_LAYOUT = BindGroupLayout.builder()
            .withSampler("CurrentInput")
            .withUniform("DeltaKawaseUniforms", UniformType.UNIFORM_BUFFER)
            .build();

    private static final BindGroupLayout NOISE_LAYOUT = BindGroupLayout.builder()
            .withSampler("Sampler0")
            .withSampler("Sampler1")
            .withUniform("DeltaNoiseUniforms", UniformType.UNIFORM_BUFFER)
            .build();

    public static final RenderPipeline NOISE_HANDS = RenderPipeline.builder()
            .withLocation(Identifier.parse("delta:gui/noise_hands"))
            .withVertexShader(Identifier.parse("delta:core/noise/noise_shader"))
            .withFragmentShader(Identifier.parse("delta:core/noise/noise_shader"))
            .withBindGroupLayout(NOISE_LAYOUT)
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .withCull(false)
            .build();

    public static final RenderPipeline FROSTED_SHADOW = frostedBuilder("frosted_shadow").build();
    public static final RenderPipeline RECT = builder("rect", false).build();
    public static final RenderPipeline FROSTED = frostedBuilder("frosted").build();
    public static final RenderPipeline TEXT = builder("text", true).build();
    public static final RenderPipeline TEXTURE = builder("texture", true).build();
    public static final RenderPipeline BLUR_DOWNSCALE = blurBuilder("blur_downscale", "downscale").build();
    public static final RenderPipeline BLUR_UPSCALE = blurBuilder("blur_upscale", "upscale").build();


    private static final BindGroupLayout ATMO_LAYOUT = BindGroupLayout.builder()
            .withSampler("ScreenTexture")
            .withSampler("DepthTexture")
            .withUniform("AtmoUniforms", UniformType.UNIFORM_BUFFER)
            .build();

    public static final RenderPipeline ATMO_DAWN_FOG = RenderPipeline.builder()
            .withLocation(Identifier.parse("delta:pipeline/atmo_dawn_fog"))
            .withVertexShader(Identifier.parse("delta:core/atmo/atmo_dawn_fog"))
            .withFragmentShader(Identifier.parse("delta:core/atmo/atmo_dawn_fog"))
            .withBindGroupLayout(ATMO_LAYOUT)
            .withColorTargetState(ColorTargetState.DEFAULT)
            .withDepthStencilState(Optional.empty())
            .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX)
            .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
            .withCull(false)
            .build();


    private static final BindGroupLayout SKY_LAYOUT = BindGroupLayout.builder()
            .withSampler("ScreenTexture")
            .withSampler("DepthTexture")
            .withUniform("SkyUniforms", UniformType.UNIFORM_BUFFER)
            .build();

    public static final RenderPipeline SKY_CAUSTICS = skyBuilder("sky_caustics", "caustics");
    public static final RenderPipeline SKY_CLOUDS = skyBuilder("sky_clouds", "clouds");
    public static final RenderPipeline SKY_DARKNESS = skyBuilder("sky_darkness", "darkness");
    public static final RenderPipeline SKY_MATRIX = skyBuilder("sky_matrix", "matrix");
    public static final RenderPipeline SKY_STARFIELD = skyBuilder("sky_starfield", "starfield");

    private static RenderPipeline skyBuilder(String pipelineName, String fragment) {
        return RenderPipeline.builder()
                .withLocation(Identifier.parse("delta:pipeline/" + pipelineName))
                .withVertexShader(Identifier.parse("delta:core/sky/sky"))
                .withFragmentShader(Identifier.parse("delta:core/sky/" + fragment))
                .withBindGroupLayout(SKY_LAYOUT)


                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))

                .withDepthStencilState(new DepthStencilState(CompareOp.EQUAL, false))
                .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX)
                .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
                .withCull(false)
                .build();
    }

    private static RenderPipeline.Snippet getGuiSnippet() {
        try {
            Field f = RenderPipelines.class.getDeclaredField("GUI_SNIPPET");
            f.setAccessible(true);
            return (RenderPipeline.Snippet) f.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static RenderPipeline.Builder builder(String name, boolean sampled) {
        RenderPipeline.Builder builder = RenderPipeline.builder(getGuiSnippet())
                .withLocation(Identifier.parse("delta:gui/" + name))
                .withVertexShader(Identifier.parse("delta:core/" + name))
                .withFragmentShader(Identifier.parse("delta:core/" + name))
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
                .withVertexBinding(0, DeltaVertexFormats.UI)
                .withPrimitiveTopology(PrimitiveTopology.QUADS)
                .withCull(false);
        if (sampled) {
            builder.withBindGroupLayout(BindGroupLayouts.SAMPLER0);
        }
        return builder;
    }

    private static RenderPipeline.Builder frostedBuilder(String name) {
        return builder(name, true)
                .withVertexShader(Identifier.parse("delta:core/rect/blurred_rect"))
                .withFragmentShader(Identifier.parse("delta:core/rect/blurred_rect"));
    }

    private static RenderPipeline.Builder blurBuilder(String name, String fragment) {
        return RenderPipeline.builder()
                .withLocation(Identifier.parse("delta:gui/" + name))
                .withVertexShader(Identifier.parse("delta:core/blur/kawase"))
                .withFragmentShader(Identifier.parse("delta:core/blur/" + fragment))
                .withBindGroupLayout(BLUR_LAYOUT)
                .withColorTargetState(ColorTargetState.DEFAULT)
                .withDepthStencilState(Optional.empty())
                .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX)
                .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
                .withCull(false);
    }

    private DeltaPipelines() {}
}
