import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * LibraTrack — Library Management System
 * Console demo of the core logic behind the full Swing + JDBC/MySQL version:
 * book catalog, member accounts, issue/return workflow, and per-day fine
 * calculation. This version keeps everything in memory so it can be
 * compiled and run with zero setup.
 */
public class LibraTrack {

    static final int LOAN_DAYS = 14;
    static final double FINE_PER_DAY = 5.0;

    static class Book {
        String isbn, title, author;
        int totalCopies, availableCopies;

        Book(String isbn, String title, String author, int copies) {
            this.isbn = isbn;
            this.title = title;
            this.author = author;
            this.totalCopies = copies;
            this.availableCopies = copies;
        }

        @Override
        public String toString() {
            return String.format("%-12s %-28s %-18s %d/%d available",
                    isbn, title, author, availableCopies, totalCopies);
        }
    }

    static class Loan {
        String isbn, memberId;
        LocalDate issueDate, dueDate;
        boolean returned = false;

        Loan(String isbn, String memberId) {
            this.isbn = isbn;
            this.memberId = memberId;
            this.issueDate = LocalDate.now();
            this.dueDate = issueDate.plusDays(LOAN_DAYS);
        }
    }

    static class Member {
        String id, name;
        List<Loan> loans = new ArrayList<>();

        Member(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    static Map<String, Book> catalog = new LinkedHashMap<>();
    static Map<String, Member> members = new LinkedHashMap<>();

    public static void main(String[] args) {
        seedData();
        Scanner sc = new Scanner(System.in);
        int choice;
        do {
            printMenu();
            choice = readInt(sc);
            switch (choice) {
                case 1 -> catalog.values().forEach(System.out::println);
                case 2 -> issueBook(sc);
                case 3 -> returnBook(sc);
                case 4 -> viewFines(sc);
                case 5 -> System.out.println("Goodbye!");
                default -> System.out.println("Invalid option.");
            }
        } while (choice != 5);
        sc.close();
    }

    static void seedData() {
        catalog.put("978-0134685991", new Book("978-0134685991", "Effective Java", "Joshua Bloch", 3));
        catalog.put("978-0596009205", new Book("978-0596009205", "Head First Design Patterns", "Freeman & Robson", 2));
        catalog.put("978-0132350884", new Book("978-0132350884", "Clean Code", "Robert C. Martin", 4));

        members.put("M001", new Member("M001", "Kirtan Patel"));
        members.put("M002", new Member("M002", "Aisha Shah"));
    }

    static void printMenu() {
        System.out.println("\n=== LibraTrack ===");
        System.out.println("1. View catalog");
        System.out.println("2. Issue book");
        System.out.println("3. Return book");
        System.out.println("4. View member fines");
        System.out.println("5. Exit");
        System.out.print("Choose: ");
    }

    static void issueBook(Scanner sc) {
        System.out.print("Member ID: ");
        Member m = members.get(sc.next());
        System.out.print("ISBN: ");
        Book b = catalog.get(sc.next());

        if (m == null || b == null) {
            System.out.println("Member or book not found.");
            return;
        }
        if (b.availableCopies <= 0) {
            System.out.println("No copies available right now.");
            return;
        }
        b.availableCopies--;
        m.loans.add(new Loan(b.isbn, m.id));
        System.out.println("Issued \"" + b.title + "\" to " + m.name + ". Due " + m.loans.get(m.loans.size() - 1).dueDate);
    }

    static void returnBook(Scanner sc) {
        System.out.print("Member ID: ");
        Member m = members.get(sc.next());
        System.out.print("ISBN: ");
        String isbn = sc.next();

        if (m == null) {
            System.out.println("Member not found.");
            return;
        }
        for (Loan l : m.loans) {
            if (l.isbn.equals(isbn) && !l.returned) {
                l.returned = true;
                catalog.get(isbn).availableCopies++;
                long lateDays = Math.max(0, ChronoUnit.DAYS.between(l.dueDate, LocalDate.now()));
                double fine = lateDays * FINE_PER_DAY;
                System.out.printf("Returned. %s%n", fine > 0 ? "Fine due: Rs " + fine : "No fine — on time.");
                return;
            }
        }
        System.out.println("No active loan for that ISBN.");
    }

    static void viewFines(Scanner sc) {
        System.out.print("Member ID: ");
        Member m = members.get(sc.next());
        if (m == null) {
            System.out.println("Member not found.");
            return;
        }
        double total = 0;
        for (Loan l : m.loans) {
            if (!l.returned) {
                long lateDays = Math.max(0, ChronoUnit.DAYS.between(l.dueDate, LocalDate.now()));
                total += lateDays * FINE_PER_DAY;
            }
        }
        System.out.printf("Outstanding fines for %s: Rs %.2f%n", m.name, total);
    }

    static int readInt(Scanner sc) {
        while (!sc.hasNextInt()) {
            sc.next();
            System.out.print("Enter a number: ");
        }
        return sc.nextInt();
    }
}
