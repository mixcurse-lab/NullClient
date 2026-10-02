package platform.api.auth;

import platform.client.utils.bridge.discord.NativeUserInfoState;

public class Aethereal {
    public static String uid() {
        return NativeUserInfoState.uid();
    }

    public static String username() {
        return NativeUserInfoState.username();
    }

    public static String hwid() {
        return NativeUserInfoState.hwid();
    }

    public static String role() {
        return NativeUserInfoState.role();
    }

    public static String expire() {
        return NativeUserInfoState.expire();
    }

    public static String token() {
        return NativeUserInfoState.token();
    }
}



