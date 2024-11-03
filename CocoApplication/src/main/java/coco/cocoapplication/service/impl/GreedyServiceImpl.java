package coco.cocoapplication.service.impl;

import coco.cocoapplication.helper.Pair;
import coco.cocoapplication.model.*;
import coco.cocoapplication.service.SessionService;
import coco.cocoapplication.service.GreedyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GreedyServiceImpl implements GreedyService {
    final static Logger logger = LoggerFactory.getLogger(GreedyServiceImpl.class);

    private final SessionService sessionService;
    public List<Refinery> refineries;
    public List<Tank> tanks;
    public List<Customer> customers;
    public List<Connection> connections;

    public List<Round> rounds = new ArrayList<>();

    // Create a priority queue to store the demands sorted by the ratio of demand to distance
    private int currentDay = 0;
    public PriorityQueue<Demand> demands = new PriorityQueue<>((d1, d2) -> {
        double ratio1, ratio2;
        try {
            ratio1 = (double) d1.amount / (d1.endDay - currentDay);
        } catch (ArithmeticException e) {
            ratio1 = Double.MAX_VALUE;
        }
        try {
            ratio2 = (double) d2.amount / (d2.endDay - currentDay);
        } catch (ArithmeticException e) {
            ratio2 = Double.MAX_VALUE;
        }
        return Double.compare(ratio2, ratio1);
    });
    private HashMap<String, Integer> nodeLevel = new HashMap<>();
    private Map<Integer, List<String>> levelBuckets = new HashMap<>();

    private void createLevelBuckets() {
        for (Map.Entry<String, Integer> entry : nodeLevel.entrySet()) {
            String nodeId = entry.getKey();
            int level = entry.getValue();
            levelBuckets.computeIfAbsent(level, k -> new ArrayList<>()).add(nodeId);
        }
    }

    private void breadthFirstSearch() {
        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        for (Refinery refinery : refineries) {
            queue.add(refinery.id);
            nodeLevel.put(refinery.id, 0);
        }
        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (visited.contains(current))
                continue;
            visited.add(current);
            int level = nodeLevel.get(current);
            for (Connection connection : connections) {
                if (Objects.equals(connection.from_id, current)) {
                    queue.add(connection.to_id);
                    nodeLevel.put(connection.to_id, level + 1);
                }
            }
        }
    }

    @Autowired
    public GreedyServiceImpl(SessionServiceImpl apiService) {
        this.sessionService = apiService;
    }

    @Override
    public String getRoundMoves(int round) {
        return rounds.get(round).toString();
    }

    private RoundResponse sendRoundAndReturnAnswer(Round round, String sessionId) throws JsonProcessingException{
        rounds.add(round);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(round);
        logger.info("Sending round: {}", jsonString);
        return sessionService.playRound(sessionId, jsonString);
    }

    @Override
    public void precalculate() {
        breadthFirstSearch();
        createLevelBuckets();
        // Show for each level the nodes
        for (Map.Entry<Integer, List<String>> entry : levelBuckets.entrySet()) {
            logger.info("Level {}: {}", entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void startRandom() throws JsonProcessingException {
        Session session = sessionService.getSession();
        logger.info("Started session with id {}", session.id);

        // Send a round with no movements
        Round round = new Round();
        round.day = 0; round.movements = new ArrayList<>();
        RoundResponse response = sendRoundAndReturnAnswer(round, session.id);

        for (currentDay = 1; currentDay < 42; currentDay++) {
            updateConnections();
            updateClientDemands(response.demand);

            round.day = currentDay;
            round.movements = attemptToSatisfyDemands(currentDay);

            round.movements.addAll(updateTanks());
            round.movements.addAll(updateRefineries());

            response = sendRoundAndReturnAnswer(round, session.id);
        }

        for (Connection connection : connections)
            assert connection.transits.isEmpty();

        // End the session and log the result
        String answer = sessionService.endSession();
        logger.info("Session ended with result: {}", answer);
    }

    private void updateClientDemands(List<Demand> demands) {
        this.demands.addAll(demands);
    }

    boolean fullCapacity(Connection connection) {
        if (connection.transits == null || connection.transits.isEmpty())
            return false;
        int sumCapacity = connection.transits.stream().mapToInt(pair -> pair.second).sum();
        return sumCapacity >= connection.max_capacity;
    }

    private void updateConnections() {
        for (Connection connection : connections) {
            if (connection.transits == null)
                connection.transits = new ArrayList<>();
            if (connection.transits.isEmpty())
                continue;
            // Decrement the first element of all transits
            connection.transits.forEach(pair -> pair.first--);
            // Delete all transits that have the first element 0
            Tank tank = getTank(connection.from_id);
            if (tank != null) {
                // Add to tank the amount of the transits that have the first element 0
                for (Pair<Integer, Integer > pair : connection.transits) {
                    if (pair.first == 0) {
                        tank.addStock(pair.second);
                    }
                }
            }
            connection.transits.removeIf(pair -> pair.first == 0);
        }
    }

    private List<Movement> updateTanks() {
        List<Movement> movements = new ArrayList<>();
        for (int i = levelBuckets.size() - 1; i >= 0; i--) {
            Map.Entry<Integer, List<String>> entry = (Map.Entry<Integer, List<String>>) levelBuckets.entrySet().toArray()[i];
            int level = entry.getKey();
            List<String> nodes = entry.getValue();
            for (String nodeId : nodes) {
                Tank tank = getTank(nodeId);
                if (tank == null) {
                    continue;
                }
                int tank_output = 0;
                List<Connection> connections = getConnectionsTankTank(nodeId);
                for (Connection connection : connections) {
                    if (connection.lead_time_days + currentDay > 42)
                        continue;
                    if (tank_output >= tank.max_output)
                        break;
                    if (fullCapacity(connection))
                        continue;
//                    if (nodeLevel.get(connection.to_id) < level) {
//                        continue;
//                    }
                    // TODO: check max output and max input
                    Tank dumpTank = getTank(connection.to_id);

                    if (dumpTank == null) {
                        logger.error("Dump tank is null");
                        continue;
                    }

                    if (dumpTank.initial_stock < dumpTank.capacity) {
                        int amount = Math.min(tank.initial_stock, dumpTank.capacity - dumpTank.initial_stock);
                        int totalCapacity = connection.transits.stream().mapToInt(pair -> pair.second).sum();
                        amount = Math.min(amount, connection.max_capacity - totalCapacity);
                        amount = Math.min(amount, tank.max_output - tank_output);
                        assert amount >= 0;
                        tank.subtractStock(amount);
                        movements.add(new Movement(connection.id, amount));
                        connection.transits.add(new Pair<>(connection.lead_time_days, amount));
                        tank_output += amount;
                    }
                }
            }
        }
        return movements;
    }

    private List<Movement> updateRefineries() {
        List<Movement> movements = new ArrayList<>();
        for (Refinery refinery : refineries) {
            refinery.addStock(refinery.production);

            List<Connection> connections = getConnectionsFromRefinery(refinery.id);
            // Sort the connections by the tank capacity in descending order
            connections.sort((c1, c2) -> {
                Tank t1 = getTank(c1.from_id);
                Tank t2 = getTank(c2.from_id);
                if (t2 != null) {
                    return Integer.compare(Math.min(t2.capacity, t2.max_input), Math.min(t1.capacity, t1.max_input));
                }
                return 0;
            });
            for (Connection connection : connections) {
                if (connection.lead_time_days + currentDay > 42) {
                    continue;
                }
//                if (fullCapacity(connection))
//                    continue;
                Tank dumpTank = getTank(connection.to_id);

                if (dumpTank == null) {
                    logger.error("Dump tank is null");
                    continue;
                }

                if (dumpTank.initial_stock < dumpTank.capacity) {
                    int amount = Math.min(refinery.initial_stock, dumpTank.capacity - dumpTank.initial_stock);
                    amount = Math.min(amount, dumpTank.max_input + dumpTank.max_input / 10);
//                    int totalCapacity = connection.transits.stream().mapToInt(pair -> pair.second).sum();
//                    amount = Math.min(amount, connection.max_capacity - totalCapacity);
                    refinery.subtractStock(amount);
                    assert amount >= 0;
                    movements.add(new Movement(connection.id, amount));
                    connection.transits.add(new Pair<>(connection.lead_time_days, amount));
                }
            }
        }
        return movements;
    }

    private List<Movement> attemptToSatisfyDemands(int currentDay) {
        List<Movement> movements = new ArrayList<>();
        for (int i = 0; i < demands.size(); i++) {
            Demand demand = demands.poll();
            if (demand == null) {
                logger.error("Demand is null");
                continue;
            }

            // Get all the Connections that tie the Tank to the Customer
            List<Connection> connections = getConnectionsToCustomer(demand.customerId);
            // Sort the connections by the tank capacity in descending order
            connections.sort((c1, c2) -> {
                Tank t1 = getTank(c1.from_id);
                Tank t2 = getTank(c2.from_id);
                return Integer.compare(t2.initial_stock, t1.initial_stock);
            });
            for (Connection connection : connections) {
                if (connection.lead_time_days + currentDay > 42) {
                    continue;
                }

                Tank tank = getTank(connection.from_id);

                if (tank == null) {
                    logger.error("Tank is null");
                    continue;
                }
                if (demand.amount > tank.initial_stock) {
                    continue;
                }
                tank.subtractStock(demand.amount);
                movements.add(new Movement(connection.id, demand.amount));
                connection.transits.add(new Pair<>(connection.lead_time_days, demand.amount));
                break;
            }
        }
        return movements;
    }

    private Tank getTank(String tankId) {
        for (Tank tank : this.tanks) {
            if (Objects.equals(tank.id, tankId)) {
                return tank;
            }
        }
        return null;
    }

    private List<Connection> getConnectionsToCustomer(String customerId) {
        List<Connection> connections = new ArrayList<>();
        for (Connection connection : this.connections) {
            if (Objects.equals(connection.to_id, customerId)) {
                connections.add(connection);
            }
        }
        return connections;
    }

    private List<Connection> getConnectionsFromRefinery(String refineryId) {
        List<Connection> connections = new ArrayList<>();
        for (Connection connection : this.connections) {
            if (Objects.equals(connection.from_id, refineryId)) {
                connections.add(connection);
            }
        }
        return connections;
    }

    private List<Connection> getConnectionsTankTank(String tankId) {
        List<Connection> connections = new ArrayList<>();
        for (Connection connection : this.connections) {
            if (Objects.equals(connection.to_id, tankId)) {
                Tank oth = getTank(connection.from_id);
                if (oth != null)
                    connections.add(connection);
            }
        }
        return connections;
    }
}
