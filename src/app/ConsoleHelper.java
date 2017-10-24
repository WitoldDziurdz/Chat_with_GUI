package app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by vitia on 07.09.2017.
 */
public class ConsoleHelper {
    private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    public static void writeMessage(String message){
        System.out.println(message);
    }

    public static String readString(){
        String string = null;
        try {
            string = reader.readLine();
        } catch (IOException e) {
            System.out.println("An error occurred while trying to enter text. Try again.");
            string = readString();
        }
        return string;
    }

    public static int readInt(){
        int number = 0;
        try {
            number = Integer.parseInt(readString());
        }catch (NumberFormatException e){
            System.out.println("An error occurred while trying to enter a number. Try again.");
            number = readInt();
        }
        return number;
    }
}
