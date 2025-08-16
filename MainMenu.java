
// FileName: MainMenu.java
// FileContents:
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainMenu extends JFrame {

    public MainMenu() {
        setTitle("CPU Scheduling Algorithms");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window
        setLayout(new GridLayout(5, 1, 10, 10)); // 5 rows, 1 column, with gaps

        // Create buttons for each scheduling algorithm
        JButton btnFCFS = new JButton("FCFS Scheduling");
        JButton btnSJF = new JButton("SJF Scheduling");
        JButton btnPriority = new JButton("Priority Scheduling");
        JButton btnRoundRobin = new JButton("Round Robin Scheduling");
        JButton btnExit = new JButton("Exit");

        // Add action listeners to the buttons
        btnFCFS.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new FCFS().setVisible(true);
            }
        });

        btnSJF.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new SJF().setVisible(true);
            }
        });

        btnPriority.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new PriorityScheduling().setVisible(true);
            }
        });

        btnRoundRobin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new RoundRobinScheduling().setVisible(true);
            }
        });

        btnExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0); // Exit the application
            }
        });

        // Add buttons to the frame
        add(btnFCFS);
        add(btnSJF);
        add(btnPriority);
        add(btnRoundRobin);
        add(btnExit);
    }

    public static void main(String[] args) {
        // Ensure GUI updates are done on the Event Dispatch Thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainMenu().setVisible(true);
            }
        });
    }
}
