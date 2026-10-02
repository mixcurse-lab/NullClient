package platform.client.utils.bridge.discord;


import java.util.List;

@FunctionalInterface
public interface PipePathProvider {
    List<String> locateAll();
}



