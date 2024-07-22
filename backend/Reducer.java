import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/** Represents the Reducer Server
 * @author Velissaridis Yorgos - 3210255
 */
public class Reducer {

    ServerSocket reducerSocket;

    Socket workerConnection;

    static HashMap<String, Integer> currentlyActiveRequests = new HashMap<>();
    final static HashMap<String, ArrayList<Accommodation>> currentlyStoredAccommodations = new HashMap<>();

    /**
     * Creates a new Reducer Object and calls the .openServer() function on the newly created Object
     * @param args The command line arguments that can be passed to the main function (No arguments used)
     */
    public static void main(String[] args) {
        Reducer reducer = new Reducer();
        reducer.openServer();
    }

    /**
     * Opens a new ServerSocket to listen to incoming requests from other nodes. When a connection is established,
     * a new Thread is created that handles the specific connection accordingly
     */
    public void openServer() {
        try {
            reducerSocket = new ServerSocket(config.reducerPort);

            while (true) {
                workerConnection = reducerSocket.accept();

                Thread t = new ReducerHandlerThread(workerConnection);
                t.start();
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                reducerSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}