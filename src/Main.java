import org.jspace.SpaceRepository;

import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) throws URISyntaxException {

        Server server = new Server();

        Thread t1 = new Thread(new Main().new RunnableServerListener(server));
        Thread t2 = new Thread(new Main().new RunnableServerCreator(server));
        t1.start();
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
