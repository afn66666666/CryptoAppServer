package com.example.Server;

public class Message {
    public String name;
    public String type;
    public String status;
    public byte[] data;
    public int size;

    public Message(String name, String type, String status, int size, byte[] data){
        this.name = name;
        this.type = type;
        this.status = status;
        this.data = data;
        this.size = size;
    }
}
