package com.example.Server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Server {
//    public static LinkedList<ServerSmth> serverList = new LinkedList<>(); // список всех нитей

    private static Socket clientSocket; //сокет для общения
    private static BufferedReader in; // поток чтения из сокета
    private static BufferedWriter out; // поток записи в сокет
    public static Map<Integer,Session> sessions;
    public static Map<Integer,String> keys;
    public static Map<Integer,byte[]> ivs;

    public static void launch() {
        try {
            try {
                sessions = new HashMap();
                keys = new HashMap<>();
                ivs = new HashMap<>();
                ServerSocket server = new ServerSocket(4004);
                MainViewController.log("launched");
                while (true) {
                    clientSocket = server.accept();
                    out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    var isCurSocketHost = readMessage();
                    System.out.println(isCurSocketHost);
                    if (isCurSocketHost.equals("1")) {
                        // creating session
                        var encryptionMode = readMessage();
                        if(encryptionMode.equals("MARS(CBC)")){
                            var iv = readMessage();
                            var decrypted = Base64.getDecoder().decode((iv));
                            ivs.put(Session.idGenerator,decrypted);
                        }
                        var key = readMessage();
                        keys.put(Session.idGenerator,key);
                        writeSystemMessage("server : session inited");
                        writeSystemMessage(Integer.toString(Session.idGenerator));
                        MainViewController.log("user : " + clientSocket.getPort() + " init a session " + Session.idGenerator);
                        sessions.put(Session.idGenerator, new Session(clientSocket, encryptionMode));
                    } else {
                        // join to server
                        var sessionId = readMessage();
                        var castedId = Integer.valueOf(sessionId);
                        var session = sessions.get(Integer.valueOf(castedId));
                        var key = keys.get(castedId);
                        if (session == null) {
                            var msg = "user " + clientSocket.getPort() + " : cant find session";
                            MainViewController.log("user " + clientSocket.getPort() + " : cant find session");
                            writeSystemMessage("session " + castedId + " doesn't exist");
                        } else {
                            MainViewController.log("user " + clientSocket.getPort() + " : " + "joined to session " + sessionId);
                            writeSystemMessage(session.encryptionMethod);
                            if(session.encryptionMethod.equals("MARS(CBC)")){
                                var iv = ivs.get(castedId);
                                var b64 = Base64.getEncoder().encodeToString(iv);
                                writeSystemMessage(b64);
                            }
                            writeSystemMessage(key);
                            writeSystemMessage("server : you joined to session " + sessionId);
                            session.addClient(clientSocket);

                        }
                    }
//                System.out.println(clientSocket.getPort() + " added");
//                MainViewController.log(clientSocket.getPort() + " added");
                }
            }
            finally {
                clientSocket.close();
                in.close();
                out.close();
            }
        } catch (IOException e) {
            MainViewController.log(e.getMessage());
        }
    }

    public static void writeMessage(Message msg){
        try {
            var msgJson = new Gson().toJson(msg);
            out.write(msgJson.toString() + "\n");
            out.flush();
        }
        catch(IOException e){
            MainViewController.log(e.getMessage());
        }
    }

    public static void writeSystemMessage(String data){
        var b64 = Base64.getEncoder().encode(data.getBytes());
        var msg = new Message("","system","",0,b64);
        writeMessage(msg);
    }

    public static String readMessage(){
        String result = "";
        try{
            var data = in.readLine();
            Message msg = new Gson().fromJson(data,Message.class);
            var decodedBytes = Base64.getDecoder().decode(msg.data);
            switch(msg.type){
                case "system":
                    result = new String(decodedBytes);
                    break;
            }
        }
        catch(IOException e){
            MainViewController.log("read message error : " + e.getMessage());
        }
        return result;
    }

    public static void closeSession(int sessionId){
        MainViewController.log("session " + sessionId + " closed");
        sessions.remove(sessionId);
    }
}

