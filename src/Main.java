import org.jspace.SpaceRepository;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        SpaceRepository repository = new SpaceRepository();
        Server server = new Server(repository);

        // Keep serving requests to enter or create auctions
        while (true) {

            server.listenForRequestToCreateAuction();
            server.listenForRequestToJoinAuction();

        }
    }
}
