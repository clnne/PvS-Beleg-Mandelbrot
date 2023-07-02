package Mandelbrot;

import io.github.cdimascio.dotenv.Dotenv;

import java.awt.Color;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class ApfelClient {
    Dotenv dotenv = Dotenv.configure()
        .directory("src/")
        .load();

    final String SERVER_IP = dotenv.get("SERVER_IP");
    final int SERVER_PORT = Integer.parseInt(dotenv.get("SERVER_PORT"));

    public static void main(String[] args) {
        // Erstelle einen neuen Client und starte die Berechnung
        ApfelClient client = new ApfelClient();
        client.startCalculation(null);
    }

    public void startCalculation(ApfelView view) {
        try {
            Socket socket = new Socket(SERVER_IP, SERVER_PORT);
            System.out.println("[+] Connected to server.");

            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

            // Empfange Daten vom Server
            Object obj;
            while ((obj = inputStream.readObject()) != null) {
                if (obj instanceof Color[][]) {
                    // Update das Bild, sofern es ein Bild ist
                    Color[][] image = (Color[][]) obj;
                    if (view != null) {
                        view.updateImage(image);
                    }
                } else if (obj instanceof String) {
                    // Starte die nächste Iteration, sofern es ein String ist
                    String message = (String) obj;
                    if (message.equals("NextIteration")) {
                        outputStream.writeObject("NextIteration");
                        outputStream.flush();
                    } else if (message.equals("Finish")) {
                        break;
                    }
                }
            }

            socket.close();
            System.out.println("[-] Disconnected from server.");

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("[-] Connection to server failed.");
            System.out.println("[-] Server-Info: " + SERVER_IP + ":" + SERVER_PORT);
            e.printStackTrace();
        }
    }
}
