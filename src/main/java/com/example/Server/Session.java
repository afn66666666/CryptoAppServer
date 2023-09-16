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
    public int keyLength;
    public int id;

    public static Map<Integer,ClientHandler> clientListeners;

    public Session(Socket socket, String encr,int keyLength) {
        clientListeners = new HashMap();
        this.socket = socket;
        this.host = socket.getPort();
        this.encryptionMethod = encr;
        this.keyLength = keyLength;
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
}
