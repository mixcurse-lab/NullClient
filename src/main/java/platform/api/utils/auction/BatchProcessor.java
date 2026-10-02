package platform.api.utils.auction;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.BlendFactor;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexConsumer;
import platform.api.system.interfaces.NativeMethodLookup;
import static platform.api.module.Interface.aM_;
import platform.api.module.Interface;
import platform.client.utils.render.ColorUtil;

import platform.api.system.configs.BaseProcessor;

import platform.api.annotation.Compile;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class BatchProcessor extends BaseProcessor {
    private static final RenderPipeline DELTA_COLOR_QUADS = RenderPipeline.builder()
            .withLocation(Identifier.parse("delta:pipeline/batch_quads"))
            .withVertexShader(Identifier.parse("delta:core/aura_filled"))
            .withFragmentShader(Identifier.parse("delta:core/aura_filled"))
            .withBindGroupLayout(BindGroupLayouts.PROJECTION)
            .withColorTargetState(new ColorTargetState(new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA)))
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .withCull(false)
            .build();

    private ProjectionMatrixBuffer screenProjectionBuffer;

    private final List<b> b = new ArrayList();
    private final List<a> c = new ArrayList();

    @Override
    @Compile
    public void setup() {
        this.b.clear();
        this.c.clear();
    }

    static {
        NativeMethodLookup.lookup(BatchProcessor.class, 27);
    }

    @Override
    public void unSetup() {
        this.b.clear();
        this.c.clear();
    }

    public void a(b task) {
        this.b.add(task);
    }

    public void a(a task) {
        this.c.add(task);
    }

    public void a() {
        if (this.c.isEmpty()) {
            return;
        }
        try {
            int totalQuads = 0;
            for (a task : this.c) {
                totalQuads += task.quadCount();
            }
            int vertexCount = totalQuads * 4;
            int indexCount = totalQuads * 6;
            int vertexSize = DefaultVertexFormat.POSITION_COLOR.getVertexSize();

            try (ByteBufferBuilder bbBuilder = new ByteBufferBuilder(vertexSize * vertexCount)) {
                BufferBuilder builder = new BufferBuilder(bbBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);
                for (a task : this.c) {
                    task.a(builder);
                }
                try (MeshData mesh = builder.buildOrThrow()) {
                    GpuBuffer gpuBuffer = RenderSystem.getDevice().createBuffer(() -> "Delta-2D-Batch", 32, mesh.vertexBuffer());

                    GameRenderer gr = aM_.gameRenderer;
                    var renderTarget = gr.mainRenderTarget();
                    var colorTexture = renderTarget.getColorTextureView();
                    var depthTexture = renderTarget.getDepthTextureView();

                    try (var renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                            () -> "Delta-2D", colorTexture, java.util.Optional.empty(), depthTexture, java.util.OptionalDouble.empty())) {
                        renderPass.setPipeline(DELTA_COLOR_QUADS);
                        Projection projection = new Projection();
                        projection.setupOrtho(-1000.0F, 1000.0F, aM_.getWindow().getGuiScaledWidth(), aM_.getWindow().getGuiScaledHeight(), true);
                        if (this.screenProjectionBuffer == null) {
                            this.screenProjectionBuffer = new ProjectionMatrixBuffer("delta-2d-screen");
                        }
                        renderPass.setUniform("Projection", this.screenProjectionBuffer.getBuffer(projection));
                        renderPass.setVertexBuffer(0, gpuBuffer.slice());
                        GpuBuffer indexBuffer = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS).getBuffer(indexCount);
                        renderPass.setIndexBuffer(indexBuffer, RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS).type());
                        renderPass.drawIndexed(indexCount, 1, 0, 0, 0);
                    }
                    gpuBuffer.close();
                }
            }
        } catch (Exception e) {
        }
        this.c.clear();
    }

    private static GpuBufferSlice deltaLevelProjection() {
        GpuBufferSlice slice = platform.client.utils.render.LevelProjection.get();
        return slice != null ? slice : RenderSystem.getProjectionMatrixBuffer();
    }

    public void b() {
        if (this.b.isEmpty()) {
            return;
        }
        try {
            Vec3 cam = aM_.gameRenderer.mainCamera().position();
            org.joml.Quaternionf viewRot = aM_.gameRenderer.mainCamera().rotation().invert(new org.joml.Quaternionf());
            Matrix4f offset = new Matrix4f().rotation(viewRot).translate((float) -cam.x, (float) -cam.y, (float) -cam.z);

            if (this.b.stream().anyMatch(t -> !t.h)) {
                int quadCount = 0;
                for (b task : this.b) {
                    if (!task.h) quadCount += task.quadCount();
                }
                if (quadCount > 0) {
                    int vertexCount = quadCount * 4;
                    int indexCount = quadCount * 6;
                    int vertexSize = DefaultVertexFormat.POSITION_COLOR.getVertexSize();

                    try (ByteBufferBuilder bbBuilder = new ByteBufferBuilder(vertexSize * vertexCount)) {
                        BufferBuilder builder = new BufferBuilder(bbBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);
                        for (b task : this.b) {
                            if (!task.h) {
                                task.a(builder, offset);
                            }
                        }
                        try (MeshData mesh = builder.buildOrThrow()) {
                            GpuBuffer gpuBuffer = RenderSystem.getDevice().createBuffer(() -> "Delta-3D-Quad-Batch", 32, mesh.vertexBuffer());

                            GameRenderer gr = aM_.gameRenderer;
                            var renderTarget = gr.mainRenderTarget();
                            var colorTexture = renderTarget.getColorTextureView();
                            var depthTexture = renderTarget.getDepthTextureView();

try (var renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                                () -> "Delta-3D-Quad", colorTexture, java.util.Optional.empty(), depthTexture, java.util.OptionalDouble.empty())) {
                            renderPass.setPipeline(DELTA_COLOR_QUADS);
                            renderPass.setUniform("Projection", deltaLevelProjection());
                            renderPass.setVertexBuffer(0, gpuBuffer.slice());
                                GpuBuffer indexBuffer = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS).getBuffer(indexCount);
                                renderPass.setIndexBuffer(indexBuffer, RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS).type());
                                renderPass.drawIndexed(indexCount, 1, 0, 0, 0);
                            }
                            gpuBuffer.close();
                        }
                    }
                }
            }

            Map<Float, List<b>> byWidth = new TreeMap<>(Comparator.naturalOrder());
            for (b task2 : this.b) {
                byWidth.computeIfAbsent(task2.d, k -> new ArrayList<>()).add(task2);
            }
            GL11.glEnable(2881);
            for (Map.Entry<Float, List<b>> entry : byWidth.entrySet()) {
                List<b> tasks = entry.getValue();
                int lineVertexCount = 0;
                for (b task3 : tasks) {
                    lineVertexCount += task3.lineVertexCount();
                }
                int vertexSize = DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH.getVertexSize();

                try (ByteBufferBuilder bbBuilder = new ByteBufferBuilder(vertexSize * lineVertexCount)) {
                    BufferBuilder builder = new BufferBuilder(bbBuilder, PrimitiveTopology.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH);
                    for (b task3 : tasks) {
                        if (task3.h) {
                            task3.c(builder, offset);
                        } else {
                            task3.b(builder, offset);
                        }
                    }
                    try (MeshData mesh = builder.buildOrThrow()) {
                        GpuBuffer gpuBuffer = RenderSystem.getDevice().createBuffer(() -> "Delta-3D-Line-Batch", 32, mesh.vertexBuffer());

                        GameRenderer gr = aM_.gameRenderer;
                        var renderTarget = gr.mainRenderTarget();
                        var colorTexture = renderTarget.getColorTextureView();
                        var depthTexture = renderTarget.getDepthTextureView();

                        try (var renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                                () -> "Delta-3D-Line", colorTexture, java.util.Optional.empty(), depthTexture, java.util.OptionalDouble.empty())) {
                        renderPass.setPipeline(RenderPipelines.LINES);
                        RenderSystem.bindDefaultUniforms(renderPass);
                        renderPass.setUniform("Projection", deltaLevelProjection());
                            renderPass.setVertexBuffer(0, gpuBuffer.slice());
                            GpuBuffer indexBuffer = RenderSystem.getSequentialBuffer(PrimitiveTopology.LINES).getBuffer(lineVertexCount);
                            renderPass.setIndexBuffer(indexBuffer, RenderSystem.getSequentialBuffer(PrimitiveTopology.LINES).type());
                            renderPass.drawIndexed(lineVertexCount, 1, 0, 0, 0);
                        }
                        gpuBuffer.close();
                    }
                }
            }
            GL11.glDisable(2881);
        } catch (Exception e) {
        }
        this.b.clear();
    }

    public static class b {
        final Object a;
        final AABB b;
        final int c;
        final float d;
        final Vec3 e;
        final Vec3 f;
        final Vec3 g;
        final boolean h;

        public b(Object entry, AABB box, int color, float width) {
            this.a = entry;
            this.b = box;
            this.c = color;
            this.d = width;
            this.e = null;
            this.f = null;
            this.g = null;
            this.h = false;
        }

        public int quadCount() {
            return this.h ? 0 : 6;
        }

        public int lineVertexCount() {
            if (!this.h) {
                return 24;
            }
            return this.g == null ? 2 : 64;
        }

        public b(Object entry, Vec3 start, Vec3 end, Vec3 control, int color, float width) {
            this.a = entry;
            this.b = null;
            this.c = color;
            this.d = width;
            this.e = start;
            this.f = end;
            this.g = control;
            this.h = true;
        }

        public static b a(com.mojang.blaze3d.vertex.PoseStack matrices, AABB box, int color, float width) {
            return new b(matrices, box.inflate(9.999995420800828E-4d), color, width);
        }

        public static b a(com.mojang.blaze3d.vertex.PoseStack matrices, Vec3 start, Vec3 end, Vec3 control, int color, float width) {
            return new b(matrices, start, end, control, color, width);
        }

        void a(VertexConsumer buffer, Matrix4f offset) {
            float[] rgba = ColorUtil.a(ColorUtil.a(this.c, (((this.c >> 24) & 255) / 255.0f) * 0.12f));
            double[][][] faces = {
                {new double[]{this.b.minX, this.b.minY, this.b.minZ}, new double[]{this.b.maxX, this.b.minY, this.b.minZ}, new double[]{this.b.maxX, this.b.minY, this.b.maxZ}, new double[]{this.b.minX, this.b.minY, this.b.maxZ}},
                {new double[]{this.b.minX, this.b.maxY, this.b.minZ}, new double[]{this.b.maxX, this.b.maxY, this.b.minZ}, new double[]{this.b.maxX, this.b.maxY, this.b.maxZ}, new double[]{this.b.minX, this.b.maxY, this.b.maxZ}},
                {new double[]{this.b.minX, this.b.minY, this.b.minZ}, new double[]{this.b.minX, this.b.maxY, this.b.minZ}, new double[]{this.b.maxX, this.b.maxY, this.b.minZ}, new double[]{this.b.maxX, this.b.minY, this.b.minZ}},
                {new double[]{this.b.maxX, this.b.minY, this.b.minZ}, new double[]{this.b.maxX, this.b.maxY, this.b.minZ}, new double[]{this.b.maxX, this.b.maxY, this.b.maxZ}, new double[]{this.b.maxX, this.b.minY, this.b.maxZ}},
                {new double[]{this.b.maxX, this.b.minY, this.b.maxZ}, new double[]{this.b.maxX, this.b.maxY, this.b.maxZ}, new double[]{this.b.minX, this.b.maxY, this.b.maxZ}, new double[]{this.b.minX, this.b.minY, this.b.maxZ}},
                {new double[]{this.b.minX, this.b.minY, this.b.maxZ}, new double[]{this.b.minX, this.b.maxY, this.b.maxZ}, new double[]{this.b.minX, this.b.maxY, this.b.minZ}, new double[]{this.b.minX, this.b.minY, this.b.minZ}}
            };
            for (double[][] face : faces) {
                a(buffer, offset, face[0][0], face[0][1], face[0][2], face[1][0], face[1][1], face[1][2], face[2][0], face[2][1], face[2][2], face[3][0], face[3][1], face[3][2], rgba[0], rgba[1], rgba[2], rgba[3]);
            }
        }

        void b(VertexConsumer buffer, Matrix4f offset) {
            float[] rgba = ColorUtil.a(ColorUtil.a(this.c, ((this.c >> 24) & 255) / 255.0f));
            double[][] edges = {
                {this.b.minX, this.b.minY, this.b.minZ, this.b.maxX, this.b.minY, this.b.minZ},
                {this.b.maxX, this.b.minY, this.b.minZ, this.b.maxX, this.b.minY, this.b.maxZ},
                {this.b.maxX, this.b.minY, this.b.maxZ, this.b.minX, this.b.minY, this.b.maxZ},
                {this.b.minX, this.b.minY, this.b.maxZ, this.b.minX, this.b.minY, this.b.minZ},
                {this.b.minX, this.b.maxY, this.b.minZ, this.b.maxX, this.b.maxY, this.b.minZ},
                {this.b.maxX, this.b.maxY, this.b.minZ, this.b.maxX, this.b.maxY, this.b.maxZ},
                {this.b.maxX, this.b.maxY, this.b.maxZ, this.b.minX, this.b.maxY, this.b.maxZ},
                {this.b.minX, this.b.maxY, this.b.maxZ, this.b.minX, this.b.maxY, this.b.minZ},
                {this.b.minX, this.b.minY, this.b.minZ, this.b.minX, this.b.maxY, this.b.minZ},
                {this.b.maxX, this.b.minY, this.b.minZ, this.b.maxX, this.b.maxY, this.b.minZ},
                {this.b.maxX, this.b.minY, this.b.maxZ, this.b.maxX, this.b.maxY, this.b.maxZ},
                {this.b.minX, this.b.minY, this.b.maxZ, this.b.minX, this.b.maxY, this.b.maxZ}
            };
            for (double[] edge : edges) {
                a(offset, buffer, edge[0], edge[1], edge[2], edge[3], edge[4], edge[5], rgba);
            }
        }

        private void a(VertexConsumer buffer, Matrix4f offset, double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4, float r, float g, float b, float a) {
            buffer.addVertex(offset, (float) x1, (float) y1, (float) z1).setColor(r, g, b, a);
            buffer.addVertex(offset, (float) x2, (float) y2, (float) z2).setColor(r, g, b, a);
            buffer.addVertex(offset, (float) x3, (float) y3, (float) z3).setColor(r, g, b, a);
            buffer.addVertex(offset, (float) x4, (float) y4, (float) z4).setColor(r, g, b, a);
        }

        private void a(Matrix4f offset, VertexConsumer buffer, double x1, double y1, double z1, double x2, double y2, double z2, float[] rgba) {
            float lenSq = (float) (((x2 - x1) * (x2 - x1)) + ((y2 - y1) * (y2 - y1)) + ((z2 - z1) * (z2 - z1)));
            float len = Mth.sqrt(lenSq);
            float nx = len > 1.0E-6f ? ((float) (x2 - x1)) / len : 0.0f;
            float ny = len > 1.0E-6f ? ((float) (y2 - y1)) / len : 0.0f;
            float nz = len > 1.0E-6f ? ((float) (z2 - z1)) / len : 0.0f;
            buffer.addVertex(offset, (float) x1, (float) y1, (float) z1).setColor(rgba[0], rgba[1], rgba[2], rgba[3]).setNormal(nx, ny, nz).setLineWidth(this.d);
            buffer.addVertex(offset, (float) x2, (float) y2, (float) z2).setColor(rgba[0], rgba[1], rgba[2], rgba[3]).setNormal(nx, ny, nz).setLineWidth(this.d);
        }

        void c(VertexConsumer buffer, Matrix4f offset) {
            float[] rgba = ColorUtil.a(ColorUtil.a(this.c, ((this.c >> 24) & 255) / 255.0f));
            if (this.g == null) {
                a(offset, buffer, this.e.x, this.e.y, this.e.z, this.f.x, this.f.y, this.f.z, rgba);
                return;
            }
            Vec3 prev = this.e;
            for (int i = 1; i <= 32; i++) {
                float t = i / 32.0f;
                float oneMinusT = 1.0f - t;
                Vec3 point = new Vec3(
                    ((oneMinusT * oneMinusT) * this.e.x) + ((2.0f * oneMinusT * t) * this.g.x) + ((t * t) * this.f.x),
                    ((oneMinusT * oneMinusT) * this.e.y) + ((2.0f * oneMinusT * t) * this.g.y) + ((t * t) * this.f.y),
                    ((oneMinusT * oneMinusT) * this.e.z) + ((2.0f * oneMinusT * t) * this.g.z) + ((t * t) * this.f.z)
                );
                a(offset, buffer, prev.x, prev.y, prev.z, point.x, point.y, point.z, rgba);
                prev = point;
            }
        }
    }

    public static class a {
        private static final int k = ColorUtil.a(0, 0, 0, 255);
        private static final float l = 0.5f;
        final Matrix4f matrix;
        final float b;
        final float c;
        final float d;
        final float e;
        final int f;
        final boolean g;
        final boolean h;
        final float i;
        final int j;

        public a(Matrix4f matrix, float minX, float minY, float maxX, float maxY, int color, boolean corners, boolean healthBar, float healthPercent, int healthColor) {
            this.matrix = matrix;
            this.b = minX;
            this.c = minY;
            this.d = maxX;
            this.e = maxY;
            this.f = color;
            this.g = corners;
            this.h = healthBar;
            this.i = healthPercent;
            this.j = healthColor;
        }

        public int quadCount() {
            return this.h ? 18 : 16;
        }

        void a(VertexConsumer buffer) {
            float fMin;
            if (this.g) {
                fMin = Math.min(this.d - this.b, this.e - this.c) * 0.25f;
            } else {
                fMin = Math.min(this.d - this.b, this.e - this.c) * l;
            }
            float length = fMin;
            int pass = 0;
            while (pass < 2) {
                for (int corner = 0; corner < 4; corner++) {
                    float cornerX = (corner & 1) == 0 ? this.b : this.d;
                    float cornerY = (corner & 2) == 0 ? this.c : this.e;
                    float directionX = (corner & 1) == 0 ? 1.0f : -1.0f;
                    float directionY = (corner & 2) == 0 ? 1.0f : -1.0f;
                    a(buffer, cornerX, cornerY, directionX * length, 0.0f, pass == 0);
                    a(buffer, cornerX, cornerY, 0.0f, directionY * length, pass == 0);
                }
                pass++;
            }
            if (this.h) {
                float height = this.e - this.c;
                float x = (this.b - 2.0f) - l;
                a(buffer, x - l, this.c - l, 1.5f, height + 1.5f, k);
                a(buffer, x, this.c + (height * (1.0f - this.i)), l, (height * this.i) + l, this.j);
            }
        }

        private void a(VertexConsumer buffer, float x, float y, float lengthX, float lengthY, boolean outline) {
            float left = Math.min(x, x + lengthX) - (lengthX == 0.0f ? 0.25f : 0.0f);
            float top = Math.min(y, y + lengthY) - (lengthY == 0.0f ? 0.25f : 0.0f);
            float width = lengthX == 0.0f ? l : Math.abs(lengthX);
            float height = lengthY == 0.0f ? l : Math.abs(lengthY);
            if (outline) {
                a(buffer, left - l, top - l, width + 1.0f, height + 1.0f, k);
            } else {
                a(buffer, left, top, width, height, this.f);
            }
        }

        private void a(VertexConsumer buffer, float x, float y, float width, float height, int color) {
            buffer.addVertex(this.matrix, x, y, 0.0f).setColor(color);
            buffer.addVertex(this.matrix, x, y + height, 0.0f).setColor(color);
            buffer.addVertex(this.matrix, x + width, y + height, 0.0f).setColor(color);
            buffer.addVertex(this.matrix, x + width, y, 0.0f).setColor(color);
        }
    }
}




