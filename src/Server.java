import org.jspace.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

public class Server {
    public SpaceRepository repository;
    public SequentialSpace auctionatorLobby;
    public String IpV4;
    public String lobbyURI;
    public String auctionBaseURI;
    public Integer auctionCount;

    // Constructor
    public Server() throws URISyntaxException {

        auctionCount = 0;
        SpaceRepository repository = new SpaceRepository();
        this.repository = repository;

        SequentialSpace lobby = new SequentialSpace();
        repository.add("lobby", lobby);
        this.auctionatorLobby = lobby;

        //BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        // Set the URI of the lobby space
        // Default value
        String uri = "tcp://127.0.0.1:9001/?keep";

        // Open a gate
        URI myUri = new URI(uri);
        auctionBaseURI = "tcp://" + myUri.getHost() + ":" + myUri.getPort();
        String gateUri = "tcp://" + myUri.getHost() + ":" + myUri.getPort() +  "?keep" ;
        System.out.println("Opening repository gate at " + gateUri + "...");
        repository.addGate(gateUri);
        this.lobbyURI = gateUri;
    }

    public void readMessage() throws InterruptedException {

        // Read request to enter auction
        Object[] request = this.auctionatorLobby.get(new FormalField(String.class), new FormalField(String.class));
        System.out.println(request[0].toString() + request[1].toString());

    }

    public void listenForRequestToJoinAuction() throws InterruptedException {
        String auctionURI;

        // Read request to enter auction
        Object[] request = auctionatorLobby.get(new ActualField("enter"), new FormalField(String.class), new FormalField(String.class));
        String user = (String) request[1];
        String auctionId = (String) request[2];
        System.out.println(user + " requesting to enter " + auctionId + "...");

        // If auction exists prepare response with the corresponding URI
        Object[] the_auction = auctionatorLobby.queryp(new ActualField(auctionId), new FormalField(String.class));
        if (the_auction != null) {
            auctionURI = the_auction[1] + "?keep";
            // Sending response back to the chat client
            System.out.println("Telling " + user + " to go for auction " + auctionId + " at " + auctionURI + "...");
            auctionatorLobby.put("roomURI", user, auctionId, auctionURI);
        }
        // If the auction does not exist
        else {
            // Sending response back to the chat client with URI = null
            System.out.println("Telling " + user + " the requested auction " + auctionId + " does not exist");
            auctionatorLobby.put("roomURI", user, 0, "null");
        }
    }

    public void listenForRequestToCreateAuction () throws InterruptedException {
        // Read request to create auction
        Object[] createRequest = auctionatorLobby.get(
                new ActualField("create"),
                new FormalField(String.class),  // Username
                new FormalField(String.class),  // Item name
                new FormalField(String.class), // Start price
                //new FormalField(String.class),  // End-date
                new FormalField(String.class),  // End-time
                new FormalField(String.class)   // Description
        );

        // Setup new thread with Auctioneer for handling the auction
        String auctionURI;
        String username = createRequest[1].toString();
        String auctionName = createRequest[2].toString();
        String endTime = createRequest[4].toString();
        auctionURI = auctionBaseURI + "/auction" + auctionCount + "?keep";
        System.out.println("New auctionCreate request received from " + username);

        new Thread(new Auctioneer(
                auctionCount.toString(),        // AuctionID
                repository,                     // SpaceRepository
                auctionURI,                     // The URI to the newly created space for the auction
                username,                       // Username
                auctionName,                    // Item name
                createRequest[3].toString(),    // Start-price
                createRequest[4].toString(),    // End-time
                endTime                         // Description
        )).start();

        System.out.println("Creating auction " + auctionCount + " containing " + auctionName + " for " + username + " ...");
        System.out.println("Setting up auction space " + auctionURI + "...");

        /* Sending response back to the chat client */
        System.out.println("Telling " + username + " to go for auction " + auctionCount + " at " + auctionURI + "...");
        System.out.println("");
        // Tuple for user requesting to create auction
        auctionatorLobby.put("auctionURI", username, auctionCount.toString(), auctionURI);

        // Tuple for other users wishing to join
        auctionatorLobby.put("auction", auctionCount.toString(), auctionName, endTime, auctionURI);
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
}





