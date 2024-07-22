import java.io.Serializable;
import java.util.ArrayList;

// General Abstract Class
abstract class RequestObj implements Serializable {

    int requestType;
    String username;

    public RequestObj(int type, String username) {
        this.requestType = type;
        this.username = username;
    }

    int getRequestType() { return requestType; }
    String getUsername() { return username; }
}

// Manager Requests
class StoreRoomsObject extends RequestObj {

    String path;
    Accommodation room;

    public StoreRoomsObject(int type, String path, String username, Accommodation room) {
        super(type, username);
        this.path = path;
        this.room = room;
    }

    String getPath() { return path; }
    Accommodation getRoom() { return room; }
    void setRoom(Accommodation room) { this.room = room; }
}

class StoreRoomObject extends RequestObj {

    String path;
    Accommodation room;

    public StoreRoomObject(StoreRoomsObject storeRoomsObject, Accommodation room) {//int type, UUID id, String username, Accommodation room) {
        super(storeRoomsObject.getRequestType(), storeRoomsObject.getUsername());
        this.username = storeRoomsObject.getUsername();
        this.room = room;
    }
    Accommodation getRoom() { return room; }
}


class AddDatesObject extends RequestObj {

    String roomName;
    String dates;

    public AddDatesObject(int type, String roomName, String dates, String username) {
        super(type, username);
        this.roomName = roomName;
        this.dates = dates;
    }

    String getRoomName() { return roomName; }
    String getDates() { return dates; }
}

class ViewBookingsObject extends RequestObj {

    public ViewBookingsObject(int requestType, String username) {
        super(requestType, username);
    }
}

class AreaBookingsObject extends RequestObj {

    String dates;

    public AreaBookingsObject(int requestType, String dates, String username) {
        super(requestType, username);
        this.dates = dates;
    }

    String getDates() { return dates; }

}


// User Requests
class FilterRoomsObject extends RequestObj {

    String[] filters;

    public FilterRoomsObject(int type, String username, String[] filters) {
        super(type, username);
        this.filters = filters;
    }

    String[] getFilters() { return filters; }
}

class BookRoomObject extends RequestObj {

    String roomName;
    String dates;

    public BookRoomObject(int type, String customerName, String roomName, String dates) {
        super(type, customerName);
        this.roomName = roomName;
        this.dates = dates;
    }

    String getRoomName() { return roomName; }
    String getCustomerName() { return getUsername(); }
    String getDates() { return dates; }
}

class RateRoomObject extends RequestObj {

    String name;
    int rating;

    public RateRoomObject(int requestType, String username, String roomName, int roomRating) {
        super(requestType, username);
        this.name = roomName;
        this.rating = roomRating;
    }

    String getName() { return name; }
    int getRating() { return rating; }
}

// Reducer Requests

class MappedObject extends RequestObj {

    RequestObj request;
    ArrayList<Accommodation> mappedRooms = new ArrayList<>();

    public MappedObject(RequestObj request, ArrayList<Accommodation> rooms) {
        super(request.getRequestType(), request.getUsername());
        this.request = request;
        this.mappedRooms.addAll(rooms);
    }

    RequestObj getRequest() { return request; }
    ArrayList<Accommodation> getMappedRooms() { return mappedRooms; }
}

// android requests

class getRoomExtrasRequest extends RequestObj {

    RequestObj request;
    String roomName;

    public getRoomExtrasRequest(int requestType, String username, String roomName) {
        super(requestType, username);
        this.roomName = roomName;
    }

    RequestObj getRequest() { return request; }
    String getRoomName() { return roomName; }
}