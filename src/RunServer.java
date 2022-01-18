
import java.io.IOException;
import java.net.URISyntaxException;

public class RunServer {
    public static void main(String[] args) throws URISyntaxException, InterruptedException {

        Server server = new Server();
        server.fillServerWithDummyData();
        while(true){
            server.listenForRequestToCreateAuction();
        }
    }
}
