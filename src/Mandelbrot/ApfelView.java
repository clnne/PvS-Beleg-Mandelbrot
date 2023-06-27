package Mandelbrot;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class ApfelView {
    private int xpix, ypix;
    private BufferedImage image;
    private ApfelPanel ap;

    public static void main(String[] args) {
        ApfelView v = new ApfelView(640, 480);
    }

    public ApfelView(int xpix, int ypix) {
        this.xpix = xpix;
        this.ypix = ypix;
        image = new BufferedImage(xpix, ypix, BufferedImage.TYPE_INT_RGB);
        JFrame f = new JFrame();
        ap = new ApfelPanel();
        JButton sb = new JButton("Start");
        sb.addActionListener(e -> {
            sb.setEnabled(false);
            // über Thread da sonst die gui blockiert
            Thread calculationThread = new Thread(() -> {
                ApfelClient client = new ApfelClient();
                client.startCalculation(xpix, ypix, ApfelView.this);
                SwingUtilities.invokeLater(() -> sb.setEnabled(true));
            });
            calculationThread.start();
        });

        JPanel sp = new JPanel(new FlowLayout());
        sp.add(sb);

        f.setContentPane(ap);
        f.setLayout(new BorderLayout());
        f.add(sp, BorderLayout.SOUTH);
        f.setSize(xpix, ypix + 100);
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void updateImage(Color[][] c) {
        for (int y = 0; y < ypix; y++) {
            for (int x = 0; x < xpix; x++) {
                image.setRGB(x, y, c[x][y].getRGB());
            }
        }
        ap.repaint();
    }

    class ApfelPanel extends JPanel {
        public void paint(Graphics g) {
            g.drawImage(image, 0, 0, null);
        }
    }
}
