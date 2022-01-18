
import java.io.IOException;
import java.net.URISyntaxException;

public class RunServer {
    public static void main(String[] args) throws URISyntaxException, InterruptedException {

        Server server = new Server();
        server.fillServerWithDummyData();

        Thread t1 = new Thread(new RunServer().new RunnableServerListener(server));
        t1.start();

        Thread t2 = new Thread(new RunServer().new RunnableServerCreator(server));
        t2.start();

    }

    private class RunnableServerListener implements Runnable {

        Server server;
        public RunnableServerListener(Server server) {
            this.server = server;
        }

        public void run() {

            while (true) {
                try {
                    server.listenForRequestToJoinAuction();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class RunnableServerCreator implements Runnable {

        Server server;
        public RunnableServerCreator(Server server) {
            this.server = server;
        }

        public void run() {

            while (true) {
                try {
                    server.listenForRequestToCreateAuction();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
