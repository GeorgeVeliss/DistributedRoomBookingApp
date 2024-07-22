/** Several configurations regarding the IPs and ports that enable the distributed execution of the application
 * @author Velissaridis Yorgos - 3210255
 */
public class config {

    public static String masterIP = "localhost";
    public static int masterPort = 10000;
    public static String[] workerIPs = {"192.168.2.7", "localhost"};
    public static int[] workerPorts = {10001, 10010};
    public static String reducerIP = "192.168.2.7";
    public static int reducerPort = 11111;

}
