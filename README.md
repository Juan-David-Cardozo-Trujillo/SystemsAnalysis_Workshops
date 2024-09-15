## Introduction

This project simulates the generation of random genetic sequences with user-defined nucleotide probabilities. It concurrently generates a specified number of sequences using multithreading to improve performance. Additionally, the program identifies motifs (specific subsequences) of a user-defined size and counts their occurrences across all sequences. The most frequent motif is then displayed.

The main goals of this workshop include:
1. Simulating genetic sequence generation with random probabilities for nucleotide bases.
2. Utilizing multithreading to divide the workload of generating sequences.
3. Identifying and counting motifs of a specified size from the generated sequences.

## Features

- **Random Sequence Generation**: Generate a specified number of DNA sequences with varying probabilities for each base (A, C, G, T).
- **Multithreading**: Efficiently generate sequences in parallel using 10 threads.
- **Motif Detection**: Detect all possible motifs of a given size and identify the most frequent motif across all sequences.
- **File Output**: Save all generated sequences to a text file (`dataset.txt`).

## Proyect structure
genetic-sequence-simulator/
│
├── Controller.java           # Main code for sequence generation and motif detection
├── dataset.txt               # Generated dataset (after running the program)
├── README.md                 # Project documentation

## Usage
After running the program, you will be prompted to input the following values:

1. Number of sequences to generate.
2. Probability (0-25) of each nucleotide base (A, C, G, T).
3. Size of the motif to detect.
The program will generate the sequences based on the provided probabilities and save them to dataset.txt.

It will also count the occurrences of all possible motifs of the specified size and print the most frequent motif and its count.

