package Mandelbrot;

import java.awt.Color;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ApfelClient {

    final String SERVER_IP = "192.168.178.88";
    final int SERVER_PORT = 1337;
    final double ZOOM_RATE = 2;

    public static void main(String[] args) {
        // Erstelle einen neuen Client und starte die Berechnung
        ApfelClient client = new ApfelClient();
        client.startCalculation(640, 480, null);
    }

    public void startCalculation(int xpix, int ypix, ApfelView view) {
        try {
            Socket socket = new Socket(SERVER_IP, SERVER_PORT);
            System.out.println("[+] Connected to server.");

            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

            outputStream.writeObject(xpix);
            outputStream.writeObject(ypix);
            outputStream.writeObject(ZOOM_RATE);
            outputStream.flush();

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
            System.out.println("[?] Connection to server failed.");
            e.printStackTrace();
        }
    }
}
