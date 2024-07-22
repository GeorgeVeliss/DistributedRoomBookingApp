package com.example.booking;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class getAvailableDatesThread extends Thread {

    Handler getAvailableDatesHandler;
    String username;
    String roomName;


    public getAvailableDatesThread(Handler getAvailableDatesHandler, String username, String roomName) {
        this.getAvailableDatesHandler = getAvailableDatesHandler;
        this.username = username;
        this.roomName = roomName;
    }

    @Override
    public void run() {

        try {
            Socket masterConnection = new Socket("192.168.2.13", 10000);

            ObjectOutputStream out = new ObjectOutputStream(masterConnection.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(masterConnection.getInputStream());

            out.write(1);
            out.flush();

            out.write(2);
            out.flush();

            out.writeUTF(username);
            out.flush();

            out.writeUTF(roomName);
            out.flush();

            ArrayList<String> availableDates = (ArrayList<String>) in.readObject();

            Log.i("test", availableDates.get(0));

            Message msg = new Message();
            Bundle bundle = new Bundle();
            bundle.putSerializable("availableDates", availableDates);
            msg.setData(bundle);
            getAvailableDatesHandler.sendMessage(msg);

            masterConnection.close();

        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
