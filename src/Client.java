
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.List;

public class Client  {


    public RemoteSpace connectToServer(String uri) throws IOException {

        //String uri = "tcp://"+ InetAddress.getLocalHost().getHostAddress()+"/lobby?keep";
        String URI = uri;

        // Connect to the remote lobby
        System.out.println("Connecting to lobby " + uri + "...");
        RemoteSpace space = new RemoteSpace(uri);
    return space;

    }

    public void printCommands(){
        System.out.println("\t ###################### \n");
        System.out.println("\t Welcome to Auctionator \n");
        System.out.println("\t Please type a command: \n");
        System.out.println("\t 1: List Auctions \n");
        System.out.println("\t 2: Create Auction \n");
        System.out.println("\t exit: Close Auctionator \n");
        System.out.println("\t ###################### \n");
    }

    public void listAuctions(RemoteSpace space) throws InterruptedException {
        List<Object[]> auctions = space.queryAll(
                new ActualField("auction"),
                new FormalField(String.class),
                new FormalField(String.class),
                new FormalField(String.class)
        );

        for (Object[] auction: auctions) {
            System.out.println(
                    "\t ###############################################\n"  +
                    "\t # Auction number: " + auction[1].toString() + "\n"  +
                    "\t # Item name     : " + auction[2].toString() + "\n"  +
                    "\t # Ends in       : " + auction[3].toString() + "\n"  +
                    "\t ###############################################\n\n"
            );
        }

        //System.out.println("\t To join an auction type: enter <auctionID> - example: 'enter 34'");
        System.out.println("\t To join an auction type: join");

    }

    public void joinAuction(String username){

    }

    public void createAuction(RemoteSpace space, BufferedReader userInput, String username) throws InterruptedException, IOException {
        String userName = username;
        System.out.println("\t Please enter item name: \n");
        System.out.println("\t Example: 'Sort stol' \n");
        String itemName = userInput.readLine();

        System.out.println("\t Please enter item start price in DKK: \n");
        System.out.println("\t Example: '500' \n");
        String startPrice = userInput.readLine();

        System.out.println("\t Please enter timer for the auction in minutes: \n");
        System.out.println("\t Example: '60' \n");
        String endTime = userInput.readLine();

        System.out.println("\t Please enter item description: \n");
        System.out.println("\t Example: 'Sort stol fra Georg Jensen. Købt i 2014. Har kvittering. Lidt skrammer men ingen større ridser' \n");
        String description = userInput.readLine();

        space.put(
                   "create",
                    userName,           // Username
                    itemName,           // Item name
                    startPrice,         // Start price
                    endTime,            // End-time
                    description         // Description
        );

        Object[] response = space.get(
                new ActualField("auctionURI"),
                new ActualField(userName),
                new FormalField(String.class),
                new FormalField(String.class)
        );

        System.out.println("\t Succes! Auction # " + response[2] + " created  @ " + response[3] +  "\n");
        System.out.println("\t Would you like to join the auction? \n");
        System.out.println("\t Yes: Type 1 \n");
        System.out.println("\t No : Type 2 (You will be returned to lobby) \n");
        String joinAuction = userInput.readLine();

        if (joinAuction.equals("1")){
            joinAuction(username);
        }
    }
}
