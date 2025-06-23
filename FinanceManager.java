package net.javaguides.pfm;

import java.io.*;
import java.util.*;

public class FinanceManager {
    private final String filePath;
    private List<Transaction> transactions;

    public FinanceManager() {
        this.filePath = System.getProperty("user.home") + File.separator + "transactions.dat";
        this.transactions = new ArrayList<>();
        loadTransactions();
    }

    public void addTransaction(Transaction t) {
        transactions.add(t);
        saveTransactions();
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public double getTotalIncome() {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getTotalExpense() {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getNetBalance() {
        return getTotalIncome() - getTotalExpense();
    }

    public List<Transaction> getTransactionsSortedByDate() {
        return transactions.stream()
                .sorted(Comparator.comparing(Transaction::getDate))
                .toList();
    }

    public List<Transaction> getTransactionsSortedByAmountDesc() {
        return transactions.stream()
                .sorted(Comparator.comparing(Transaction::getAmount).reversed())
                .toList();
    }

    public List<Transaction> filterByType(TransactionType type) {
        return transactions.stream()
                .filter(t -> t.getType() == type)
                .toList();
    }

    public List<Transaction> filterByCategory(String category) {
        return transactions.stream()
                .filter(t -> t.getCategory().equalsIgnoreCase(category))
                .toList();
    }

    public void exportToCsv(File file) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("Description,Amount,Type,Category,Date");
            for (Transaction t : transactions) {
                pw.printf("%s,%.2f,%s,%s,%s%n",
                        escapeCsv(t.getDescription()),
                        t.getAmount(),
                        t.getType(),
                        escapeCsv(t.getCategory()),
                        t.getDate());
            }
        }
    }

    private String escapeCsv(String s) {
        if (s.contains(",")) {
            return "\"" + s.replace("\"", "\"\"") + "\""; // minimal CSV escaping
        }
        return s;
    }

    private void saveTransactions() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath))) {
            out.writeObject(transactions);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadTransactions() {
        File file = new File(filePath);
        if (!file.exists())
            return;

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath))) {
            @SuppressWarnings("unchecked")
            List<Transaction> loadedTransactions = (List<Transaction>) in.readObject();
            transactions = loadedTransactions;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
