import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

/**
 * Represents a room of the application
 * @author Velissaridis Yorgos - 3210255
 */
public class Accommodation implements Serializable {

    private String roomName;
    private final String manager;
    private String area;
    private String roomImage;
    private int noOfPersons, price, noOfReviews;
    private float stars;
    private byte[] image;

    protected List<String> all_dates_String = new ArrayList<>();
    protected List<Boolean> booking = new ArrayList<>();
    protected List<String> customerList = new ArrayList<>();

    /**
     * Creates a room by inputting specific parameters contained in a .json file from the Manager's terminal
     * (the functionality is explained further in ManagerConsoleApp.java)
     * @param roomName The name of the room
     * @param noOfPersons The maximum number of people that the room can host
     * @param area The area where the room is located
     * @param stars The current rating of the room
     * @param noOfReviews The current number of reviews that the room has
     * @param roomImage A path to where the image of the room is located
     * @param dates The dates that the room is available for bookings
     * @param price The price of the room
     * @param manager The name of the manager that created the room
     * @throws IOException
     */
    public Accommodation(String roomName, String noOfPersons, String area, String stars, String noOfReviews, String roomImage, String dates, String price, String manager) throws IOException {
        this.roomName = roomName;
        this.noOfPersons = Integer.parseInt(noOfPersons);
        this.area = area.replaceFirst(" ","");
        this.stars = Integer.parseInt(stars);
        this.noOfReviews = Integer.parseInt(noOfReviews);
        this.roomImage= roomImage;
        this.price = Integer.parseInt(price.replace(" ",""));
        this.manager = manager;
        this.image = imageToByteArray(roomImage);

        // Split the line by commas
        String[] parts = dates.replace(" ","").split("---");

        // Print each part
        for (String part : parts) {
            part = part.replace("\"dates\":", "");
            read_Multiple_Dates(part);
        }
    }

    /**
     * Creates a room by inputting an already existing Accommodation Object. All the parameters of the newly
     * created room are set according to the ones stored withing the room passed as argument to the constructor
     * @param room An Accommodation Object
     */
    public Accommodation(Accommodation room) {
        this.roomName = room.getName();
        this.noOfPersons = room.get_noOfPersons();
        this.area = room.get_area();
        this.stars = room.get_stars();
        this.noOfReviews = room.get_noOfReviews();
        this.roomImage = room.get_roomImage();
        this.price = room.get_price();
        this.manager = room.get_manager();
        this.all_dates_String = room.get_dates();
        this.booking = room.get_booking();
        this.image = room.get_image();
        this.customerList = room.customerList;
    }

    /**
     * Prints out all important information of a room
     */
    void print_Accommodation()
    {
        System.out.println("roomName: " + getName() + ", noOfPersons: " + get_noOfPersons() + ", area: " + get_area() + ", stars: " + get_stars() + ", noOfReviews: " + get_noOfReviews() + ", price: " + get_price());

    }

    /**
     * Adds a user's rating to the current rating of a room and adjusts the noOfReviews of the room accordingly
     * @param rating The rating that is inputted by the user
     */
    //Addition of rating and then taking it into consideration in the total rating of the room
    void add_Rating(int rating)
    {
        this.stars = (this.stars*this.noOfReviews+rating)/(this.noOfReviews+1);

        this.noOfReviews++;
    }

    /**
     * Gets the price of a room
     * @return An integer representing the price of a room
     */
    public int get_price() {
        return this.price;
    }

    /**
     * Gets the name of a room
     * @return A String representing the room's name
     */
    public String getName() {
        return roomName;
    }

    /**
     * Gets the maximum number of people that a room can host
     * @return An integer representing the maximum number of people for a room
     */
    public int get_noOfPersons() {
        return noOfPersons;
    }

    /**
     * Sets the maximum number of people that a room can host
     */
    public void set_noOfPersons(int age) {
        this.noOfPersons = age;
    }

    /**
     * Gets the number of reviews that have been made for a room
     * @return An integer representing a room's total number of reviews at the given point
     */
    public int get_noOfReviews() {
        return noOfReviews;
    }

    /**
     * Gets the area where a room is located
     * @return A String representing the location of a room
     */
    public String get_area() {
        return this.area;
    }

    /**
     * Gets the path of the image for a room
     * @return A String representing the path to where the image of the room is located
     */
    public String get_roomImage() {
        return roomImage;
    }

    /**
     * Gets the rating of a room
     * @return An integer representing the currently stored rating for a room
     */
    public int get_stars() {
        return (int)Math.floor(stars);
    }

    /**
     * Gets the username of the manager for a room
     * @return A String that represents the username of the manager of a room
     */
    public String get_manager() {
        return manager;
    }

