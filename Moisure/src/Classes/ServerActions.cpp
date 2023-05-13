#include "ServerActions.h"

ServerActions::ServerActions()
{
    lastLightStatus = true;
    oneWire = new OneWire(TEMPERATURE_ANALOG);
    sensors = new DallasTemperature(oneWire);
}

ServerActions::~ServerActions()
{
    delete(oneWire);
    delete(sensors);
}

unsigned long ServerActions::run() 
{
    sensors->requestTemperatures();

    int moisture = analogRead(MOISTURE_ANALOG);
    float moisturePrc = moisturePercent(moisture);
    float temperature = sensors->getTempCByIndex(0);
    float optimumMoisture;
    bool pumpSwitch = "false";
    int count = 0;

    String date = timeZone.dateTime("d-m-y");
    Serial.print("Date: ");
    Serial.println(date);

    String time = timeZone.dateTime("H:i");
    Serial.print("Time: ");
    Serial.println(time);

    Serial.print("Moisture: ");
    Serial.println(moisturePrc);

    Serial.print("Temperature: ");
    Serial.println(temperature);

    String jsonStr = "{\"date\":\"" + date + "\",\"time\":\"" + time + "\",\"moisture\":" + String(moisturePrc,6) + ",\"temperature\":" + String(temperature,6) + "}";

    while(!Firebase.setJSON(firebaseData, actualPath, jsonStr) && count < 5)
    {
        Serial.println("Error: " + firebaseData.errorReason());
        ++count;
    }
    Serial.println("Actual setted");

    delay(5000);

    count = 0;
    while (!Firebase.pushJSON(firebaseData, historyPath, jsonStr) && count < 5)
    {
        Serial.println("Error: " + firebaseData.errorReason());
        ++count;
    }
    Serial.println(firebaseData.dataPath() + " = " + firebaseData.pushName());

    delay(2000);
    
    count = 0;
    while(!Firebase.getFloat(firebaseData, percentagePath) && count < 5)
    {
        Serial.println("Error: don't get the moisture percentage");
        ++count;
        delay(2000);
    }

    Serial.print("Optimum moisture percentage: ");
    Serial.println(firebaseData.floatData());
    
    optimumMoisture = firebaseData.floatData();
    delay(2000);

    count = 0;
    while(!Firebase.getBool(firebaseData, pumpPath) && count < 5)
    {
        Serial.println("Error: don't get the pump status");
        ++count;
    }
    pumpSwitch = firebaseData.boolData();
  
    if(pumpSwitch)
    {
        Serial.println("Pump is on");
        water(optimumMoisture);
    }
    else
    {
        Serial.println("Pump is off");
    }

    Serial.println();

    return 0;
}

void ServerActions::setup()
{
    Serial.begin(9600);
    sensors->begin();

    Serial.println("Connecting to WiFi…");
    int status = WL_IDLE_STATUS;

    while (status != WL_CONNECTED) 
    {
        Serial.print("Connecting to ");
        Serial.println(WIFI_SSID);
        status = WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
        delay(2000);
    }
  
    Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH, WIFI_SSID, WIFI_PASSWORD);
    Firebase.reconnectWiFi(true);
    waitForSync();
    timeZone.setLocation(F(TIME_ZONE));

    if(!Firebase.getBool(firebaseData, pumpPath))
    {
        int count = 0;
        while(!Firebase.setBool(firebaseData, pumpPath, false) && count < 5)
        {
            Serial.println("Error: Setup failed (pump)");
            delay(2000);
            ++count;
        }
    }

    if(!Firebase.getFloat(firebaseData, percentagePath))
    {
        int count = 0;
        while(!Firebase.setInt(firebaseData, percentagePath, 0) && count < 5)
        {
            Serial.println("Error: Setup failed (moisture percentage)");
            delay(2000);
            ++count;
        }
    }

    if(!Firebase.getBool(firebaseData, lightPath))
    {
        int count = 0;
        while(!Firebase.setBool(firebaseData, lightPath, false) && count < 5)
        {
          Serial.println("Error: " + firebaseData.errorReason());
          ++count;
        }
    }
}

void ServerActions::updateLightStatus()
{
    bool light = digitalRead(LIGHT_SENSOR);
    if(light != lastLightStatus)
    {
        lastLightStatus = light;
        int count = 0;
        while(!Firebase.setBool(firebaseData, lightPath, !light) && count < 5)
        {
          Serial.println("Error: " + firebaseData.errorReason());
          ++count;
        }
    }
}

float ServerActions::moisturePercent(int moisture)
{
    return moisture * MOISTURE_RAPORT;
}

void ServerActions::water(float optimum) 
{

    int moisture = analogRead(MOISTURE_ANALOG);
    float moisturePrc = moisturePercent(moisture);

    Firebase.getBool(firebaseData, pumpPath);
    bool pumpSwitch = firebaseData.boolData();

    while(moisturePrc < optimum && pumpSwitch == true ) {
      digitalWrite(PUMP, LOW);
      delay(500);
      digitalWrite(PUMP, HIGH);
      delay(10000);
    
      moisture = analogRead(MOISTURE_ANALOG);
      moisturePrc = moisturePercent(moisture);
      int count = 0;
      while(!Firebase.getBool(firebaseData, pumpPath) && count < 5)
      {
        Serial.println("Error: getBool in water() function!");
      }
      
      pumpSwitch = firebaseData.boolData();
    }
    Serial.println("Pump is off");
}