package app;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by vitia on 07.09.2017.
 */
public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap();



    public static void sendBroadcastMessage(Message message){
        for(Connection connection : connectionMap.values()){
            try {
                connection.send(message);
            } catch (IOException e) {
                ConsoleHelper.writeMessage("An error occured while sending the message.");
            }
        }
    }

    private static class Handler extends  Thread{
        private Socket socket;
        public Handler(Socket socket){
            this.socket = socket;
        }

        public void run(){
            SocketAddress address = socket.getRemoteSocketAddress();
            ConsoleHelper.writeMessage("A new connection with a remote address  " + address);
            Connection connection = null;
            String name = null;
            try {
                connection = new Connection(socket);
                name = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED,name));
                sendListOfUsers(connection,name);
                serverMainLoop(connection,name);
            } catch (IOException e) {
                ConsoleHelper.writeMessage("An error occurred while communicating with the remote address:" + address);
            } catch (ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Произошла ошибка при обмене данными с удаленным адресом:" + address);
            }finally {
               if(name != null){
                   connectionMap.remove(name);
                   sendBroadcastMessage(new Message(MessageType.USER_REMOVED, name));
               }
               if(connection!=null){
                   try {
                       connection.close();
                       ConsoleHelper.writeMessage("Произошла ошибка при обмене данными с удаленным адресом:" + address);
                       ConsoleHelper.writeMessage("Соединение с удаленным адресом закрыто:" + address);
                   } catch (IOException e) {
                       ConsoleHelper.writeMessage("Произошла ошибка при закрытии сойдинения с удаленным адресом:" + address);
                   }
               }
            }
        }



        private boolean checkRooles(Message message){
            if(message.getType() != MessageType.USER_NAME)
                return false;
            if (message.getData()=="")
                return false;
            if(connectionMap.containsKey(message.getData()))
                return false;
            return true;
        }

        private void sendListOfUsers(Connection connection, String userName) throws IOException{
            for(Map.Entry entry : connectionMap.entrySet()){
                if(!userName.equals(entry.getKey())){
                    connection.send(new Message(MessageType.USER_ADDED, (String)entry.getKey()));
                }
            }
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException{
            String name = null;
            Message message = null;
            connection.send(new Message(MessageType.NAME_REQUEST));
            message = connection.receive();
            if(checkRooles(message)){
                connectionMap.put(message.getData(), connection);
                connection.send(new Message(MessageType.NAME_ACCEPTED));
                name = message.getData();
            }else {
                name = serverHandshake(connection);
            }
            return name;
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT) {
                    String string = userName + ": " + message.getData();
                    sendBroadcastMessage(new Message(MessageType.TEXT, string));
                } else {
                    ConsoleHelper.writeMessage("Incorrect data type.");
                }
            }
        }
    }

    public static void main(String[] args)  {
        int port = ConsoleHelper.readInt();
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            ConsoleHelper.writeMessage("Сурвер запущен.");
            while (true){
                Socket socket = serverSocket.accept();
                Handler handler = new Handler(socket);
                handler.start();
            }
        } catch (IOException e) {
            ConsoleHelper.writeMessage(e.getMessage());
        }finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                ConsoleHelper.writeMessage(e.getMessage());
            }
        }

    }
}
