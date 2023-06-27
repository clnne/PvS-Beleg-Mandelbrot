package Mandelbrot;

import java.awt.*;
import java.io.*;
import java.net.*;

public class ApfelServer {
    private ServerSocket serverSocket;
    final int max_iter = 5000;
    final double max_betrag2 = 4;

    public static void main(String[] args) {
        ApfelServer server = new ApfelServer();
        server.start();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(1337);
            System.out.println("Server started. Waiting for the client to connect...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client" + clientSocket.getInetAddress() + " connected");

                Thread clientThread = new Thread(() -> handleClient(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket clientSocket) {
        try {
            ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());
            ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());

            int xpix = (int) inputStream.readObject();
            int ypix = (int) inputStream.readObject();

            double xmin = -1.666, xmax = 1, ymin = -1, ymax = 1; // Parameter des Ausschnitts
            double cr = -0.743643887036151, ci = 0.131825904205330;
            double zoomRate = 1.5;
            int maxIter = 5000;

            for (int i = 1; i < 65; i++) {
                System.out.println(i + " Vergrößerung: " + 2.6 / (xmax - xmin) + " xmin: " + xmin + " xmax: " + xmax);

                Color[][] image = new Color[xpix][ypix];

                calculateImage(xpix, ypix, xmin, xmax, ymin, ymax, image);

                outputStream.writeObject(image);
                outputStream.flush();

                Thread.sleep(50); // unnötig?

                double xdim = xmax - xmin;
                double ydim = ymax - ymin;
                xmin = cr - xdim / 2 / zoomRate;
                xmax = cr + xdim / 2 / zoomRate;
                ymin = ci - ydim / 2 / zoomRate;
                ymax = ci + ydim / 2 / zoomRate;

                // Sende Bestätigung an den client, um die nächste Iteration zu starten
                outputStream.writeObject("NextIteration");
                outputStream.flush();
            }

            outputStream.writeObject("Finish");
            outputStream.flush();

            clientSocket.close();
            System.out.println("Client disconnected: " + clientSocket.getInetAddress());
        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void calculateImage(int xpix, int ypix, double xmin, double xmax, double ymin, double ymax, Color[][] image) {
        Thread[] calculationThreads = new Thread[ypix];
        int numThreads = Runtime.getRuntime().availableProcessors();

        for (int y = 0; y < ypix; y++) {
            final int yPos = y;
            calculationThreads[y] = new Thread(() -> run(xpix, ypix, xmin, xmax, ymin, ymax, image, yPos));
            calculationThreads[y].start();

            // wir begrenzen die anzahl der threads
            if (y % numThreads == 0) {
                joinThreads(calculationThreads, y - numThreads);
            }
        }

        // warte auf Abschluss aller threads
        joinThreads(calculationThreads, ypix - 1);
    }

    private void joinThreads(Thread[] threads, int endIndex) {
        for (int i = 0; i <= endIndex; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void run(int xpix, int ypix, double xmin, double xmax, double ymin, double ymax, Color[][] bild, int y) {
        double c_im = ymin + (ymax - ymin) * y / ypix;

        for (int x = 0; x < xpix; x++) {
            double c_re = xmin + (xmax - xmin) * x / xpix;
            int iter = calc(c_re, c_im);
            Color pix = farbwert(iter);
            bild[x][y] = pix;
        }
    }


    public int calc(double cr, double ci) {
        int iter;
        double zr, zi, zr2 = 0, zi2 = 0, zri = 0, betrag2 = 0;

        for (iter = 0; iter < max_iter && betrag2 <= max_betrag2; iter++) {
            zr = zr2 - zi2 + cr;
            zi = zri + zri + ci;

            zr2 = zr * zr;
            zi2 = zi * zi;
            zri = zr * zi;
            betrag2 = zr2 + zi2;
        }
        return iter;
    }

    Color farbwert(int iter) {
        boolean farbe = true;

        final int[][] farben = {
                {1, 255, 255, 255}, // Hohe Iterationszahlen sollen hell,
                {30, 10, 255, 40}, //
                {300, 10, 10, 40}, // die etwas niedrigeren dunkel,
                {500, 205, 60, 40}, // die "Spiralen" rot
                {850, 120, 140, 255}, // und die "Arme" hellblau werden.
                {1000, 50, 30, 255}, // Innen kommt ein dunkleres Blau,
                {1100, 0, 255, 0}, // dann grelles Gr�n
                {1997, 20, 70, 20}, // und ein dunkleres Gr�n.
                {max_iter, 0, 0, 0}
        };

        if (!farbe) {
            if (iter == max_iter) return Color.BLACK;
            else return Color.RED;
        }

        int[] F = new int[3];
        for (int i = 1; i < farben.length - 1; i++) {
            if (iter < farben[i][0]) {
                int iterationsInterval = farben[i - 1][0] - farben[i][0];
                double gewichtetesMittel = (iter - farben[i][0]) / (double) iterationsInterval;

                for (int f = 0; f < 3; f++) {
                    int farbInterval = farben[i - 1][f + 1] - farben[i][f + 1];
                    F[f] = (int) (gewichtetesMittel * farbInterval) + farben[i][f + 1];
                }
                return new Color(F[0], F[1], F[2]);
            }
        }
        return Color.BLACK;
    }
}