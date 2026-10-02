package platform.client.utils.render;

import net.minecraft.resources.Identifier;

public class FontBuilder {
    private String fontName;

    public FontBuilder setFont(String name) {
        this.fontName = name;
        return this;
    }

    public Font build() {
        Identifier id = Identifier.fromNamespaceAndPath("delta", "fonts/" + this.fontName + ".json");
        MsdfFont msdfFont = MsdfFont.load(id);
        return new Font(this.fontName, msdfFont);
    }
}



