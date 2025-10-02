import java.io.*;
import java.net.*;

/**
 * 20 questions client to connect to the game server on a specified port
 */
public class newClient {
    /**.
     * Connects to a local server at the specified port and handles message exchange.
     *
     * @param args Command line argument, specifying port number
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java Client <port>");
            return;
        }

        try {
            int port = Integer.parseInt(args[0]);

            // Connect to the server at localhost and specified port
            Socket socket = new Socket("127.0.0.1", port);

            // Reader for server messages
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // Writer to send messages to the server
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            // Reader for user input from the console
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

            String serverMessage;
            while ((serverMessage = in.readLine()) != null) {
                // Prints server message
                System.out.println(serverMessage);

                // If the server is prompting for a response, read from user and send it
                if (serverMessage.endsWith(":") || serverMessage.endsWith("!")) {
                    String response = userInput.readLine();
                    out.println(response);
                }
            }

            // Close the socket when done
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
