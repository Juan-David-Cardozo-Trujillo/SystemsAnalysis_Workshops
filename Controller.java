import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

// Class that implements the logic for each thread to generate sequences
class ThreadSequence implements Runnable {

    Controller controller; // Instance of the Controller class
    int numSequences;      // Number of sequences this thread will generate
    int sequenceSize;      // Size of the sequences

    // Constructor that initializes the class values
    public ThreadSequence(Controller c, int numSequences, int sequenceSize) {
        this.controller = c;
        this.numSequences = numSequences;
        this.sequenceSize = sequenceSize;
    }

    // Method executed when the thread starts
    @Override
    public void run() {
        // Generate and save the specified number of sequences
        for (int i = 0; i < numSequences; i++) {
            String sequence = controller.sequenceGenerator(sequenceSize);
            controller.saveDatabase(sequence);
        }
    }
}

// Main class that controls the process
public class Controller {

    List<String> database; // List to store generated sequences
    Random random;         // Random number generator
    char[] nbases;         // Array to store nucleotide bases with assigned probabilities
    BufferedWriter writer; // Object to write the sequences to a file

    // Constructor that sets up the writer and initializes fields
    public Controller() {
        try {
            this.writer = new BufferedWriter(new FileWriter("dataset.txt")); // File to store the dataset
        } catch (Exception e) {
            System.out.println("Error, your file could not be created: " + e.getMessage());
        }
        this.random = new Random();
        database = new ArrayList<>();
    }

    // Assign probabilities to the nucleotide bases (A, C, G, T)
    public synchronized void assignateProbability(int pA, int pC, int pG, int pT) {
        int cantA = pA * 4;
        int cantC = pC * 4;
        int cantG = pG * 4;
        int cantT = pT * 4;

        int cantNbases = cantA + cantC + cantG + cantT;
        this.nbases = new char[cantNbases]; // Array to hold nucleotides based on their probabilities

        // Fill the array with nucleotides according to their probabilities
        for (int i = 0; i < cantA; i++) {
            nbases[i] = 'A';
        }
        for (int i = 0; i < cantC; i++) {
            nbases[cantA + i] = 'C';
        }
        for (int i = 0; i < cantG; i++) {
            nbases[cantA + cantC + i] = 'G';
        }
        for (int i = 0; i < cantT; i++) {
            nbases[cantA + cantC + cantG + i] = 'T';
        }
    }

    // Generate a random sequence of specified size using the nucleotide base probabilities
    public synchronized String sequenceGenerator(int sequenceSize) {
        String sequence = "";

        // Randomly select nucleotides based on the probabilities to form a sequence
        for (int j = 0; j < sequenceSize; j++) {
            int randomNum = this.random.nextInt(nbases.length); 
            sequence += nbases[randomNum];
        }
        return sequence;
    }

    // Create a database of sequences by generating them concurrently using threads
    public void createDatabase(int sequenceNum) throws InterruptedException {
        int splitSequences = sequenceNum / 10; // Divide sequences among 10 threads
        int sequenceSize = this.random.nextInt((100 - 5) + 1) + 5; // Random sequence size between 5 and 100

        // Create and start 10 threads to generate sequences concurrently
        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            ThreadSequence threadSequence = new ThreadSequence(this, splitSequences, sequenceSize);
            threads[i] = new Thread(threadSequence);
            threads[i].start();
        }

