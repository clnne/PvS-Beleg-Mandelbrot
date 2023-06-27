package Mandelbrot;

import java.awt.Color;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ApfelClient {
    public static void main(String[] args) {
        ApfelClient client = new ApfelClient();
        client.startCalculation(640, 480, null);
    }

    public void startCalculation(int xpix, int ypix, ApfelView view) {
        try {
            Socket socket = new Socket("10.0.1.1", 1337);
            System.out.println("Connected to server.");

            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

            outputStream.writeObject(xpix);
            outputStream.writeObject(ypix);
            outputStream.flush();

            Object obj;
            while ((obj = inputStream.readObject()) != null) {
                if (obj instanceof Color[][]) {
                    Color[][] image = (Color[][]) obj;
                    if (view != null) {
                        view.updateImage(image);
                    }
                } else if (obj instanceof String) {
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
            System.out.println("Disconnected from server.");

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
