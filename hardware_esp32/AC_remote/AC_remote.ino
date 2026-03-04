#include <Arduino.h>
#include <WiFi.h>
#include <WebServer.h>
#include <IRremoteESP8266.h>
#include <IRsend.h>

// --- CONFIGURATION ---
const char* ssid = "YOUR_WIFI_NAME";
const char* password = "YOUR_WIFI_PASSWORD";

const uint16_t kIrLedPin = 4; // Your transmitter pin
IRsend irsend(kIrLedPin);
WebServer server(80);

// PASTE YOUR CAPTURED CODE HERE (from the Sniffer step)
uint16_t heatOnCode[] = {9000, 4500, 560, 1690, 560, 560, ...}; // Replace with your actual array

void handleRoot() {
  String html = "<html><head><meta name='viewport' content='width=device-width, initial-scale=1'>";
  html += "<style>button { width: 100%; height: 100px; font-size: 24px; background: #ff4747; color: white; border-radius: 10px; }</style></head>";
  html += "<body><h1>Home Climate</h1>";
  html += "<form action='/heat' method='POST'><button type='submit'>TURN HEATING ON</button></form>";
  html += "</body></html>";
  server.send(200, "text/html", html);
}

void handleHeat() {
  Serial.println("Sending IR Signal...");
  // Use sendRaw(array, length, frequency)
  irsend.sendRaw(heatOnCode, sizeof(heatOnCode) / sizeof(heatOnCode[0]), 38);
  server.sendHeader("Location", "/");
  server.send(303);
}

void setup() {
  Serial.begin(115200);
  irsend.begin();
  
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) { delay(500); Serial.print("."); }
  
  Serial.println("");
  Serial.print("Connected! IP address: ");
  Serial.println(WiFi.localIP()); // COPY THIS IP ADDRESS

  server.on("/", handleRoot);
  server.on("/heat", HTTP_POST, handleHeat);
  server.begin();
}

void loop() {
  server.handleClient();
}