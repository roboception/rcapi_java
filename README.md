Java client library for Roboception's REST-API
==============================================

Java client library to interface the REST-API provided on [Roboception's rc_visard][rc_visard] 3D sensors. 

Provided functionality:

* access all parameters for configuration of the rc_visard and all on-board components (nodes)
* access all services provided by the on-board components
* access the current system state including
    * firmware version
    * time synchronization state (ptp/ntp)
    * network properties

This repository contains two modules, i.e.

* the core library `com.roboception.rcapi.core` in the `./rcapi-java/` directory
* and some basic examples `com.roboception.rcapi.examples` in the `./rcapi-java-examples/` directory


Installation
-------------

### Dependencies

This library uses the [Restlet framework](https://restlet.com/open-source/) for issueing REST-API calls. The following dependencies are required

* org.restlet
* org.json
* org.yaml
* org.apache.commons-lang3


### Building from source

The repository is organized as a [Maven project](https://maven.apache.org/what-is-maven.html). So simply download Maven and follow the typical [Maven build-flow](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html). Note that building from source first time probably requires an internet connection as dependencies are downloaded on demand.

For instance, issueing
```
mvn package
``` 
in the main repository will download all dependencies, compile and test the source code of both modules, as well as create the packages

* `./rcapi-java/target/rcapi-0.1-SNAPSHOT.jar`
* `./rcapi-java/target/rcapi-0.1-SNAPSHOT-jar-with-dependencies.jar`

which contains the core-classes only or with required dependencies, respectively.

API
---

The API basically provides client interfaces for four different use cases:

* single parameter access: `Parameter.java`
    ```java
    // creation of a local Parameter client 
    Parameter param = Parameter.connectTo("192.168.1.101", 
            "rc_hand_eye_calibration", "grid_width");
    System.out.println("Connected to param: " + param.getName() + " - "
            + param.getDescription());

    // value access - setting and getting; Parameter type is inferred
    // for most cases
    double value = param.getValue();
    param.setValue(value + 1);
    System.out.println("new local value: " + param.getValue());

    // synchronization from remote Parameter overwrites local value
    System.out.println("synced local value: "
            + param.syncFromRemote().getValue());

    // synchronization to remote Parameter (might overwrite local value
    // in case it is not accepted)
    param.setValue(param.<Double> getMax() + 1);
    System.out.println("too large local value: " + param.getValue());
    System.out.println("synced local value: "
            + param.syncToRemote().getValue());
    ```
* single service access: `Service.java`
    ```java
    // connect to service that does not require any arguments nor
    // return any result
    Service service = Service.connectTo("192.168.1.101",
            "rc_hand_eye_calibration", "reset_defaults");
    System.out.println("Connected to service: " + service.getInfo());
    service.call();
    System.out.println("Successfully called service!");
    ```

    ```java
    // connect to service that does not require any arguments but
    // returns some result
    Service service = Service.connectTo("rc-visard-02938425.local",
            "rc_hand_eye_calibration", "get_calibration");

    // if during compile time no Java type can be defined for
    // the response of this service call, it will be return as
    // com.fasterxml.jackson.databind.JsonNode    
    JsonNode result = (JsonNode) service.call();
    System.out.println("Service call returned object of type "
            + result.getClass().getName() + " " + result);

    // if we can define the Java class as which the response
    // should be returned, we can simply cast the result of
    // the service call
    service.setResponseType(CalibrationResponse.class);
    CalibrationResponse result = (CalibrationResponse) service.call();
    System.out.println("Service call returned object of type "
            + result.getClass().getName() + " " + result);    
    ```
* interfacing an entire node with all its parameters, services, and its status: `Node.java`
    ```java
    // connect to a node in the rc_visard's REST-API
    Node node = Node.connectTo("192.168.1.101", "rc_stereocamera");
    System.out.println("Connected - status: " + node.getStatus());

    // list and access all its parameters
    System.out.println("Available parameters: "
                    + node.getAvailableParameters());
    Parameter param = node.getParameter("exp_value");
    param.setValue(0.015).syncToRemote();

    // list and access all its services
    System.out.println("Available services: "
                    + node.getAvailableServices());
    node.getService("reset_defaults").call();
    ```
* interfacing an rc_visard device with all its nodes and the system state: `Visard.java`
    ```java
    // connect to an rc_visard device
    Visard rcvisard = Visard.connectTo("rc-visard-02938425.local");

    // access full system state
    SysInfo sysInfo = rcvisard.getSystemInfo().syncFromRemote();
    System.out.println("Ready to rumble? " + sysInfo.ready);

    // list and access all its nodes
    System.out.println("Available nodes: "
                    + rcvisard.getAvailableNodes());
    rcvisard.getNode("rc_stereocamera").getParameter("exp_value")
                    .setValue(0.004).syncToRemote();
    ```


Examples
--------

The `/rcapi-java-examples/` modules provides more funcational examples, currently
* `PrintRCVisardInfo.java` showing how to interface an rc_visard device and gather information about paramters, services, and the current system state
* `GetHandEyeCalibration.java` showing the interface for service calls on a real example: getting the current hand-eye-calibration transformation from an rc_visard device.






[rc_visard]: http://roboception.com/rc_visard