import org.jspace.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Server {
    public SpaceRepository repository;
    public SequentialSpace auctionatorLobby;
    public String IpV4;
    public String lobbyURI;
    public Integer auctionCount;

    // Server constructor takes repository as parameter to work on
    public Server(SpaceRepository repository) {

        this.repository = repository;
        SequentialSpace auctionatorLobby = new SequentialSpace();
        repository.add("lobby", auctionatorLobby);
        this.auctionatorLobby = auctionatorLobby;

        // Set the URI of the lobby space from LocalHost of machine
        // TODO : Maybe it is required to change this code when server is to be setup on the interwebz
        this.lobbyURI = getLocalMachineIPv4() + "/lobby";

        // Open a gate
        repository.addGate(lobbyURI + "/?keep");
        System.out.println("Opening repository gate at " + lobbyURI + "...");

        /*
        // This space holds room identifiers to port numbers (keep track of open auctions and their addresses (ports))
        SequentialSpace auctions = new SequentialSpace();
        this.auctions = auctions;
        */

        auctionCount = 0;
    }

   public void listenForRequestToJoinAuction() throws InterruptedException {
        String auctionURI = "null";

        // Read request to enter auction
        Object[] request = auctionatorLobby.get(new ActualField("enter"), new FormalField(String.class), new FormalField(String.class));
        String user = (String) request[1];
        String auctionId = (String) request[2];
        System.out.println(user + " requesting to enter " + auctionId + "...");

        // If auction exists prepare response with the corresponding URI
        Object[] the_auction = auctionatorLobby.queryp(new ActualField(auctionId), new FormalField(Integer.class));
        if (the_auction != null) {
            auctionURI = lobbyURI + "/auction/" + the_auction[1] + "?keep";
            // Sending response back to the chat client
            System.out.println("Telling " + user + " to go for auction " + auctionId + " at " + auctionURI + "...");
            auctionatorLobby.put("roomURI", user, auctionId, auctionURI);
        }
        // If the auction does not exist
        else {
            // Sending response back to the chat client with URI = null
            System.out.println("Telling " + user + " the requested auction does not exist");
            auctionatorLobby.put("roomURI", user, 0, "null");
        }
    }

    public void listenForRequestToCreateAuction () throws InterruptedException {

        // Read request to create auction
        Object[] createRequest = auctionatorLobby.get(
                new ActualField("create"),
                new FormalField(String.class),  // Username
                new FormalField(String.class),  // Item name
                new FormalField(Integer.class), // Start price
                new FormalField(String.class),  // End-date
                new FormalField(String.class),  // End-time
                new FormalField(String.class),  // Description
                new FormalField(String.class)   //
        );

        // Setup new thread with Auctioneer for handling the auction
        String auctionURI;
        String username = createRequest[1].toString();
        auctionURI = lobbyURI + "/auction" + auctionCount;

        new Thread(new Auctioneer(
                auctionCount.toString(),        // AuctionID
                repository,                     // SpaceRepository
                username,                       // Username
                createRequest[2].toString(),    // Item name
                (Integer)createRequest[3],      // Start-price
                createRequest[4].toString(),    // End-date
                createRequest[5].toString(),    // End-time
                createRequest[6].toString()     // Description
        )).start();
        this.auctionCount++;

        System.out.println("Creating auction " + auctionCount + " for " + username + " ...");
        System.out.println("Setting up auction space " + auctionURI + "...");

        // Sending response back to the chat client
        System.out.println("Telling " + username + " to go for auction " + auctionCount + " at " + auctionURI + "...");
        auctionatorLobby.put("auctionURI", username, auctionCount, auctionURI);
        System.out.println(username + " requesting to enter " + auctionURI + "...");
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
}





