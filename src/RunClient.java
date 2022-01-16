import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class RunClient {
    public static void main(String[] args) throws IOException, InterruptedException {

        Client client = new Client("null",null);
        RemoteSpace auctionatorLobby = client.connectToRemoteSpace("tcp://127.0.0.1:9001/lobby?keep");
        BufferedReader inputBuffer = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("## Welcome to Auctionator ##");
        System.out.println("Please enter your name:");
        String username = inputBuffer.readLine();
        client.setUsername(username);
        client.printCommands();

        while(true){

            String userInput = inputBuffer.readLine();

            switch (userInput){
                case "1": client.listAuctions(auctionatorLobby);
                    break;

                case "2": client.createAuction(auctionatorLobby, inputBuffer, username);
                    break;

                case "3":
                    client.joinAuction(auctionatorLobby, inputBuffer);
                    break;

                case "4": System.exit(0);
                    break;

                case "5": client.fillServerWithDummyAuctions(auctionatorLobby);
                    break;

                default:
                    throw new IllegalStateException("Unexpected value: " + userInput);
            }
        }
    }
}
            /*// Keep sending whatever the user types
            System.out.println("Start bidding... (Increments of minimum 10 above highest bid)");
            while(true) {
                String message = input.readLine();
                auctionroom_space.put(username, message);
            }*/
