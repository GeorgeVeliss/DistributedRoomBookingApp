package com.example.booking;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class ratingThread extends Thread {

    Handler ratingHandler;
    String roomName;
    String rating;


    public ratingThread(Handler ratingHandler, String roomName, String rating) {
        this.ratingHandler = ratingHandler;
        this.roomName = roomName;
        this.rating = rating;
    }

    @Override
    public void run() {

        try {
            Socket masterConnection = new Socket("192.168.2.13", 10000);

            ObjectOutputStream out = new ObjectOutputStream(masterConnection.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(masterConnection.getInputStream());

            out.write(1);
            out.flush();

            out.write(4);
            out.flush();

            out.writeUTF(roomName);
            out.flush();

            out.writeUTF(rating);
            out.flush();

            Boolean success = in.readBoolean();

            Message msg = new Message();
            Bundle bundle = new Bundle();
            bundle.putBoolean("success", success);
            msg.setData(bundle);
            ratingHandler.sendMessage(msg);

            masterConnection.close();

        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

