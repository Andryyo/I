#include <jni.h>

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <vector>
#include <string>
#include <stdlib.h>

static jclass JavaDoubleArray;
static double SIMULATION_STEP = 0.00001;
static double MAX_SIGMA = 0.001;
static double MIN_SIGMA = MAX_SIGMA/10;
static const int POSITION_X = 0;
static const int POSITION_Y = 1;
static const int SPEED_X = 2;
static const int SPEED_Y = 3;
static const int OWN_ACCELERATION_X = 4;
static const int OWN_ACCELERATION_Y = 5;
static const int MASS = 6;

extern "C" JNIEXPORT
void JNICALL Java_com_Andryyo_I_Simulation_nativeInit(JNIEnv * env, jclass pThis) {
        JavaDoubleArray = env->FindClass("[D");
}

double sqr(double value)    {
    return value*value;
}

void f1(double positions[][7], int objectsCount, double result[][2])
{
    double accelerations[objectsCount][objectsCount][2];
    for (int i = 0; i<objectsCount; i++)
    {
        accelerations[i][i][0] = positions[i][OWN_ACCELERATION_X];
        accelerations[i][i][1] = positions[i][OWN_ACCELERATION_Y];
    }
    for (int i = 0; i<objectsCount; i++)
        for (int j = 0; j<objectsCount; j++)
        {
            if (i == j)
                continue;
            double range = sqrt(
                            sqr(positions[i][POSITION_X]-positions[j][POSITION_X]) +
                            sqr(positions[i][POSITION_Y]-positions[j][POSITION_Y])
                            );
            double k = positions[i][MASS]/(range*range*range);
            accelerations[i][j][0] = (positions[j][POSITION_X]-positions[i][POSITION_X])*k;
            accelerations[i][j][1] = (positions[j][POSITION_Y]-positions[i][POSITION_Y])*k;
        }
    for (int i = 0; i<objectsCount;i++)
    {
        result[i][0] = 0;
        result[i][1] = 0;
        for (int j = 0; j<objectsCount; j++)
        {
            result[i][0] += accelerations[i][j][0];
            result[i][1] += accelerations[i][j][1];
        }
    }
}

void f2(double positions[][7], int objectsCount, double result[][2])
{
    for (int i = 0; i<objectsCount; i++)
    {
        result[i][0] = positions[i][SPEED_X];
        result[i][1] = positions[i][SPEED_Y];
    }
}

