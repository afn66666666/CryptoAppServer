package com.example.Server;

import java.io.*;
import java.net.Socket;

public class ServerSmth extends Thread{
    private Socket socket; // сокет, через который сервер общается с клиентом,
    // кроме него - клиент и сервер никак не связаны
    private BufferedReader in; // поток чтения из сокета
    private BufferedWriter out; // поток записи в сокет

    public ServerSmth(Socket socket) throws IOException {
        this.socket = socket;
        // если потоку ввода/вывода приведут к генерированию исключения, оно проброситься дальше
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        start(); // вызываем run()
    }
    @Override
    public void run() {
        try {
            String word;
            while (true) {
                word = in.readLine();
                System.out.println("got message from " + socket.getPort() + " : " + word);
                MainViewController.log("user " + socket.getPort() + " : " + word);
                if(word.equals("stop")) {
                    break;                }
//                for (var vr : Server.sessions) {
////                    vr.send(word); // отослать принятое сообщение с
//                    // привязанного клиента всем остальным включая его
//                }
            }

        }
        catch (IOException e) {
            MainViewController.log("user : " + socket.getPort() + " disconnected");
        }
    }

    private void send(String msg) {
        try {
            out.write(msg + "\n");
            out.flush();
        } catch (IOException ignored) {}
    }
}
