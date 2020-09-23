package org.kyungrae.practice.threadpool;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BatchMultiThreadedExecutor {
    private String conditionPath;
    private String outputCsvPath;
    private int numThreads;

    public BatchMultiThreadedExecutor(String conditionPath, String outputCsvPath, int numThreads) {
        this.conditionPath = conditionPath;
        this.outputCsvPath = outputCsvPath;
        this.numThreads = numThreads;
    }

    public void run() throws IOException {
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        try (BufferedReader br = new BufferedReader(new FileReader(conditionPath));
             SynchronizedCsvWriter writer = new SynchronizedCsvWriter(outputCsvPath)) {
            writer.open();
            List<Future> taskResults = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                String[] condition = line.split(",");
                Future taskResult = executor.submit(new CsvWritingTask(writer, new InputData(condition)));
                taskResults.add(taskResult);
            }
            for (Future taskResult : taskResults) {
                taskResult.get();
            }
            executor.shutdown();
        } catch (IOException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        // java -jar csvbatch.jar CSV_FILE_NAME OUTPUT_FILE_NAME THREADS
        BatchMultiThreadedExecutor executor = new BatchMultiThreadedExecutor(
                args[0], args[1], Integer.parseInt(args[2]));

        executor.run();
    }
}
