import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/**
 * Main server which handles the creation of 20 Questions game lobbies.
 * Listens on port 9999 and allows clients to specify custom ports for new game lobbies.
 */
public class mainServer {
    private static final int MAIN_PORT = 9999;

    /**
     * Starts the lobby server and listens for incoming client requests to create game lobbies.
     *
     * @throws IOException if the main server socket cannot be created.
     */
    public static void main(String[] args) throws IOException {
        ServerSocket mainSocket = new ServerSocket(MAIN_PORT);
        System.out.println("Lobby server started on port " + MAIN_PORT);

        ExecutorService pool = Executors.newCachedThreadPool();

        while (true) {
            Socket clientSocket = mainSocket.accept();
            pool.execute(() -> handleLobbyRequest(clientSocket));
        }
    }

    /**
     * Handles a client's request to create a new game lobby.
     * Asks for a port and starts a game on that port if available
     *
     * @param clientSocket the socket connected to the requesting client.
     */
    private static void handleLobbyRequest(Socket clientSocket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            out.println("Welcome! Enter a port number for your game lobby (e.g., 1000–9998):");
            String portStr = in.readLine();

            int port = Integer.parseInt(portStr.trim());

            if (!isPortAvailable(port)) {
                out.println("That port is already in use! Please choose a different one.");
            } else {
                launchGameLobby(port);
                out.println("Game lobby started on port " + port);
                out.println("Whoever connects first will be the answerer; the second will be the questioner.");
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error in lobby setup: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ignored) {}
        }
    }

    /**
     * Checks if a port is open.
     *
     * @param port the port number to check.
     * @return true if the port is available, false if its already in use.
     */
    private static boolean isPortAvailable(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Launches a new game lobby on a separate thread using the specified port.
     *
     * @param port the port number on which the game will run.
     */
    private static void launchGameLobby(int port) {
        new Thread(() -> {
            try {
                gameLogic.startGameOnPort(port);
            } catch (IOException e) {
                System.err.println("Error launching game on port " + port);
                e.printStackTrace();
            }
        }).start();
    }
}
