package com.example.Server;

import java.io.File;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Session {
    public static int idGenerator = 1000;
    private Socket socket;
    public int host;
    public String encryptionMethod;
    private String encryptedHostKey;
    private String encryptedUserKey;
    private String publicHostKey;
    private String userHostKey;

    public int id;

    public static Map<Integer,ClientHandler> clientListeners;

    public Session(Socket socket, String encr) {
        clientListeners = new HashMap();
        this.socket = socket;
        this.host = socket.getPort();
        this.encryptionMethod = encr;
        id = idGenerator;
        ++idGenerator;
        addClient(this.socket);
        var sessionDir = new File("sessions/" + id);
        if(!sessionDir.exists()){
            var res = sessionDir.mkdirs();
        }
    }


    public void addClient(Socket addedSocket){
        try {
            clientListeners.put(addedSocket.getPort(),new ClientHandler(addedSocket,id));
        }
        catch (Exception e){
            MainViewController.log("session " + idGenerator + " not add socket " + addedSocket.getPort()
            + " " + e.getMessage());
        }
    }

    static public void removeClient(int clientPort,int sessionId){
        clientListeners.remove(clientPort);
        if(clientListeners.isEmpty()){
            Server.closeSession(sessionId);
        }
    }

    public boolean isFull() {
        return clientListeners.size() == 2;
    }

    public String getEncryptedHostKey() {
        return encryptedHostKey;
    }

    public void setEncryptedHostKey(String encryptedHostKey) {
        this.encryptedHostKey = encryptedHostKey;
    }

    public String getEncryptedUserKey() {
        return encryptedUserKey;
    }

    public void setEncryptedUserKey(String encryptedUserKey) {
        this.encryptedUserKey = encryptedUserKey;
    }

    public String getPublicHostKey() {
        return publicHostKey;
    }

    public void setPublicHostKey(String publicHostKey) {
        this.publicHostKey = publicHostKey;
    }

    public String getUserHostKey() {
        return userHostKey;
    }

    public void setUserHostKey(String userHostKey) {
        this.userHostKey = userHostKey;
    }
}
