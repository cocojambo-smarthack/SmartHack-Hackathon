package coco.cocoapplication.model;

import coco.cocoapplication.helper.Pair;

import java.util.List;

public class Connection {
    public String id;
    public String from_id;
    public String to_id;
    public int distance;
    public int lead_time_days;
    public String connection_type;
    public int max_capacity;
    public List<Pair<Integer, Integer>> transits;
}