        // Wait for all threads to finish
        for (Thread thread : threads) {
            thread.join(); // Wait for each thread to finish
        }
    }

    // Save a sequence to a text file
    public synchronized void saveTotxt(String sequence) {
        try {
            writer.write(sequence);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error, your file could not be saved: " + e.getMessage());
        }
    }

    // Save a sequence to the in-memory database and to the text file
    public synchronized void saveDatabase(String sequence) {
        database.add(sequence); // Add sequence to the list
        saveTotxt(sequence);    // Save sequence to file
    }

    // Close the file writer
    public void closeWriter() {
        try {
            writer.close();
        } catch (IOException e) {
            System.out.println("Error closing writer: " + e.getMessage());
        }
    }

    // Generate motifs of a specific length and find the most frequent one
    public void motifGenerator(int motifSize) {
        List<String> motifs = getMotifCombinations(motifSize); // Generate all possible motifs of the specified size
        Map<String, Integer> motifCounts = countMotifOccurrences(this.database, motifs); // Count motif occurrences
        String mostFrequentMotif = findMostFrequentMotif(motifCounts); // Find and print the most frequent motif
    }

    // Recursively generate all possible motif combinations of a given size
    public List<String> getMotifCombinations(int motifSize) {
        List<String> motifs = new ArrayList<>();
        char[] bases = {'A', 'C', 'G', 'T'}; // Nucleotide bases
        char[] motif = new char[motifSize];

        generateMotifs(motif, 0, bases, motifs); // Recursive method to generate combinations
        return motifs;
    }

    // Recursive method to generate all possible motifs by iterating through base combinations
    private void generateMotifs(char[] motif, int index, char[] bases, List<String> motifs) {
        if (index == motif.length) {
            motifs.add(new String(motif)); // Add complete motif to the list
            return;
        }

        // Iterate through each base and recurse to build the motif
        for (char base : bases) {
            motif[index] = base;
            generateMotifs(motif, index + 1, bases, motifs);
        }
    }

    // Count the occurrences of each motif in the database of sequences
    private static Map<String, Integer> countMotifOccurrences(List<String> sequences, List<String> motifs) {
        Map<String, Integer> motifCounts = new HashMap<>();

        // For each motif, count how many times it appears in the sequences
        for (String motif : motifs) {
            int count = 0;
            for (String sequence : sequences) {
                count += countMotifOccurrencesInSequence(sequence, motif); // Count occurrences in each sequence
            }
            motifCounts.put(motif, count); // Store the count in a map
        }
        return motifCounts;
    }

    // Find the most frequent motif by iterating through the map of motif counts
    private static String findMostFrequentMotif(Map<String, Integer> motifCounts) {
        String mostFrequentMotif = null;
        int maxCount = 0;

        // Identify the motif with the highest count
        for (Map.Entry<String, Integer> entry : motifCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostFrequentMotif = entry.getKey();
            }
        }
        System.out.println("Motif: " + mostFrequentMotif);
        System.out.println("Motif occurrences: " + maxCount);
        return mostFrequentMotif;
    }

    // Count the number of times a motif appears in a single sequence
    private static int countMotifOccurrencesInSequence(String sequence, String motif) {
        int count = 0;
        int index = sequence.indexOf(motif);

        // Loop to find all occurrences of the motif in the sequence
        while (index != -1) {
            count++;
            index = sequence.indexOf(motif, index + 1);
        }
        return count;
    }

    // Main method to run the simulation
    public static void main(String[] args) throws IOException, InterruptedException {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        // Welcome message
        System.out.println("WELCOME TO YOUR FAVOURITE SIMULATOR\n");

        // Get the number of sequences
        System.out.println("Enter the amount of sequences");
        int sequenceNum = Integer.parseInt(br.readLine());

        // Get the probability for each nucleotide base
        System.out.println("Enter the probability of each base\n A: ");
        int pA = Integer.parseInt(br.readLine());

        System.out.println("Enter the probability of each base\n C: ");
        int pC = Integer.parseInt(br.readLine());

        System.out.println("Enter the probability of each base\n G: ");
        int pG = Integer.parseInt(br.readLine());

        System.out.println("Enter the probability of each base\n T: ");
        int pT = Integer.parseInt(br.readLine());

        // Get the size of the motif to search for
        System.out.println("Enter the motif size\n");
        int motifSize = Integer.parseInt(br.readLine());

        // Initialize the controller and generate sequences
        Controller controller = new Controller();
        controller.assignateProbability(pA, pC, pG, pT);
        controller.createDatabase(sequenceNum);
        controller.closeWriter();

        // Generate motifs and find the most frequent one
        controller.motifGenerator(motifSize);
    }
}
