package platform;


import platform.client.Delta;
import platform.api.system.interfaces.NativeMethodLookup;
import platform.api.annotation.Compile;
import net.fabricmc.api.ClientModInitializer;

public class Initializer implements ClientModInitializer {
    @Compile
    public void onInitializeClient() {
        new Delta();
    }

    static {
        NativeMethodLookup.lookup(Initializer.class, 1);
    }
}

