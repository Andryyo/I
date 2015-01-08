package com.Andryyo.I;

import android.os.SystemClock;

import java.util.HashMap;

/**
 * Created by Андрей on 02.01.2015.
 */
public class Simulation implements Runnable {
    public static double SIMULATION_STEP = 0.0000001;
    public static double MAX_SIGMA = 0.001;
    public static double MIN_SIGMA = MAX_SIGMA/10;
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
        SimulationObject[] objects = new SimulationObject[currentStep.positions.keySet().size()];
        int i = 0;
        for (SimulationObject o : currentStep.positions.keySet())   {
            objects[i] = new SimulationObject(o.getMass(), o.getPosition());
            i++;
        }
        return objects;
    }

    public synchronized  double getSigma()  {
        return  currentStep.sigma;
    }

    public synchronized double getSimulationStep()  {
        return SIMULATION_STEP;
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

    public void predict()  {
        predictSimulation();
    }

    public synchronized void addSimulationObject(SimulationObject o)   {
        currentStep.setObjectPosition(o, o.getPosition());
    }

    public synchronized void removeSimulationObject(SimulationObject o)  {
        currentStep.removeObject(o);
    }

    private synchronized void predictSimulation()    {
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
        public double sigma = 0;

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

            HashMap<SimulationObject, SimulationObjectPosition> buf = new HashMap<SimulationObject, SimulationObjectPosition>();
            for (SimulationObject object : positions.keySet())   {
                buf.put(
                        object,
                        new SimulationObjectPosition(
                                SimulationVector.add(positions.get(object).position, SimulationVector.multiply(K1.get(object), 0.5)),
                                SimulationVector.add(positions.get(object).speed, SimulationVector.multiply(M1.get(object), 0.5)
                                )
                        )
                );
            }

            for (SimulationObject object : positions.keySet())   {
                M2.put(object, SimulationVector.multiply(f1(object, buf),SIMULATION_STEP));
                K2.put(object, SimulationVector.multiply(f2(object, buf),SIMULATION_STEP));
            }

            buf = new HashMap<SimulationObject, SimulationObjectPosition>();
            for (SimulationObject object : positions.keySet())   {
                buf.put(
                        object,
                        new SimulationObjectPosition(
                                SimulationVector.add(positions.get(object).position, SimulationVector.multiply(K2.get(object), 0.5)),
                                SimulationVector.add(positions.get(object).speed, SimulationVector.multiply(M2.get(object), 0.5)
                                )
                        )
                );
            }

            for (SimulationObject object : positions.keySet())   {
                M3.put(object, SimulationVector.multiply(f1(object, buf),SIMULATION_STEP));
                K3.put(object, SimulationVector.multiply(f2(object, buf),SIMULATION_STEP));
            }

            buf = new HashMap<SimulationObject, SimulationObjectPosition>();
            for (SimulationObject object : positions.keySet())   {
                buf.put(
                        object,
                        new SimulationObjectPosition(
                                SimulationVector.add(positions.get(object).position, K3.get(object)),
                                SimulationVector.add(positions.get(object).speed, M3.get(object)
                                )
                        )
                );
            }

            for (SimulationObject object : positions.keySet())   {
                M4.put(object, SimulationVector.multiply(f1(object, buf),SIMULATION_STEP));
                K4.put(object, SimulationVector.multiply(f2(object, buf),SIMULATION_STEP));
            }

            HashMap<SimulationObject, SimulationObjectPosition> result = new HashMap<SimulationObject, SimulationObjectPosition>();
            for (SimulationObject object : positions.keySet())   {
                SimulationVector position = SimulationVector.add(
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
                SimulationVector speed = SimulationVector.add(
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
                result.put(object, new SimulationObjectPosition(position, speed));
            }

            sigma = 0;
             for (SimulationObject object : result.keySet()) {
                double part1 =
                        SimulationVector.value(
                                SimulationVector.substract(M2.get(object), M3.get(object)))
                        ;
                double part2 =
                        SimulationVector.value(
                                SimulationVector.substract(M1.get(object), M2.get(object))
                        );

                if (part2 != 0 && part1 != 0 && (Math.abs((1 - part1/part2)) > sigma))
                    sigma = Math.abs(part1/part2);
            }

            if (sigma > MAX_SIGMA)    {
                SIMULATION_STEP /= 10;
                //result = calculateObjectsAccelerationRungeKutta();
            }
            if (sigma < MIN_SIGMA)    {
                SIMULATION_STEP *= 2;
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
            nextStep.sigma = currentStep.sigma;
            return nextStep;
        }
    }

}
