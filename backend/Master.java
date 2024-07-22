import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

/** Represents the Master Server
 * @author Velissaridis Yorgos - 3210255
 */
public class Master {

    // pass as argument at the end
    public static int numberOfWorkers = config.workerPorts.length;

    ServerSocket masterSocket;

    static HashMap<String, Integer> countPerArea = new HashMap<>();

    /**
     * Creates a new Master Object and calls the .openNetwork() function on the newly created Object
     * @param args The command line arguments that can be passed to the main function (No arguments used)
     */
    public static void main(String[] args) throws IOException {

        Master master = new Master();
        master.openNetwork();
    }

    /**
     * Opens a new ServerSocket to listen to incoming requests from other servers and clients
     * when a connection is established, a new Thread is created that handles the specific connection
     */
    private void openNetwork() {

        try {
            masterSocket = new ServerSocket(config.masterPort);

            try {
                while (true) {
                    Socket masterConnection = masterSocket.accept();

                    Thread t = new MasterHandlerThread(masterConnection);
                    t.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            System.out.println("I/O error: " + e);
        }
    }

    /**
     * Parses a .json file that is given by the manager's terminal (documentation @ ManagerConsoleApp.java)
     * @param path The path to the .json file
     * @param manager The username of the manager that inputted the .json file
     * @return An Array List of Accommodation Objects where its Object represents a room in the system
     */
    static ArrayList<Accommodation> readJson(String path, String manager)
    {
        ArrayList<Accommodation> list = new ArrayList<Accommodation>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            //Read and edit each line separately
            String line;


            while ((line = br.readLine()) != null) {

                //Remove unnecessary characters/strings/spaces
                line = line.replace("{", "");
                line = line.replace("}", "");

                line = line.replace("\"roomName\": ", "");
                line = line.replace("\"noOfPersons\": ", "");
                line = line.replace("\"area\": ", "");
                line = line.replace("\"stars\": ", "");
                line = line.replace("\"noOfReviews\": ", "");
                line = line.replace("\"roomImage\": ", "");


                line = line.replace("\"dates\":", "");
                line = line.replace("\"price\":", "");
                line = line.replace("\"", "");

                String[] parts = line.split(",");

                //Add new accomodation item to our accomodations list
                list.add(new Accommodation(parts[0],parts[1].replace(" ", ""),parts[2],parts[3].replace(" ", ""),parts[4].replace(" ", ""),parts[5].replace(" ", ""),parts[6].replace(" ", ""),parts[7], manager));//,parts[8]));


            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Creates a hash code based on the room's name, used to distribute rooms created by the readJson() function
     * @param room_name The name of the room
     * @return An integer representing a hash code
     */
    public static int Hash(String room_name) {
        return Math.abs(room_name.hashCode() % numberOfWorkers);
    }

    /**
     * Takes in as input an string representing a date range, formatted in a specific way, 
     * and transforms it into a list of strings, each representing one specific date of the date range.
     * @param range A range of dates
     * @return A List of Strings where its String represents a date
     */
    static List<String> string_to_Dates(String range) {

        List<String> dates = new ArrayList<>();
        LocalDate start = null;
        LocalDate end = null;
        String[] parts;

        //if a date range is given/ else if a single date is given
        if (range.contains("/")) {
            parts = range.split("/");
            start = LocalDate.parse(parts[0], DateTimeFormatter.ofPattern("yyyy-d-M"));
            end = LocalDate.parse(parts[1], DateTimeFormatter.ofPattern("yyyy-d-M"));

        }else
        {
            start = LocalDate.parse(range, DateTimeFormatter.ofPattern("yyyy-d-M"));
            end = LocalDate.parse(range, DateTimeFormatter.ofPattern("yyyy-d-M"));
        }


        Stream.iterate(start, date -> date.plusDays(1))
                .limit(ChronoUnit.DAYS.between(start, end) + 1)
                .forEach(item -> {
                    dates.add(item.toString());

                });

        return dates;
    }
}
