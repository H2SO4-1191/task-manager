import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TaskManagerApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new TaskManagerFrame().setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error starting application: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}

class TaskManagerFrame extends JFrame {
    private final TaskManager taskManager;
    private DefaultListModel<Task> listModel;
    private JList<Task> taskList;
    public TaskManagerFrame() {
        taskManager = new TaskManager();
        taskManager.loadTasks();
        setTitle("Advanced Task Manager");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        JPanel listPanel = createListPanel();
        mainPanel.add(new JScrollPane(listPanel), BorderLayout.CENTER);
        JPanel controlPanel = createControlPanel();
        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        add(mainPanel);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                taskManager.saveTasks();
            }
        });
    }
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(70, 130, 180));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel titleLabel = new JLabel("Task Manager", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel, BorderLayout.CENTER);
        return panel;
    }
    private JPanel createListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        listModel = new DefaultListModel<>();
        taskList = new JList<>(listModel);
        taskList.setCellRenderer(new TaskListRenderer());
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskList.setFont(new Font("Arial", Font.PLAIN, 14));
        taskList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = taskList.locationToIndex(e.getPoint());
                if (index != -1) {
                    taskList.setSelectedIndex(index);
                    taskList.repaint(taskList.getCellBounds(index, index));
                    if (e.getClickCount() == 2) {
                        editSelectedTask();
                    }
                }
            }
        });
        refreshTaskList();
        panel.add(new JScrollPane(taskList), BorderLayout.CENTER);
        return panel;
    }
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        JButton addButton = new JButton("Add Task");
        styleButton(addButton, new Color(34, 139, 34));
        addButton.addActionListener(e -> showAddTaskDialog());
        JButton editButton = new JButton("Edit Task");
        styleButton(editButton, new Color(255, 165, 0));
        editButton.addActionListener(e -> editSelectedTask());
        JButton deleteButton = new JButton("Delete Task");
        styleButton(deleteButton, new Color(220, 20, 60));
        deleteButton.addActionListener(e -> deleteSelectedTask());
        JButton completeButton = new JButton("Toggle Complete");
        styleButton(completeButton, new Color(75, 0, 130));
        completeButton.addActionListener(e -> toggleTaskCompletion());
        panel.add(addButton);
        panel.add(editButton);
        panel.add(deleteButton);
        panel.add(completeButton);
        return panel;
    }
    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    }
    private void showAddTaskDialog() {
        JTextField titleField = new JTextField(20);
        JTextArea descArea = new JTextArea(5, 20);
        JComboBox<Priority> priorityCombo = new JComboBox<>(Priority.values());
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("Title"));
        panel.add(titleField);
        panel.add(new JLabel("Description"));
        panel.add(new JScrollPane(descArea));
        panel.add(new JLabel("Priority"));
        panel.add(priorityCombo);
        Component[] panelChildren = panel.getComponents();
        for(Component child: panelChildren) if(child instanceof JLabel) ((JLabel) child).setAlignmentX(Component.CENTER_ALIGNMENT);
        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Task",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String title = titleField.getText().trim();
            String description = descArea.getText().trim();
            Priority priority = (Priority) priorityCombo.getSelectedItem();
            if (!title.isEmpty()) {
                Task task = new Task(title, description, priority);
                taskManager.addTask(task);
                refreshTaskList();
            }
        }
    }
    private void editSelectedTask() {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex != -1) {
            Task task = listModel.getElementAt(selectedIndex);
            JTextField titleField = new JTextField(task.getTitle(), 20);
            JTextArea descArea = new JTextArea(task.getDescription(), 5, 20);
            JComboBox<Priority> priorityCombo = new JComboBox<>(Priority.values());
            priorityCombo.setSelectedItem(task.getPriority());
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(new JLabel("Title"));
            panel.add(titleField);
            panel.add(new JLabel("Description"));
            panel.add(new JScrollPane(descArea));
            panel.add(new JLabel("Priority"));
            panel.add(priorityCombo);
            Component[] panelChildren = panel.getComponents();
            for(Component child: panelChildren) if(child instanceof JLabel) ((JLabel) child).setAlignmentX(Component.CENTER_ALIGNMENT);
            int result = JOptionPane.showConfirmDialog(this, panel, "Edit Task",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                task.setTitle(titleField.getText().trim());
                task.setDescription(descArea.getText().trim());
                task.setPriority((Priority) priorityCombo.getSelectedItem());
                refreshTaskList();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a task to edit.",
                    "No Task Selected", JOptionPane.WARNING_MESSAGE);
        }
    }
    private void deleteSelectedTask() {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex != -1) {
            Task task = listModel.getElementAt(selectedIndex);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete '" + task.getTitle() + "'?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                taskManager.removeTask(task);
                refreshTaskList();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a task to delete.",
                    "No Task Selected", JOptionPane.WARNING_MESSAGE);
        }
    }
    private void toggleTaskCompletion() {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex != -1) {
            Task task = listModel.getElementAt(selectedIndex);
            task.setCompleted(!task.isCompleted());
            refreshTaskList();
        } else {
            JOptionPane.showMessageDialog(this, "Please select a task to mark complete.",
                    "No Task Selected", JOptionPane.WARNING_MESSAGE);
        }
    }
    private void refreshTaskList() {
        listModel.clear();
        for (Task task : taskManager.getTasks()) {
            listModel.addElement(task);
        }
    }
}

class TaskListRenderer extends DefaultListCellRenderer {
    private final Color SELECTED_BG = new Color(60, 140, 230);
    private final Color SELECTED_FG = Color.WHITE;
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof Task task) {
            String htmlText = String.format("<html><b>%s</b> - %s<br><font color='gray'>%s</font></html>",
                    task.getTitle(),
                    task.getPriority().toString(),
                    task.getDescription());
            setText(htmlText);
            if (isSelected) {
                setBackground(SELECTED_BG);
                setForeground(SELECTED_FG);
            }
            else if (task.isCompleted()) {
                setBackground(new Color(60, 60, 60));
                setForeground(Color.GRAY);
            } else {
                switch (task.getPriority()) {
                    case HIGH -> setBackground(new Color(255, 150, 150));
                    case MEDIUM -> setBackground(new Color(255, 255, 150));
                    case LOW -> setBackground(new Color(150, 255, 150));
                }
            }
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        }
        return this;
    }
}

class TaskManager {
    private List<Task> tasks;
    private static final String FILE_NAME = "tasks.dat";
    public TaskManager() {
        tasks = new ArrayList<>();
    }
    public void addTask(Task task) {
        tasks.add(task);
    }
    public void removeTask(Task task) {
        tasks.remove(task);
    }
    public List<Task> getTasks() {
        return new ArrayList<>(tasks);
    }
    public void saveTasks() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(tasks);
        } catch (IOException e) {
            System.err.println("Error saving tasks: " + e.getMessage());
        }
    }
    @SuppressWarnings("unchecked")
    public void loadTasks() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            tasks = (List<Task>) ois.readObject();
        } catch (Exception e) {
            tasks = new ArrayList<>();
        }
    }
}

class Task implements Serializable {
    private String title;
    private String description;
    private Priority priority;
    private boolean completed;
    public Task(String title, String description, Priority priority) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.completed = false;
    }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    @Override
    public String toString() {
        return title + " (" + priority + ")";
    }
}

enum Priority {
    HIGH, MEDIUM, LOW
}
