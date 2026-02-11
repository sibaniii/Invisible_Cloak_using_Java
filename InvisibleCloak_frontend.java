package frontend;
import backend.InvisibleCloakEngine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class InvisibleCloakGUI extends JFrame implements ActionListener {

    JButton startBtn, stopBtn, colorBtn;
    JLabel videoLabel, statusLabel;

    InvisibleCloakEngine engine;

    public InvisibleCloakGUI() {

        setTitle("Invisible Cloak - OpenCV");
        setSize(900, 650);
        setLayout(new BorderLayout());

        // ---------- TOP PANEL ----------
        JPanel topPanel = new JPanel(new FlowLayout());

        startBtn = new JButton("Start Camera");
        stopBtn = new JButton("Stop Camera");
        colorBtn = new JButton("Red Cloak");

        startBtn.addActionListener(this);
        stopBtn.addActionListener(this);
        colorBtn.addActionListener(this);

        topPanel.add(startBtn);
        topPanel.add(stopBtn);
        topPanel.add(colorBtn);

        add(topPanel, BorderLayout.NORTH);

        // ---------- VIDEO PANEL ----------
        videoLabel = new JLabel("Camera Feed");
        videoLabel.setHorizontalAlignment(JLabel.CENTER);
        videoLabel.setOpaque(true);
        videoLabel.setBackground(Color.BLACK);

        add(videoLabel, BorderLayout.CENTER);

        // ---------- STATUS BAR ----------
        statusLabel = new JLabel("Status: Ready");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        add(statusLabel, BorderLayout.SOUTH);

        // ---------- BACKEND ENGINE ----------
        engine = new InvisibleCloakEngine(videoLabel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == startBtn) {
            statusLabel.setText("Status: Starting camera (step away for 3 sec)");
            engine.startCamera();
        }

        if (e.getSource() == stopBtn) {
            statusLabel.setText("Status: Camera stopped");
            engine.stopCamera();
        }

        if (e.getSource() == colorBtn) {
            statusLabel.setText("Cloak color: RED");
        }
    }

    public static void main(String[] args) {
        new InvisibleCloakGUI();
    }
}
