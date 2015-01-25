package com.simplechat.client.net;

import android.os.SystemClock;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by Rufim on 24.01.2015.
 */
public class Client {

    private static final String TAG = "Client";

    Socket socket;
    BufferedReader in;
    Queue<String> buffer;
    boolean stop = false;
    OnMessageListener messageListener;

    public interface OnMessageListener {
        void onMessage(String from, String message);
    }

    public Client(Socket socket, OnMessageListener messageListener) {
        this.socket = socket;
        this.messageListener = messageListener;
    }

    public void runChat(String user) throws Exception {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            buffer = new ArrayBlockingQueue<String>(10);
            out.println(user);
            Listener listener = new Listener();
            listener.start();
            while (!stop) {
                if(buffer.isEmpty())  {
                    SystemClock.sleep(500);
                } else {
                    out.println(buffer.poll());
                }
            }
            listener.setStop();
        } catch (Exception e) {
            Log.e(TAG, "Unknown exception");
        }
    }

    public void sendMessage(String message) {
        buffer.offer(message);
    }

    private class Listener extends Thread {
        private boolean stoped;

        public void setStop() {
            stoped = true;
        }

        @Override
        public void run() {
            try {
                while (!stoped) {
                    String str = in.readLine();
                    if(str.contains(":")) {
                        String from = str.substring(0, str.indexOf(":"));
                        String message = str.substring(str.indexOf(":") + 1);
                        messageListener.onMessage(from, message);
                    } else {
                        messageListener.onMessage(null, str);
                    }

                }
            } catch (IOException e) {
                Log.e(TAG, "Ошибка при получении сообщения.");
            }
        }
    }
}

