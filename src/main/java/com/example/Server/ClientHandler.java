package com.example.Server;

import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;

public class ClientHandler extends Thread {
    public static final int FILE_BUF_SIZE = 1048576;
    private Socket socket;
    private BufferedReader in; // поток чтения из сокета
    private BufferedWriter out; // поток записи в сокет
    private int sessionId;
    private byte[] fileBuf;
    private int byteCounter;

    public ClientHandler(Socket socket, int id) throws IOException {
        this.socket = socket;
        byteCounter = 0;
        sessionId = id;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        start();
    }

    @Override
    public void run() {
        while (true) {
            var data = readMessage();
            if (data.equals("closed")) {
                Session.removeClient(socket.getPort(), sessionId);
            } else if (!data.isEmpty()) {
                var msg = "session : " + sessionId + " : user " + socket.getPort() + " : " + data;
                MainViewController.log(msg);
            }

        }
    }


    public String readMessage() {
        String result = "";
        try {
            var data = in.readLine();
            Message msg = new Gson().fromJson(data, Message.class);
            msg.data = Base64.getDecoder().decode(msg.data);
            File file;
            switch (msg.type) {
                case "system":
                    result = new String(msg.data);
                    break;
                case "file":
                    switch (msg.status) {
                        case "init":
                            if (msg.size != 0) {
                                fileBuf = new byte[msg.size];
                                System.arraycopy(msg.data, 0, fileBuf, byteCounter, msg.data.length);
                                byteCounter += msg.data.length;
                            }
                            break;
                        case "loading":
                            System.arraycopy(msg.data, 0, fileBuf, byteCounter, msg.data.length);
                            byteCounter += msg.data.length;
                            break;
                        case "ready":
                            if (fileBuf == null) {
                                fileBuf = new byte[msg.size];
                            }
                            System.arraycopy(msg.data, 0, fileBuf, byteCounter, msg.data.length);
                            byteCounter += msg.data.length;
                            file = new File("sessions/" + sessionId, generateUniqueFileName(msg.name));
                            try (var writer = new FileOutputStream(file)) {
                                writer.write(fileBuf);
                            }
                            byteCounter = 0;
                            return msg.status;
                    }
                    break;
                case "text":
                    file = new File("sessions/" + sessionId, generateUniqueFileName(Integer.toString(socket.getPort())) + ".txt");
                    try (var writer = new FileOutputStream(file)) {
                        writer.write(msg.data);
                    }
                    result = new String(msg.data);
                    break;
                case "load":
                    file = new File("sessions/" + sessionId, msg.name);
                    if (file.exists()) {
                        sendFile(file);
                    }
            }
        } catch (Exception e) {
            MainViewController.log("read message error : " + e.getMessage());
        }
        return result;
    }

    private void sendFile(File file) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    var bytes = Files.readAllBytes(file.toPath());
                    var size = bytes.length;
                    int percent = size / 100;
                    int percentage = 0;
                    for (int i = 0; i < size; i += FILE_BUF_SIZE) {
                        byte[] buf;
                        String status;
                        if (i + FILE_BUF_SIZE >= size) {
                            buf = new byte[size - i];
                            System.arraycopy(bytes, i, buf, 0, size - i);
                            status = "ready";
                        } else {
                            buf = new byte[FILE_BUF_SIZE];
                            System.arraycopy(bytes, i, buf, 0, FILE_BUF_SIZE);
                            if (i == 0) {
                                status = "init";
                            } else {
                                status = "loading";
                            }
                        }
                        var b64 = Base64.getEncoder().encode(buf);
                        var msg = new Message(file.getName(), "file", status, bytes.length, b64);
                        if (i > percent) {
                            ++percentage;
                            MainViewController.log("loading file " + file.getName() + " : [" + percentage + "%]");
                            percent += percent;
                        }
                        writeMessage(msg);
                    }
                } catch (Exception e) {
                    int a = 1;
                }
            }

        }).start();
    }

    private String generateUniqueFileName(String fileName) {
        var dir = new File("sessions/" + sessionId);
        var buf = new ArrayList<String>();
        var files = Arrays.stream(dir.list()).toList();
        int counter = 1;
        String result = fileName;
        if (!files.contains(fileName)) {
            result = fileName;
        } else {
            while (files.contains(result)) {
                var st = new StringBuilder(result);
                var i = fileName.lastIndexOf(".");
                result = st.insert(i, "(" + Integer.toString(counter) + ")").toString();
                ++counter;
            }
        }
        return result;
    }

    public void writeMessage(Message msg) {
        try {
            var msgJson = new Gson().toJson(msg);
            out.write(msgJson.toString() + "\n");
            out.flush();
        } catch (IOException e) {
            MainViewController.log(e.getMessage());
        }
    }
}
