import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Comparator;

class ProcessData {
    String pid;
    int bt, at, wt, rt, ct, tat;
    int remainingBt;
    int index;
}

public class FCFS extends JFrame {
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextArea ganttArea;
    private JTextField txtPid, txtBt, txtAt;
    private java.util.List<ProcessData> processList = new ArrayList<>();

    private JLabel cpuLabel;
    private java.util.List<JProgressBar> progressBars = new ArrayList<>();
    private java.util.List<JLabel> remainingLabels = new ArrayList<>();
    private java.util.List<JLabel> waitingLabels = new ArrayList<>();
    private JPanel middlePanel;

    public FCFS() {
        setTitle("FCFS Scheduling with Arrival Time (Animated)");
        setSize(1000, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Dark theme for frame
        getContentPane().setBackground(Color.BLACK);

        // Top input panel
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.setBackground(Color.BLACK);
        inputPanel.setForeground(Color.WHITE);

        txtPid = new JTextField(5);
        txtBt = new JTextField(5);
        txtAt = new JTextField(5);

        JButton btnAdd = new JButton("Add Process");
        JButton btnCalc = new JButton("Run Animation");

        styleTextField(txtPid);
        styleTextField(txtBt);
        styleTextField(txtAt);
        styleButton(btnAdd);
        styleButton(btnCalc);

        JLabel lblPid = new JLabel("PID:");
        JLabel lblBt = new JLabel("BT:");
        JLabel lblAt = new JLabel("AT:");
        lblPid.setForeground(Color.WHITE);
        lblBt.setForeground(Color.WHITE);
        lblAt.setForeground(Color.WHITE);

        inputPanel.add(lblPid);
        inputPanel.add(txtPid);
        inputPanel.add(lblBt);
        inputPanel.add(txtBt);
        inputPanel.add(lblAt);
        inputPanel.add(txtAt);
        inputPanel.add(btnAdd);
        inputPanel.add(btnCalc);

        add(inputPanel, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(new String[] { "PID", "BT", "AT", "WT", "RT", "CT", "TAT" }, 0);
        table = new JTable(tableModel);
        table.setBackground(Color.BLACK);
        table.setForeground(Color.WHITE);
        table.setGridColor(Color.GRAY);
        table.setSelectionBackground(Color.DARK_GRAY);
        table.setSelectionForeground(Color.WHITE);

        JTableHeader header = table.getTableHeader();
        header.setBackground(Color.DARK_GRAY);
        header.setForeground(Color.WHITE);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, centerRenderer);

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
        middleScroll.getViewport().setBackground(Color.BLACK);
        middleScroll.setMinimumSize(new Dimension(300, 200));

        // SplitPane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScroll, middleScroll);
        splitPane.setDividerLocation(600);
        splitPane.setResizeWeight(0.7);
        add(splitPane, BorderLayout.CENTER);

        // Gantt chart area
        ganttArea = new JTextArea(4, 20);
        ganttArea.setFont(new Font("Monospaced", Font.BOLD, 14));
        ganttArea.setEditable(false);
        ganttArea.setBackground(Color.BLACK);
        ganttArea.setForeground(Color.WHITE);
        add(new JScrollPane(ganttArea), BorderLayout.SOUTH);

        // Add process
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

                JLabel pidLabel = new JLabel(p.pid);
                pidLabel.setForeground(Color.WHITE);
                rowPanel.add(pidLabel);

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

        // Run animation
        btnCalc.addActionListener((ActionEvent e) -> runFCFSAnimation());
    }

    private void styleTextField(JTextField field) {
        field.setBackground(Color.DARK_GRAY);
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
    }

    private void styleButton(JButton button) {
        button.setBackground(Color.GRAY);
        button.setForeground(Color.WHITE);
    }

    private void runFCFSAnimation() {
        if (processList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No processes to run!");
            return;
        }

        processList.sort(Comparator.comparingInt(p -> p.at));

        new Thread(() -> {
            int currentTime = 0;
            StringBuilder gantt = new StringBuilder("Gantt Chart:\n");

            for (ProcessData p : processList) {
                if (currentTime < p.at) {
                    currentTime = p.at;
                }

                p.rt = currentTime - p.at;
                p.wt = currentTime - p.at;

                final int startTime = currentTime;

                for (int t = 1; t <= p.bt; t++) {
                    final int elapsed = t;
                    final int globalTime = startTime + elapsed;

                    SwingUtilities.invokeLater(() -> {
                        cpuLabel.setText("CPU: " + p.pid);
                        progressBars.get(p.index).setValue(elapsed);
                        p.remainingBt = p.bt - elapsed;
                        remainingLabels.get(p.index).setText("Remaining: " + p.remainingBt);
                        waitingLabels.get(p.index).setText("WT: " + (p.wt + elapsed));

                        gantt.append("| ").append(p.pid).append(" ");
                        ganttArea.setText(gantt.toString());
                        updateTableLive(p, globalTime);
                    });

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }

                currentTime = startTime + p.bt;
                p.ct = currentTime;
                p.tat = p.ct - p.at;
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
                tableModel.addRow(
                        new Object[] { p.pid, p.bt, p.at, p.wt, p.rt, p.ct > 0 ? p.ct : "", p.tat > 0 ? p.tat : "" });
            }
        }
    }

    public void execute() {
        SwingUtilities.invokeLater(() -> this.setVisible(true));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FCFS().execute());
    }
}
