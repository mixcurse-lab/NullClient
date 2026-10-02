package platform.api.command;

import platform.client.utils.bridge.discord.DiscordIPCException;

import platform.client.utils.bridge.discord.RpcErrorCode;
import lombok.Generated;

public class CommandException extends DiscordIPCException {
    private final RpcErrorCode a;

    @Generated
    public RpcErrorCode a() {
        return this.a;
    }

    public CommandException(RpcErrorCode errorCode, String message) {
        super("RPC error " + errorCode.a() + " (" + errorCode.name() + "): " + message);
        this.a = errorCode;
    }
}



