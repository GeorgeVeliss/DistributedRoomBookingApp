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

public class getImageThread extends Thread {

    Handler getImageHandler;
    String username;
    String roomName;


    public getImageThread(Handler getImageHandler, String username, String roomName) {
        this.getImageHandler = getImageHandler;
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

            out.write(1);
            out.flush();

            out.writeUTF(username);
            out.flush();

            out.writeUTF(roomName);
            out.flush();

            int roomImageLength = in.readInt();

            if (roomImageLength > 0) {
                byte[] roomImage = new byte[roomImageLength];
                in.readFully(roomImage, 0, roomImage.length);

                Message msg = new Message();
                Bundle bundle = new Bundle();
                bundle.putByteArray("roomImage", roomImage);
                msg.setData(bundle);
                getImageHandler.sendMessage(msg);

            }
            masterConnection.close();


        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
