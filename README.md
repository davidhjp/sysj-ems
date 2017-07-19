# SystemJ IPC Classes for the Energy Monitoring System

Input and output SystemJ signals using `com.systemjx.ems.InputSignal` and `com.systemjx.ems.OutputSignal` 
IPC classes build and transmit packets via TCP socket based on the packet formats shown below. 
All fields are 1 byte long except for temperature, humidity and light values, which are 2 bytes long.

Packet format for `com.systemjx.ems.OutputSignal`:
```
----------------------------------------------------------------------------------------------
| AA | BB | Size | Node Group ID (0x0B) | Node ID | Packet type (0xA0) | Actuator ID | Value |
----------------------------------------------------------------------------------------------
```

Packet format for `com.systemjx.ems.InputSignal`:
```
----------------------------------------------------------------------------------------------------
| AA | BB | Size | Node Group ID (0x0B) | Node ID | Packet type | Custom Fields (e.g. temperature) |
----------------------------------------------------------------------------------------------------
```
where `Packet type` is `0x84` for temperature, humidity, and light, `0x30` for a heater's state (e.g. ON or OFF), 
and `0x31` for the heater's instantaneous power value. Format of `Custom Fields` depends on the `Packet type`.

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
2. Copy the generated jar file (e.g. `sysj-ems-1.0-SNAPSHOT.jar`) to the `lib` directory of the SystemJ tool, or add 
the file to build path of the Eclipse project. 

WIP...
