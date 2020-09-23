package org.kyungrae.practice.threadpool;

import lombok.AllArgsConstructor;

import java.io.IOException;

@AllArgsConstructor
public class CsvWritingTask implements Runnable {
    private SynchronizedCsvWriter writer;
    private InputData data;

    @Override
    public void run() {
        Result result = new Result(0.1, 0.1, 0.3);
        try {
            writer.write(result.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
