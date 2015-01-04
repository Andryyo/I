package com.Andryyo.I;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Андрей on 02.01.2015.
 */
public class SimulationObjectPosition {
    public SimulationVector position;
    public SimulationVector speed;
    public HashMap<SimulationObject, SimulationVector> forces;

    public SimulationObjectPosition(SimulationVector position, SimulationVector speed)   {
        this.position = position;
        this.speed = speed;
        this.forces = new HashMap<SimulationObject, SimulationVector>();
    }

    public SimulationObjectPosition(SimulationObjectPosition p1)   {
        this.position = p1.position;
        this.speed = p1.speed;
        this.forces = p1.forces;
    }
}
