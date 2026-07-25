import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

/**
 * StockLedger — Inventory & Billing System
 * Console-based inventory and billing tool. Tracks stock with low-stock
 * alerts, builds itemized invoices on sale, and appends a daily sales
 * report to disk. Pure core Java — collections + file I/O, no database.
 */
public class StockLedger {

    static class Product {
        String sku, name;
        double price;
        int quantity, reorderLevel;

        Product(String sku, String name, double price, int quantity, int reorderLevel) {
            this.sku = sku;
            this.name = name;
            this.price = price;
            this.quantity = quantity;
            this.reorderLevel = reorderLevel;
        }

        @Override
        public String toString() {
            String warn = quantity <= reorderLevel ? "  ⚠ low stock" : "";
            return String.format("%-8s %-18s Rs %-8.2f qty:%-4d%s", sku, name, price, quantity, warn);
        }
    }

    static Map<String, Product> inventory = new LinkedHashMap<>();
    static int nextInvoiceId = 1001;

    public static void main(String[] args) {
        seedData();
        Scanner sc = new Scanner(System.in);
        int choice;
        do {
            printMenu();
            choice = readInt(sc);
            switch (choice) {
                case 1 -> inventory.values().forEach(System.out::println);
                case 2 -> sellItems(sc);
                case 3 -> restock(sc);
                case 4 -> System.out.println("Goodbye!");
                default -> System.out.println("Invalid option.");
            }
        } while (choice != 4);
        sc.close();
    }

    static void seedData() {
        inventory.put("SKU001", new Product("SKU001", "USB-C Cable", 249.0, 40, 10));
        inventory.put("SKU002", new Product("SKU002", "Wireless Mouse", 899.0, 15, 5));
        inventory.put("SKU003", new Product("SKU003", "Notebook (A5)", 60.0, 4, 10));
    }

    static void printMenu() {
        System.out.println("\n=== StockLedger ===");
        System.out.println("1. View inventory");
        System.out.println("2. Sell items (create invoice)");
        System.out.println("3. Restock a product");
        System.out.println("4. Exit");
        System.out.print("Choose: ");
    }

    static void sellItems(Scanner sc) {
        List<String> lines = new ArrayList<>();
        double total = 0;
        System.out.print("How many different products in this sale? ");
        int n = readInt(sc);

        for (int i = 0; i < n; i++) {
            System.out.print("SKU: ");
            Product p = inventory.get(sc.next());
            System.out.print("Quantity: ");
            int qty = readInt(sc);

            if (p == null || qty <= 0 || qty > p.quantity) {
                System.out.println("Skipping — invalid SKU or insufficient stock.");
                continue;
            }
            p.quantity -= qty;
            double lineTotal = p.price * qty;
            total += lineTotal;
            lines.add(String.format("%-18s x%-3d Rs %.2f", p.name, qty, lineTotal));

            if (p.quantity <= p.reorderLevel) {
                System.out.println("Note: " + p.name + " is now at/below reorder level (" + p.quantity + " left).");
            }
        }

        int invoiceId = nextInvoiceId++;
        System.out.println("\n--- Invoice #" + invoiceId + " ---");
        lines.forEach(System.out::println);
        System.out.printf("TOTAL: Rs %.2f%n", total);

        appendDailyReport(invoiceId, lines, total);
    }

    static void restock(Scanner sc) {
        System.out.print("SKU: ");
        Product p = inventory.get(sc.next());
        if (p == null) {
            System.out.println("Product not found.");
            return;
        }
        System.out.print("Add quantity: ");
        p.quantity += readInt(sc);
        System.out.println("New stock for " + p.name + ": " + p.quantity);
    }

    static void appendDailyReport(int invoiceId, List<String> lines, double total) {
        try (FileWriter fw = new FileWriter("sales_report.txt", true)) {
            fw.write("Invoice #" + invoiceId + " — " + LocalDate.now() + "\n");
            for (String line : lines) fw.write("  " + line + "\n");
            fw.write(String.format("  TOTAL: Rs %.2f%n%n", total));
        } catch (IOException e) {
            System.out.println("Could not write to sales_report.txt: " + e.getMessage());
        }
    }

    static int readInt(Scanner sc) {
        while (!sc.hasNextInt()) {
            sc.next();
            System.out.print("Enter a number: ");
        }
        return sc.nextInt();
    }
}
