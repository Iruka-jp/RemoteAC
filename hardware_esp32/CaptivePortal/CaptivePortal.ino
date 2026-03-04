#include <WiFiManager.h> // https://github.com/tzapu/WiFiManager

void setup() {
    WiFi.mode(WIFI_STA); // Explicitly set mode to Station
    Serial.begin(115200);

    // Create an instance of WiFiManager
    WiFiManager wm;

    // Uncomment this line if you want to wipe saved settings for testing
    // wm.resetSettings();

    // This sets a timeout so the ESP32 doesn't stay in AP mode forever
    // If no one connects in 180 seconds, it will move on/reboot
    wm.setConfigPortalTimeout(180);

    // autoConnect(AP_Name, AP_Password)
    // This fetches SSID and pass from NVS and tries to connect.
    // If it fails, it starts an AP with the specified name.
    bool res;
    String nw_name = "RemoteAC_" + WiFi.macAddress();
    res = wm.autoConnect(nw_name.c_str(), "remote123"); 

    if(!res) {
        Serial.println("Failed to connect or hit timeout");
        // ESP.restart();
    } else {
        // If you get here, you are connected to the WiFi!
        Serial.println("Connected... yeey :)");
        Serial.print("IP Address: ");
        Serial.println(WiFi.localIP());
    }
}

void loop() {
    // Your normal code goes here
}