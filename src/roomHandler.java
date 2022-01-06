import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import org.jspace.Space;
import org.jspace.SpaceRepository;

class roomHandler implements Runnable {

    private Space newAuction;
    private String roomID;
    private String spaceID;

    public roomHandler(String roomID, String spaceID, String uri, SpaceRepository repository) {

        this.roomID = roomID;
        this.spaceID = spaceID;

        // Create a local space for the chatroom
        newAuction = new SequentialSpace();

        // Add the space to the repository
        repository.add(this.spaceID, newAuction);
    }

    @Override
    public void run() {
        try {

            // Keep reading chat messages and printing them
            while (true) {
                Object[] message = newAuction.get(new FormalField(String.class), new FormalField(String.class));
                System.out.println("ROOM " + roomID + " | " + message[0] + ":" + message[1]);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}
