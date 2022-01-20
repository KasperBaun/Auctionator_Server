import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import org.jspace.SpaceRepository;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

public class Server {
    public SpaceRepository repository;
    public SequentialSpace auctionatorLobby;
    public String lobbyURI;
    public Integer auctionCount;

    // Constructor
    public Server() throws URISyntaxException {

        auctionCount = 0;
        SpaceRepository repository = new SpaceRepository();
        this.repository = repository;
        SequentialSpace lobby = new SequentialSpace();
        repository.add("lobby", lobby);
        this.auctionatorLobby = lobby;

        // Set the URI of the lobby space
        String uri = "tcp://127.0.0.1:9001/?keep";

        // Open a gate
        URI myUri = new URI(uri);
        lobbyURI = "tcp://" + myUri.getHost() + ":" + myUri.getPort() +  "?keep" ;
        System.out.println("Opening repository gate at " + lobbyURI + "...\n");
        repository.addGate(lobbyURI);
    }

    public void listenForRequestToCreateAuction () throws InterruptedException {
        // Read request to create auction
        Object[] createRequest = auctionatorLobby.get(
                new ActualField("create"),
                new FormalField(String.class),      // Username
                new FormalField(String.class),      // Auction title
                new FormalField(String.class),      // Auction start-price
                new FormalField(String.class),      // End-time
                new FormalField(String.class),      // Description
                new FormalField(String.class)       // Image-URL
        );

        // Setup new thread with Auctioneer for handling the auction
        String username = createRequest[1].toString();
        String auctionTitle = createRequest[2].toString();
        String auctionPrice = createRequest[3].toString();
        String endTime = createRequest[4].toString();
        String description = createRequest[5].toString();
        String imageUrl = createRequest[6].toString();
        System.out.println("New auctionCreate request received from " + username);

        new Thread(new Auctioneer(
                auctionCount.toString(),        // AuctionID
                auctionatorLobby,
                username,                       // Username
                auctionTitle,                   // Auction title
                auctionPrice,                   // Auction price
                endTime,                        // End-time
                description,                    // Description
                imageUrl                        // Image-URL
        )).start();

        System.out.println("Creating auction " + auctionCount + " containing " + auctionTitle + " for " + username + " ...\n");

        // Increment auctionCount for next create
        this.auctionCount++;
    }

    public void fillServerWithDummyData() throws InterruptedException {
        auctionatorLobby.put(
                "create",
                "Simon Søhår",              // Username
                "Icelandic pony",             // Item name
                "500",                       // Start price
                "5",                       // End-time
                "The Icelandic pony is a descendant of the ponies and horses that the\n Vikings brought with them when they settled in Iceland in the ninth and tenth centuries.",               // Description
                "https://www.lundemoellen.dk/images/kr%C3%A6sen-hest2.jpg"
        );

        auctionatorLobby.put(
                "create",
                "Simon Hans Christian Fridolf Van Der Mark ",              // Username
                "My grandads old corn-pibe",             // Item name
                "150",                    // Start price
                "10",                     // End-time
                "A corn pipe is a cheap and natural pipe, where the pipe head itself is made of corn cob while the mouthpiece is made of wood and plastic",               // Description
                "https://norrebroskiosk.dk/wp-content/uploads/2018/03/majspiber-ny.jpeg"
        );

        auctionatorLobby.put(
                "create",
                "Kristoffer Von Schnitzel",              // Username
                "Sage Espresso Machine",             // Item name
                "2500",                    // Start price
                "10",                     // End-time
                "Selling my old sage espresso machine, it works fine and a good bag of coffee beans is included with quick trade",               // Description
                "https://royaldesign.se/image/1/sage-barista-pro-espressomaskin-1?w=168&quality=80"
        );

        auctionatorLobby.put(
                "create",
                "Kasper Kristian Kristoffer Van Der Ocean",              // Username
                "Cookie Mug",             // Item name
                "149",                    // Start price
                "15",                     // End-time
                "Great mug I bought on a charter vacation in florida. \n " +
                        "Coffee and cakes at your fingertips - what's not to like?",               // Description
                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQW4wH9Uf9z7qNn6yyZ4nzAt7PissvVQy9QUQ&usqp=CAU"
        );

        auctionatorLobby.put(
                "create",
                "Dennis Dingo",              // Username
                "My pet squirrel",             // Item name
                "85",                    // Start price
                "20",                     // End-time
                "Selling my pet squirl Niels because i am moving to Australia. \n" +
                        "He is soft, furry and quite the companion, " +
                        "however, he may well find himself biting you if you forget to give him nuts.",               // Description
                "https://styles.redditmedia.com/t5_3qzv06/styles/profileIcon_7zhvolvs8wq71.jpg?width=256&height=256&crop=256:256,smart&s=627e2372839bcbdf48a7dc00511202ae2e47001e"
        );
    }
}





