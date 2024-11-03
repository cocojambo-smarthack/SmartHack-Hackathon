package coco.cocoapplication.model;

public class Refinery {
    public String id;
    public String name;
    public int capacity;
    public int max_output;
    public int production;
    public double overflow_penalty;
    public double underflow_penalty;
    public double over_output_penalty;
    public double production_cost;
    public double production_co2;
    public int initial_stock;
    public String node_type;

    public void subtractStock(int amount) {
        initial_stock -= amount;
    }

    public void addStock(int amount) {
        initial_stock += amount;
    }
}
