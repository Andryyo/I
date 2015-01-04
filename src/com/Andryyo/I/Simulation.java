package com.Andryyo.I;

import android.os.SystemClock;

import java.util.HashMap;

/**
 * Created by Андрей on 02.01.2015.
 */
public class Simulation implements Runnable {
    public static double SIMULATION_STEP = 0.0001;
    public static final int SIMULATION_PREDICTION_DEPTH = 1;
    public static final double GRAVITATIONAL_CONSTANT = 6.67384;// * 0.00000000001;
    public static long TIME_BETWEEN_STEPS = 0;
    private OnUpdateListener onUpdateListener;

    private SimulationStep currentStep;

    public Simulation() {
        currentStep = new SimulationStep();
        new Thread(this).start();
    }

    public synchronized SimulationObject [] getObjects()  {
        return currentStep.positions.keySet().toArray(new SimulationObject[currentStep.positions.keySet().size()]);
    }

    public void setOnUpdateListener(OnUpdateListener listener)  {
        onUpdateListener = listener;
    }

    public synchronized void step()  {
        if (currentStep.getNextStep() == null)
            predictSimulation();
        currentStep = currentStep.getNextStep();
        currentStep.updateObjectsPositions();
        if (onUpdateListener!=null)
            onUpdateListener.update();
    }

    public synchronized void addSimulationObject(SimulationObject o)   {
        currentStep.setObjectPosition(o, o.getPosition());
        predictSimulation();
    }

    public void removeSimulationObject(SimulationObject o)  {
        currentStep.removeObject(o);
        predictSimulation();
    }

    private void predictSimulation()    {
        SimulationStep predictedStep = currentStep;
        for (int i = 0; i<SIMULATION_PREDICTION_DEPTH; i++)
        {
            predictedStep = predictedStep.generateNextSimulationStep();
        }
    }

    public static SimulationVector calcGravitationAcceleration(double mass, SimulationObjectPosition p1, SimulationObjectPosition p2)   {
        double r = SimulationVector.value(SimulationVector.substract(p1.position, p2.position));
        if (r == 0)
            return new SimulationVector(0, 0);
        return SimulationVector.multiply(SimulationVector.substract(p2.position, p1.position), GRAVITATIONAL_CONSTANT * mass / (r*r*r));
    }

    public static SimulationVector f1(SimulationObject currentObject, HashMap<SimulationObject, SimulationObjectPosition> args) {
        SimulationVector acceleration = new SimulationVector(currentObject.getOwnAcceleration());
        for (SimulationObject otherObject : args.keySet())  {
            if (currentObject.equals(otherObject))
                continue;
            acceleration = SimulationVector.add(
                    acceleration,
                    calcGravitationAcceleration(
                            otherObject.getMass(),
                            args.get(currentObject),
                            args.get(otherObject)));
        }
        return acceleration;
    }

    public static SimulationVector f2(SimulationObject currentObject, HashMap<SimulationObject, SimulationObjectPosition> args)    {
        return args.get(currentObject).speed;
    }

    @Override
    public void run() {
        while (true) {
            step();
            SystemClock.sleep(TIME_BETWEEN_STEPS);
        }
    }

    class SimulationStep    {
        private SimulationStep prevStep, nextStep;
        public HashMap<SimulationObject, SimulationObjectPosition> positions = new HashMap<SimulationObject, SimulationObjectPosition>();

        public SimulationStep getNextStep() {
            return nextStep;
        }

