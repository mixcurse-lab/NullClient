package platform.client.utils.render;

import platform.client.utils.render.Font;
import lombok.Generated;

public class Fonts {
    public static final Font a = new FontBuilder().setFont("icons").build();
    public static final Font b = new FontBuilder().setFont("sf_regular").build();
    public static final Font c = new FontBuilder().setFont("onest_regular").build();
    public static final Font d = new FontBuilder().setFont("sf_medium").build();
    public static final Font e = new FontBuilder().setFont("gt_regular").build();

    public static void preload() {
        try {
            a.msdfFont().textureSetup();
            b.msdfFont().textureSetup();
            c.msdfFont().textureSetup();
            d.msdfFont().textureSetup();
            e.msdfFont().textureSetup();
        } catch (Exception ignored) {
        }
    }

    @Generated
    private Fonts() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}



