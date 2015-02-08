package com.Andryyo.I;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.HashMap;
import java.util.Vector;

/**
 * Created by Андрей on 02.01.2015.
 */
public class SimulationView extends View implements OnUpdateListener {
    private static int TRAJECTORY_LENGTH = 1000;
    private Simulation s;
    private Paint paint = new Paint();
    private HashMap<SimulationObject, Vector<SimulationObjectPosition>> trajectories = new HashMap<SimulationObject, Vector<SimulationObjectPosition>>();
    private double maxCoordinate = 0;
    private float scaleFactor = 0;

    public SimulationView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public void setSimulation(Simulation s) {
        this.s = s;
    }

    @Override
    protected void onDraw(android.graphics.Canvas canvas)   {
        if (s == null)
            return;
        canvas.drawARGB(255, 0, 0, 255);
        int centerX = canvas.getWidth()/2;
        int centerY = canvas.getHeight()/2;
        paint.setColor(Color.WHITE);
        SimulationObject [] objects = s.getObjects();
        //maxCoordinate = 0;
        for (SimulationObject object : objects) {
            if (maxCoordinate < Math.abs(object.getPosition().position.x*2))   {
                maxCoordinate = Math.abs(object.getPosition().position.x)*2;
                scaleFactor = (float)(Math.min(canvas.getHeight(), canvas.getWidth()) / maxCoordinate);
            }
            if (maxCoordinate < Math.abs(object.getPosition().position.y*2)) {
                maxCoordinate = Math.abs(object.getPosition().position.y)*2;
                scaleFactor = (float)(Math.min(canvas.getHeight(), canvas.getWidth()) / maxCoordinate);
            }
        }
        //scaleFactor = 1;
        paint.setColor(Color.GRAY);
        for (SimulationObject o : trajectories.keySet())    {
            for (SimulationObjectPosition p : trajectories.get(o))  {
                int x = (int)(p.position.x*scaleFactor + centerX);
                int y = (int)(p.position.y*scaleFactor + centerY);
                //float x = centerX;
                //float y = centerY;
                canvas.drawPoint(x, y, paint);
            }
        }
        paint.setColor(Color.WHITE);

        for (SimulationObject object : objects) {
            if (!trajectories.containsKey(object))
                trajectories.put(object, new Vector<SimulationObjectPosition>());
            trajectories.get(object).add(new SimulationObjectPosition(object.getPosition()));
            //if (trajectories.get(object).size() > TRAJECTORY_LENGTH)
            //     trajectories.get(object).remove(0);
            paint.setColor(Color.WHITE);
            canvas.drawCircle(
                    (float)(object.getPosition().position.x*scaleFactor + centerX),
                    (float)(object.getPosition().position.y*scaleFactor + centerY),
                    1*scaleFactor,
                    paint);
            paint.setColor(Color.RED);
            canvas.drawLine(
                    (float)(object.getPosition().position.x*scaleFactor + centerX),
                    (float)(object.getPosition().position.y*scaleFactor + centerY),
                    (float)(object.getPosition().position.x*scaleFactor + centerX + object.getPosition().speed.x*scaleFactor),
                    (float)(object.getPosition().position.y*scaleFactor + centerY + object.getPosition().speed.y*scaleFactor),
                    paint);
            paint.setColor(Color.GREEN);
            for (SimulationObject key : object.getPosition().forces.keySet())    {
                SimulationVector force = object.getPosition().forces.get(key);
                canvas.drawLine(
                        (float)(object.getPosition().position.x*scaleFactor + centerX),
                        (float)(object.getPosition().position.y*scaleFactor + centerY),
                        (float)(object.getPosition().position.x*scaleFactor + centerX + force.x*scaleFactor),
                        (float)(object.getPosition().position.y*scaleFactor + centerY + force.y*scaleFactor),
                        paint);
            }
        }
        canvas.drawText("Scale: " + scaleFactor, 50, 50, paint);
        canvas.drawText("Sigma: " + String.valueOf(s.getSigma()), 50, 60, paint);
        canvas.drawText("Step: " + String.valueOf(s.getSimulationStep()), 50, 70, paint);
    }

    @Override
    public void update() {
        postInvalidate();
    }
}