    /**
     * Gets the dates where a room is available for booking as set by the manager
     * @return A List of Strings where its String is a date that the room is available for booking
     */
    public List<String>  get_dates() {
        return all_dates_String;
    }

    /**
     * Gets the dates where a room is booked or not
     * @return A List of Boolean values indicating whether a room is booked (True) or available for booking (False)
     */
    public List<Boolean>  get_booking() {
        return booking;
    }

    /**
     * Gets the image of a room
     * @return A byte[] that contains the parsed image
     */
    public byte[] get_image() {
        return image;
    }


    /**
     * Transforms the dates that are passed as a parameter to a List of single dates (format: YYYY-DD-MM)
     * @param range A String representing a single date (format: YYYY-DD-MM)
     *              or a date range (format: YYYY-DD-MM/YYYY-DD-MM)
     */
    // read_Multiple_Dates takes as input a date(yyyy-dd-mm) or a date range (yyyy-dd-mm/yyyy-dd-mm)
    // and then using LocalDate library in order to transform them into a list of single dates in yyyy-dd-mm format
    void read_Multiple_Dates(String range) {

        LocalDate start,end;

        if (range.contains("/")) {
            String[] parts = range.split("/");
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
                    //System.out.println(item);
                    this.all_dates_String.add(item.toString());
                    booking.add(false);
                    customerList.add("");
                });
    }

    /**
     * Calculates the dates that a room is not booked by anyone
     * @return A List of Strings where its String represents a date that the room is not booked by anyone
     */
    public List<String> freeDates()
    {
        List<String> freeDates = new ArrayList<>();
        for (int i = 0; i < booking.size(); i++)
        {
            if (!booking.get(i)) {
                freeDates.add(all_dates_String.get(i));
            }
        }

        return freeDates;
    }

    /**
     * Calculates the dates that a room is booked
     * @return A List of Strings where its String represents a date that the room is booked by a user
     */
    public List<String> print_Bookings()
    {
        List<String> bookedDates = new ArrayList<>();
        for (int i = 0; i < booking.size(); i++)
        {
            if (booking.get(i)) {
                bookedDates.add(customerList.get(i) +": "+ all_dates_String.get(i));
            }
        }
        return bookedDates;
    }


    /**
     * Calculates the total number of bookings for a room
     * @param dates A String representing a date range (format: YYYY-DD-MM/YYYY-DD-MM)
     * @return An integer representing the total number of bookings for a room
     */
    public int number_of_Bookings(String dates)
    {
        int count = 0;
        List<String> all_Dates = new ArrayList<>();
        // Split the line by commas
        String[] parts = dates.replace(" ","").split("---");

        for (String part : parts) {
            part = part.replace("\"dates\":", "");

            if (part.contains("/")) {
                String[] tempt = part.split("/");

                LocalDate start = LocalDate.parse(tempt[0], DateTimeFormatter.ofPattern("yyyy-d-M")),
                        end = LocalDate.parse(tempt[1], DateTimeFormatter.ofPattern("yyyy-d-M"));

                Stream.iterate(start, date -> date.plusDays(1))
                        .limit(ChronoUnit.DAYS.between(start, end) + 1)
                        .forEach(item -> {
                            all_Dates.add(item.toString());
                        });
            } else
            {

                LocalDate start = LocalDate.parse(part, DateTimeFormatter.ofPattern("yyyy-d-M")),
                        end = LocalDate.parse(part, DateTimeFormatter.ofPattern("yyyy-d-M"));

                Stream.iterate(start, date -> date.plusDays(1))
                        .limit(ChronoUnit.DAYS.between(start, end) + 1)
                        .forEach(item -> {
                            all_Dates.add(item.toString());
                        });
            }

            for (int i = 0; i < all_Dates.size(); i++) {
                for (int x = 0; x < all_dates_String.size(); x++) {
                    if(Objects.equals(all_Dates.get(i), all_dates_String.get(x)))
                    {
                        if(booking.get(x)){
                            count++;
                        }
                    }
                }
            }

        }

        return count;
    }

    /**
     * Checks if a room is booked on any dates
     * @return A Boolean value representing whether a room has any bookings or not
     */
    public Boolean checkBooked() {
        int index = 0;
        for (Boolean booked : booking) {
            if (booked) {
                return true;
            }
            index++;
        }
        return false;
    }

    /**
     *
     * @param path A String representing the path of the image as specified by the manager
     * @return A byte[] containing the parsed image's information
     * @throws IOException When the ImageIO.read() can't parse the data that are passed to it
     */
    public byte[] imageToByteArray(String path) throws IOException {
        BufferedImage image = ImageIO.read(new File("C:\\Users\\georg\\Desktop\\3210255_3160056_3180219\\backend"+path));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", bos);
        return bos.toByteArray();
    }
}
