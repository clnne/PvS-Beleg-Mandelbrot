package Mandelbrot;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.zip.GZIPOutputStream;


public class ApfelServer {
    private ServerSocket serverSocket;
    final int MAX_ITERATIONS = 5000;
    final double MAX_BETRAG = 4;
    final int MAX_ZOOM_COUNT = 240;
    final double ZOOM_RATE = 1.1;
    final int SERVER_PORT = 1337;

    final int numThreads = Runtime.getRuntime().availableProcessors() * 4;

    public static void main(String[] args) {
        Util.createDirectory(Util.recordingPath);
        Util.createDirectory(Util.videoPath);
        Util.createDirectory(Util.imagePath);

        ApfelServer server = new ApfelServer();
        server.start();
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

            System.out.println("[+] Starting calculation with " + numThreads + " threads on resolution " + Util.RESOLUTION_WIDTH + "x" + Util.RESOLUTION_HEIGHT+ ".");

            for (int i = 1; i <= MAX_ZOOM_COUNT; i++) {
                System.out.println("[" + i + "/" + MAX_ZOOM_COUNT + "] Vergrößerung: " + 2.6 / (xmax - xmin) + " | xmin: " + xmin + " | xmax: " + xmax );

                Color[][] image = new Color[Util.RESOLUTION_WIDTH][Util.RESOLUTION_HEIGHT];

                long startTimeCalculation = System.nanoTime();
                    calculateImage(Util.RESOLUTION_WIDTH, Util.RESOLUTION_HEIGHT, xmin, xmax, ymin, ymax, image);
                long endTimeCalculation = System.nanoTime();
                System.out.println("[-] Calculation time: " + (endTimeCalculation - startTimeCalculation) / 1000000 + "ms");


                long startTimeImageWrite = System.nanoTime();
                    outputStream.writeObject(image);
                    outputStream.flush();
                long endTimeImageWrite = System.nanoTime();
                System.out.println("[-] Image write time: " + (endTimeImageWrite - startTimeImageWrite) / 1000000 + "ms");


                long startTimeZoom = System.nanoTime();
                    double xdim = xmax - xmin;
                    double ydim = ymax - ymin;
                    xmin = cr - xdim / 2 / ZOOM_RATE;
                    xmax = cr + xdim / 2 / ZOOM_RATE;
                    ymin = ci - ydim / 2 / ZOOM_RATE;
                    ymax = ci + ydim / 2 / ZOOM_RATE;
                long endTimeZoom = System.nanoTime();
                System.out.println("[-] Zoom-Calculation time: " + (endTimeZoom - startTimeZoom) / 1000000 + "ms");


                // Sende Bestätigung an den client, um die nächste Iteration zu starten
                long startTimeNextIteration = System.nanoTime();
                    outputStream.writeObject("NextIteration");
                    outputStream.flush();
                long endTimeNextIteration = System.nanoTime();
                System.out.println("[-] Next iteration time: " + (endTimeNextIteration - startTimeNextIteration) / 1000000 + "ms");
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
            Color pix = farbwert(iter);
            bild[x][y] = pix;
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

    Color farbwert(int iter) {
        /*boolean farbe = true;

        final int[][] farben = {
            {1, 255, 255, 255}, // Hohe Iterationszahlen sollen hell,
            {30, 10, 255, 40}, //
            {300, 10, 10, 40}, // die etwas niedrigeren dunkel,
            {500, 205, 60, 40}, // die "Spiralen" rot
            {850, 120, 140, 255}, // und die "Arme" hellblau werden.
            {1000, 50, 30, 255}, // Innen kommt ein dunkleres Blau,
            {1100, 0, 255, 0}, // dann grelles Grün
            {1997, 20, 70, 20}, // und ein dunkleres Grün.
            {MAX_ITERATIONS, 0, 0, 0}
        };

        if (!farbe) {
            if (iter == MAX_ITERATIONS) return Color.BLACK;
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
        */
        float hsb=(float)iter / (float)MAX_ITERATIONS * 20.0f;
        Color color;
        if(hsb>0.999f){ //inneres rot
            color = Color.getHSBColor(0.666f, 1.0f, 1.0f);//zu blau
        }
        else if(hsb<0.05f){//äußeres rot
            color = Color.getHSBColor(0.333f, 1.0f, 1.0f);//zu grün
        }
        else {
            color = Color.getHSBColor(hsb, 1.0f, 1.0f);
        }
        return color;
    }
}