import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Auctioneer implements Runnable{
    private final Space           auctionLobby;
    private final String          auctionID;
    private final String          auctionCreator;
    private final String          auctionTitle;
    private final String          auctionStartPrice;
    private final String          auctionDescription;
    private String          highestBidUser;
    private final String          imageURL;
    private Integer         onlineClients;
    private Integer         highestBid;
    private Integer         timeRemaining;
    private String          timeStamp;
    private final Thread          bidListenerThread;
    private final Thread          clientListenerThread;

    // Constructor
    public Auctioneer(
            String auctionID,
            Space lobby,
            String auctionOwner,
            String auctionTitle,
            String auctionStartPrice,
            String timeRemaining,
            String auctionDescription,
            String imageURL
    ) throws InterruptedException
    {
        this.auctionLobby = lobby;
        this.auctionID = auctionID;
        this.auctionCreator = auctionOwner;
        this.auctionTitle = auctionTitle;
        this.auctionStartPrice = auctionStartPrice;
        this.timeRemaining = Integer.parseInt(timeRemaining)*60;
        this.auctionDescription = auctionDescription;
        this.imageURL = imageURL;
        bidListenerThread = new Thread(new RunnableAuctioneerBidListener(this));
        clientListenerThread = new Thread(new RunnableAuctioneerClientListener(this));
        handleDateTime();
        highestBid = 0;
        highestBidUser = "null";
        putAuctionInfoInLobby();
    }

    @Override
    public void run() {
        startAuction();
        bidListenerThread.start();
        clientListenerThread.start();
    }

    private void startAuction(){
        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        final Runnable runnable = new Runnable() {
            public void run() {
                timeRemaining--;

                if (timeRemaining <= 0) {
                    try {
                        endAuction();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    scheduler.shutdown();
                }
            }
        };
        scheduler.scheduleAtFixedRate(runnable, 0, 1, SECONDS);
    }

    private void endAuction() throws InterruptedException {

        if (highestBidUser.equals("null") && highestBid ==0){
            System.out.println("Auctioneer @auction" + auctionID + " no bidders has placed a bid on this auction " );
        } else {
            System.out.println("Auctioneer @auction" + auctionID + " the winner of the auction is " + highestBidUser +  " with the winning bid of " + highestBid );
        }
        System.out.println("Auctioneer @auction" + auctionID + " Time is up!" );
        System.out.println("Auctioneer @auction" + auctionID + " is closing the auction" );

        // Consume all tokens
        consumeAuctionInfoInLobby();

        // Consume all specific user tokens
        consumeSpecificAuctionInfoInLobby();
        // Close threads
        bidListenerThread.interrupt();
        clientListenerThread.interrupt();
    }

    private void handleDateTime(){
        Calendar initialDate = Calendar.getInstance();                  // Current DateTime
        initialDate.setTimeZone(TimeZone.getTimeZone("GMT+1"));         // Set TimeZone
        long timeInSecs = initialDate.getTimeInMillis();                // Convert to seconds
        Date EndTime = new Date(timeInSecs + (timeRemaining * 1000));   // Set new Date Time + timeRemaining

        Timestamp ts = new Timestamp(EndTime.getTime());
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        timeStamp = formatter.format(ts);
        System.out.println("End of Auction: " + timeStamp);
    }

    private void updateOnlineBidders() throws InterruptedException {
        // Get list of online-clients
        List<Object[]> clientList = auctionLobby.queryAll(
                new ActualField("online"),
                new ActualField(auctionID),
                new FormalField(String.class)   // Username
        );
        this.onlineClients = clientList.size();

        List<Object[]> oldTokens = auctionLobby.getAll(
                new ActualField("onlineclients"),
                new FormalField(String.class)
        );

        auctionLobby.put("onlineclients",onlineClients.toString());
    }

    private void listenForBids() throws InterruptedException {

                // Keep reading bids and printing them
                Object[] newBid = auctionLobby.get(
                        new ActualField("bid"),
                        new ActualField(auctionID),     // Auction ID
                        new FormalField(String.class),  // Bid
                        new FormalField(String.class)   // Username
                );
                int bid = Integer.parseInt(newBid[2].toString());
                String username = newBid[3].toString();
                System.out.println("Auctioneer @auction" + auctionID + " received bid " + bid + " from: " + username);


                if (bid > Integer.parseInt(auctionStartPrice) && bid > highestBid) {
                    //Update the highest bid for all clients
                    highestBid = bid;
                    highestBidUser = username;
                    System.out.println("Auctioneer @auction" + auctionID + " highest bid updated " + highestBid);
                    updateHighestBid();
                }
    }

    private void listenForNewBidders() throws InterruptedException {
            Object[] newBidder = auctionLobby.get(
                    new ActualField("hello"),
                    new ActualField(auctionID),
                    new FormalField(String.class) // Expecting username
            );

            if (newBidder != null) {
                String newBidderName = newBidder[2].toString();
                // New client connected - send initial auctiondata for new clients
                System.out.println("Auctioneer @auction" + auctionID + " received new bidder " + newBidderName + " connecting");
                sendData(newBidderName);
            }
    }

    private void sendData(String username) throws InterruptedException {
        System.out.println("Auctioneer @auction" + auctionID+ " sending auction data to " + username );
        auctionLobby.put(
                "auction_"+auctionID,
                username,
                auctionTitle,
                auctionStartPrice,
                highestBid.toString(),
                timeStamp,
                auctionDescription,
                imageURL,
                auctionCreator
        );
    }

    private void updateHighestBid() throws InterruptedException {
        consumeAuctionInfoInLobby();
        putAuctionInfoInLobby();

        // Get list of online-clients
        List<Object[]> onlineClients = auctionLobby.queryAll(
                new ActualField("online"),
                new ActualField(auctionID),
                new FormalField(String.class)   // Username
        );

        consumeSpecificAuctionInfoInLobby();

        // Send new data for each online client
        if (onlineClients != null){
            for (Object[] client : onlineClients) {
                sendData(client[2].toString());
            }
        }
        System.out.println("Auctioneer @auction" + auctionID +  " sending updated highest bid for all clients " );
        updateOnlineBidders();
    }

    private void putAuctionInfoInLobby() throws InterruptedException {
        auctionLobby.put(
                "auction",
                auctionID,
                auctionTitle,
                auctionStartPrice,
                highestBid.toString(),
                timeStamp,
                auctionDescription,
                imageURL,
                auctionCreator
        );
    }

    private void consumeAuctionInfoInLobby() throws InterruptedException {
        List<Object[]> cleanTokens = auctionLobby.getAll(
                new ActualField("auction"),
                new ActualField(auctionID),         // Auction ID
                new FormalField(String.class),      // Auction title
                new FormalField(String.class),      // Auction start price
                new FormalField(String.class),      // Highest bid currently
                new FormalField(String.class),      // Timestamp
                new FormalField(String.class),      // Description
                new FormalField(String.class),      // Image URL
                new FormalField(String.class)       // Auction creator
        );
    }

    private void consumeSpecificAuctionInfoInLobby() throws InterruptedException {
        List<Object[]> cleanTokens = auctionLobby.getAll(
                new ActualField("auction_"+auctionID),
                new FormalField(String.class),  // client name
                new FormalField(String.class),  // Auction title
                new FormalField(String.class),  // Auction starting price
                new FormalField(String.class),  // Current highest bid
                new FormalField(String.class),  // Timestamp
                new FormalField(String.class),  // Description
                new FormalField(String.class),  // Image url
                new FormalField(String.class)   // Auction owner
        );
    }

    private static class RunnableAuctioneerBidListener implements Runnable {

        Auctioneer auctioneer;
        public RunnableAuctioneerBidListener(Auctioneer auctioneer) {
            this.auctioneer = auctioneer;
        }

        public void run() {
            while(true){
                try {
                    auctioneer.listenForBids();
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                }
            }
        }
    }

    private static class RunnableAuctioneerClientListener implements Runnable {

        Auctioneer auctioneer;
        public RunnableAuctioneerClientListener(Auctioneer auctioneer) {
            this.auctioneer = auctioneer;
        }

        public void run() {
            while(true) {
                try {
                    auctioneer.updateOnlineBidders();
                    auctioneer.listenForNewBidders();
                    auctioneer.updateOnlineBidders();

                } catch (InterruptedException e) {
                    //e.printStackTrace();
                    }
            }
        }
    }
}



