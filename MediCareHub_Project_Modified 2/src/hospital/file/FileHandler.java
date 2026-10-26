package hospital.file;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FileHandler provides generic methods for reading and writing text files.
 * All data is stored in the "data" folder as .txt files.
 * Demonstrates EXCEPTION HANDLING with try-catch.
 *
 * REFERENCES:
 * - BufferedReader / BufferedWriter / try-with-resources pattern:
 *   https://docs.oracle.com/javase/tutorial/essential/io/file.html
 *   (Oracle's official Java I/O tutorial)
 *
 * All other logic (method structure, validation) is my own original work.
 */
public class FileHandler {

    private static final String DATA_DIR = "data";

    private static File dataDirectory() {
        File current = new File(System.getProperty("user.dir")).getAbsoluteFile();
        List<File> candidates = new ArrayList<>();
        candidates.add(new File(current, "data"));
        candidates.add(new File(current, "MediCareHub_Project_Modified/data"));
        candidates.add(new File(current, "MediCareHub_Final/data"));
        if (current.getParentFile() != null) candidates.add(new File(current.getParentFile(), "data"));
        // Prefer a folder that already contains the application's persisted records.
        for (File candidate : candidates) {
            if (!candidate.isDirectory()) continue;
            for (String name : new String[]{"patients.txt", "doctors.txt", "appointments.txt", "bills.txt"}) {
                File f = new File(candidate, name);
                if (f.isFile() && f.length() > 0) return candidate;
            }
        }
        for (File candidate : candidates) if (candidate.isDirectory()) return candidate;
        File dir = candidates.get(0);
        dir.mkdirs();
        return dir;
    }

    // Ensure the data directory exists before writing/reading
    static {
        File dir = dataDirectory();
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Reads all lines from a file. Returns an empty list if file doesn't exist.
     */
    public static List<String> readLines(String fileName) {
        List<String> lines = new ArrayList<>();
        File file = new File(dataDirectory(), fileName);

        // Try-with-resources ensures the reader is closed automatically
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lines.add(line);
                }
            }
        } catch (FileNotFoundException e) {
            // File not created yet - treat as empty, no crash
        } catch (IOException e) {
            System.out.println("Error reading " + fileName + ": " + e.getMessage());
        }
        return lines;
    }

    /**
     * Writes a list of lines to a file (overwrites existing content).
     */
    public static void writeLines(String fileName, List<String> lines) {
        File file = new File(dataDirectory(), fileName);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error writing " + fileName + ": " + e.getMessage());
        }
    }

    /**
     * Appends a single line to a file (used for adding records).
     */
    public static void appendLine(String fileName, String line) {
        File file = new File(dataDirectory(), fileName);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            bw.write(line);
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Error appending to " + fileName + ": " + e.getMessage());
        }
    }
}