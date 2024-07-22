package com.example.booking;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class filteringThread extends Thread {

    Handler filteringHandler;
    String username;
    String[] filters;

    filteringThread(Handler filteringHandler, String username, String[] filters) {
        this.filteringHandler = filteringHandler;
        this.username = username;
        this.filters = filters;
    }

    @Override
    public void run() {

        try {
            Socket masterConnection = new Socket("192.168.2.13", 10000);

            ObjectOutputStream out = new ObjectOutputStream(masterConnection.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(masterConnection.getInputStream());

            out.write(1);
            out.flush();

            out.write(0);
            out.flush();

            out.writeUTF(username);
            out.flush();

            out.writeObject(filters);
            out.flush();

            ArrayList<ArrayList<String>> filteredRooms = (ArrayList<ArrayList<String>>) in.readObject();

            masterConnection.close();

            Message msg = new Message();
            Bundle bundle = new Bundle();
            bundle.putSerializable("filteredRooms", filteredRooms);
            msg.setData(bundle);
            filteringHandler.sendMessage(msg);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
