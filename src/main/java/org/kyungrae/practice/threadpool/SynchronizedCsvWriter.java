package org.kyungrae.practice.threadpool;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SynchronizedCsvWriter implements Closeable {
    private String outputFileName;
    private BufferedWriter writer;

    public SynchronizedCsvWriter(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    public void open() throws IOException {
        File outputFile = new File(outputFileName);
        FileOutputStream outStream = new FileOutputStream(outputFile);
        writer = new BufferedWriter(new OutputStreamWriter(outStream));
    }

    public String convertToCsv(Result result) {
        return result.toString();
    }

    public String convertToCsv(String[] data) {
        return Stream.of(data).collect(Collectors.joining(","));
    }

    public synchronized void write(String csvLine) throws IOException {
        System.out.println(Thread.currentThread().getName());
        System.out.println(csvLine);
        writer.write(csvLine);
        writer.newLine();
    }

    public void close() throws IOException {
        writer.close();
    }
}
