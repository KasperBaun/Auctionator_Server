import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class RunClient {
    public static void main(String[] args) throws IOException, InterruptedException {

        BufferedReader inputBuffer = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("## Welcome to Auctionator ##");
        System.out.println("Please enter your name:");
        String username = inputBuffer.readLine();
        Client client = new Client(username);
        client.printCommands();

        while(true){

            String userInput = inputBuffer.readLine();

            switch (userInput){
                case "1":
                    client.listAuctions();
                    client.printCommands();
                    break;

                case "2": client.createAuction(inputBuffer);
                    break;

                case "3": client.joinAuction(inputBuffer);
                    break;

                case "4":
                case "exit":
                    System.exit(0);
                    break;

                default:
                    throw new IllegalStateException("Unexpected value: " + userInput);
            }
        }
    }
}
