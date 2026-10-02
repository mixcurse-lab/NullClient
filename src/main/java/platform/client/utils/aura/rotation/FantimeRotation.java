package platform.client.utils.aura.rotation;

import platform.client.utils.aura.AuraContext;


public class FantimeRotation extends FantimeStyleRotation {

    public FantimeRotation(AuraContext ctx) {
        super(ctx);
    }

    @Override
    public String name() {
        return "ФанТайм";
    }

    @Override
    protected boolean useMousePitch() {
        return false;
    }
}
