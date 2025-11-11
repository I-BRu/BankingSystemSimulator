package model;

import exceptions.*;

public class Account {
    private final String accountNumber;
    private final String accountHolderName;
    private double balance;
    private final Object lock = new Object();

    public Account(String name) throws InvalidNameException {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidNameException("Account holder name cannot be empty");
        }
        this.accountHolderName = name.trim();
        this.accountNumber = generateAccountNumber(name);
        this.balance = 0.0;
    }

    private String generateAccountNumber(String name) {
        String initials = extractInitials(name);
        int randomNum = (int) (Math.random() * 9000) + 1000;
        return initials + randomNum;
    }

    private String extractInitials(String name) {
        StringBuilder initials = new StringBuilder();
        String[] words = name.trim().split("\\s+");
        for (String word : words) {
            if (!word.isEmpty()) {
                initials.append(Character.toUpperCase(word.charAt(0)));
            }
        }
        return initials.length() > 0 ? initials.toString() : "XX";
    }

    public synchronized void deposit(double amount) throws InvalidAmountException {
        if (amount <= 0) throw new InvalidAmountException("Deposit amount must be positive");
        synchronized (lock) {
            balance += amount;
            System.out.println("✓ Deposited: ₹" + amount);
            System.out.println("New Balance: ₹" + balance);
        }
    }

    public synchronized void withdraw(double amount) throws InvalidAmountException, InsufficientBalanceException {
        if (amount <= 0) throw new InvalidAmountException("Withdrawal amount must be positive");
        synchronized (lock) {
            if (balance < amount) {
                throw new InsufficientBalanceException(
                        "Insufficient balance. Available: ₹" + balance + ", Requested: ₹" + amount);
            }
            balance -= amount;
            System.out.println("✓ Withdrawn: ₹" + amount);
            System.out.println("New Balance: ₹" + balance);
        }
    }

    public synchronized void transfer(Account destination, double amount)
            throws InvalidAmountException, InsufficientBalanceException {
        if (amount <= 0) throw new InvalidAmountException("Transfer amount must be positive");

        Account first = this.accountNumber.compareTo(destination.accountNumber) < 0 ? this : destination;
        Account second = (first == this) ? destination : this;

        synchronized (first.lock) {
            synchronized (second.lock) {
                if (this.balance < amount) {
                    throw new InsufficientBalanceException(
                            "Insufficient balance. Available: ₹" + this.balance + ", Requested: ₹" + amount);
                }
                this.balance -= amount;
                destination.balance += amount;
                System.out.println("✓ Transferred ₹" + amount + " to " + destination.accountNumber);
                System.out.println("Your New Balance: ₹" + this.balance);
            }
        }
    }

    public void displayBalance() {
        synchronized (lock) {
            System.out.println("\n╔════════════════════════════════════╗");
            System.out.println("║      ACCOUNT INFORMATION          ║");
            System.out.println("╠════════════════════════════════════╣");
            System.out.printf("║ Account Number: %-18s║%n", accountNumber);
            System.out.printf("║ Account Holder: %-18s║%n", accountHolderName);
            System.out.printf("║ Balance: ₹%-23.2f║%n", balance);
            System.out.println("╚════════════════════════════════════╝");
        }
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public double getBalance() {
        synchronized (lock) {
            return balance;
        }
    }
}
