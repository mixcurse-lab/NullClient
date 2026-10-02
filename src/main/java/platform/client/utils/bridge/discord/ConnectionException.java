package platform.client.utils.bridge.discord;

import platform.client.utils.bridge.discord.DiscordIPCException;

public class ConnectionException extends DiscordIPCException {
    public ConnectionException(String message) {
        super(message);
    }

    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}



