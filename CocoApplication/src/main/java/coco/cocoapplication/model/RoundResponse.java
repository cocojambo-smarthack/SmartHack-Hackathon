package coco.cocoapplication.model;

import java.util.List;

public class RoundResponse {
    public int round;
    public List<Demand> demand;
    public List<Penalty> penalties;
    public Kpis deltaKpis;
    public Kpis totalKpis;
}
