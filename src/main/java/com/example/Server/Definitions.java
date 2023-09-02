package com.example.Server;

public class Definitions {
    public static int CREATE_SESSION_MACROS = 1;
    public static int KEY_LENGTH_1 = 128;
    public static int KEY_LENGTH_2 = 192;
    public static int KEY_LENGTH_3 = 256;
    public enum EncryptionMods{
        MARS_EBC,
        MARS_CBC,
        XTR,
        ModsCount
    }


    public static String encryptionModeName(EncryptionMods mode){
        switch(mode) {
            case MARS_CBC:
                return "MARS(CBC)";
            case MARS_EBC:
                return "MARS(EBC)";
            case XTR:
                return "XTR";
        }
        return "";
    }
    public static EncryptionMods encryptionMode(String value){
        switch(value) {
            case "MARS(CBC)":
                return EncryptionMods.MARS_CBC;
            case "MARS(EBC)":
                return EncryptionMods.MARS_EBC;
            case "XTR":
                return EncryptionMods.XTR;
        }
        return EncryptionMods.ModsCount;
    }
}

