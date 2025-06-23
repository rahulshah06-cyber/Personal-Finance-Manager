package net.javaguides.pfm;

import java.io.Serializable;
import java.time.LocalDate;

public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    private String description;
    private double amount;
    private LocalDate date;
    private TransactionType type;
    private String category;

    public Transaction(String description, double amount, TransactionType type, String category, LocalDate date) {
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.date = date;
    }

    public Transaction(String description, double amount, LocalDate date) {
        this(description, amount, TransactionType.EXPENSE, "Other", date);
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public TransactionType getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    @Override
    public String toString() {
        return String.format("%s - %s: ₹%.2f (%s | %s)", date, description, amount, type, category);
    }
}
