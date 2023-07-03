package Mandelbrot;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.zip.GZIPOutputStream;


public class ApfelServer {
    private ServerSocket serverSocket;
    final int MAX_ITERATIONS = 5000;
    final double MAX_BETRAG = 4;
    final int MAX_ZOOM_COUNT = 720;
    final double ZOOM_RATE = 1.050;
    final int SERVER_PORT = 1337;

    final int numThreads = Runtime.getRuntime().availableProcessors() * 4;

    public Color[] pixelColors;

    public static void main(String[] args) {
        Util.createDirectory(Util.recordingPath);
        Util.createDirectory(Util.videoPath);
        Util.createDirectory(Util.imagePath);

        ApfelServer server = new ApfelServer();
        server.pixelColors = new Color[server.MAX_ITERATIONS + 101];
        server.initPixelColors();
        server.start();
    }

    public void initPixelColors() {
        for (int i = 0; i <= MAX_ITERATIONS + 100; i++) {
            pixelColors[i] = Color.getHSBColor((float)i / (float)MAX_ITERATIONS * 20.0f, 1.0f, 1.0f);
        }
        System.out.println("[+] Pixel colors initialized.");
    }

    public Color getPixelColor(int i) {
        return pixelColors[i];
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("[+] Server started. Waiting for the client to connect...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[+] Client connected from " + clientSocket.getInetAddress() + ".");

                Thread clientThread = new Thread(() -> handleClient(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket clientSocket) {
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(new GZIPOutputStream(clientSocket.getOutputStream()));

            // Parameter des Ausschnitts
            double xmin = -1.666, xmax = 1, ymin = -1, ymax = 1;
            double cr = -0.743643887036151, ci = 0.131825904205330;

            Rectangle r = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
            System.out.println("[+] Starting calculation with " + numThreads + " threads on resolution " + r.width + "x" + r.height+ ".");

            for (int i = 1; i <= MAX_ZOOM_COUNT; i++) {
                System.out.println("[" + i + "/" + MAX_ZOOM_COUNT + "] Vergrößerung: " + 2.6 / (xmax - xmin) + " | xmin: " + xmin + " | xmax: " + xmax );


                Color[][] image = new Color[r.width][r.height];

                long startTimeCalculation = System.nanoTime();
                    calculateImage(r.width, r.height, xmin, xmax, ymin, ymax, image);
                long endTimeCalculation = System.nanoTime();
                long calculationDuration = (endTimeCalculation - startTimeCalculation) / 1000000;
                //System.out.println("[-] Calculation time: " + calculationDuration + "ms");


                long startTimeImageWrite = System.nanoTime();
                    outputStream.writeObject(image);
                    outputStream.flush();
                long endTimeImageWrite = System.nanoTime();
                long imageWriteDuration = (endTimeImageWrite - startTimeImageWrite) / 1000000;
                //System.out.println("[-] Image write time: " + imageWriteDuration + "ms");


                double xdim = xmax - xmin;
                double ydim = ymax - ymin;
                xmin = cr - xdim / 2 / ZOOM_RATE;
                xmax = cr + xdim / 2 / ZOOM_RATE;
                ymin = ci - ydim / 2 / ZOOM_RATE;
                ymax = ci + ydim / 2 / ZOOM_RATE;


                // Sende Bestätigung an den client, um die nächste Iteration zu starten
                outputStream.writeObject("NextIteration");
                outputStream.flush();

                // Zeige Berechnungsdauer an
                System.out.println("[?] Berechnungszeit: " + (calculationDuration + imageWriteDuration) + "ms");

            }

            outputStream.writeObject("Finish");
            outputStream.flush();

            clientSocket.close();
            System.out.println("[-] Client " + clientSocket.getInetAddress() + " disconnected.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void calculateImage(int xpix, int ypix, double xmin, double xmax, double ymin, double ymax, Color[][] image) {
        Thread[] calculationThreads = new Thread[ypix];

        for (int y = 0; y < ypix; y++) {
            final int yPos = y;
            calculationThreads[y] = new Thread(() -> run(xpix, ypix, xmin, xmax, ymin, ymax, image, yPos));
            calculationThreads[y].setPriority(Thread.MAX_PRIORITY);
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
            bild[x][y] = getPixelColor(iter);
        }
    }


    public int calc(double cr, double ci) {
        int iter;
        double zr, zi, zr2 = 0, zi2 = 0, zri = 0, betrag2 = 0;

        for (iter = 0; iter <= MAX_ITERATIONS && betrag2 <= MAX_BETRAG; iter++) {
            zr = zr2 - zi2 + cr;
            zi = zri + zri + ci;

            zr2 = zr * zr;
            zi2 = zi * zi;
            zri = zr * zi;
            betrag2 = zr2 + zi2;
        }
        return iter;
    }
}