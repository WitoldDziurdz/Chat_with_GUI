package app.client;

/**
 * Created by vitia on 07.09.2017.
 */
public class ClientGuiController extends Client {
    private ClientGuiModel model = new ClientGuiModel();
    private ClientGuiView view = new ClientGuiView(this);

    protected String getServerAddress(){
        return view.getServerAddress();
    }

    protected int getServerPort(){
        return view.getServerPort();
    }

    protected String getUserName(){
        return view.getUserName();
    }

    protected SocketThread getSocketThread(){
        return new GuiSocketThread();
    }

    public void run(){
        SocketThread socketThread = getSocketThread();
        socketThread.run();
    }

    public ClientGuiModel getModel(){
        return model;
    }

    public class GuiSocketThread extends  SocketThread{
        protected void processIncomingMessage(String message){
            model.setNewMessage(message);
            view.refreshMessages();
        }

        protected void informAboutAddingNewUser(String userName){
            model.addUser(userName);
            view.refreshUsers();
        }

        protected void informAboutDeletingNewUser(String userName){
            model.deleteUser(userName);
            view.refreshUsers();
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected){
            view.notifyConnectionStatusChanged(clientConnected);
        }
    }

    public static void main(String[] args) {
        new ClientGuiController().run();
    }
}
