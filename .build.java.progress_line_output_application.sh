#!/bin/bash
rm -rf .build/java/classes
/Library/Java/JavaVirtualMachines/jdk-23.jdk/Contents/Home/bin/javac -d .build/java/classes Java/com/lesrere/playground/ProgressLineOutputApplication.java
cd .build/java/classes/
/Library/Java/JavaVirtualMachines/jdk-23.jdk/Contents/Home/bin/java Java/com/lesrere/playground/ProgressLineOutputApplication