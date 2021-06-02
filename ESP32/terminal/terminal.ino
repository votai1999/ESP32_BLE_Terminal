#include "BluetoothSerial.h"
#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run `make menuconfig` to and enable it
#endif

BluetoothSerial SerialBT;

void setup() {
  Serial.begin(115200);
  SerialBT.begin("VN_AQI"); //Bluetooth device name
  Serial.println("The device started, now you can pair it with bluetooth!");
}

void loop() {
  SerialBT.setTimeout(10); 
  Serial.setTimeout(10);
  if (Serial.available()) {
    SerialBT.print(Serial.readString()); // Transmit data to android app
  }
  if (SerialBT.available()) {
    Serial.println(SerialBT.readString()); // Receiver data from android app
  }
}
