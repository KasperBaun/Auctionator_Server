import org.jspace.SequentialSpace;
import org.jspace.SpaceRepository;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        SpaceRepository repository = new SpaceRepository();
        Server server = new Server(repository);

        // Keep serving requests to enter or create auctions
        while (true) {

            // auctionCount will be used to ensure every auction space has a unique name
            Integer auctionCount = 0;

            server.listenForRequestToJoinAuction();
            server.listenForRequestToCreateAuction();

        }
    }
}
