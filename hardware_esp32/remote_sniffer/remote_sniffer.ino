#include <Arduino.h>
#include <IRrecv.h>
#include <IRremoteESP8266.h>
#include <IRutils.h>

// Define the pins
const uint16_t kRecvPin = 14; 
const uint16_t kCaptureBufferSize = 1024; // AC signals are large
const uint8_t kTimeout = 50;              // Gap between signals

IRrecv irrecv(kRecvPin, kCaptureBufferSize, kTimeout, true);
decode_results results;

void setup() {
  Serial.begin(115200);
  irrecv.enableIRIn(); // Start the receiver
  Serial.println("Ready to receive AC signals. Point your remote at the KY-022...");
}

void loop() {
  if (irrecv.decode(&results)) {
    // This is the magic part: it prints the code you'll need later
    Serial.println("--- Signal Detected ---");
    Serial.print(resultToHumanReadableBasic(&results));
    
    // This gives you the 'Raw Data' array for unsupported ACs
    Serial.println(resultToSourceCode(&results)); 
    
    Serial.println("-----------------------");
    irrecv.resume(); // Receive the next value
  }
}