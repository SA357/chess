package com.network;

import com.network.message.Message;
import com.network.message.Message.CryptedMessage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Transport {

    public void sendMessage_CRYPTED(Message msg, Socket socket, String password) throws Exception{
        ObjectOutputStream o = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        o.writeObject(CryptedMessage.crypt(msg, password));
        o.flush();
    }

    public void sendMessage_CRYPTED(Message msg, InetSocketAddress address, String password) throws Exception{
        try (Socket socket = new Socket()) {
            socket.setSoTimeout(1000);
            socket.connect(address);
            ObjectOutputStream o = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            o.writeObject(CryptedMessage.crypt(msg, password));
            o.flush();
        }
    }

    public Message sendAndReceive_CRYPTED(Message msg, InetSocketAddress address, String password) throws Exception {
        try (Socket socket = new Socket()) {
            socket.setSoTimeout(1000);
            socket.connect(address);
            ObjectOutputStream o = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            o.writeObject(CryptedMessage.crypt(msg, password));
            o.flush();
            return CryptedMessage.decrypt((CryptedMessage) new ObjectInputStream(new BufferedInputStream(socket.getInputStream())).readObject(), password);
        }
    }

    /**
     * @param withoutTimeOut параметр - лишь метка того, что у сокета не будет Timeout
     */
    public Message sendAndReceive_CRYPTED(Message msg, InetSocketAddress address, String password, boolean withoutTimeOut) throws Exception {
        if (!withoutTimeOut) {
            throw new RuntimeException(" Неправильное использование метода ");
        }
        try (Socket socket = new Socket()) {
            socket.connect(address);
            ObjectOutputStream o = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            o.writeObject(CryptedMessage.crypt(msg, password));
            o.flush();
            return CryptedMessage.decrypt((CryptedMessage) new ObjectInputStream(new BufferedInputStream(socket.getInputStream())).readObject(), password);
        }
    }

    public Message sendAndReceive_NOT_CRYPTED(Message msg, InetSocketAddress address) throws Exception {
        try (Socket socket = new Socket()) {
            socket.setSoTimeout(1000);
            socket.connect(address);
            ObjectOutputStream o = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            o.writeObject(msg);
            o.flush();
            return (Message) new ObjectInputStream(new BufferedInputStream(socket.getInputStream())).readObject();
        }
    }

    public void sendMessage_NOT_CRYPTED(Message msg, InetSocketAddress address) throws Exception {
        try (Socket socket = new Socket()) {
            socket.setSoTimeout(1000);
            socket.connect(address);
            ObjectOutputStream o = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            o.writeObject(msg);
            o.flush();
        }
    }

    public void sendMessage_NOT_CRYPTED(Message msg, Socket socket) throws Exception {
        ObjectOutputStream o = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        o.writeObject(msg);
        o.flush();
    }
}