extern "C" JNIEXPORT
jobjectArray JNICALL Java_com_Andryyo_I_Simulation_calculateObjectsAccelerationRungeKutta(JNIEnv* env, jobject pThis, jobjectArray javaPositions)
{
    int objectsCount = env->GetArrayLength(javaPositions);
    double  positions[objectsCount][7];
    for (int i = 0; i<objectsCount; i++)
    {
        jdoubleArray array = (jdoubleArray)env->GetObjectArrayElement(javaPositions, i);
        env->GetDoubleArrayRegion(array, 0, 7, positions[i]);
    }
    double M1[objectsCount][2];
    double M2[objectsCount][2];
    double M3[objectsCount][2];
    double M4[objectsCount][2];
    double K1[objectsCount][2];
    double K2[objectsCount][2];
    double K3[objectsCount][2];
    double K4[objectsCount][2];

    f1(positions, objectsCount, M1);
    f2(positions, objectsCount, K1);
    for (int i = 0; i<objectsCount; i++)
    {
        M1[i][0]*=SIMULATION_STEP;
        M1[i][1]*=SIMULATION_STEP;
        K1[i][0]*=SIMULATION_STEP;
        K1[i][1]*=SIMULATION_STEP;
    }

    double buf[objectsCount][7];
    for (int i=0; i<objectsCount; i++)
    {
        buf[i][POSITION_X] = positions[i][POSITION_X] + K1[i][0]*0.5;
        buf[i][POSITION_Y] = positions[i][POSITION_Y] + K1[i][1]*0.5;
        buf[i][SPEED_X] = positions[i][SPEED_X] + M1[i][0]*0.5;
        buf[i][SPEED_Y] = positions[i][SPEED_Y] + M1[i][1]*0.5;
        buf[i][OWN_ACCELERATION_X] = positions[i][OWN_ACCELERATION_X];
        buf[i][OWN_ACCELERATION_Y] = positions[i][OWN_ACCELERATION_Y];
        buf[i][MASS] = positions[i][MASS];
    }

    f1(buf, objectsCount, M2);
    f2(buf, objectsCount, K2);
    for (int i = 0; i<objectsCount; i++)
    {
        M2[i][0]*=SIMULATION_STEP;
        M2[i][1]*=SIMULATION_STEP;
        K2[i][0]*=SIMULATION_STEP;
        K2[i][1]*=SIMULATION_STEP;
    }

    for (int i=0; i<objectsCount; i++)
    {
        buf[i][POSITION_X] = positions[i][POSITION_X] + K2[i][0]*0.5;
        buf[i][POSITION_Y] = positions[i][POSITION_Y] + K2[i][1]*0.5;
        buf[i][SPEED_X] = positions[i][SPEED_X] + M2[i][0]*0.5;
        buf[i][SPEED_Y] = positions[i][SPEED_Y] + M2[i][1]*0.5;
        buf[i][OWN_ACCELERATION_X] = positions[i][OWN_ACCELERATION_X];
        buf[i][OWN_ACCELERATION_Y] = positions[i][OWN_ACCELERATION_Y];
        buf[i][MASS] = positions[i][MASS];
    }

    f1(buf, objectsCount, M3);
    f2(buf, objectsCount, K3);
    for (int i = 0; i<objectsCount; i++)
    {
        M3[i][0]*=SIMULATION_STEP;
        M3[i][1]*=SIMULATION_STEP;
        K3[i][0]*=SIMULATION_STEP;
        K3[i][1]*=SIMULATION_STEP;
    }

    for (int i=0; i<objectsCount; i++)
    {
        buf[i][POSITION_X] = positions[i][POSITION_X] + K3[i][0];
        buf[i][POSITION_Y] = positions[i][POSITION_Y] + K3[i][1];
        buf[i][SPEED_X] = positions[i][SPEED_X] + M3[i][0];
        buf[i][SPEED_Y] = positions[i][SPEED_Y] + M3[i][1];
        buf[i][OWN_ACCELERATION_X] = positions[i][OWN_ACCELERATION_X];
        buf[i][OWN_ACCELERATION_Y] = positions[i][OWN_ACCELERATION_Y];
        buf[i][MASS] = positions[i][MASS];
    }

    f1(buf, objectsCount, M4);
    f2(buf, objectsCount, K4);
    for (int i = 0; i<objectsCount; i++)
    {
        M4[i][0]*=SIMULATION_STEP;
        M4[i][1]*=SIMULATION_STEP;
        K4[i][0]*=SIMULATION_STEP;
        K4[i][1]*=SIMULATION_STEP;
    }

    for (int i=0; i<objectsCount; i++)
    {
        buf[i][POSITION_X] = positions[i][POSITION_X] + (K1[i][0] + K2[i][0]*0.5 + K3[i][0]*0.5 + K4[i][0])*(1.0/6);
        buf[i][POSITION_Y] = positions[i][POSITION_Y] + (K1[i][1] + K2[i][1]*0.5 + K3[i][1]*0.5 + K4[i][1])*(1.0/6);
        buf[i][SPEED_X] = positions[i][SPEED_X] + (M1[i][0] + M2[i][0]*0.5 + M3[i][0]*0.5 + M4[i][0])*(1.0/6);
        buf[i][SPEED_Y] = positions[i][SPEED_Y] + (M1[i][1] + M2[i][1]*0.5 + M3[i][1]*0.5 + M4[i][1])*(1.0/6);
        buf[i][OWN_ACCELERATION_X] = positions[i][OWN_ACCELERATION_X];
        buf[i][OWN_ACCELERATION_Y] = positions[i][OWN_ACCELERATION_Y];
        buf[i][MASS] = positions[i][MASS];
    }

    double sigma = -1;
    for (int i = 0; i<objectsCount; i++) {
        double part1 = sqrt(
                            sqr(M2[i][0] - M3[i][0]) +
                            sqr(M2[i][1] - M3[i][1])
                       );
        double part2 = sqrt(
                            sqr(M1[i][0] - M2[i][0]) +
                            sqr(M1[i][1] - M2[i][1])
                       );
        if (part2 != 0 && part1 != 0 && (abs(part1/part2) > sigma))
            sigma = abs(part1/part2);
    }

    if (sigma > MAX_SIGMA)    {
        SIMULATION_STEP /= 10;
    }
    if (sigma < MIN_SIGMA)    {
        SIMULATION_STEP *= 2;
    }
    jobjectArray result = env->NewObjectArray(objectsCount, JavaDoubleArray, NULL);
    for (int i = 0; i<objectsCount; i++)
    {
        jdoubleArray doubleArray = env->NewDoubleArray(7);
        env->SetDoubleArrayRegion(doubleArray, 0, 7, buf[i]);
        env->SetObjectArrayElement(result, i, doubleArray);
        env->DeleteLocalRef(doubleArray);
    }
    return result;
}