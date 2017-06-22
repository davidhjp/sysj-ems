# SystemJ IPC Classes for the Energy Monitoring System

Input and output SystemJ signals using `com.systemjx.ems.InputSignal` and `com.systemjx.ems.OutputSignal` 
IPC classes build and transmit packets via TCP socket using the following packet formats. 
All fields are 1 byte except temperature, humidity and light values in the outgoing packet.

Incoming packet format:
```
------------------------------------------------------------------------------------------------
| AA | BB | Size | Node Group ID | Node ID | Packet type (Fixed to 0x84) | Actuator ID | Value |
------------------------------------------------------------------------------------------------
```

Outgoing packet format:
```
-----------------------------------------------------------------------------------------------------------
| AA | BB | Size | Node Group ID | Node ID | Packet type (Fixed to 0xA0) | Temperature | Humidity | Light |
-----------------------------------------------------------------------------------------------------------
```

## Running the test program
1. Open `src/main/configurations/rpi.xml` and update IPs for the signal elements to the hostname of the RP.
2. Run:
```
$ ./gradlew(.bat) run
```


## Integrate IPC classes to the SystemJ runtime
1. Build jar file using gradle:
```
$ ./gradlew # For Linux/Unix
```
```
gradlew.bat # For Windows
```
2. Copy the generated jar file (e.g. `sysj-ems-1.0-SNAPSHOT.jar`) to the `lib` directory of the SystemJ tool. 

WIP...
