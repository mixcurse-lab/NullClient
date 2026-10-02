package platform.api.system.interfaces;


import platform.api.annotation.InternalApi;

@FunctionalInterface
@InternalApi
public interface Supplier<T> extends java.util.function.Supplier<T> {
    @Override
    T get();
}



