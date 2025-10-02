import java.io.*;
import java.net.*;

/**
 * Core logic for a 20 Questions game on specified port with 2 players.
 */
public class gameLogic {

    /**
     * Starts a new game on the specified port.
     * Waits for two players to connect, then begins the game.
     *
     * @param port the port number to host the game on.
     * @throws IOException if an error occurs during setup.
     */
    public static void startGameOnPort(int port) throws IOException {
        ServerSocket gameSocket = new ServerSocket(port);
        System.out.println("Game server started on port " + port + ". Waiting for 2 players...");

        Socket answererSocket = gameSocket.accept();
        System.out.println("Answerer connected.");

        Socket questionerSocket = gameSocket.accept();
        System.out.println("Questioner connected.");

        // Reject additional connections after two players join
        new Thread(() -> {
            try {
                while (true) {
                    Socket extraSocket = gameSocket.accept();
                    PrintWriter out = new PrintWriter(extraSocket.getOutputStream(), true);
                    out.println("Lobby full! This game already has 2 players.");
                    extraSocket.close();
                }
            } catch (IOException e) {
                System.out.println("Game socket closed or error while rejecting extra connections.");
            }
        }).start();

        BufferedReader answererIn = new BufferedReader(new InputStreamReader(answererSocket.getInputStream()));
        PrintWriter answererOut = new PrintWriter(answererSocket.getOutputStream(), true);

        BufferedReader questionerIn = new BufferedReader(new InputStreamReader(questionerSocket.getInputStream()));
        PrintWriter questionerOut = new PrintWriter(questionerSocket.getOutputStream(), true);

        playGame(answererIn, answererOut, questionerIn, questionerOut);
        
        System.out.println("Game on port " + port + " is closing.");
        answererSocket.close();
        questionerSocket.close();
        gameSocket.close();
    }

    /**
     * Runs the main 20 Questions game loop.
     *
     * @param answererIn input stream from the answerer.
     * @param answererOut output stream to the answerer.
     * @param questionerIn input stream from the questioner.
     * @param questionerOut output stream to the questioner.
     * @throws IOException if a communication error occurs.
     */
    private static void playGame(BufferedReader answererIn, PrintWriter answererOut, BufferedReader questionerIn, PrintWriter questionerOut) throws IOException {
        answererOut.println("You are the answerer. Enter the object to be guessed:");
        String answer = answererIn.readLine();

        answererOut.println("Enter the category (Person, Place, or Thing):");
        String category = answererIn.readLine();

        questionerOut.println("You are the questioner. The category is: " + category);
        questionerOut.println("Enter your question (1/20):");

        int questionCount = 0;
        while (questionCount < 20) {
            String question = questionerIn.readLine();
            if (question == null) break;

            if (question.strip().toLowerCase().contains(answer.toLowerCase())) {
                questionerOut.println("YOU GOT IT, CONGRATS!");
                answererOut.println("Game over! The questioner got it");
                break;
            }

            questionCount++;
            answererOut.println("Question " + questionCount + ": " + question);
            answererOut.println("Respond with the number: Yes (1), No (2), Maybe (3), Not Sure (4), Close Enough! (5):");

            String response = getValidResponse(answererIn, answererOut);
            questionerOut.println("Answer: " + response);

            if (response.equals("Close Enough!")) {
                questionerOut.println("The answerer says your close enough!");
                answererOut.println("Game over!");
                break;
            }

            if (questionCount == 20) {
                questionerOut.println("Game over! You ran out of questions.");
                answererOut.println("Game over! The questioner used all 20 questions.");
            } else {
                questionerOut.println("Enter your next question (" + (questionCount + 1) + "/20):");
            }
        }
    }

    /**
     * Prompts the answerer for a valid response and returns the corresponding text.
     *
     * @param in input stream from the answerer.
     * @param out output stream to the answerer.
     * @return the text version of the numeric response.
     * @throws IOException if input cannot be read.
     */
    private static String getValidResponse(BufferedReader in, PrintWriter out) throws IOException {
        while (true) {
            try {
                int response = Integer.parseInt(in.readLine());
                switch (response) {
                    case 1: return "Yes";
                    case 2: return "No";
                    case 3: return "Maybe";
                    case 4: return "Not Sure";
                    case 5: return "Close Enough!";
                    default:
                        out.println("Please enter a number between 1–5:");
                }
            } catch (NumberFormatException e) {
                out.println("Invalid input. Enter a number (1–5):");
            }
        }
    }
}
