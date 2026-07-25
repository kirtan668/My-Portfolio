import java.util.*;

/**
 * MediQueue — Hospital Appointment System
 * Console demo of the scheduling core behind the full JDBC/MySQL version:
 * doctors publish available slots, patients book against them, and each
 * doctor can pull their own day's queue. Kept dependency-free so it runs
 * anywhere with just a JDK.
 */
public class MediQueue {

    static class Doctor {
        String id, name, specialization;
        List<String> slots = new ArrayList<>();

        Doctor(String id, String name, String specialization, String... slots) {
            this.id = id;
            this.name = name;
            this.specialization = specialization;
            this.slots.addAll(Arrays.asList(slots));
        }
    }

    static class Patient {
        String id, name;
        Patient(String id, String name) { this.id = id; this.name = name; }
    }

    static class Appointment {
        Patient patient;
        Doctor doctor;
        String slot;
        Appointment(Patient p, Doctor d, String slot) { this.patient = p; this.doctor = d; this.slot = slot; }

        @Override
        public String toString() {
            return slot + " — " + patient.name + " with Dr. " + doctor.name + " (" + doctor.specialization + ")";
        }
    }

    static Map<String, Doctor> doctors = new LinkedHashMap<>();
    static Map<String, Patient> patients = new LinkedHashMap<>();
    static List<Appointment> appointments = new ArrayList<>();

    public static void main(String[] args) {
        seedData();
        Scanner sc = new Scanner(System.in);
        int choice;
        do {
            printMenu();
            choice = readInt(sc);
            switch (choice) {
                case 1 -> doctors.values().forEach(MediQueue::printDoctorSlots);
                case 2 -> bookAppointment(sc);
                case 3 -> viewDoctorQueue(sc);
                case 4 -> System.out.println("Goodbye!");
                default -> System.out.println("Invalid option.");
            }
        } while (choice != 4);
        sc.close();
    }

    static void seedData() {
        doctors.put("D01", new Doctor("D01", "Rina Mehta", "Cardiology", "10:00", "10:30", "11:00"));
        doctors.put("D02", new Doctor("D02", "Sanjay Rao", "Orthopedics", "09:00", "09:30", "14:00"));

        patients.put("P01", new Patient("P01", "Kirtan Patel"));
        patients.put("P02", new Patient("P02", "Meera Joshi"));
    }

    static void printMenu() {
        System.out.println("\n=== MediQueue ===");
        System.out.println("1. View doctors & open slots");
        System.out.println("2. Book appointment");
        System.out.println("3. View a doctor's queue");
        System.out.println("4. Exit");
        System.out.print("Choose: ");
    }

    static void printDoctorSlots(Doctor d) {
        System.out.printf("%s — Dr. %s (%s) — open slots: %s%n",
                d.id, d.name, d.specialization,
                d.slots.isEmpty() ? "none" : String.join(", ", d.slots));
    }

    static void bookAppointment(Scanner sc) {
        System.out.print("Patient ID: ");
        Patient p = patients.get(sc.next());
        System.out.print("Doctor ID: ");
        Doctor d = doctors.get(sc.next());

        if (p == null || d == null) {
            System.out.println("Patient or doctor not found.");
            return;
        }
        if (d.slots.isEmpty()) {
            System.out.println("Dr. " + d.name + " has no open slots.");
            return;
        }
        System.out.print("Pick a slot (" + String.join(", ", d.slots) + "): ");
        String slot = sc.next();
        if (!d.slots.remove(slot)) {
            System.out.println("That slot isn't available.");
            return;
        }
        appointments.add(new Appointment(p, d, slot));
        System.out.println("Booked: " + slot + " with Dr. " + d.name + " for " + p.name);
    }

    static void viewDoctorQueue(Scanner sc) {
        System.out.print("Doctor ID: ");
        Doctor d = doctors.get(sc.next());
        if (d == null) {
            System.out.println("Doctor not found.");
            return;
        }
        System.out.println("Queue for Dr. " + d.name + ":");
        appointments.stream()
                .filter(a -> a.doctor.id.equals(d.id))
                .sorted(Comparator.comparing(a -> a.slot))
                .forEach(a -> System.out.println("  " + a.slot + " — " + a.patient.name));
    }

    static int readInt(Scanner sc) {
        while (!sc.hasNextInt()) {
            sc.next();
            System.out.print("Enter a number: ");
        }
        return sc.nextInt();
    }
}
