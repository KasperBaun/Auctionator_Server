import org.jspace.RemoteSpace;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class RunClient {
    public static void main(String[] args) throws IOException, InterruptedException {

        Client client = new Client();
        RemoteSpace auctionatorLobby = client.connectToServer("tcp://127.0.0.1:9001/lobby?keep");
        RemoteSpace currentAuction = null;
        BufferedReader inputBuffer = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("## Welcome to Auctionator ##");
        System.out.println("Please enter your name:");
        String username = inputBuffer.readLine();
        client.printCommands();

        while(true){

            String userInput = inputBuffer.readLine();

            switch (userInput){
                case "1": client.listAuctions(auctionatorLobby);
                    break;

                case "2": client.createAuction(auctionatorLobby, inputBuffer, username);
                    break;

                case "3":
                    currentAuction = client.joinAuction(auctionatorLobby, inputBuffer);
                    if(currentAuction != null) currentAuction.put(username,"online");
                    client.startBidding(currentAuction, inputBuffer, username);
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
