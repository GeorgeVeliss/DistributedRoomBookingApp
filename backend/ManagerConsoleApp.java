import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

/** Represents a client terminal that holds the manager functionality of the application
 * @author Velissaridis Yorgos - 3210255
 */
public class ManagerConsoleApp {

    ObjectOutputStream out;
    ObjectInputStream in;
    Socket masterConnection;

    /**
     * Creates a new ManagerConsoleApp Object and calls the .run() function on the newly created Object
     * @param args The command line arguments that can be passed to the main function (No arguments used)
     */
    public static void main(String[] args) throws IOException {
        ManagerConsoleApp console = new ManagerConsoleApp();
        console.run();
    }

    /**
     * Displays a menu and prompts the user to choose an operation from the following operations:
     * Add rooms        : given a .json file from the manager, a single or several Accommodation Objects are created
     * Add dates        : a single date or a range of dates (given by the manager) are added to a specific Accommodation
     * Room bookings    : displays information regarding bookings for every room that belongs to the manager
     * Total bookings   : displays the total number of current bookings for all rooms present in the system given a
     *                    specific date range by the manager, categorizing the results based on the area
     */
    public void run() {

        try{
            Scanner scanner = new Scanner(System.in);

            System.out.println("Welcome to Manager Console App.\nEnter username: ");
            String username = scanner.nextLine();

            // ManagerConsoleApp menu
            while (true) {
                System.out.println("Enter 1 to add rooms (from file).");
                System.out.println("Enter 2 to add available dates for one of your rooms.");
                System.out.println("Enter 3 to view the bookings for your rooms.");
                System.out.println("Enter 4 to view total number of bookings per area for a specific time period.");
                System.out.println("Enter anything else to exit.");

                Integer choice = 0;

                try {
                    choice = Integer.parseInt(scanner.nextLine());
                }
                catch (NumberFormatException e) {
                    System.out.println("Exiting console...");
                    System.exit(0);
                };

                // Read from JSON file
                if (choice == 1) {
                    System.out.println("Enter the path of the file that contains your rooms' information: ");
                    String path = scanner.nextLine();
                    StoreRoomsObject new_request = new StoreRoomsObject(1, path, username, null);

                    masterConnection = new Socket(config.masterIP, config.masterPort);

                    out = new ObjectOutputStream(masterConnection.getOutputStream());
                    in = new ObjectInputStream(masterConnection.getInputStream());

                    out.write(0);
                    out.flush();

                    out.writeObject(new_request);
                    out.flush();

                    String roomName = in.readUTF();

                    while (!roomName.equals("rooms_storing_complete")) {
                        System.out.println("Room "+roomName+" successfully stored.");
                        roomName = in.readUTF();
                    }

                    System.out.println();
                }

                // Add dates to a specific room
                else if (choice == 2) {

                    System.out.println("Enter the room name");
                    String roomName = scanner.nextLine();
                    System.out.println("Enter the available date or date range (using \"/\" between start and end dates)" +
                            "in the format YYYY-DD-MM (e.g. 2024-05-05/2024-15-05).");
                    String dates = scanner.nextLine();
                    AddDatesObject new_request = new AddDatesObject(2, roomName, dates, username);

                    masterConnection = new Socket(config.masterIP, config.masterPort);

                    out = new ObjectOutputStream(masterConnection.getOutputStream());
                    in = new ObjectInputStream(masterConnection.getInputStream());

                    out.write(0);
                    out.flush();

                    out.writeObject(new_request);
                    out.flush();

                    int result = in.read();
                    if (result == 1)
                        System.out.println("Availability of room \""+roomName+"\" successfully updated.\n");
                    else if (result == 2)
                        System.out.println("The room specified is not stored in the system.\n");
                    else if (result == 3)
                        System.out.println("The room specified does not belong to you, therefore you cannot update its availability.\n");


                // View bookings for all rooms
                } else if (choice == 3) {
                    ViewBookingsObject new_request = new ViewBookingsObject(3, username);

                    masterConnection = new Socket(config.masterIP, config.masterPort);

                    out = new ObjectOutputStream(masterConnection.getOutputStream());
                    in = new ObjectInputStream(masterConnection.getInputStream());

                    out.write(0);
                    out.flush();

                    out.writeObject(new_request);
                    out.flush();

                    ArrayList<Accommodation> bookedRooms = (ArrayList<Accommodation>) in.readObject();

                    System.out.print("Total bookings for " + username + "'s rooms:\n");
                    for (Accommodation room : bookedRooms) {
                        if (room.checkBooked()) {
                            System.out.println(room.getName() + ": " + room.print_Bookings());
                        } else {
                            System.out.println(room.getName() + ": No bookings");
                        }
                    }
                    System.out.println();

                // Check total bookings for a specific date range
                } else if (choice == 4) {
                    System.out.println("Enter date range: ");
                    String date_range = scanner.nextLine();
                    AreaBookingsObject new_request = new AreaBookingsObject(4, date_range, username);

                    masterConnection = new Socket(config.masterIP, config.masterPort);

                    out = new ObjectOutputStream(masterConnection.getOutputStream());
                    in = new ObjectInputStream(masterConnection.getInputStream());

                    out.write(0);
                    out.flush();

                    out.writeObject(new_request);
                    out.flush();

                    HashMap<String, Integer> totalBookings = (HashMap<String, Integer>) in.readObject();

                    System.out.print("Total bookings per area: \n");
                    Iterator<Map.Entry<String, Integer>> iterator = totalBookings.entrySet().iterator();

                    while(iterator.hasNext()) {
                        Map.Entry<String, Integer> entry = iterator.next();
                        String key = entry.getKey();
                        Integer value = entry.getValue();
                        System.out.println(key + ": " + value);
                    }
                    System.out.println();

                // Exit the menu
                } else {
                    System.out.println("Exiting console...");
                    System.exit(0);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                masterConnection.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
