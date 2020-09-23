package org.kyungrae.practice.threadpool;

public class InputData {
    private double first;
    private int second;
    private double third;

    public InputData(double first, int second, double third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public InputData(String[] args) {
        this.first = Double.parseDouble(args[0]);
        this.second = Integer.parseInt(args[1]);
        this.third = Double.parseDouble(args[2]);
    }
}
