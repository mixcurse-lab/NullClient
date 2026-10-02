package platform.api.handlers;

import platform.api.system.interfaces.NativeMethodLookup;
import platform.client.utils.lib.log4j.LoggerFactory;

import platform.api.system.configs.BaseProcessor;
import platform.api.handlers.InventoryHandler;
import platform.api.handlers.InteractHandler;
import platform.api.handlers.PvEHandler;
import platform.api.handlers.StopHandler;
import platform.api.handlers.UseableHandler;
import platform.client.features.modules.combat.AimHandler;
import platform.client.features.modules.combat.AuraHandler;
import platform.client.features.modules.misc.AFKHandler;

import platform.client.utils.lib.log4j.Logger_2;
import platform.api.annotation.Compile;
import lombok.Generated;

public class HandlerProcessor extends BaseProcessor {

    @Generated
    private static final Logger_2 b;
    private final InventoryHandler c = new InventoryHandler();
    private final UseableHandler d = new UseableHandler();
    private final StopHandler e = new StopHandler();
    private final AuraHandler f = new AuraHandler();
    private final AimHandler g = new AimHandler();
    private final AFKHandler i = new AFKHandler();
    private final PvEHandler k = new PvEHandler();
    private final InteractHandler m = new InteractHandler();

    @Override
    @Compile
    public void setup() {
    }

    static {
        NativeMethodLookup.lookup(HandlerProcessor.class, 31);
        b = LoggerFactory.a((Class<?>) HandlerProcessor.class);
    }

    @Generated
    public InventoryHandler a() {
        return this.c;
    }

    @Generated
    public UseableHandler b() {
        return this.d;
    }

    @Generated
    public StopHandler c() {
        return this.e;
    }

    @Generated
    public AuraHandler d() {
        return this.f;
    }

    @Generated
    public AimHandler e() {
        return this.g;
    }

    @Generated
    public AFKHandler g() {
        return this.i;
    }

    @Generated
    public PvEHandler i() {
        return this.k;
    }

    @Generated
    public InteractHandler k() {
        return this.m;
    }

    @Override
    public void unSetup() {
    }
}


