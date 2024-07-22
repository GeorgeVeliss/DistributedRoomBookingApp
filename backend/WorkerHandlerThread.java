import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/** Represents a Thread created by the Worker Server to handle an established connection
 * @author Velissaridis Yorgos - 3210255
 */
public class WorkerHandlerThread extends Thread {
    private final Worker worker;
    ObjectInputStream in;
    ObjectOutputStream out;
    Socket masterConnection;

    /**
     * Creates a handling Thread by initializing I/O Stream for communicating with other endpoints
     * @param worker The Worker Object that creates the Thread is passed in as a parameter
     * @param masterConnection The Socket passed by the Worker Server
     */
    public WorkerHandlerThread(Worker worker, Socket masterConnection) {
        super();
        this.worker = worker;
        this.masterConnection = masterConnection;

        try {
            out = new ObjectOutputStream(masterConnection.getOutputStream());
            out.flush();
            in = new ObjectInputStream(masterConnection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads a request coming from Master and handles it accordingly
     */
    public void run() {

        RequestObj requestFromMaster;

        try {
            requestFromMaster = (RequestObj) in.readObject();

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        // ManagerConsoleApp Functionality #1 : Room Storing (continuation of readJson() functionality)
        if (requestFromMaster.requestType == 1) {
            StoreRoomObject request = (StoreRoomObject) requestFromMaster;
            worker.rooms.add(request.getRoom());

            try {
                out.writeUTF(request.getRoom().getName());
                out.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

        // Manager Functionality #2 : Add available dates
        else if (requestFromMaster.requestType == 2) {

            AddDatesObject request = (AddDatesObject) requestFromMaster;

            String requestName = request.getRoomName();

            Boolean roomFound = false;
            Boolean isOwner = false;

            for (Accommodation room : worker.rooms) {
                if (room.getName().equals(requestName)) {
                    roomFound = true;
                    if (Objects.equals(room.get_manager(), request.getUsername())) {
                        isOwner = true;
                        room.read_Multiple_Dates(request.getDates());
                        break;
                    }
                }
            }
            try {
                if (!roomFound) {
                    out.write(2);
                    out.flush();
                }
                else if (!isOwner) {
                    out.write(3);
                    out.flush();
                }
                else {
                    out.write(1);
                    out.flush();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Manager Functionality #3 : View all bookings
        else if (requestFromMaster.requestType == 3) {

            ViewBookingsObject request = (ViewBookingsObject) requestFromMaster;
            ArrayList<Accommodation> booked_rooms = new ArrayList<>();

            for (Accommodation room : worker.rooms) {
                if(Objects.equals(room.get_manager(), request.getUsername())) {
                    booked_rooms.add(new Accommodation(room));
                }
            }

            MappedObject requestToReducer = new MappedObject(request, booked_rooms);
            sendToReducer(requestToReducer);

        }
        // Manager Functionality #4 : View all bookings per area for a specific date range
        else if (requestFromMaster.requestType == 4) {

            AreaBookingsObject request = (AreaBookingsObject) requestFromMaster;
            ArrayList<Accommodation> total_bookings = new ArrayList<>();

            for (Accommodation room : worker.rooms) {
                total_bookings.add(new Accommodation(room));
            }

            System.out.println("Worker rooms:");
            for (Accommodation room: total_bookings)
                System.out.println(room.getName());

            MappedObject requestToReducer = new MappedObject(request, total_bookings);
            sendToReducer(requestToReducer);
        }
        // DummyApp Functionality #1 : Filtering rooms
        else if (requestFromMaster.requestType == 5) {

            FilterRoomsObject request = (FilterRoomsObject) requestFromMaster;
            ArrayList<Accommodation> filtered_rooms = new ArrayList<>();

            Boolean[] placeholder = {false,false,false,false,false,false,false};
            for (int i = 0; i <= 5; i++)
            {
                if (Objects.equals(request.getFilters()[i], "0")) {placeholder[i]=true;}
            }

            List<String> datesList = null;

            if (!placeholder[4] == true)
                datesList = Master.string_to_Dates(request.filters[4]);

            for (Accommodation room : worker.rooms) {
                if ((placeholder[0] || room.get_noOfPersons() >= Integer.parseInt(request.filters[0])) &&
                    (placeholder[1] || room.get_stars() >= Integer.parseInt(request.filters[1])) &&
                    (placeholder[2] || room.get_noOfReviews() >= Integer.parseInt(request.filters[2])) &&
                    (placeholder[3] || Objects.equals(room.get_area(), request.filters[3])) &&
                    (placeholder[4] || room.all_dates_String.containsAll(datesList)) &&
                    (placeholder[5] || room.get_price() <= Integer.parseInt(request.filters[5])))
                        filtered_rooms.add(new Accommodation(room));
            }

            System.out.println("Filtered rooms:");
            for (Accommodation room: filtered_rooms)
                System.out.println(room.getName());

            MappedObject requestToReducer = new MappedObject(request, filtered_rooms);
            sendToReducer(requestToReducer);
        }
        // DummyApp Functionality #2 : Booking a room
        else if (requestFromMaster.requestType == 6) {

            Boolean roomFound = false;
            Boolean datesAvailable = false;

            synchronized (worker) {
                BookRoomObject request = (BookRoomObject) requestFromMaster;
                List<String> reservationDates = createDates(request.getDates());
                for (Accommodation room : worker.rooms) {
                    if (Objects.equals(room.getName(), request.getRoomName())) {
                        roomFound = true;
                        if (room.freeDates().containsAll(reservationDates)) {
                            datesAvailable = true;
                            int index = 0;
                            for (String date : room.freeDates()) {
                                System.out.println("For day: " + date);
                                for (String res : reservationDates) {
                                    if (Objects.equals(date, res)) {
                                        System.out.println("Booked day: " + res);
                                        room.booking.set(index, true);
                                        room.customerList.set(index, request.getCustomerName());
                                        System.out.println("Booking successful.");
                                        System.out.println("customer: "+room.customerList.get(index));
                                    }
                                }
                                index++;
                            }
                        }
                    }
                }
            }
            try {
                if (!roomFound)
                    out.write(1);
                else if (!datesAvailable)
                    out.write(2);
                else
                    out.write(3);

                out.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        // DummyApp Functionality #3 : Rating a room
        else if (requestFromMaster.requestType == 7) {
            RateRoomObject request = (RateRoomObject) requestFromMaster;

            Boolean roomFound = false;

            for (Accommodation room : worker.rooms) {
                if (room.getName().equals(request.getName())) {
                    roomFound = true;
                    room.add_Rating(request.getRating());
                }
            }
            try {
                out.writeBoolean(roomFound);
                out.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // AndroidApp Functionality #1 : Get Image
        else if (requestFromMaster.requestType == 8) {

            getRoomExtrasRequest selectedRoomRequest = (getRoomExtrasRequest) requestFromMaster;

            String selectedRoomName = selectedRoomRequest.getRoomName();

            System.out.println("\nselectedRoomName: "+selectedRoomName);

            Accommodation selectedRoom = null;

            for (Accommodation room : worker.rooms) {
                if (room.getName().equals(selectedRoomName)) {
                    selectedRoom = room;
                    break;
                }
            }
            byte[] roomImage = selectedRoom.get_image();
            int roomImageLength = roomImage.length;

            System.out.println(roomImage);

            try {
                out.writeInt(roomImageLength);
                out.flush();
                out.write(selectedRoom.get_image());
                out.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // AndroidApp Functionality #2 : Get available dates
        else if (requestFromMaster.requestType == 9) {

            getRoomExtrasRequest selectedRoomRequest = (getRoomExtrasRequest) requestFromMaster;

            String selectedRoomName = selectedRoomRequest.getRoomName();

            Accommodation selectedRoom = null;

            for (Accommodation room : worker.rooms) {
                if (room.getName().equals(selectedRoomName)) {
                    selectedRoom = room;
                    break;
                }
            }

            ArrayList<String> availableDates = (ArrayList<String>) selectedRoom.freeDates();

            try {
                out.writeObject(availableDates);
                out.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * Sends a query to the Reducer Server that need to be aggregated with queries from other Worker nodes regarding
     * the same original request
     * @param request A MappedObject Object that contains all the information needed for the aggregation
     */
    void sendToReducer(MappedObject request) {
        Socket reducerSocket = null;
        try {
            reducerSocket = new Socket(config.reducerIP, config.reducerPort);
            ObjectOutputStream out = new ObjectOutputStream(reducerSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(reducerSocket.getInputStream());
            out.write(2);
            out.flush();
            out.writeObject(request);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                reducerSocket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Creates multiple single date from a range of dates and stores them in a List
     * @param range A String that represents a range of dates
     * @return A List of Strings where its String represents a date
     */
    List<String> createDates(String range) {

        LocalDate start, end;
        List<String> reservationDates = new ArrayList<>();

        if (range.contains("/")) {
            String[] parts = range.split("/");
            start = LocalDate.parse(parts[0], DateTimeFormatter.ofPattern("yyyy-d-M"));
            end = LocalDate.parse(parts[1], DateTimeFormatter.ofPattern("yyyy-d-M"));
        } else {
            start = LocalDate.parse(range, DateTimeFormatter.ofPattern("yyyy-d-M"));
            end = LocalDate.parse(range, DateTimeFormatter.ofPattern("yyyy-d-M"));
        }

        Stream.iterate(start, date -> date.plusDays(1))
                .limit(ChronoUnit.DAYS.between(start, end) + 1)
                .forEach(item -> {
                    reservationDates.add(item.toString());
                });

        return reservationDates;
    }
}
