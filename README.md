# JBiclustGE-CLI

This repository contains the source-code of the command line interface to execute algorithms provided by [JBiclustGE API](https://github.com/jbiclustge/JBiclustGE)

Next, please visit the [website](https://jbiclustge.github.io) to see the usage instructions.

## Compile JBiclustGE-CLI
### Requirements
 - [Java SDK 1.8](http://www.oracle.com/technetwork/java/javase/downloads/index.html) 
 - [Git](https://git-scm.com)
 - [Maven](https://maven.apache.org)

### Compile

1. Install git 
  - Follow the instruction to install [git](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git) 
  - Run the following command in console:
       ``` 
       git clone https://github.com/jbiclustge/JBiclustGE-CLI.git
       ```

2. Install maven
  - Follow the instructions to install [Maven](https://maven.apache.org/install.html)
  - Windows users have to define JAVA_HOME to JDK, follow this [instructions](http://roufid.com/no-compiler-is-provided-in-this-environment/)

3. Enter into the folder of JBiclustGE-CLI (downloaded in the previous step 1)
   - The compilation process produces verbose output, presenting all the operations that are being performed in compilation. Thus, you can perform the compilation in two ways:
      - Quiet Mode, presenting only the errors:
           ```
           mvn clean package -q 
           ```
           
      - Normal Mode, presenting the main operations occurring in compilation:
           ```
           mvn clean package 
          ```

3. Installers will be placed into folder "target"
    - File "jbiclustge-cli-installer.jar" is the installer that can be used in windows or linux
    - File "jbiclustge-cli.deb" is the deb installer that can be used only in linux

## Installation

To proceed with installation using jbiclustge-cli-installer.jar, follow this [instructions](https://jbiclustge.github.io/configs/download/#windows-and-linux-jar-installer_1)
Otherwise if you are using the jbiclustge-cli.deb, follow this [instructions](https://jbiclustge.github.io/configs/download/#linux-deb-installer_1)



