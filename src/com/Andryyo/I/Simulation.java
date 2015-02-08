package com.Andryyo.I;

import android.os.SystemClock;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Андрей on 02.01.2015.
 */
public class Simulation implements Runnable {

    static  {
        System.loadLibrary("I");
        nativeInit();
    }

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
            objects[i] = new SimulationObject(o.getPosition());
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

    private static native void nativeInit();
    private native double[][] calculateObjectsAccelerationRungeKutta(double[][] positions);

    //public native String calculateObjectsAccelerationRungeKutta(HashMap<SimulationObject, SimulationObjectPosition> input);

    @Override
    public void run() {
        while (true) {
            step();
            SystemClock.sleep(TIME_BETWEEN_STEPS);
        }
    }

    class SimulationStep    {
        private SimulationStep prevStep, nextStep;
        public LinkedHashMap<SimulationObject, SimulationObjectPosition> positions = new LinkedHashMap<SimulationObject, SimulationObjectPosition>();
        public double sigma = 0;

        public SimulationStep getNextStep() {
            return nextStep;
        }

        public void updateObjectsPositions() {
            for (Map.Entry<SimulationObject, SimulationObjectPosition> entry : positions.entrySet())
                entry.getKey().setPosition(entry.getValue());
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
            double [][] positionsArray = new double[positions.size()][];
            int index = 0;
            for (Map.Entry<SimulationObject, SimulationObjectPosition> entry : positions.entrySet()) {
                positionsArray[index] = entry.getValue().toArray();
                index++;
            }
            positionsArray = calculateObjectsAccelerationRungeKutta(positionsArray);
            nextStep.positions = new LinkedHashMap<SimulationObject, SimulationObjectPosition>();
            index = 0;
            for (Map.Entry<SimulationObject, SimulationObjectPosition> entry : positions.entrySet())   {
                nextStep.positions.put(entry.getKey(), new SimulationObjectPosition(positionsArray[index]));
                index++;
            }
            nextStep.sigma = currentStep.sigma;
            return nextStep;
        }
    }

}
