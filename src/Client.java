
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Client  {

    private String username;
    private RemoteSpace auctionatorLobby;
    private int balance;
    private Object[] currentAuction;

    public Client(String username) throws IOException {
        this.username = username;
        this.balance = 10000;
        auctionatorLobby = new RemoteSpace("tcp://127.0.0.1:9001/lobby?keep");
    }

    public void printCommands(){
        //System.out.println("######################");
        System.out.println("Please type a command:");
        System.out.println("1: List auctions");
        System.out.println("2: Create auction");
        System.out.println("3: Join auction");
        System.out.println("4: Exit Auctionator");
        //System.out.println("######################");
    }

    public void listAuctions() throws InterruptedException {
        ArrayList<Integer> auctionsIDS = new ArrayList();
        List<Object[]> auctions = auctionatorLobby.queryAll(
                new ActualField("auction"),
                new FormalField(String.class),      // Auction ID
                new FormalField(String.class),      // Auction title
                new FormalField(String.class),      // Auction start price
                new FormalField(String.class),      // Highest bid currently
                new FormalField(String.class),      // Timestamp
                new FormalField(String.class),      // Description
                new FormalField(String.class),      // Image URL
                new FormalField(String.class)       // Auction creator
        );

        if (auctions.isEmpty()){
            System.out.println("No current auctions are available");
            System.out.println("Returning to lobby");
            System.out.println("\n");
            printCommands();
            return;
        }

        for (Object[] auction: auctions) {
            auctionsIDS.add(Integer.parseInt(auction[1].toString()));
        }

        Collections.sort(auctionsIDS);
        for (int index: auctionsIDS) {
            for (Object[] auction: auctions) {
                if (Integer.parseInt(auction[1].toString()) == index){
                    System.out.println(
                            "Auction number: " + auction[1].toString() +
                                    " - " + auction[2].toString() +
                                    " - Current highest bid is: " + auction[4].toString() +
                                    " - Ends at: " + auction[5].toString()
                    );
                }
            }
        }


        System.out.println("");
    }

    public void joinAuction(BufferedReader inputBuffer) throws InterruptedException, IOException {

        listAuctions();

        System.out.println("Which auction would you like to join? ");
        System.out.println("To join type <auction number>");
        System.out.println("Example: 34");
        String auctionID;
        auctionID = inputBuffer.readLine();
        /*
        Object[] auctionTuple = auctionatorLobby.query(
                new ActualField("auction"),
                new FormalField(String.class),      // Auction ID
                new FormalField(String.class),      // Auction title
                new FormalField(String.class),      // Auction start price
                new FormalField(String.class),      // Highest bid currently
                new FormalField(String.class),      // Timestamp
                new FormalField(String.class),      // Description
                new FormalField(String.class),      // Image URL
                new FormalField(String.class)       // Auction creator
        );

         */

        System.out.println("Connecting to auction #: " + auctionID );
        startBidding(auctionID, inputBuffer);
    }

    public void createAuction(BufferedReader userInput) throws InterruptedException, IOException {
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

        auctionatorLobby.put(
                   "create",
                    userName,           // Username
                    itemName,           // Auction title
                    startPrice,         // Auction start-price
                    endTime,            // End-time
                    description,        // Description
                    imageURL            // ImageURL
        );
        /*
        Object[] newAuction = auctionatorLobby.get(
                new ActualField("auction"),
                new FormalField(String.class),      // Auction ID
                new FormalField(String.class),      // Auction title
                new FormalField(String.class),      // Auction start price
                new FormalField(String.class),      // Highest bid currently
                new FormalField(String.class),      // Timestamp
                new FormalField(String.class),      // Description
                new FormalField(String.class),      // Image URL
                new ActualField(userName)           // Auction creator
        );

        System.out.println("Succes! Auction # " + newAuction[1] + " created  for user " + newAuction[8]);
        System.out.println("");

         */
        printCommands();
    }

    public void startBidding(String auctionID, BufferedReader inputBuffer) throws InterruptedException, IOException {

        auctionatorLobby.put("hello", auctionID, username);
        int currentHighestBid = 0;

        currentAuction = auctionatorLobby.get(
            new ActualField("auction_"+auctionID),
            new ActualField(username),      // client name
            new FormalField(String.class),  // Auction title
            new FormalField(String.class),  // Auction starting price
            new FormalField(String.class),  // Current highest bid
            new FormalField(String.class),  // Timestamp
            new FormalField(String.class),  // Description
            new FormalField(String.class),  // Image url
            new FormalField(String.class)   // Auction owner
        );

        if (currentAuction != null){
            System.out.println("Welcome to "+ currentAuction[0].toString() );
            System.out.println("Item on auction is: " + currentAuction[2].toString() );
            System.out.println("Item on auction is: " + currentAuction[3].toString() );
        } else {
            System.out.println("Failed to connect to auction");
            printCommands();
            return;
        }

        // Do bids loop
        Thread connectToServer = new Thread(new RunnableServerListener(auctionID));
        connectToServer.start();

        Boolean userActive = true;
        while (userActive){
            if(connectToServer.isAlive()){
                // Do nothing
            } else connectToServer.start();
            int startingPrice = Integer.parseInt(currentAuction[3].toString());
            currentHighestBid = Integer.parseInt(currentAuction[4].toString());
            System.out.println("Current highest bid is "+ currentHighestBid );
            System.out.println("To place a bid type:'bid'");
            System.out.println("To leave the auction type 'exit' ");
            String userInput = inputBuffer.readLine();

            switch (userInput){
                case "bid" :
                    System.out.println("How much would you like to bid?");

                    if(balance > currentHighestBid){
                        String input = inputBuffer.readLine();
                        int bidAmount = Integer.parseInt(input);

                        if (bidAmount < startingPrice){
                            System.out.println("Bid is lower than starting price!");
                            break;
                        }
                        if (bidAmount<currentHighestBid) {
                            System.out.println("your bid is lower that the current highest bid");
                            break;
                        }
                        if(bidAmount <= balance && bidAmount > currentHighestBid){
                            auctionatorLobby.put("bid", auctionID, input, username);
                            System.out.println("You have sent the bid: " + bidAmount + "\n");
                            break;
                        }

                        } else {
                            System.out.println("Your balance is lower than the current highest bid!");
                    }
                    break;
                case "update TUI":
                    break;
                case "exit" :

                case "leave" :
                    System.out.println("Goodbye!\n");
                    userActive = false;
                    break;
            }

        }
        printCommands();
    }

    public Object[] getAuctionData(String auctionID) throws InterruptedException {

        auctionatorLobby.put("online", auctionID, username);

        Object[] onlineResponse = auctionatorLobby.query(
                new ActualField("auction_"+auctionID),
                new ActualField(username),      // client name
                new FormalField(String.class),  // Auction title
                new FormalField(String.class),  // Auction starting price
                new FormalField(String.class),  // Current highest bid
                new FormalField(String.class),  // Timestamp
                new FormalField(String.class),  // Description
                new FormalField(String.class),  // Image url
                new FormalField(String.class)   // Auction owner
        );
        return onlineResponse;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    private class RunnableServerListener implements Runnable {

        String auctionID;
        public RunnableServerListener(String auctionID) {
            this.auctionID = auctionID;
        }

        public void run() {

            while (true) {
                try {
                    currentAuction = getAuctionData(auctionID);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
