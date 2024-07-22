import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/** Represents the Worker Server
 * @author Velissaridis Yorgos - 3210255
 */
public class Worker {

    // Server Socket for Worker Server
    ServerSocket workerSocket;
    ArrayList<Accommodation> rooms = new ArrayList<>();
    int workerPort;

    /**
     * Creates a new Master Object and calls the .openNetwork() function on the newly created Object
     * @param args The Worker's port is passed as a command line argument to the function
     */
    public static void main(String[] args) throws IOException {
        Worker worker = new Worker();
        worker.workerPort = Integer.parseInt(args[0]);
        worker.openServer();

    }

    /**
     * Opens a new ServerSocket to listen to incoming requests from other servers and clients
     * when a connection is established, a new Thread is created that handles the specific connection
     */
    private void openServer() throws IOException {
        workerSocket = new ServerSocket(this.workerPort);
        System.out.println(workerPort);

        try {
            while (true) {
                Socket masterConnection = workerSocket.accept();

                Thread t = new WorkerHandlerThread(this, masterConnection);
                t.start();
            }
        } catch(IOException ioException)
        {
            ioException.printStackTrace();
        }
        finally
        {
            try {
                workerSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
