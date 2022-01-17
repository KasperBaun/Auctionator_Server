
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Client  {

    private String username;
    private RemoteSpace currentAuction;

    public Client(String username, RemoteSpace currentAuction) {
        this.username = username;
        this.currentAuction = currentAuction;
    }

    public RemoteSpace connectToRemoteSpace(String uri) throws IOException {

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
                new FormalField(String.class),      // Auction ID
                new FormalField(String.class),      // Auction title
                new FormalField(String.class),      // Time remaining
                new FormalField(String.class),      // Auction startprice
                new FormalField(String.class)       // Auction URI
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

    public void joinAuction(RemoteSpace space, BufferedReader userinput) throws InterruptedException, IOException {

        List<Object[]> auctions = space.queryAll(
                new ActualField("auction"),
                new FormalField(String.class),      // Auction ID
                new FormalField(String.class),      // Auction title
                new FormalField(String.class),      // Time remaining
                new FormalField(String.class),      // Auction startprice
                new FormalField(String.class)       // Auction URI
        );

        if (auctions.isEmpty()){
            System.out.println("No current auctions are available");
            System.out.println("Returning to lobby");
            System.out.println("\n");
            printCommands();
        }

        System.out.println("Which auction would you like to join? ");
        for (Object[] auction: auctions) {
            System.out.println("# Auction number: " + auction[1].toString() + " : " + auction[2].toString() + " @ " + auction[4].toString() + " Ends in " + auction[3].toString() + " minutes");
        }

        System.out.println("To join type <auction number>");
        System.out.println("Example: 34");
        String auctionURI = null;
        String auctionToJoin = userinput.readLine();
        //System.out.println(auctionToJoin);
        for (Object[] auction : auctions) {
            if (auction[1].toString().equals(auctionToJoin)){
                auctionURI = (String) auction[4];
            }
        }
        if (auctionURI == null){
            System.out.println("Failed to connect to auction. Returning to lobby.\n");
            printCommands();
        } else {
            System.out.println("Initiating connection to auction #: " + auctionToJoin + " @ " + auctionURI);
             currentAuction = connectToRemoteSpace(auctionURI);
             startBidding(currentAuction,userinput);
        }
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

        System.out.println("Please enter image URL: ");
        System.out.println("Example: 'https://www.lundemoellen.dk/images/kr%C3%A6sen-hest2.jpg' ");
        String imageURL = userInput.readLine();

        space.put(
                   "create",
                    userName,           // Username
                    itemName,           // Auction title
                    startPrice,         // Auction start-price
                    endTime,            // End-time
                    description,        // Description
                    imageURL            // ImageURL
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

    public void startBidding(RemoteSpace auction, BufferedReader input) throws InterruptedException, IOException {

        currentAuction.put("hello", username);

        Object[] helloResponse = currentAuction.get(
                new ActualField("initialdata"),
                new ActualField(username),      // client name
                new FormalField(String.class),  // Auction title
                new FormalField(String.class),  // Auction starting price
                new FormalField(String.class),  // Current highest bid
                new FormalField(String.class),  // Time remaining
                new FormalField(String.class),  // Description
                new FormalField(String.class),  // Image url
                new FormalField(String.class)   // Auction owner
        );

        if (helloResponse != null){
            System.out.println(Arrays.toString(helloResponse));
            System.out.println("Welcome to auction #: "+ helloResponse[2] );
            System.out.println("Current highest bid is "+ helloResponse[4].toString() );
            System.out.println("To place a bid type 'bid <amount>'");
            System.out.println("Example 'bid 550' ");
            System.out.println("Increments of minimum 10");
            System.out.println("To leave the auction type 'exit' ");
        }

        //client.startBidding(currentAuction, inputBuffer, username);

        Boolean userActive = true;
        while (userActive){
            String bid = input.readLine();
            if (bid.equals("exit")) return;

            auction.put("bid", bid, username);
        }
    }


    public void fillServerWithDummyAuctions(RemoteSpace space) throws InterruptedException {
        for (int i = 0; i<10; i++){
            space.put(
                    "create",
                    "testUser1"+i,              // Username
                    "Motorcykel"+i,             // Item name
                    "500"+i,                    // Start price
                    "25"+i,                     // End-time
                    "Flot stand",               // Description
                    "https://www.lundemoellen.dk/images/kr%C3%A6sen-hest2.jpg"
            );
        }
        printCommands();
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
