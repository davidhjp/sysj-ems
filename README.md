# SystemJ IPC Classes for the Energy Monitoring System

Packet format (`<Name:# of Byte(s)>`)
```
AA BB <Size:1> <Node Group ID:1> <Node ID:1>  <Packet type:1> <Temp:2> <Humidity:2> <Light:2>
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
