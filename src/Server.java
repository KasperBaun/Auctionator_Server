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
    public String IpV4;
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

    public String getLocalMachineIPv4(){
        String localMachineIpV4;
        String port = "9001";
        try {
            localMachineIpV4 = "tcp://" + InetAddress.getLocalHost().getHostAddress() + ":" + port;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            // If it fails set variable to localhost
            localMachineIpV4 = "tcp://127.0.0.1:" + port;
        }
        this.IpV4 = localMachineIpV4;
        return localMachineIpV4;
    }

    public void fillServerWithDummyData() throws InterruptedException {
        auctionatorLobby.put(
                "create",
                "Simon Søhår",              // Username
                "Islandsk pony",             // Item name
                "50",                       // Start price
                "1",                       // End-time
                "Den islandske pony er efterkommer af de ponyer og heste, vikingerne havde med sig, da de bosatte sig på Island i niende og tiende århundrede. De medbragte heste var forskellige i udseende og farver, hvilket forklarer den store farvevariation i den islandske race.",               // Description
                "https://www.lundemoellen.dk/images/kr%C3%A6sen-hest2.jpg"
        );

        auctionatorLobby.put(
                "create",
                "Fridolski",              // Username
                "Min farfars gamle majspibe",             // Item name
                "650",                    // Start price
                "5",                     // End-time
                "En majspibe er en billig og naturlig pibe, hvor selve pibehovedet er lavet af majskolbe mens mundstykket er lavet af træ og plastik",               // Description
                "https://cdn.shopify.com/s/files/1/0082/0445/1903/products/OriginalMissouriMeerschaumCompanyCornCobPipe.jpg?v=1622039364"
        );

        auctionatorLobby.put(
                "create",
                "KænguruKris",              // Username
                "Østersmaske",             // Item name
                "319",                    // Start price
                "10",                     // End-time
                "This mask is made from natural latex. It is environmentally friendly and non-toxic.\n" +
                        "\n" +
                        "Also, it doesn't really look like an Oyster...",               // Description
                "http://www.weirdshityoucanbuy.com/uploads/7/0/8/8/70881739/oyster-mask_orig.jpg"
        );

        auctionatorLobby.put(
                "create",
                "Kasper Knæhopper",              // Username
                "Shark Cookie Mug",             // Item name
                "149",                    // Start price
                "15",                     // End-time
                "This is a handmade mug created in New Jersey studio with all lead free, eco friendly, non toxic materials- kiln fired twice to over 1900 degrees. Safe to place in the dishwasher and microwave.\n" +
                        "\n" +
                        "The front of the mug has a special compartment for cookies or biscuits. Those are chocolate chips pictured above. Where did they go? I will just say the shark did not eat them. Inside is a scuba diver, and the back reads LIVE EVERY WEEK LIKE IT'S SHARK WEEK.",               // Description
                "http://www.weirdshityoucanbuy.com/uploads/7/0/8/8/70881739/shark-week-cookie-dunk-mug_orig.jpg"
        );

        auctionatorLobby.put(
                "create",
                "Dennis Dingo",              // Username
                "Bog om porno for kvinder",             // Item name
                "69",                    // Start price
                "20",                     // End-time
                "Glem nøgenbilleder! Det her er virkelig sexet! Mænd, der laver mad, lytter til hvert et ord, gør rent i huset, spørger om vej og flere fantasier.",               // Description
                "https://cdn.shopify.com/s/files/1/0072/1432/products/hachette-chronicle-books-books-porn-for-women-gag-book-funny-gag-gifts-30385002971297_1800x1800.jpg?v=1628416905"
        );
    }
}





