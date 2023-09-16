package com.example.Server;

import com.google.gson.Gson;

import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Server {
//    public static LinkedList<ServerSmth> serverList = new LinkedList<>(); // список всех нитей

    private static Socket clientSocket; //сокет для общения
    private static BufferedReader in; // поток чтения из сокета
    private static BufferedWriter out; // поток записи в сокет
    private static BufferedWriter bufOut; // for transfering
    private static BufferedReader bufIn; // for transfering
    public static Map<Integer, Session> sessions;
    // session key, encryption key
    public static Map<Integer, String> keys;
    public static Map<Integer, byte[]> ivs;

    private static BigInteger[] hostPublicKey;

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
                    if (isCurSocketHost.equals(String.valueOf(Definitions.CREATE_SESSION_MACROS))) {
                        // creating session
                        var encryptionMode = readMessage();
                        var keyLength = Integer.valueOf(readMessage());
                        if (encryptionMode.equals("MARS(CBC)")) {
                            var iv = readMessage();
                            var decrypted = Base64.getDecoder().decode((iv));
                            ivs.put(Session.idGenerator, decrypted);
                        }
                        writeSystemMessage("you inited session : " + Session.idGenerator);
                        hostPublicKey = readPublicKey();
                        bufIn = in;
                        bufOut = out;
                        sessions.put(Session.idGenerator, new Session(clientSocket, encryptionMode,keyLength));

                    } else {
                        // join to server
                        var sessionId = readMessage();
                        var castedId = Integer.valueOf(sessionId);
                        var session = sessions.get(castedId);
                        if (session == null || session.isFull()) {
                            var msg = "user " + clientSocket.getPort() + " : cant connect session";
                            MainViewController.log(msg);
                            writeSystemMessage("session " + castedId + " doesn't exist or full");
                        } else {
                            MainViewController.log("user " + clientSocket.getPort() + " : " + "joined to session " + sessionId);
                            writeSystemMessage(session.encryptionMethod);
                            writeSystemMessage(String.valueOf(session.keyLength));
                            if (session.encryptionMethod.equals("MARS(CBC)")) {
                                var iv = ivs.get(castedId);
                                var b64 = Base64.getEncoder().encodeToString(iv);
                                writeSystemMessage(b64);
                            }
//                            writeSystemMessage(key);
                            writeSystemMessage("server : you joined to session " + sessionId);

                            publicKeysExchange(bufOut, out, bufIn, in);
                            session.addClient(clientSocket);
                        }
                    }
                }
            } finally {
                clientSocket.close();
                in.close();
                out.close();
            }
        } catch (IOException e) {
            MainViewController.log(e.getMessage());
        }
    }

    public static void writeMessage(Message msg) {
        try {
            var msgJson = new Gson().toJson(msg);
            out.write(msgJson.toString() + "\n");
            out.flush();
        } catch (IOException e) {
            MainViewController.log(e.getMessage());
        }
    }

    public static void writeSystemMessage(String data) {
        var b64 = Base64.getEncoder().encode(data.getBytes());
        var msg = new Message("", "system", "", 0, b64);
        writeMessage(msg);
    }

    public static void writeSystemMessage(byte[] data) {
        var b64 = Base64.getEncoder().encode(data);
        var msg = new Message("", "system", "", 0, b64);
        writeMessage(msg);
    }

    public static String readMessage() {
        String result = "";
        try {
            var data = in.readLine();
            Message msg = new Gson().fromJson(data, Message.class);
            var decodedBytes = Base64.getDecoder().decode(msg.data);
            switch (msg.type) {
                case "system":
                    result = new String(decodedBytes);
                    break;
                case "pKey":
                    result = new String(decodedBytes);
            }
        } catch (IOException e) {
            MainViewController.log("read message error : " + e.getMessage());
        }
        return result;
    }

    public static void closeSession(int sessionId) {
        MainViewController.log("session " + sessionId + " closed");
        sessions.remove(sessionId);
    }

    private static void publicKeysExchange(BufferedWriter hostOut, BufferedWriter userOut,
                                           BufferedReader hostIn, BufferedReader userIn) throws IOException {

        writePublicKey(hostPublicKey);

        var data = in.readLine();
        Message msg = new Gson().fromJson(data, Message.class);
        var encryptedUserKey = Base64.getDecoder().decode(msg.data);


        out = hostOut;
        in = hostIn;
        writeSystemMessage(encryptedUserKey);
        out = userOut;

    }

    private static void endExchange() {

//        writeSystemMessage(encryptedHostKey);
    }

    public static void setEncryptedHostKey(byte[] encryptedHostKey) {
//        Server.encryptedHostKey = encryptedHostKey;
//        endExchange();
    }

    private static BigInteger[] readPublicKey() {
        String data;
        BigInteger[] result = new BigInteger[7];
        for (int i = 0; i < 7; ++i) {
            try {
                data = in.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Message msg = new Gson().fromJson(data, Message.class);
            var decodedBytes = Base64.getDecoder().decode(msg.data);
            var sVal = new String(decodedBytes);
            result[i] = new BigInteger(sVal);
            System.out.println("get BI : " + result[i].toString(10));
        }
        return result;
    }

    private static void writePublicKey(BigInteger[] val) {
        for (int i = 0; i < val.length; ++i) {
            writeSystemMessage(val[i].toString(10));
        }
    }

}

