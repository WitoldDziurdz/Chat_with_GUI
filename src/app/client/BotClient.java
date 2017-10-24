package app.client;


import app.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by vitia on 07.09.2017.
 */
public class BotClient extends Client {
    protected SocketThread getSocketThread(){
        return new BotSocketThread();
    }

    protected boolean shouldSendTextFromConsole(){
        return false;
    }

    protected String getUserName(){
        return "date_bot_" + (int)(Math.random()*100);
    }

    public class BotSocketThread extends SocketThread{

        protected void clientMainLoop()throws IOException, ClassNotFoundException{
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        protected void processIncomingMessage(String message){
            SimpleDateFormat format = null;
            ConsoleHelper.writeMessage(message);
            if(message.contains(":")){
                String[] arr = message.split(": ");
                String userName = arr[0];
                String mes = arr[1];
                Date date = GregorianCalendar.getInstance().getTime();
                switch (mes) {
                    case "дата":
                        format = new SimpleDateFormat("d.MM.YYYY");
                        break;
                    case "день":
                        format = new SimpleDateFormat("d");
                        break;
                    case "месяц":
                        format = new SimpleDateFormat("MMMM");
                        break;
                    case "год":
                        format = new SimpleDateFormat("YYYY");
                        break;
                    case "время":
                        format = new SimpleDateFormat("H:mm:ss");
                        break;
                    case "час":
                        format = new SimpleDateFormat("H");
                        break;
                    case "минуты":
                        format = new SimpleDateFormat("m");
                        break;
                    case "секунды":
                        format = new SimpleDateFormat("s");
                        break;
                }
                if (format != null) {
                    sendTextMessage(String.format("Информация для %s: %s", userName, format.format(date)));
                }
            }
        }

        private void sendDate(String userName, String date){
            String answer = String.format("Информация для %s: %s", userName,date);
            sendTextMessage(answer);
        }
    }


    public static void main(String[] args) {
        new BotClient().run();
    }
}
