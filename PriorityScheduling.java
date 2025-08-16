import javax.swing.*; // Non-Premititve
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

class ProcessData {
    String pid;
    int bt, at, wt, rt, ct, tat, priority;
    int remainingBt;
    int index;
    boolean completed = false;
}

public class PriorityScheduling extends JFrame {
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextArea ganttArea;
    private JTextField txtPid, txtBt, txtAt, txtPriority;
    private List<ProcessData> processList = new ArrayList<>();

    // Middle section components
    private JLabel cpuLabel;
    private List<JProgressBar> progressBars = new ArrayList<>();
    private List<JLabel> remainingLabels = new ArrayList<>();
    private List<JLabel> waitingLabels = new ArrayList<>();
    private JPanel middlePanel;

    public PriorityScheduling() {
        setTitle("Priority Scheduling (Non-preemptive) - Animated");
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
        txtPriority = new JTextField(5);
        JButton btnAdd = new JButton("Add Process");
        JButton btnCalc = new JButton("Run Animation");

        JLabel pidLabel = new JLabel("PID:");
        pidLabel.setForeground(Color.WHITE);
        JLabel btLabel = new JLabel("BT:");
        btLabel.setForeground(Color.WHITE);
        JLabel atLabel = new JLabel("AT:");
        atLabel.setForeground(Color.WHITE);
        JLabel prLabel = new JLabel("Priority:");
        prLabel.setForeground(Color.WHITE);

        inputPanel.add(pidLabel);
        inputPanel.add(txtPid);
        inputPanel.add(btLabel);
        inputPanel.add(txtBt);
        inputPanel.add(atLabel);
        inputPanel.add(txtAt);
        inputPanel.add(prLabel);
        inputPanel.add(txtPriority);
        inputPanel.add(btnAdd);
        inputPanel.add(btnCalc);

        add(inputPanel, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(new String[] { "PID", "BT", "AT", "Priority", "WT", "RT", "CT", "TAT" }, 0);
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
                p.priority = Integer.parseInt(txtPriority.getText().trim());
                p.remainingBt = p.bt;
                p.index = processList.size();
                processList.add(p);

                tableModel.addRow(new Object[] { p.pid, p.bt, p.at, p.priority, "", "", "", "" });

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
                txtPriority.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid input!");
            }
        });

        // Run animation button
        btnCalc.addActionListener((ActionEvent e) -> runPriorityAnimation());
    }

    private void runPriorityAnimation() {
        if (processList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No processes to run!");
            return;
        }

        new Thread(() -> {
            int currentTime = 0;
            int completed = 0;
            StringBuilder gantt = new StringBuilder("Gantt Chart:\n");

            while (completed < processList.size()) {
                int finalCurrentTime = currentTime;

                // Choose highest priority process (lowest priority number is higher priority)
                ProcessData next = processList.stream()
                        .filter(p -> !p.completed && p.at <= finalCurrentTime)
                        .min(Comparator.comparingInt((ProcessData p) -> p.priority)
                                .thenComparingInt(p -> p.at))
                        .orElse(null);

                if (next == null) {
                    currentTime++;
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    continue;
                }

                next.rt = currentTime - next.at;
                next.wt = currentTime - next.at;

                int startTime = currentTime;
                for (int t = 1; t <= next.bt; t++) {
                    final int elapsed = t;
                    final int globalTime = startTime + elapsed;

                    SwingUtilities.invokeLater(() -> {
                        cpuLabel.setText("CPU: " + next.pid + " (P=" + next.priority + ")");
                        progressBars.get(next.index).setValue(elapsed);
                        next.remainingBt = next.bt - elapsed;
                        remainingLabels.get(next.index).setText("Remaining: " + next.remainingBt);
                        waitingLabels.get(next.index).setText("WT: " + (next.wt + elapsed));

                        gantt.append("| ").append(next.pid).append(" ");
                        ganttArea.setText(gantt.toString());
                        updateTableLive(next, globalTime);
                    });

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }

                currentTime = startTime + next.bt;
                next.ct = currentTime;
                next.tat = next.ct - next.at;
                next.completed = true;
                completed++;
            }

            SwingUtilities.invokeLater(() -> {
                cpuLabel.setText("CPU: Idle");
                tableModel.setRowCount(0);
                for (ProcessData p : processList) {
                    tableModel.addRow(new Object[] { p.pid, p.bt, p.at, p.priority, p.wt, p.rt, p.ct, p.tat });
                }
                ganttArea.append("|\nDone!");
            });

        }).start();
    }

    private void updateTableLive(ProcessData currentProcess, int time) {
        tableModel.setRowCount(0);
        for (ProcessData p : processList) {
            if (p == currentProcess) {
                tableModel.addRow(new Object[] { p.pid, p.bt, p.at, p.priority, p.wt, p.rt, time, "" });
            } else {
                tableModel.addRow(new Object[] { p.pid, p.bt, p.at, p.priority,
                        p.wt, p.rt, p.ct > 0 ? p.ct : "", p.tat > 0 ? p.tat : "" });
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PriorityScheduling().setVisible(true));
    }
}
