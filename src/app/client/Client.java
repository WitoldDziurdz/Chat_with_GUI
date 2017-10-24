package app.client;

import app.Connection;
import app.ConsoleHelper;
import app.Message;
import app.MessageType;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by vitia on 07.09.2017.
 */
public class Client {
    protected Connection connection;
    private volatile boolean clientConnected;

    protected String getServerAddress(){
        return ConsoleHelper.readString();
    }

    protected int getServerPort(){
        return ConsoleHelper.readInt();
    }

    protected String getUserName(){
        return ConsoleHelper.readString();
    }

    protected boolean shouldSendTextFromConsole(){
        return true;
    }

    protected SocketThread getSocketThread(){
        return new SocketThread();
    }

    protected void sendTextMessage(String text){
        try{
            Message message = new Message(MessageType.TEXT, text);
            connection.send(message);
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Во время отправки сообщения произошла ошибка!");
            clientConnected = false;
        }
    }

    public void run(){
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();
        try {
            synchronized (this) {
                wait();
            }
        } catch (InterruptedException e) {
            ConsoleHelper.writeMessage("Ошибка во время ожидания!");
            System.exit(1);
        }
        if(clientConnected == true){
            ConsoleHelper.writeMessage("Соединение установлено. Для выхода наберите команду 'exit'.");
            while (clientConnected){
                String message = ConsoleHelper.readString();
                if(message.equalsIgnoreCase("exit")) break;
                if (shouldSendTextFromConsole()){
                    sendTextMessage(message);
                }
            }
        }else {
            ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
        }
    }

    public class  SocketThread extends Thread{
       protected void processIncomingMessage(String message){
           ConsoleHelper.writeMessage(message);
       }
       protected void informAboutAddingNewUser(String userName){
           ConsoleHelper.writeMessage(String.format("участник с именем %s присоединился к чату",userName));
       }

       protected void informAboutDeletingNewUser(String userName){
           ConsoleHelper.writeMessage(String.format("участник с именем %s покинул чат",userName));
       }

       protected void notifyConnectionStatusChanged(boolean clientConnected){
           Client.this.clientConnected = clientConnected;
           synchronized (Client.this){
               Client.this.notify();
           }
       }

        protected void clientHandshake() throws IOException, ClassNotFoundException{
           while (true) {
               Message message = connection.receive();
               MessageType type = message.getType();
               if (type == MessageType.NAME_REQUEST) {
                   connection.send(new Message(MessageType.USER_NAME, getUserName()));
               } else if (type == MessageType.NAME_ACCEPTED) {
                   notifyConnectionStatusChanged(true);
                   break;
               } else {
                   throw new IOException("Unexpected app.MessageType");
               }
           }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException{
           while (true){
               Message message = connection.receive();
               String text = message.getData();
               MessageType type = message.getType();
               if(type == MessageType.TEXT){
                   processIncomingMessage(message.getData());
               }else if(type == MessageType.USER_ADDED){
                   informAboutAddingNewUser(message.getData());
               }else if(type == MessageType.USER_REMOVED){
                   informAboutDeletingNewUser(message.getData());
               }else {
                   throw new IOException("Unexpected app.MessageType");
               }
           }
        }

        public void run(){
           String address = getServerAddress();
           int port = getServerPort();
            try {
                Socket socket = new Socket(address,port);
                connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();
            } catch (IOException e) {
                notifyConnectionStatusChanged(false);
            } catch (ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}
