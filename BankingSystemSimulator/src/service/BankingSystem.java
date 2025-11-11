package service;

import model.Account;
import exceptions.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

public class BankingSystem {
    private final Map<String, Account> accounts;
    private final Scanner scanner;
    private final ExecutorService executor;

    public BankingSystem() {
        accounts = new ConcurrentHashMap<>();
        scanner = new Scanner(System.in);
        executor = Executors.newFixedThreadPool(5);
    }

    public void start() {
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║   WELCOME TO BANKING SYSTEM SIMULATOR     ║");
        System.out.println("╚════════════════════════════════════════════╝");

        boolean running = true;
        while (running) {
            displayMainMenu();
            int choice = getValidIntInput();

            switch (choice) {
                case 1 -> createAccount();
                case 2 -> performAccountOperations();
                case 3 -> demonstrateMultithreading();
                case 4 -> listAllAccounts();
                case 5 -> {
                    running = false;
                    System.out.println("\n✓ Thank you for using Banking System. Goodbye!");
                }
                default -> System.out.println("✗ Invalid choice. Please try again.");
            }
        }

        executor.shutdown();
        scanner.close();
    }

    private void displayMainMenu() {
        System.out.println("\n┌────────────────────────────────┐");
        System.out.println("│         MAIN MENU              │");
        System.out.println("├────────────────────────────────┤");
        System.out.println("│ 1. Create New Account          │");
        System.out.println("│ 2. Account Operations          │");
        System.out.println("│ 3. Demo Multithreading         │");
        System.out.println("│ 4. List All Accounts           │");
        System.out.println("│ 5. Exit                        │");
        System.out.println("└────────────────────────────────┘");
        System.out.print("Enter your choice: ");
    }

    private void createAccount() {
        try {
            System.out.print("\nEnter account holder name: ");
            scanner.nextLine();
            String name = scanner.nextLine();
            Account account = new Account(name);
            accounts.put(account.getAccountNumber(), account);
            System.out.println("\n✓ Account created successfully!");
            System.out.println("Account Number: " + account.getAccountNumber());
        } catch (InvalidNameException e) {
            System.out.println("✗ Error: " + e.getMessage());
        }
    }

    private void performAccountOperations() {
        try {
            System.out.print("\nEnter account number: ");
            scanner.nextLine();
            String accNum = scanner.nextLine().trim();

            Account account = findAccount(accNum);
            boolean running = true;

            while (running) {
                displayAccountMenu(account);
                int choice = getValidIntInput();

                switch (choice) {
                    case 1 -> performDeposit(account);
                    case 2 -> performWithdrawal(account);
                    case 3 -> performTransfer(account);
                    case 4 -> account.displayBalance();
                    case 5 -> running = false;
                    default -> System.out.println("✗ Invalid choice.");
                }
            }
        } catch (AccountNotFoundException e) {
            System.out.println("✗ " + e.getMessage());
        }
    }

    private void displayAccountMenu(Account account) {
        System.out.println("\n┌─────────────────────────────────────┐");
        System.out.println("│ Account: " + account.getAccountNumber());
        System.out.println("├─────────────────────────────────────┤");
        System.out.println("│ 1. Deposit                          │");
        System.out.println("│ 2. Withdraw                         │");
        System.out.println("│ 3. Transfer                         │");
        System.out.println("│ 4. Show Balance                     │");
        System.out.println("│ 5. Back to Main Menu                │");
        System.out.println("└─────────────────────────────────────┘");
        System.out.print("Enter your choice: ");
    }

    private void performDeposit(Account account) {
        try {
            System.out.print("Enter amount: ₹");
            double amount = getValidDoubleInput();
            account.deposit(amount);
        } catch (InvalidAmountException e) {
            System.out.println("✗ " + e.getMessage());
        }
    }

    private void performWithdrawal(Account account) {
        try {
            System.out.print("Enter amount: ₹");
            double amount = getValidDoubleInput();
            account.withdraw(amount);
        } catch (InvalidAmountException | InsufficientBalanceException e) {
            System.out.println("✗ " + e.getMessage());
        }
    }

    private void performTransfer(Account account) {
        try {
            System.out.print("Enter destination account: ");
            scanner.nextLine();
            String destAcc = scanner.nextLine().trim();
            Account dest = findAccount(destAcc);

            System.out.print("Enter amount: ₹");
            double amt = scanner.nextDouble();

            account.transfer(dest, amt);
        } catch (Exception e) {
            System.out.println("✗ " + e.getMessage());
        }
    }

    private void demonstrateMultithreading() {
        try {
            System.out.print("\nEnter account number for demo: ");
            scanner.nextLine();
            String accNum = scanner.nextLine().trim();
            Account acc = findAccount(accNum);

            CountDownLatch latch = new CountDownLatch(3);

            executor.submit(() -> runTask(() -> {
				try {
					acc.deposit(500);
				} catch (InvalidAmountException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}, "[Thread-1]", latch));
            executor.submit(() -> runTask(() -> {
				try {
					acc.deposit(300);
				} catch (InvalidAmountException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}, "[Thread-2]", latch));
            executor.submit(() -> runTask(() -> {
				try {
					acc.withdraw(200);
				} catch (InvalidAmountException | InsufficientBalanceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}, "[Thread-3]", latch));

            latch.await(5, TimeUnit.SECONDS);
            System.out.println("\n✓ All concurrent operations done!");
            acc.displayBalance();
        } catch (Exception e) {
            System.out.println("✗ " + e.getMessage());
        }
    }

    private void runTask(Runnable task, String name, CountDownLatch latch) {
        try {
            Thread.sleep((int) (Math.random() * 150));
            task.run();
            System.out.println(name + " Completed");
        } catch (Exception e) {
            System.out.println(name + " Error: " + e.getMessage());
        } finally {
            latch.countDown();
        }
    }

    private void listAllAccounts() {
        System.out.println("\n══════════ ALL ACCOUNTS ══════════");
        if (accounts.isEmpty()) {
            System.out.println("No accounts found.");
            return;
        }

        accounts.values().stream()
                .sorted(Comparator.comparing(Account::getAccountNumber))
                .forEach(acc -> System.out.printf("%s | %s | ₹%.2f%n",
                        acc.getAccountNumber(),
                        acc.getAccountHolderName(),
                        acc.getBalance()));
    }

    private Account findAccount(String num) throws AccountNotFoundException {
        return Optional.ofNullable(accounts.get(num))
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + num));
    }

    private int getValidIntInput() {
        while (true) {
            try {
                return scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.print("✗ Invalid input. Enter number: ");
                scanner.nextLine();
            }
        }
    }

    private double getValidDoubleInput() {
        while (true) {
            try {
                return scanner.nextDouble();
            } catch (InputMismatchException e) {
                System.out.print("✗ Invalid amount. Try again: ");
                scanner.nextLine();
            }
        }
    }
}
