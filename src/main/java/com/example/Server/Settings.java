package com.example.Server;

public class Settings {
    private static Definitions.EncryptionMods encryptionMode;
    private static String publicKey;
    private static String privateKey;
    private static int keyLength;

    public static Definitions.EncryptionMods getEncryptionMode() {
        return encryptionMode;
    }

    public static void setEncryptionMode(Definitions.EncryptionMods encryptionMode) {
        Settings.encryptionMode = encryptionMode;
    }

    public static String getPublicKey() {
        return publicKey;
    }

    public static void setPublicKey(String publicKey) {
        Settings.publicKey = publicKey;
    }

    public static String getPrivateKey() {
        return privateKey;
    }

    public static void setPrivateKey(String privateKey) {
        Settings.privateKey = privateKey;
    }

    public static int getKeyLength() {
        return keyLength;
    }

    public static void setKeyLength(int keyLength) {
        Settings.keyLength = keyLength;
    }
}
