package Mandelbrot;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class ApfelView {
    private int xpix, ypix;
    private BufferedImage image;
    private ApfelPanel apfelPanel;

    public static void main(String[] args) {
        ApfelView view = new ApfelView(Util.RESOLUTION_WIDTH, Util.RESOLUTION_HEIGHT);
    }

    public ApfelView(int xpix, int ypix) {
        // Erstelle das Bild
        this.xpix = xpix;
        this.ypix = ypix;
        image = new BufferedImage(xpix, ypix, BufferedImage.TYPE_INT_RGB);

        // Erstelle die GUI und füge den Start-Button hinzu
        apfelPanel = new ApfelPanel();
        JFrame frame = new JFrame();
        frame.setTitle("Mandelbrot");
        frame.setSize(xpix, ypix + 80);
        frame.setResizable(true);
        JPanel panelBottom = new JPanel(new FlowLayout());

        JButton startButton = new JButton("Mandelbrot berechnen");
        startButton.addActionListener(e -> {
            startButton.setEnabled(false);

            // über Thread da sonst die gui blockiert
            Thread calculationThread = new Thread(() -> {
                ApfelClient client = new ApfelClient();
                client.startCalculation(xpix, ypix, ApfelView.this);
                SwingUtilities.invokeLater(() -> panelBottom.setEnabled(true));
            });
            calculationThread.setPriority(Thread.MAX_PRIORITY);
            calculationThread.start();

            SwingUtilities.invokeLater(() -> panelBottom.setEnabled(true));
        });
        panelBottom.add(startButton);

        // Erstelle das Fenster mit allen Buttons
        frame.setContentPane(apfelPanel);
        frame.setLayout(new BorderLayout());
        frame.add(panelBottom, BorderLayout.SOUTH);

        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void updateImage(Color[][] c) {
        for (int y = 0; y < ypix; y++) {
            for (int x = 0; x < xpix; x++) {
                image.setRGB(x, y, c[x][y].getRGB());
            }
        }
        apfelPanel.repaint();
    }

    private int counter = 0;
    class ApfelPanel extends JPanel {
        public void paint(Graphics g) {
            g.drawImage(image, 0, 0, null);

            // Speichere das Bild
            Util.saveImage(image, counter++);
        }
    }
}
