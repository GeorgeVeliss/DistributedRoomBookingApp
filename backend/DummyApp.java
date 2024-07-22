import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

/** Represents a client terminal that holds the user's functionality of the application
 * (replaced by the front end of the application using Android Studio)
 * @author Velissaridis Yorgos - 3210255
 */
public class DummyApp {

    ObjectOutputStream out;
    ObjectInputStream in;
    Socket masterConnection;

    /**
     * Creates a new DummyApp Object and calls the .run() function on the newly created Object
     * @param args The command line arguments that can be passed to the main function (No arguments used)
     */
    public static void main(String[] args) {
        DummyApp app = new DummyApp();
        app.run();
    }

    /**
     * Displays a menu and prompts the user to choose an operation from the following operations:
     * Filtering    : filters the available Accommodation Objects according to the user's inputs
     *                (the available filters for are the minimum number of people,rating,number of ratings as well as
     *                the area available date(s), and maximum price, all separated by commas)
     * Booking      : performs a booking request for a single Accommodation Object given specific dates by the user
     * Rating       : adds a new rating that is given by the user to a single Accommodation Object
     */
    public void run() {

        try {
            Scanner scanner = new Scanner(System.in);

            System.out.println("Welcome to Dummy App.\nEnter username: ");
            String customerName = scanner.nextLine();

            // User menu
            while(true) {
                System.out.println("Enter 1 to filter results.");
                System.out.println("Enter 2 to book a room.");
                System.out.println("Enter 3 to rate a room.");
                System.out.println("Enter anything else to exit.");

                int choice = 0;

                try {
                    choice = Integer.parseInt(scanner.nextLine());
                }
                catch (NumberFormatException e) {
                    System.out.println("Exiting console...");
                    System.exit(0);
                }
                // Filter Rooms
                if (choice == 1) {
                    System.out.println("Enter the minimum number of people, minimum rating, minimum number of ratings, " +
                                        "area, available date(s), and maximum price you want with commas in-between:");
                    String[] filters = scanner.nextLine().split(",");
                    FilterRoomsObject new_request = new FilterRoomsObject(5, customerName, filters);

                    masterConnection = new Socket(config.masterIP, config.masterPort);

                    out = new ObjectOutputStream(masterConnection.getOutputStream());
                    in = new ObjectInputStream(masterConnection.getInputStream());

                    out.write(0);
                    out.flush();

                    out.writeObject(new_request);
                    out.flush();

                    ArrayList<Accommodation> filteredRooms = (ArrayList<Accommodation>) in.readObject();

                    System.out.println("Rooms that satisfy the selected filters:\n");
                    for (Accommodation room : filteredRooms) {
                        System.out.println("Room: "+room.getName());
                        System.out.println("Manager: "+room.get_manager());
                        System.out.println("Area: "+room.get_area());
                        System.out.println("Number of persons: "+room.get_noOfPersons());
                        System.out.println("Stars: "+room.get_stars());
                        System.out.println("Number of reviews: "+room.get_noOfReviews());
                        System.out.println("Price per night: "+room.get_price()+"€");
                        System.out.println("Available dates: "+room.get_dates());
                        System.out.println();
                    }

                // Book a room
                } else if (choice == 2) {
                    System.out.println("Enter the name of the room you would like to book:");
                    String roomName = scanner.nextLine();
                    System.out.println("Enter the desired date or date range (using \"/\" between start and end dates)" +
                            "in the format YYYY-DD-MM (e.g. 2024-05-05/2024-15-05).");
                    String dates = scanner.nextLine();
                    BookRoomObject new_request = new BookRoomObject(6, customerName, roomName, dates);

                    masterConnection = new Socket(config.masterIP, config.masterPort);

                    out = new ObjectOutputStream(masterConnection.getOutputStream());
                    in = new ObjectInputStream(masterConnection.getInputStream());

                    out.write(0);
                    out.flush();

                    out.writeObject(new_request);
                    out.flush();

                    int result = in.read();

                    if (result == 1)
                        System.out.println("The room specified is not stored in the system.\n");
                    else if (result == 2)
                        System.out.println("Date(s) specified not available.\n");
                    else
                        System.out.println("Booking for room "+roomName+" successfully completed.\n");

                // Rate a room
                } else if (choice == 3) {
                    System.out.println("Enter the name of the room you would like to rate:");
                    String roomRate = scanner.nextLine();
                    System.out.println("Enter your rating (1-5):");
                    int roomRating = Integer.parseInt(scanner.nextLine());
                    RateRoomObject new_request = new RateRoomObject(7, customerName, roomRate, roomRating);

                    masterConnection = new Socket(config.masterIP, config.masterPort);

                    out = new ObjectOutputStream(masterConnection.getOutputStream());
                    in = new ObjectInputStream(masterConnection.getInputStream());

                    out.write(0);
                    out.flush();

                    out.writeObject(new_request);
                    out.flush();

                    Boolean result = in.readBoolean();

                    if (result)
                        System.out.println("Rating for room "+roomRate+" successfully added.\n");
                    else
                        System.out.println("The room specified is not stored in the system.\n");

                } else {
                    System.out.println("Exiting...");
                    break;
                }
            }
        } catch (Exception e) {
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
