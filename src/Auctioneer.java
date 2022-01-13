
import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import org.jspace.Space;
import org.jspace.SpaceRepository;

public class Auctioneer implements Runnable{
    private Space           auctionLobby;
    private String          auctionID;
    private SpaceRepository repository;
    private String          auctionLobbyURI;
    private String          auctionOwner;
    private String          auctionName;
    private String         auctionStartPrice;
    private String          endTime;
    private String          auctionDescription;
    private Integer         highestBid;
    private String          highestBidUser;

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
        highestBid = 0;
        highestBidUser = "null";
        repository.add(auctionLobbyURI, auctionLobby);
    }

    @Override
    public void run() {
        // if current time = endTime -> endAuction
        listenForBids();
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
                Object[] newBid = auctionLobby.get(new FormalField(Integer.class), new FormalField(String.class));
                System.out.println("Received bid from: " + newBid[1] + " @ " + newBid[0] + " DKK" );
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
