package app;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Created by vitia on 07.09.2017.
 */
public class Connection implements Closeable{
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    public void send(Message message) throws IOException{
        synchronized (out){
            out.writeObject(message);
        }
    }

    public  Message receive() throws IOException, ClassNotFoundException{
        synchronized (in){
            Message message = (Message) in.readObject();
            return message;
        }
    }

    @Override
    public int hashCode() {
        int code = socket.hashCode();
        code = 31*code + out.hashCode();
        code = 31 * code + in.hashCode();
        return code;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) return false;
        if(!(obj instanceof  Connection)) return false;
        Connection c = (Connection) obj;
        if(!socket.equals(c.socket)) return false;
        if(!out.equals(c.out)) return false;
        if(!in.equals(c.in)) return false;
        return true;
    }

    public SocketAddress getRemoteSocketAddress(){
        return socket.getRemoteSocketAddress();
    }

    public void close() throws IOException{
        in.close();
        out.close();
        socket.close();
    }
}
