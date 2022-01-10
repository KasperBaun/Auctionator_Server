
import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import org.jspace.Space;
import org.jspace.SpaceRepository;

public class Auctioneer implements Runnable{
    private Space           auction;
    private String          auctionID;
    private SpaceRepository repository;
    private String          auctionOwner;
    private String          auctionName;
    private Integer         auctionStartPrice;
    private String          endDate;
    private String          endTime;
    private String          auctionDescription;
    private Integer         highestBid;
    private String          highestBidUser;

    public Auctioneer(
            String auctionID,
            SpaceRepository repository,
            String auctionOwner,
            String auctionName,
            Integer auctionStartPrice,
            String endDate,
            String endTime,
            String auctionDescription
    ){
        this.auctionID = auctionID;
        this.repository = repository;
        this.auctionOwner = auctionOwner;
        this.auctionName = auctionName;
        this.auctionStartPrice = auctionStartPrice;
        this.endDate = endDate;
        this.endTime = endTime;
        this.auctionDescription = auctionDescription;

        auction = new SequentialSpace();
        highestBid = 0;
        highestBidUser = "null";
        repository.add("auction/"+this.auctionID, auction);
    }

    @Override
    public void run() {
        // if current time = endTime -> endAuction
        try {

            // Keep reading chat messages and printing them
            while (true) {
                Object[] newBid = auction.get(new FormalField(Integer.class), new FormalField(String.class));
                System.out.println("Received bid from: " + newBid[1] + " @ " + newBid[0] + " DKK" );
            }
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
}
