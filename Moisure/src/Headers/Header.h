//Includes
#include <Arduino.h>
#include <OneWire.h>
#include <WiFi.h>
#include <DallasTemperature.h>
#include <Firebase_Arduino_WiFiNINA.h>
#include <ezTime.h>
#include <WiFi.h>
#include <VariableTimedAction.h>
#include "Headers/Configuration.h"

//sensors and constants
#define PUMP                2
#define LIGHT_SENSOR        3
#define MOISTURE_ANALOG     A0
#define TEMPERATURE_ANALOG  A1
#define MOISTURE_RAPORT     0.09775
#define TIME_ZONE           "ro"

//Paths
const String percentagePath = String('/') + String(PRODUCT_ID) + String("/percentage");
const String pumpPath = String('/') + String(PRODUCT_ID) + String("/pump");
const String lightPath = String('/') + String(PRODUCT_ID) + String("/light");
const String actualPath = String('/') + String(PRODUCT_ID) + String("/Actual");
const String historyPath = String('/') + String(PRODUCT_ID) + String("/History");