        public HashMap<SimulationObject, SimulationObjectPosition> calculateObjectsAccelerationRungeKutta() {
            HashMap<SimulationObject, SimulationVector> M1 = new HashMap<SimulationObject, SimulationVector>(); //вторая производная
            HashMap<SimulationObject, SimulationVector> K1 = new HashMap<SimulationObject, SimulationVector>(); //первая производная
            HashMap<SimulationObject, SimulationVector> M2 = new HashMap<SimulationObject, SimulationVector>();
            HashMap<SimulationObject, SimulationVector> K2 = new HashMap<SimulationObject, SimulationVector>();
            HashMap<SimulationObject, SimulationVector> M3 = new HashMap<SimulationObject, SimulationVector>();
            HashMap<SimulationObject, SimulationVector> K3 = new HashMap<SimulationObject, SimulationVector>();
            HashMap<SimulationObject, SimulationVector> M4 = new HashMap<SimulationObject, SimulationVector>();
            HashMap<SimulationObject, SimulationVector> K4 = new HashMap<SimulationObject, SimulationVector>();

            for (SimulationObject object : positions.keySet())   {
                M1.put(object, SimulationVector.multiply(f1(object, positions),SIMULATION_STEP));
                K1.put(object, SimulationVector.multiply(f2(object, positions),SIMULATION_STEP));
            }

            HashMap<SimulationObject, SimulationObjectPosition> buf = new HashMap<SimulationObject, SimulationObjectPosition>(positions);
            for (SimulationObject object : buf.keySet())   {
                buf.get(object).speed = SimulationVector.add(buf.get(object).speed, SimulationVector.multiply(M1.get(object), 0.5));
                buf.get(object).position = SimulationVector.add(buf.get(object).position, SimulationVector.multiply(K1.get(object), 0.5));
            }

            for (SimulationObject object : positions.keySet())   {
                M2.put(object, SimulationVector.multiply(f1(object, buf),SIMULATION_STEP));
                K2.put(object, SimulationVector.multiply(f2(object, buf),SIMULATION_STEP));
            }

            buf = new HashMap<SimulationObject, SimulationObjectPosition>(positions);
            for (SimulationObject object : buf.keySet())   {
                buf.get(object).speed = SimulationVector.add(buf.get(object).speed, SimulationVector.multiply(M2.get(object), 0.5));
                buf.get(object).position = SimulationVector.add(buf.get(object).position, SimulationVector.multiply(K2.get(object), 0.5));
            }

            for (SimulationObject object : positions.keySet())   {
                M3.put(object, SimulationVector.multiply(f1(object, buf),SIMULATION_STEP));
                K3.put(object, SimulationVector.multiply(f2(object, buf),SIMULATION_STEP));
            }

            buf = new HashMap<SimulationObject, SimulationObjectPosition>(positions);
            for (SimulationObject object : buf.keySet())   {
                buf.get(object).speed = SimulationVector.add(buf.get(object).speed, M3.get(object));
                buf.get(object).position = SimulationVector.add(buf.get(object).position, K3.get(object));
            }

            for (SimulationObject object : positions.keySet())   {
                M4.put(object, SimulationVector.multiply(f1(object, buf),SIMULATION_STEP));
                K4.put(object, SimulationVector.multiply(f2(object, buf),SIMULATION_STEP));
            }

            HashMap<SimulationObject, SimulationObjectPosition> result = new HashMap<SimulationObject, SimulationObjectPosition>(positions);
            for (SimulationObject object : positions.keySet())   {
                result.get(object).position = SimulationVector.add(
                        positions.get(object).position,
                        SimulationVector.multiply(
                                SimulationVector.add(
                                        K1.get(object),
                                        SimulationVector.add(
                                                SimulationVector.multiply(K2.get(object),2),
                                                SimulationVector.add(
                                                        SimulationVector.multiply(K3.get(object), 2),
                                                        K4.get(object))
                                                )
                                        ),
                                1.0/6));
                result.get(object).speed = SimulationVector.add(
                        positions.get(object).speed,
                        SimulationVector.multiply(
                                SimulationVector.add(
                                        M1.get(object),
                                        SimulationVector.add(
                                                SimulationVector.multiply(M2.get(object),2),
                                                SimulationVector.add(
                                                        SimulationVector.multiply(M3.get(object), 2),
                                                        M4.get(object))
                                        )
                                ),
                                1.0/6));
            }

            for (SimulationObject object : result.keySet()) {
                double part1 =
                        SimulationVector.value(
                                SimulationVector.substract(K2.get(object), K3.get(object)))
                        ;
                double part2 =
                        SimulationVector.value(
                                SimulationVector.substract(K1.get(object), K2.get(object))
                        );

                double sigma = 0;
                if (part2 != 0)
                    sigma = part1/part2;
                if (sigma > 0.1)    {
                    SIMULATION_STEP /= 2;
                    break;
                }
                if (sigma < 0.01)    {
                    SIMULATION_STEP *= 2;
                    break;
                }
            }

            return result;
        }

        public void updateObjectsPositions() {
            for (SimulationObject o : positions.keySet())
                o.setPosition(positions.get(o));
        }

        public void setObjectPosition(SimulationObject o, SimulationObjectPosition p)
        {
            positions.put(o, p);
        }

        public void removeObject(SimulationObject o)    {
            positions.remove(o);
        }

        public void getObjectPosition(SimulationObject o)
        {
            positions.get(o);
        }

        public SimulationStep generateNextSimulationStep()    {
            nextStep = new SimulationStep();
            nextStep.positions = calculateObjectsAccelerationRungeKutta();
            return nextStep;
        }
    }

}
