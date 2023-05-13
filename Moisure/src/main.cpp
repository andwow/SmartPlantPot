#include "Classes/ServerActions.h"

ServerActions serverActions;

void setup()
{
    pinMode(MOISTURE_ANALOG, INPUT);
    pinMode(TEMPERATURE_ANALOG, INPUT);
    pinMode(LIGHT_SENSOR, INPUT);
    pinMode(PUMP, OUTPUT);

    digitalWrite(PUMP, HIGH);

    serverActions.setup();
    serverActions.start(900000);//1800000
}

void loop() 
{
  VariableTimedAction::updateActions();
  serverActions.updateLightStatus();
}