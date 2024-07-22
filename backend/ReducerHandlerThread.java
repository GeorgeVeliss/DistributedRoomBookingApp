import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

/** Represents a Thread created by the Reducer Server to handle an established connection
 * @author Velissaridis Yorgos - 3210255
 */
public class ReducerHandlerThread extends Thread {

    Socket connection;
    int sender;
    MappedObject request;
    ObjectOutputStream out;
    ObjectInputStream in;

    /**
     * Creates a handling Thread by initializing I/O Stream for communicating with other endpoints
     * @param connection The Socket passed by the Reducer Server
     */
    public ReducerHandlerThread(Socket connection) {
        this.connection = connection;
    }

    /**
     * Checks where the connection was established from and handles the connection by utilizing the Map-Reduce Framework
     * to aggregate the results of queries coming from various nodes simultaneously. The aggregated result is sent back
     * be presented to the endpoint that sent the original request to the Master Server
     * *
     * The aggregation works through the utilization of the HashMap data structure that enables the grouping of several
     * queries coming from Worker nodes that refer to the same original user request, based off the user's username.
     * *
     * The storing of data inside the HashMap is synchronized in order to prevent multiple conflicting queries from
     * overwriting existing data
     */
    public void run() {

        try {
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            sender = in.read();

            // Master related functionality
            if (sender == 1) {

                this.setPriority(10);

                String username = in.readUTF();

                    if (!Reducer.currentlyStoredAccommodations.containsKey(username)) {
                        ArrayList<Accommodation> dummy_list = new ArrayList<>();

                        Reducer.currentlyStoredAccommodations.put(username, dummy_list);
                        Reducer.currentlyActiveRequests.put(username, 0);
                    }

                synchronized (Reducer.currentlyStoredAccommodations) {

                    while (Reducer.currentlyActiveRequests.get(username) != Master.numberOfWorkers) {
                        try {
                            Reducer.currentlyStoredAccommodations.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                ArrayList<Accommodation> rooms = Reducer.currentlyStoredAccommodations.get(username);

                out.writeObject(rooms);
                out.flush();

                Reducer.currentlyStoredAccommodations.remove(username);
                Reducer.currentlyActiveRequests.remove(username);
            }

            // Worker related functionality
            if (sender == 2) {

                request = (MappedObject) in.readObject();
                String username = request.getRequest().getUsername();

                    if (!Reducer.currentlyStoredAccommodations.containsKey(username)) {
                        ArrayList<Accommodation> dummy_list = new ArrayList<>();

                        Reducer.currentlyStoredAccommodations.put(username, dummy_list);
                        Reducer.currentlyActiveRequests.put(username, 0);
                    }

                synchronized (Reducer.currentlyStoredAccommodations) {

                    Reducer.currentlyActiveRequests.put(username, Reducer.currentlyActiveRequests.get(username) + 1);
                    Reducer.currentlyStoredAccommodations.get(username).addAll(request.getMappedRooms());
                    Reducer.currentlyStoredAccommodations.notifyAll();
                }

            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
