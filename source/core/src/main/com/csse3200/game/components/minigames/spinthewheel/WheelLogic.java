package com.csse3200.game.components.minigames.spinthewheel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Stores and calculates the wheel's logic  */
public class WheelLogic {
    private final List<Map.Entry<String, Integer>> items = new ArrayList<>();
    private final Random random;

   
    public WheelLogic(Map<String, Integer> source){
        this(source, new Random());
    }

    /**
     * Creates a wheel with a supplied source and new randomness so tests can seed it
     *
     * @param source item labels mapped to their value
     * @param random the randomness used to pick a winner
     */
    WheelLogic(Map<String, Integer> source, Random random){
        this.random = random;
        for (Map.Entry<String, Integer> item : source.entrySet()) {
            items.add(Map.entry(item.getKey(), item.getValue()));
        }
    }

    public Map.Entry<String, Integer> Spin(){
        int index = random.nextInt(items.size());
        return items.get(index);
    }
}
