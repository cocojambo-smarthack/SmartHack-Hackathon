package coco.cocoapplication.model;

public class Movement {
    public String connectionId;
    public int amount;

    public Movement(String id, int amount) {
        this.connectionId = id;
        this.amount = amount;
    }
}
