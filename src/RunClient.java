import org.jspace.RemoteSpace;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class RunClient {
    public void main(String[] args) throws IOException, InterruptedException {

        Client client = new Client();
        RemoteSpace auctionatorLobby = client.connectToServer("tcp://127.0.0.1:9001/lobby?keep");
        BufferedReader inputBuffer = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("\t Please enter your name: \n");
        String username = inputBuffer.readLine();
        client.printCommands();

        while(true){

            String userInput = inputBuffer.readLine();

            switch (userInput){
                case "1": client.listAuctions(auctionatorLobby);
                    break;

                case "2": client.createAuction(auctionatorLobby, inputBuffer, username);
                    break;

                case "join": client.joinAuction(username);
                    break;

                case "exit": System.exit(0);
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
