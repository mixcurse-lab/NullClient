package platform.client.ui.element;

import platform.api.system.interfaces.NativeMethodLookup;
import platform.api.module.setting.StringSetting;
import platform.client.ui.element.TextField;
import platform.api.annotation.Compile;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Vector2f;

public class StringElement extends Element_2<StringSetting> {
    private TextField d;

    @Override
    @Compile
    public boolean a(double mouseX, double mouseY, int button) {
        TextField textFieldG = g();
        if (textFieldG != null) {
            textFieldG.a(mouseX, mouseY, button);
            return g().j();
        }
        return false;
    }

    @Override
    @Compile
    public boolean a(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.d == null) return false;
        this.d.b(mouseX, mouseY, button);
        return this.d.j();
    }

    @Override
    @Compile
    public boolean a(int keyCode, int scanCode, int modifiers) {
        if (this.d == null || !this.d.j()) return false;
        this.d.a(keyCode, scanCode, modifiers);
        return true;
    }

    @Override
    @Compile
    public boolean a(char chr, int modifiers) {
        if (this.d == null || !this.d.j()) return false;
        this.d.a(chr, modifiers);
        return true;
    }

    static {
        NativeMethodLookup.lookup(StringElement.class, 14);
    }

    public StringElement(StringSetting setting) {
        super(setting);
        this.a.w = 12.0f;
    }

    private TextField g() {
        if (this.d == null) {
            this.d = new TextField(TextField.a.GUI_SETTING, ((StringSetting) this.b).k());
            this.d.a(((StringSetting) this.b).i());
            this.d.g().append(((StringSetting) this.b).c());
        }
        return this.d;
    }

    @Override
    public void a(GuiGraphicsExtractor context, double mouseX, double mouseY, float delta, float extend) {
        TextField field = g();
        field.b(new Vector2f(this.a.z, this.a.w));
        field.a(new Vector2f(this.a.x, this.a.y));
        field.a(context, mouseX, mouseY, delta, extend);
        ((StringSetting) this.b).a(field.g().toString());
    }
}



