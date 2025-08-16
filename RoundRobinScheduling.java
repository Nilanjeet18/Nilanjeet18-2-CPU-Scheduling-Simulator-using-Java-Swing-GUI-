import javax.swing.*; // Primitive
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

class ProcessData {
    String pid;
    int bt, at, wt, rt, ct, tat;
    int remainingBt;
    int index;
    boolean completed = false;
    boolean firstExecution = true;
}

public class RoundRobinScheduling extends JFrame {
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextArea ganttArea;
    private JTextField txtPid, txtBt, txtAt, txtQuantum;
    private List<ProcessData> processList = new ArrayList<>();

    // Middle section components
    private JLabel cpuLabel;
    private List<JProgressBar> progressBars = new ArrayList<>();
    private List<JLabel> remainingLabels = new ArrayList<>();
    private List<JLabel> waitingLabels = new ArrayList<>();
    private JPanel middlePanel;

    public RoundRobinScheduling() {
        setTitle("Round Robin Scheduling (Preemptive) - Animated");
        setSize(1100, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.BLACK);

        // Top input panel
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.setBackground(Color.BLACK);
        txtPid = new JTextField(5);
        txtBt = new JTextField(5);
        txtAt = new JTextField(5);
        txtQuantum = new JTextField(5);
        JButton btnAdd = new JButton("Add Process");
        JButton btnCalc = new JButton("Run Animation");

        JLabel pidLabel = new JLabel("PID:");
        pidLabel.setForeground(Color.WHITE);
        JLabel btLabel = new JLabel("BT:");
        btLabel.setForeground(Color.WHITE);
        JLabel atLabel = new JLabel("AT:");
        atLabel.setForeground(Color.WHITE);
        JLabel qLabel = new JLabel("Quantum:");
        qLabel.setForeground(Color.WHITE);

        inputPanel.add(pidLabel);
        inputPanel.add(txtPid);
        inputPanel.add(btLabel);
        inputPanel.add(txtBt);
        inputPanel.add(atLabel);
        inputPanel.add(txtAt);
        inputPanel.add(qLabel);
        inputPanel.add(txtQuantum);
        inputPanel.add(btnAdd);
        inputPanel.add(btnCalc);

        add(inputPanel, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(new String[] { "PID", "BT", "AT", "WT", "RT", "CT", "TAT" }, 0);
        table = new JTable(tableModel);
        table.setBackground(Color.BLACK);
        table.setForeground(Color.WHITE);
        table.getTableHeader().setBackground(Color.DARK_GRAY);
        table.getTableHeader().setForeground(Color.WHITE);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.getViewport().setBackground(Color.BLACK);

        // Middle panel
        middlePanel = new JPanel();
        middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));
        middlePanel.setBackground(Color.BLACK);
        cpuLabel = new JLabel("CPU: Idle", SwingConstants.CENTER);
        cpuLabel.setFont(new Font("Arial", Font.BOLD, 16));
        cpuLabel.setForeground(Color.WHITE);
        middlePanel.add(cpuLabel);

