
import org.jspace.*;

public class Auctioneer implements Runnable{
    private SpaceRepository repository;
    private Space           auctionLobby;
    private String          auctionID;
    private String          auctionLobbyURI;
    private String          auctionOwner;
    private String          auctionName;
    private String          auctionStartPrice;
    private String timeRemaining;
    private String          auctionDescription;
    private String          highestBidUser;
    private String          imageURL;
    private Integer         highestBid;
    private Boolean         auctionLive;

    // Constructor
    public Auctioneer(
            String auctionID,
            SpaceRepository repository,
            String auctionLobbyURI,
            String auctionOwner,
            String auctionName,
            String auctionStartPrice,
            String endTime,
            String auctionDescription,
            String imageURL
    ){
        this.auctionID = auctionID;
        this.repository = repository;
        this.auctionLobbyURI = auctionLobbyURI;
        this.auctionOwner = auctionOwner;
        this.auctionName = auctionName;
        this.auctionStartPrice = auctionStartPrice;
        this.timeRemaining = endTime;
        this.auctionDescription = auctionDescription;
        this.imageURL = imageURL;

        auctionLobby = new SequentialSpace();
        highestBid = 1;
        highestBidUser = "Kris";
        auctionLive = true;
        System.out.println("Auctioneer adding lobby : " + auctionLobbyURI );
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
        auctionLive = false;
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
            String newBidderName = newBidder[1].toString();
            // New client connected - send initial auctiondata for new clients
            System.out.println("Auctioneer @auction" + auctionID+ " received new bidder" + newBidderName + "connecting");
            sendData(newBidderName);
        }
    }

    private void sendData(String username) throws InterruptedException {
        System.out.println("Auctioneer @auction" + auctionID+ " sending auction data to" + username );
        auctionLobby.put(
                "initialdata",
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

}

