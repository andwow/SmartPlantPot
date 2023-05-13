#include "Headers/Header.h"


class ServerActions : public VariableTimedAction {
  public:
    ServerActions();
    ~ServerActions();
    unsigned long run();

  public:
    void setup();
    void updateLightStatus();

  private:
    void water(float optimum);

    float moisturePercent(int moisture);

  private:
    bool lastLightStatus;
    FirebaseData firebaseData;
    OneWire* oneWire;
    DallasTemperature* sensors;
    Timezone timeZone;
};