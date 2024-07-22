import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

/** Represents a Thread created by the Master Server to handle an established connection
 * @author Velissaridis Yorgos - 3210255
 */
public class MasterHandlerThread extends Thread {

    Socket clientConnection;
    ObjectOutputStream out;
    ObjectInputStream in;

    /**
     * Creates a handling Thread by initializing I/O Stream for communicating with other endpoints
     * @param clientConnection The Socket passed by the Master Server
     */
    public MasterHandlerThread(Socket clientConnection) {
        super();
        this.clientConnection = clientConnection;
        try {
            out = new ObjectOutputStream(clientConnection.getOutputStream());
            in = new ObjectInputStream(clientConnection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks where the connection was established from and creates the appropriate RequestObj Object
     * to handle the incoming request. Each request type corresponds to a functionality that is present in either the
     * manager's or the user's side (documentation @ ManagerConsoleApp.java, DummyApp.java)
     */
    public void run() {

        try {
            int sender = in.read();

            // Requests from ManagerConsoleApp, DummyApp
            if (sender == 0) {

                RequestObj requestFromClient = (RequestObj) in.readObject();

                int requestType = requestFromClient.getRequestType();

                // ManagerConsoleApp Functionality #1 : Read from .json file
                if (requestType == 1) {

                    StoreRoomsObject request = (StoreRoomsObject) requestFromClient;
                    ArrayList<Accommodation> rooms = Master.readJson(request.getPath(), request.getUsername());

                    for (Accommodation room : rooms) {
                        StoreRoomObject new_request = new StoreRoomObject(request, room);
                        String roomName = roomToWorker(Master.Hash(room.getName()), new_request);
                        out.writeUTF(roomName);
                        out.flush();
                    }

                    out.writeUTF("rooms_storing_complete");
                    out.flush();

                }
                // ManagerConsoleApp Functionality #2 : Add available dates
                else if (requestType == 2) {
                    AddDatesObject request = (AddDatesObject) requestFromClient;

                    int result = availabilityToWorker(Master.Hash(request.getRoomName()), request);

                    out.write(result);
                    out.flush();
                }
                // ManagerConsoleApp Functionality #4 : View all bookings
                else if (requestFromClient.getRequestType() == 3) {
                    ViewBookingsObject request = (ViewBookingsObject) requestFromClient;

                    Socket reducerConnection = new Socket(config.reducerIP, config.reducerPort);
                    ObjectOutputStream reducerOut = new ObjectOutputStream(reducerConnection.getOutputStream());
                    ObjectInputStream reducerIn = new ObjectInputStream(reducerConnection.getInputStream());

                    reducerOut.write(1);
                    reducerOut.flush();
                    reducerOut.writeUTF(request.getUsername());
                    reducerOut.flush();

                    for (int i = 0; i < Master.numberOfWorkers; i++) {
                        filtersToWorker(i, request);
                    }

                    ArrayList<Accommodation> bookedRooms = (ArrayList<Accommodation>) reducerIn.readObject();

                    out.writeObject(bookedRooms);
                    out.flush();

                }
                // ManagerConsoleApp Functionality #5: View all bookings per area for a specific date range
                else if (requestFromClient.getRequestType() == 4) {

                    AreaBookingsObject request = (AreaBookingsObject) requestFromClient;


                    Socket reducerConnection = new Socket(config.reducerIP, config.reducerPort);
                    ObjectOutputStream reducerOut = new ObjectOutputStream(reducerConnection.getOutputStream());
                    ObjectInputStream reducerIn = new ObjectInputStream(reducerConnection.getInputStream());

                    reducerOut.write(1);
                    reducerOut.flush();
                    reducerOut.writeUTF(request.getUsername());
                    reducerOut.flush();

                    for (int i = 0; i < Master.numberOfWorkers; i++) {
                        filtersToWorker(i, request);
                    }

                    ArrayList<Accommodation> totalBookingPerArea = (ArrayList<Accommodation>) reducerIn.readObject();
                    Master.countPerArea.clear();

                    for (Accommodation room : totalBookingPerArea) {
                        int count = room.number_of_Bookings(request.getDates());
                        if (!Master.countPerArea.containsKey(room.get_area())) {
                            Master.countPerArea.put(room.get_area(), count);
                        } else {
                            Master.countPerArea.put(room.get_area(), Master.countPerArea.get(room.get_area()) + count);
                        }
                    }
                    out.writeObject(Master.countPerArea);
                    out.flush();
                }
                // DummyApp Functionality #1 : Filtering rooms
                else if (requestFromClient.getRequestType() == 5) {
                    FilterRoomsObject request = (FilterRoomsObject) requestFromClient;

                    Socket reducerConnection = new Socket(config.reducerIP, config.reducerPort);
                    ObjectOutputStream reducerOut = new ObjectOutputStream(reducerConnection.getOutputStream());
                    ObjectInputStream reducerIn = new ObjectInputStream(reducerConnection.getInputStream());

                    reducerOut.write(1);
                    reducerOut.flush();
                    reducerOut.writeUTF(request.getUsername());
                    reducerOut.flush();

                    for (int i = 0; i < Master.numberOfWorkers; i++) {
                        filtersToWorker(i, request);
                    }

                    ArrayList<Accommodation> filteredRooms = (ArrayList<Accommodation>) reducerIn.readObject();

                    out.writeObject(filteredRooms);
                    out.flush();

                }
                // DummyApp Functionality #2 : Booking a room
                else if (requestFromClient.getRequestType() == 6) {
                    BookRoomObject request = (BookRoomObject) requestFromClient;
                    int result = bookingToWorker(Master.Hash(request.getRoomName()), request);

                    out.write(result);
                    out.flush();
                }
                // DummyApp Functionality #3 : Rating a room
                else if (requestFromClient.getRequestType() == 7) {
                    RateRoomObject request = (RateRoomObject) requestFromClient;
                    Boolean success = ratingToWorker(Master.Hash(request.getName()), request);

                    out.writeBoolean(success);
                    out.flush();
                }
            }
            // Requests from AndroidApp
            else if (sender == 1) {

                int requestType = in.read();

                // AndroidApp Functionality #1 : Filtering Rooms
                if (requestType == 0) {
                    String username = in.readUTF();
                    String[] filters = (String[]) in.readObject();
                    FilterRoomsObject request = new FilterRoomsObject(5, username, filters);

                    Socket reducerConnection = new Socket(config.reducerIP, config.reducerPort);
                    ObjectOutputStream reducerOut = new ObjectOutputStream(reducerConnection.getOutputStream());
                    ObjectInputStream reducerIn = new ObjectInputStream(reducerConnection.getInputStream());

                    reducerOut.write(1);
                    reducerOut.flush();
                    reducerOut.writeUTF(request.getUsername());
                    reducerOut.flush();

                    for (int i = 0; i < Master.numberOfWorkers; i++) {
                        filtersToWorker(i, request);
                    }

                    ArrayList<Accommodation> filteredRooms = (ArrayList<Accommodation>) reducerIn.readObject();

                    ArrayList<ArrayList<String>> filteredStringRooms = new ArrayList<>();

                    for (Accommodation room : filteredRooms) {
                        ArrayList<String> filteredRoom = new ArrayList<>();

                        String roomName = room.getName();
                        filteredRoom.add(roomName);
                        String roomRating = String.valueOf(room.get_stars());
                        filteredRoom.add(roomRating);
                        String roomArea = room.get_area();
                        filteredRoom.add(roomArea);
                        String noOfPersons = String.valueOf(room.get_noOfPersons());
                        filteredRoom.add(noOfPersons);
                        String price = String.valueOf(room.get_price());
                        filteredRoom.add(price);
                        String manager = room.get_manager();
                        filteredRoom.add(manager);

                        filteredStringRooms.add(filteredRoom);
                    }

                    out.writeObject(filteredStringRooms);
                    out.flush();

                    System.out.println("Successfully sent filtered rooms to Android app.");
                }
                // AndroidApp Functionality #2 : Get Image
                else if (requestType == 1) {
                    String customerName = in.readUTF();
                    String roomName = in.readUTF();

                    getRoomExtrasRequest request = new getRoomExtrasRequest(8, customerName, roomName);

                    int hashIndex = Master.Hash(roomName);
                    Socket workerConnection = new Socket(config.workerIPs[hashIndex], config.workerPorts[hashIndex]);
                    ObjectOutputStream workerOut = new ObjectOutputStream(workerConnection.getOutputStream());
                    ObjectInputStream workerIn = new ObjectInputStream(workerConnection.getInputStream());

                    workerOut.writeObject(request);
                    workerOut.flush();

                    int roomImageLength = workerIn.readInt();

                    if (roomImageLength > 0) {
                        byte[] roomImage = new byte[roomImageLength];
                        workerIn.readFully(roomImage, 0, roomImage.length);

                        out.writeInt(roomImageLength);
                        out.flush();

                        out.write(roomImage);
                        out.flush();
                    }
                }
                // AndroidApp Functionality #3 : Get available dates
                else if (requestType == 2) {
                    String customerName = in.readUTF();
                    String roomName = in.readUTF();

                    getRoomExtrasRequest request = new getRoomExtrasRequest(9, customerName, roomName);

                    int hashIndex = Master.Hash(roomName);
                    Socket workerConnection = new Socket(config.workerIPs[hashIndex], config.workerPorts[hashIndex]);
                    ObjectOutputStream workerOut = new ObjectOutputStream(workerConnection.getOutputStream());
                    ObjectInputStream workerIn = new ObjectInputStream(workerConnection.getInputStream());

                    workerOut.writeObject(request);
                    workerOut.flush();

                    ArrayList<String> availableDates = (ArrayList<String>) workerIn.readObject();

                    out.writeObject(availableDates);
                    out.flush();
                }
                // AndroidApp Functionality #4 : Booking a room
                else if (requestType == 3) {
                    String customerName = in.readUTF();
                    String roomName = in.readUTF();
                    String bookingDates = in.readUTF();

                    BookRoomObject request = new BookRoomObject(6, customerName, roomName, bookingDates);
                    int result = bookingToWorker(Master.Hash(request.getRoomName()), request);

                    out.write(result);
                    out.flush();
                }

                // AndroidApp Functionality #5 : Rating a room
                else if (requestType == 4) {

                    String roomName = in.readUTF();
                    String rating = in.readUTF();

                    RateRoomObject request = new RateRoomObject(7, "", roomName, Integer.parseInt(rating));
                    Boolean success = ratingToWorker(Master.Hash(roomName), request);

                    out.writeBoolean(success);
                    out.flush();
                }
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sends a RequestObj Object to the appropriate Worker node in order to save an Accommodation Object to the system
     * @param hashIndex The hash code created using the Hash() function (documentation @ Master.java) used to send the
     *                  request to the appropriate Worker node
     * @param request The RequestObj Object that contains all relevant information of the operation that needs to be
     *                performed by the Worker node
     * @return A String that represent the value that is sent back from the Worker node after completing its task
     */
    private String roomToWorker(int hashIndex, RequestObj request) {
        Socket workerConnection = null;
        try {
            workerConnection = new Socket(config.workerIPs[hashIndex], config.workerPorts[hashIndex]);
            ObjectOutputStream out = new ObjectOutputStream(workerConnection.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(workerConnection.getInputStream());

            out.writeObject(request);
            out.flush();

            return in.readUTF();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                workerConnection.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    /**
     * Sends a RequestObj Object to the appropriate Worker node in order to add available dates to a specific room
     * @param hashIndex The hash code created using the Hash() function (documentation @ Master.java) used to send the
     *                  request to the appropriate Worker node
     * @param request The RequestObj Object that contains all relevant information of the operation that needs to be
     *                performed by the Worker node
     * @return A String that represents the value that is sent back from the Worker node after completing its task
     */
    private int availabilityToWorker(int hashIndex, AddDatesObject request) {
        Socket workerConnection = null;
        try {
            workerConnection = new Socket(config.workerIPs[hashIndex], config.workerPorts[hashIndex]);
            ObjectOutputStream out = new ObjectOutputStream(workerConnection.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(workerConnection.getInputStream());

            out.writeObject(request);
            out.flush();

            return in.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                workerConnection.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    /**
     * Sends a RequestObj Object to the appropriate Worker node in order to filter rooms based on user inputs
     * @param hashIndex The hash code created using the Hash() function (documentation @ Master.java) used to send the
     *                  request to the appropriate Worker node
     * @param request The RequestObj Object that contains all relevant information of the operation that needs to be
     *                performed by the Worker node
     */
    private void filtersToWorker(int hashIndex, RequestObj request) {
        Socket workerConnection = null;
        try {
            workerConnection = new Socket(config.workerIPs[hashIndex], config.workerPorts[hashIndex]);
            ObjectOutputStream out = new ObjectOutputStream(workerConnection.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(workerConnection.getInputStream());

            out.writeObject(request);
            out.flush();

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                workerConnection.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    /**
     * Sends a RequestObj Object to the appropriate Worker node in order to book a specific room
     * @param hashIndex The hash code created using the Hash() function (documentation @ Master.java) used to send the
     *                  request to the appropriate Worker node
     * @param request The RequestObj Object that contains all relevant information of the operation that needs to be
     *                performed by the Worker node
     * @return An integer that represents the value that is sent back from the Worker node after completing its task
     */
    private int bookingToWorker(int hashIndex, RequestObj request) {
        Socket workerConnection = null;
        try {
            workerConnection = new Socket(config.workerIPs[hashIndex], config.workerPorts[hashIndex]);
            ObjectOutputStream out = new ObjectOutputStream(workerConnection.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(workerConnection.getInputStream());

            out.writeObject(request);
            out.flush();

            return in.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                workerConnection.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    /**
     * Sends a RequestObj Object to the appropriate Worker node in order to book a specific room
     * @param hashIndex The hash code created using the Hash() function (documentation @ Master.java) used to send the
     *                  request to the appropriate Worker node
     * @param request The RequestObj Object that contains all relevant information of the operation that needs to be
     *                performed by the Worker node
     * @return A Boolean value that represents the value that is sent back from the Worker node after completing its task
     */
    private Boolean ratingToWorker(int hashIndex, RequestObj request) {
        Socket workerConnection = null;
        try {
            workerConnection = new Socket(config.workerIPs[hashIndex], config.workerPorts[hashIndex]);
            ObjectOutputStream out = new ObjectOutputStream(workerConnection.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(workerConnection.getInputStream());

            out.writeObject(request);
            out.flush();

            return in.readBoolean();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                workerConnection.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}