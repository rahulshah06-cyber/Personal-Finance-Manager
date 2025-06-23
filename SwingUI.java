package net.javaguides.pfm;

import javax.swing.border.EmptyBorder;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class SwingUI {

    private static final String[] DEFAULT_CATEGORIES = { "Food", "Transport", "Rent", "Salary", "Shopping", "Other" };

    private final FinanceManager manager;

    private DefaultListModel<Transaction> listModel;
    private JList<Transaction> transactionList;
    private JLabel incomeLbl;
    private JLabel expenseLbl;
    private JLabel balanceLbl;
    private JComboBox<TransactionType> filterTypeCombo;
    private JComboBox<String> sortCombo;
    private JLabel categoryLbl;

    public SwingUI(FinanceManager manager) {
        this.manager = manager;
        createUI();
    }

    private void createUI() {
        JFrame frame = new JFrame("Personal Finance Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(550, 500);
        frame.setLayout(new BorderLayout(10, 10));
        frame.getRootPane().setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField descField = new JTextField();
        JTextField amountField = new JTextField();
        JComboBox<TransactionType> typeCombo = new JComboBox<>(TransactionType.values());
        JComboBox<String> categoryCombo = new JComboBox<>(DEFAULT_CATEGORIES);
        categoryLbl = new JLabel("Category:");
        JButton addBtn = new JButton("Add");

        int row = 0;
        addToGrid(formPanel, gbc, new JLabel("Description:"), 0, row);
        addToGrid(formPanel, gbc, descField, 1, row++);
        addToGrid(formPanel, gbc, new JLabel("Amount:"), 0, row);
        addToGrid(formPanel, gbc, amountField, 1, row++);
        addToGrid(formPanel, gbc, new JLabel("Type:"), 0, row);
        addToGrid(formPanel, gbc, typeCombo, 1, row++);
        addToGrid(formPanel, gbc, categoryLbl, 0, row);
        addToGrid(formPanel, gbc, categoryCombo, 1, row++);
        addToGrid(formPanel, gbc, addBtn, 1, row);

        frame.add(formPanel, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        transactionList = new JList<>(listModel);
        transactionList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel lbl = new JLabel(value.toString());
            lbl.setOpaque(true);
            if (value.getType() == TransactionType.INCOME) {
                lbl.setForeground(new Color(0, 128, 0)); // green
            } else {
                lbl.setForeground(Color.RED);
            }
            if (isSelected) {
                lbl.setBackground(new Color(220, 220, 220));
            } else {
                lbl.setBackground(Color.WHITE);
            }
            return lbl;
        });
        frame.add(new JScrollPane(transactionList), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());

        JPanel totalsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        incomeLbl = new JLabel();
        expenseLbl = new JLabel();
        balanceLbl = new JLabel();
        totalsPanel.add(incomeLbl);
        totalsPanel.add(expenseLbl);
        totalsPanel.add(balanceLbl);
        bottomPanel.add(totalsPanel, BorderLayout.NORTH);

        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterTypeCombo = new JComboBox<>(TransactionType.values());
        filterTypeCombo.insertItemAt(null, 0); // allow 'All'
        filterTypeCombo.setSelectedIndex(0);
        sortCombo = new JComboBox<>(new String[] { "Sort by Date", "Sort by Amount" });
        JButton exportBtn = new JButton("Export CSV");
        controlsPanel.add(new JLabel("Filter:"));
        controlsPanel.add(filterTypeCombo);
        controlsPanel.add(sortCombo);
        controlsPanel.add(exportBtn);
        bottomPanel.add(controlsPanel, BorderLayout.SOUTH);

        frame.add(bottomPanel, BorderLayout.SOUTH);

        typeCombo.addActionListener(ev -> {
            TransactionType sel = (TransactionType) typeCombo.getSelectedItem();
            boolean isExpense = sel == TransactionType.EXPENSE;
            categoryLbl.setVisible(isExpense);
            categoryCombo.setVisible(isExpense);
            if (!isExpense) {
                categoryCombo.setSelectedIndex(-1);
            } else if (categoryCombo.getSelectedIndex() == -1) {
                categoryCombo.setSelectedIndex(0);
            }
        });

        typeCombo.setSelectedItem(TransactionType.INCOME);

        addBtn.addActionListener(e -> {
            try {
                String desc = descField.getText().trim();
                double amount = Double.parseDouble(amountField.getText().trim());
                TransactionType type = (TransactionType) typeCombo.getSelectedItem();
                String category = type == TransactionType.EXPENSE ? (String) categoryCombo.getSelectedItem() : "";
                if (desc.isEmpty())
                    throw new IllegalArgumentException("Description required");
                Transaction t = new Transaction(desc, amount, type, category, LocalDate.now());
                manager.addTransaction(t);
                refreshList();
                descField.setText("");
                amountField.setText("");
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(frame, "Enter a valid number for amount");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage());
            }
        });

        filterTypeCombo.addActionListener(e -> refreshList());
        sortCombo.addActionListener(e -> refreshList());
        exportBtn.addActionListener(e -> doExport(frame));

        refreshList();

        frame.setVisible(true);
    }

    private void addToGrid(JPanel panel, GridBagConstraints gbc, Component c, int x, int y) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.weightx = x == 1 ? 1.0 : 0;
        panel.add(c, gbc);
    }

    private void refreshList() {
        listModel.clear();
        List<Transaction> toDisplay = manager.getTransactions();

        TransactionType filterType = (TransactionType) filterTypeCombo.getSelectedItem();
        if (filterType != null) {
            toDisplay = manager.filterByType(filterType);
        }
        if (sortCombo.getSelectedIndex() == 1) {
            toDisplay = toDisplay.stream().sorted((a, b) -> Double.compare(b.getAmount(), a.getAmount())).toList();
        } else {
            toDisplay = toDisplay.stream().sorted(java.util.Comparator.comparing(Transaction::getDate)).toList();
        }
        toDisplay.forEach(listModel::addElement);

        updateTotals();
    }

    private void updateTotals() {
        incomeLbl.setText(String.format("Income: ₹%.2f  ", manager.getTotalIncome()));
        expenseLbl.setText(String.format("Expense: ₹%.2f  ", manager.getTotalExpense()));
        balanceLbl.setText(String.format("Balance: ₹%.2f", manager.getNetBalance()));
    }

    private void doExport(Component parent) {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                manager.exportToCsv(file);
                JOptionPane.showMessageDialog(parent, "Exported to " + file.getAbsolutePath());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(parent, "Failed to export: " + ex.getMessage());
            }
        }
    }
}
