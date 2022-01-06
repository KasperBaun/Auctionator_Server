import org.jspace.QueueSpace;
import org.jspace.Space;

public class Auctioneer implements Runnable{
    private String auctionName;
    private String auctionDescription;
    private Integer auctionStartPrice;
    private String endDate;
    private String endTime;
    private Space lobby;
    private Space bids;
    private Integer highestBid;
    private String highestBidUser;

    public Auctioneer(String auctionName,
                      String auctionDescription,
                      Integer auctionStartPrice,
                      String endDate,
                      String endTime,
                      Space auctionLobby
    ){
        this.auctionName = auctionName;
        this.auctionDescription = auctionDescription;
        this.auctionStartPrice = auctionStartPrice;
        this.endDate = endDate;
        this.endTime = endTime;
        this.lobby = auctionLobby;

        bids = new QueueSpace();
        highestBid = 0;
        highestBidUser = "null";
    }

    @Override
    public void run() {
        // if current time = endTime -> endAuction
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
