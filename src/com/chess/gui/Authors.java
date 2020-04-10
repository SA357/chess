package com.chess.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Authors extends JDialog {
    public Authors(final JFrame frame,
                   final boolean modal) {
        super(frame, modal);
        final JPanel myPanel = new JPanel(new GridLayout(0,1));
        getContentPane().add(myPanel);
        getContentPane().setPreferredSize(new Dimension(100,200));
        JLabel jLabel1 = new JLabel("Смирнов Артемий"); // TODO
        JLabel jLabel2 = new JLabel("Никикта Чекмарёв");
        JLabel jLabel3 = new JLabel("Боталов Матвей");
        myPanel.add(jLabel1);
        myPanel.add(jLabel2);
        myPanel.add(jLabel3);
        JTextArea textArea = new JTextArea("Смирнов Артемий\n Никикта Чекмарёв\n Боталов Матвей");
       // myPanel.add(textArea);
        final JButton okButton = new JButton("OK");
        okButton.setSize(3,3);
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Authors.this.setVisible(false);
            }
        });

        //myPanel.add(okButton);
        setLocationRelativeTo(frame);
        pack();
        setVisible(false);
    }

    void promptUser() {
        setVisible(true);
        repaint();
    }
}
