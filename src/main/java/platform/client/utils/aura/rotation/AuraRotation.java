package platform.client.utils.aura.rotation;

import platform.client.utils.aura.AuraContext;


public abstract class AuraRotation {
    protected final AuraContext ctx;

    protected AuraRotation(AuraContext ctx) {
        this.ctx = ctx;
    }


    public abstract String name();


    public abstract void rotate();
}
