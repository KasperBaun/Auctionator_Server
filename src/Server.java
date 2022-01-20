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
                "5",                       // End-time
                "Den islandske pony er efterkommer af de ponyer og heste, vikingerne havde med sig, da de bosatte sig på Island i niende og tiende århundrede. De medbragte heste var forskellige i udseende og farver, hvilket forklarer den store farvevariation i den islandske race.",               // Description
                "https://www.lundemoellen.dk/images/kr%C3%A6sen-hest2.jpg"
        );

        auctionatorLobby.put(
                "create",
                "Simon Hans Christian Fridolf Van Der Mark ",              // Username
                "Min farfars gamle majspibe",             // Item name
                "150",                    // Start price
                "10",                     // End-time
                "En majspibe er en billig og naturlig pibe, hvor selve pibehovedet er lavet af majskolbe mens mundstykket er lavet af træ og plastik",               // Description
                "https://norrebroskiosk.dk/wp-content/uploads/2018/03/majspiber-ny.jpeg"
        );

        auctionatorLobby.put(
                "create",
                "Kristoffer Von Schnitzel",              // Username
                "Sage Espresso Maskine",             // Item name
                "2500",                    // Start price
                "10",                     // End-time
                "Sælger min gamle sage espressomaskine, den virker fint og der medfølger en god pose kaffebønner ved hurtig handel",               // Description
                "https://royaldesign.se/image/1/sage-barista-pro-espressomaskin-1?w=168&quality=80"
        );

        auctionatorLobby.put(
                "create",
                "Kasper Kristian Kristoffer Van Der Ocean",              // Username
                "Shark Cookie Mug",             // Item name
                "149",                    // Start price
                "15",                     // End-time
                "Fantastisk krus jeg købte på en charterferie i florida. Kaffe og kager lige ved hånden - what's not to like?",               // Description
                "http://www.weirdshityoucanbuy.com/uploads/7/0/8/8/70881739/shark-week-cookie-dunk-mug_orig.jpg"
        );

        auctionatorLobby.put(
                "create",
                "Dennis Dingo",              // Username
                "Mit Kæle-egern Niels",             // Item name
                "85",                    // Start price
                "20",                     // End-time
                "Sælger mit kæle-egern Niels da jeg skal flytte til Australien. Han er blød, sød og selskabelig, dog kan han godt finde på  at bide hvis du glemmer at give ham nødder.",               // Description
                "https://styles.redditmedia.com/t5_3qzv06/styles/profileIcon_7zhvolvs8wq71.jpg?width=256&height=256&crop=256:256,smart&s=627e2372839bcbdf48a7dc00511202ae2e47001e"
        );
    }
}





