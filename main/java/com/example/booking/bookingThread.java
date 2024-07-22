package com.example.booking;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class bookingThread extends Thread {

    Handler bookingHandler;
    String username;
    String roomName;
    String bookingDates;

    bookingThread(Handler bookingHandler, String username, String roomName, String bookingDates) {
        this.bookingHandler = bookingHandler;
        this.username = username;
        this.roomName = roomName;
        this.bookingDates = bookingDates;
    }

    @Override
    public void run() {

        try {
            Socket masterConnection = new Socket("192.168.2.13", 10000);

            ObjectOutputStream out = new ObjectOutputStream(masterConnection.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(masterConnection.getInputStream());

            out.write(1);
            out.flush();

            out.write(3);
            out.flush();

            out.writeUTF(username);
            out.flush();

            out.writeUTF(roomName);
            out.flush();

            out.writeUTF(bookingDates);
            out.flush();

            int result = in.read();

            masterConnection.close();

            Message msg = new Message();
            Bundle bundle = new Bundle();
            bundle.putInt("result", result);
            msg.setData(bundle);
            bookingHandler.sendMessage(msg);

        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