        JScrollPane middleScroll = new JScrollPane(middlePanel);
        middleScroll.setMinimumSize(new Dimension(300, 200));
        middleScroll.getViewport().setBackground(Color.BLACK);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScroll, middleScroll);
        splitPane.setDividerLocation(650);
        splitPane.setResizeWeight(0.7);
        splitPane.setBackground(Color.BLACK);
        add(splitPane, BorderLayout.CENTER);

        // Gantt chart area
        ganttArea = new JTextArea(4, 20);
        ganttArea.setFont(new Font("Monospaced", Font.BOLD, 14));
        ganttArea.setEditable(false);
        ganttArea.setBackground(Color.BLACK);
        ganttArea.setForeground(Color.GREEN);
        JScrollPane ganttScrollPane = new JScrollPane(ganttArea);
        ganttScrollPane.setBackground(Color.BLACK);
        add(ganttScrollPane, BorderLayout.SOUTH);

        // Add process button
        btnAdd.addActionListener((ActionEvent e) -> {
            try {
                ProcessData p = new ProcessData();
                p.pid = txtPid.getText().trim();
                p.bt = Integer.parseInt(txtBt.getText().trim());
                p.at = Integer.parseInt(txtAt.getText().trim());
                p.remainingBt = p.bt;
                p.index = processList.size();
                processList.add(p);

                tableModel.addRow(new Object[] { p.pid, p.bt, p.at, "", "", "", "" });

                JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                rowPanel.setBackground(Color.BLACK);
                JLabel pidLabel_row = new JLabel(p.pid);
                pidLabel_row.setForeground(Color.WHITE);
                rowPanel.add(pidLabel_row);
                JProgressBar pb = new JProgressBar(0, p.bt);
                pb.setStringPainted(true);
                pb.setPreferredSize(new Dimension(150, 20));
                progressBars.add(pb);
                rowPanel.add(pb);

                JLabel remLabel = new JLabel("Remaining: " + p.bt);
                remLabel.setForeground(Color.WHITE);
                remainingLabels.add(remLabel);
                rowPanel.add(remLabel);

                JLabel waitLabel = new JLabel("WT: 0");
                waitLabel.setForeground(Color.WHITE);
                waitingLabels.add(waitLabel);
                rowPanel.add(waitLabel);

                middlePanel.add(rowPanel);
                middlePanel.revalidate();

                txtPid.setText("");
                txtBt.setText("");
                txtAt.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid input!");
            }
        });

        // Run animation button
        btnCalc.addActionListener((ActionEvent e) -> runRoundRobinAnimation());
    }

    private void runRoundRobinAnimation() {
        if (processList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No processes to run!");
            return;
        }

        int quantum;
        try {
            quantum = Integer.parseInt(txtQuantum.getText().trim());
            if (quantum <= 0)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Quantum!");
            return;
        }

        int finalQuantum = quantum;

        new Thread(() -> {
            int currentTime = 0;
            int completed = 0;
            Queue<ProcessData> readyQueue = new LinkedList<>();
            StringBuilder gantt = new StringBuilder("Gantt Chart:\n");

            while (completed < processList.size()) {
                // Add newly arrived processes
                for (ProcessData p : processList) {
                    if (!p.completed && p.at <= currentTime && !readyQueue.contains(p)) {
                        readyQueue.add(p);
                    }
                }

                if (readyQueue.isEmpty()) {
                    currentTime++;
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    continue;
                }

                ProcessData current = readyQueue.poll();

                if (current.firstExecution) {
                    current.rt = currentTime - current.at;
                    current.firstExecution = false;
                }

                int executionTime = Math.min(finalQuantum, current.remainingBt);

                for (int t = 1; t <= executionTime; t++) {
                    final int elapsed = t;
                    final int globalTime = currentTime + elapsed;

                    SwingUtilities.invokeLater(() -> {
                        cpuLabel.setText("CPU: " + current.pid);
                        progressBars.get(current.index).setValue(current.bt - current.remainingBt + elapsed);
                        current.remainingBt -= 1;
                        remainingLabels.get(current.index).setText("Remaining: " + current.remainingBt);
                        waitingLabels.get(current.index).setText("WT: " + current.wt);

                        gantt.append("| ").append(current.pid).append(" ");
                        ganttArea.setText(gantt.toString());
                        updateTableLive(current, globalTime);
                    });

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }

                    // Increase waiting time for others in queue
                    for (ProcessData p : readyQueue) {
                        p.wt++;
                    }
                }

                currentTime += executionTime;

                // Add newly arrived processes after this execution
                for (ProcessData p : processList) {
                    if (!p.completed && p.at <= currentTime && !readyQueue.contains(p)) {
                        readyQueue.add(p);
                    }
                }

                if (current.remainingBt > 0) {
                    readyQueue.add(current);
                } else {
                    current.ct = currentTime;
                    current.tat = current.ct - current.at;
                    current.completed = true;
                    completed++;
                }
            }

            SwingUtilities.invokeLater(() -> {
                cpuLabel.setText("CPU: Idle");
                tableModel.setRowCount(0);
                for (ProcessData p : processList) {
                    tableModel.addRow(new Object[] { p.pid, p.bt, p.at, p.wt, p.rt, p.ct, p.tat });
                }
                ganttArea.append("|\nDone!");
            });

        }).start();
    }

    private void updateTableLive(ProcessData currentProcess, int time) {
        tableModel.setRowCount(0);
        for (ProcessData p : processList) {
            if (p == currentProcess) {
                tableModel.addRow(new Object[] { p.pid, p.bt, p.at, p.wt, p.rt, time, "" });
            } else {
                tableModel.addRow(new Object[] { p.pid, p.bt, p.at, p.wt, p.rt,
                        p.ct > 0 ? p.ct : "", p.tat > 0 ? p.tat : "" });
            }
        }
    }

    public void execute() {
        SwingUtilities.invokeLater(() -> this.setVisible(true));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RoundRobinScheduling().execute());
    }
}
