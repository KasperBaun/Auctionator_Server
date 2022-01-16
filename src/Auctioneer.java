
import org.jspace.*;

import java.util.Collections;
import java.util.List;

public class Auctioneer implements Runnable{
    private Space           auctionLobby;
    private String          auctionID;
    private SpaceRepository repository;
    private String          auctionLobbyURI;
    private String          auctionOwner;
    private String          auctionName;
    private String          auctionStartPrice;
    private String          endTime;
    private String          auctionDescription;
    private Integer         highestBid;
    private String          highestBidUser;

    // Constructor
    public Auctioneer(
            String auctionID,
            SpaceRepository repository,
            String auctionLobbyURI,
            String auctionOwner,
            String auctionName,
            String auctionStartPrice,
            String endTime,
            String auctionDescription
    ){
        this.auctionID = auctionID;
        this.repository = repository;
        this.auctionLobbyURI = auctionLobbyURI;
        this.auctionOwner = auctionOwner;
        this.auctionName = auctionName;
        this.auctionStartPrice = auctionStartPrice;
        this.endTime = endTime;
        this.auctionDescription = auctionDescription;

        auctionLobby = new SequentialSpace();
        highestBid = 1;
        highestBidUser = "Kris";
        System.out.println("Auctioneer adding lobby : " + auctionLobbyURI + " to : " + repository.isEmpty());
        this.repository.add("auction"+auctionID, auctionLobby);
    }

    @Override
    public void run() {
        try {
            listenForNewBidders();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void startAuction(){

    }

    private void endAuction(){
        // fjern muligheden for at bidde
        // find ud af hvem der har vundet
        // annoncér vinder i "lobby"
        // lukke auktionen (fjerne spaces osv) - brugeren skal have besked om at de har vundet, pris, forsendelse/afhentning bla bla
    }

    private void listenForBids(){
        try {

            // Keep reading bids and printing them
            while (true) {
                Object[] newBid = auctionLobby.get(new ActualField("bid"),new FormalField(String.class), new FormalField(String.class));
                System.out.println("Auctioneer @auction " + auctionID + "Received bid from: " + newBid[1] + " @ " + newBid[0] + " DKK" );


                // Received new bid - tuple with new bid looks like this : tuple("bid", "500", "user")
                if (newBid != null){
                    // Check if bid > highestBid
                    if ((Integer)newBid[1]>highestBid){
                        //Update highest bid for all clients


                    }

                }
            }
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
            // New client connected - send initial auctiondata for new clients
            System.out.println("Auctioneer test - received newBidder connecting");
            sendData(newBidder[1].toString());
        }

    }

    private void sendData(String username) throws InterruptedException {
        auctionLobby.put(
                "initialdata",
                username,
                auctionName,        // Auction title
                auctionStartPrice,  // Auction starting price
                highestBid,         // Current highest bid
                endTime,            // Time remaining
                auctionDescription  // Description
        );
    }

    private void sendUpdatedHighestBid() throws InterruptedException {
        List<Object[]> onlineBidders = auctionLobby.queryAll(
                new ActualField("gjgjg"),
                new FormalField(String.class),
                new FormalField(String.class),
                new FormalField(String.class),
                new FormalField(String.class)
        );
        }
    }

