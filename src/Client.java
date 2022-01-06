
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Client {

    public static void main(String[] args) {

        try {

            // Set the URI of the loby of the chat server
            String uri = "tcp://"+ InetAddress.getLocalHost().getHostAddress()+"/lobby?keep";

            // Connect to the remote lobby
            System.out.println("Connecting to lobby " + uri + "...");
            RemoteSpace lobby = new RemoteSpace(uri);

            // Read user name from the console
            System.out.print("Enter your username: ");
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            String username = input.readLine();

            // Read chatroom from the console
            System.out.print("Select an auction: (E.g.: 10)");
            System.out.print("Auction #10 - Arne Jacobsen Stol");
            System.out.print("Description: Sort, brugt - god stand - tidligere ejet af Søborg");
            System.out.print("Current highest bid: 500");

            String auctionNumber = input.readLine();

            // Send request to enter chatroom
            lobby.put("enter",username,auctionNumber);

            // Get response with chatroom URI
            Object[] response = lobby.get(new ActualField("roomURI"), new ActualField(username), new ActualField(auctionNumber), new FormalField(String.class));
            String auctionSpace_uri = (String) response[3];
            System.out.println("Connecting to auction #" + auctionNumber);
            RemoteSpace auctionroom_space = new RemoteSpace(auctionSpace_uri);

            // Keep sending whatever the user types
            System.out.println("Start bidding... (Increments of minimum 10 above highest bid)");
            while(true) {
                String message = input.readLine();
                auctionroom_space.put(username, message);
            }


        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
