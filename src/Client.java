
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

        // Connect to the remote space
        System.out.println("Connecting to RemoteSpace " + uri + "...");
        RemoteSpace space = new RemoteSpace(uri);
        return space;

    }

    public void printCommands(){
        //System.out.println("######################");
        System.out.println("Please type a command:");
        System.out.println("1: List auctions");
        System.out.println("2: Create auction");
        System.out.println("3: Join auction");
        System.out.println("4: Exit Auctionator");
        System.out.println("5: Fill server with 10 dummyauctions");
        //System.out.println("######################");
    }

    public void listAuctions(RemoteSpace space) throws InterruptedException {
        List<Object[]> auctions = space.queryAll(
                new ActualField("auction"),
                new FormalField(String.class),
                new FormalField(String.class),
                new FormalField(String.class),
                new FormalField(String.class)
        );
        if (auctions.isEmpty()){
            System.out.println("No current auctions are available");
            System.out.println("Returning to lobby");
            System.out.println("\n");
            printCommands();
            return;
        }

        for (Object[] auction: auctions) {
            System.out.println(
                    "Auction number: " + auction[1].toString() +
                    " - " + auction[2].toString() +
                    " - Ends in: " + auction[3].toString() + " minutes"
            );
        }

        System.out.println("");
        printCommands();
    }

    public RemoteSpace joinAuction(RemoteSpace space, BufferedReader userinput) throws InterruptedException, IOException {

        List<Object[]> auctions = space.queryAll(
                new ActualField("auction"),
                new FormalField(String.class),
                new FormalField(String.class),
                new FormalField(String.class)
        );
        if (auctions.isEmpty()){
            System.out.println("No current auctions are available");
            System.out.println("Returning to lobby");
            System.out.println("\n");
            printCommands();
            return null;
        }

        System.out.println("Which auction would you like to join? ");
        for (Object[] auction: auctions) {
            System.out.println("# Auction number: " + auction[1].toString() + ":" + auction[2].toString());
        }

        System.out.println("To join type <auction number>");
        System.out.println("Example: 34");
        String auctionURI = null;
        String auctionToJoin = userinput.readLine();
        for (Object[] auction:auctions) {
            if (auction[1]==auctionToJoin){
                auctionURI = auction[3].toString();
            }
        }
        if (auctionURI == null){
            System.out.println("Failed to connect to auction. Returning to lobby.");
            printCommands();
        } else {
            System.out.println("Connected to auction #: " + auctionToJoin);
            return connectToServer(auctionURI);
        }
        return null;
    }

    public void createAuction(RemoteSpace space, BufferedReader userInput, String username) throws InterruptedException, IOException {
        String userName = username;
        System.out.println("Please enter item name: ");
        System.out.println("Example: 'Sort stol' ");
        String itemName = userInput.readLine();

        System.out.println("Please enter item start price in DKK: ");
        System.out.println("Example: '500' ");
        String startPrice = userInput.readLine();

        System.out.println("Please enter timer for the auction in minutes: ");
        System.out.println("Example: '60' ");
        String endTime = userInput.readLine();

        System.out.println("Please enter item description: ");
        System.out.println("Example: 'Sort stol fra Georg Jensen. Købt i 2014. Har kvittering. Lidt skrammer men ingen større ridser' ");
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

        System.out.println("Succes! Auction # " + response[2] + " created  @ " + response[3] +  "");
        System.out.println("");
        printCommands();
    }

    public void startBidding(RemoteSpace auction, BufferedReader input, String username) throws InterruptedException, IOException {
        Object[] getAuction = auction.query(
                new ActualField("auctioninfo"),
                new FormalField(String.class),      // Auction Name
                new FormalField(String.class)       // AuctionID
        );

        if (getAuction != null){
            System.out.println("Welcome to auction #: "+ getAuction[2] );
            System.out.println("Current highest bid is 500 from Jens");
            System.out.println("To place a bid type 'bid <amount>'");
            System.out.println("Example 'bid 550' ");
            System.out.println("Increments of minimum 10");
            System.out.println("To leave the auction type 'exit' ");
            Boolean userActive = true;
            while (userActive){
                String bid = input.readLine();
                if (bid.equals("exit")) return;

                auction.put("bid", bid, username);
            }
        }
    }

    public void fillServerWithDummyAuctions(RemoteSpace space) throws InterruptedException {
        for (int i = 0; i<10; i++){
            space.put(
                    "create",
                    "testUser1"+i,           // Username
                    "Motorcykel"+i,           // Item name
                    "500"+i,         // Start price
                    "25"+i,            // End-time
                    "Flot stand"         // Description
            );
        }
        printCommands();
    }
}
