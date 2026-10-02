package platform.client.utils.aura.rotation;

import platform.client.utils.aura.AuraContext;


public class FantimeFovRotation extends FantimeStyleRotation {

    public FantimeFovRotation(AuraContext ctx) {
        super(ctx);
    }

    @Override
    public String name() {
        return "ФанТайм ФОВ";
    }

    @Override
    protected boolean useMousePitch() {
        return true;
    }
}
