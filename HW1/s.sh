#!/bin/sh

javac Switch.java
jar cvfm Switch.jar manifest_switch.txt Switch.class SwitchSockHandler.class SwitchTimerTask.class
java -cp Switch.jar Switch 1 2000
