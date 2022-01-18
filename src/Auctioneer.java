
import org.jspace.*;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.SECONDS;

public class Auctioneer implements Runnable{
    private SpaceRepository repository;
    private Space           auctionLobby;
    private String          auctionID;
    private String          auctionLobbyURI;
    private String          auctionOwner;
    private String          auctionName;
    private String          auctionStartPrice;
    private String          auctionDescription;
    private String          highestBidUser;
    private String          imageURL;
    private Integer         highestBid;
    private Integer         timeRemaining;
    private Boolean         auctionIsLive;
    private String          timeStamp;

    // Constructor
    public Auctioneer(
            String auctionID,
            SpaceRepository repository,
            String auctionLobbyURI,
            String auctionOwner,
            String auctionName,
            String auctionStartPrice,
            String timeRemaining,
            String auctionDescription,
            String imageURL
    ){
        this.auctionID = auctionID;
        this.repository = repository;
        this.auctionLobbyURI = auctionLobbyURI;
        this.auctionOwner = auctionOwner;
        this.auctionName = auctionName;
        this.auctionStartPrice = auctionStartPrice;
        this.timeRemaining = Integer.parseInt(timeRemaining)*60;
        this.auctionDescription = auctionDescription;
        this.imageURL = imageURL;

        handleDateTime();
        auctionIsLive = true;
        auctionLobby = new SequentialSpace();
        highestBid = 0;
        highestBidUser = "null";
        System.out.println("Auctioneer adding auctionSpace : " + auctionLobbyURI );
        this.repository.add("auction"+auctionID, auctionLobby);
    }

    @Override
    public void run() {

        // Start auction counter
        Thread t0 = new Thread(new RunnableAuctioneerAuctionCounter(this));
        t0.start();

        // Listen for new clients
        Thread t1 = new Thread(new RunnableAuctioneerClientListener(this));
        t1.start();

        // Listen for new bids
        Thread t2 = new Thread(new RunnableAuctioneerBidListener(this));
        t2.start();

    }

    private void startAuction(){
        countdown();
    }

    private void countdown(){
        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        final Runnable runnable = new Runnable() {

            public void run() {

                //System.out.println(auctionName + timeRemaining);
                timeRemaining--;

                if (timeRemaining < 0) {
                    endAuction();
                    scheduler.shutdown();
                }
            }
        };
        scheduler.scheduleAtFixedRate(runnable, 0, 1, SECONDS);
    }

    private void endAuction(){
        auctionIsLive = false;
        // Announce winner
        // lukke auktionen (fjerne spaces osv) - brugeren skal have besked om at de har vundet, pris, forsendelse/afhentning bla bla
    }

    private void handleDateTime(){
        Date date = new Date();
        Timestamp ts = new Timestamp(date.setTime(date.getTime()+timeRemaining));
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+1"));
        timeStamp = formatter.format(ts);
        System.out.println(timeStamp);
    }

    private void updateOnlineclients(){

    }

    private void listenForBids(){
        try {

            // Keep reading bids and printing them
            while (auctionIsLive) {
                Object[] newBid = auctionLobby.get(new ActualField("bid"),new FormalField(String.class), new FormalField(String.class));
                int bid = (Integer)newBid[1];
                String username = newBid[2].toString();
                System.out.println("Auctioneer @auction " + auctionID + "Received bid from: " + username + " @ " + bid + " USD" );


                // Received new bid - tuple with new bid looks like this : tuple("bid", "500", "user")
                if (bid != 0){
                    // Check if bid > highestBid
                    if (bid>highestBid){
                        //Update highest bid for all clients
                        highestBid = bid;
                        updateHighestBid();
                    }
                }
            }
            System.out.println("Auctioneer @auction " + auctionID + "is closing the auction" );
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void listenForNewBidders() throws InterruptedException {
        Object[] newBidder = auctionLobby.get(
                new ActualField("hello"),
                new FormalField(String.class) // Expecting username
        );

        if (newBidder != null){
            String newBidderName = newBidder[1].toString();
            // New client connected - send initial auctiondata for new clients
            System.out.println("Auctioneer @auction" + auctionID+ " received new bidder " + newBidderName + " connecting");
            sendData(newBidderName);
        }
    }

    private void sendData(String username) throws InterruptedException {
        System.out.println("Auctioneer @auction" + auctionID+ " sending auction data to " + username );
        auctionLobby.put(
                "auctiondata",
                username,
                auctionName,
                auctionStartPrice,
                highestBid.toString(),
                timeRemaining,
                auctionDescription,
                imageURL,
                auctionOwner
        );
    }

    private void updateHighestBid() throws InterruptedException {

        // Get list of online-clients
        List<Object[]> onlineClients = auctionLobby.getAll(
                new ActualField("online"),
                new FormalField(String.class)   // Username
        );

        // Send new data for each online client
        if (onlineClients != null){
            for (Object[] client : onlineClients) {
                sendData(client[1].toString());
            }
        }
    }

    private static class RunnableAuctioneerBidListener implements Runnable {

        Auctioneer auctioneer;
        public RunnableAuctioneerBidListener(Auctioneer auctioneer) {
            this.auctioneer = auctioneer;
        }

        public void run() {
                auctioneer.listenForBids();
        }
    }

    private static class RunnableAuctioneerClientListener implements Runnable {

        Auctioneer auctioneer;
        public RunnableAuctioneerClientListener(Auctioneer auctioneer) {
            this.auctioneer = auctioneer;
        }

        public void run() {
            try {
                auctioneer.listenForNewBidders();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static class RunnableAuctioneerAuctionCounter implements Runnable {

        Auctioneer auctioneer;
        public RunnableAuctioneerAuctionCounter(Auctioneer auctioneer) {
            this.auctioneer = auctioneer;
        }

        public void run() {
            auctioneer.startAuction();
        }
    }
}



