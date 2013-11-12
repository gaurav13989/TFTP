TFTP
====
*******************************************************
 Foundations of Computer Networks(csci651) Project 1
 
     Author: Gaurav Komera Email:gxk9544@rit.edu
 
     TFTP Implementation
 
 Client & server use timeouts and retransmissions for
      file transfers
 Can handle large file loads. Tested for upto file size
   25MB, with one system on the RIT network
     and another outside.
 
******************************************************
 The project has two separate sections
 Section 1 - Server
 
 1. CreateFile.java
 2. FileChunks.java
 3. SendFile.java
 4. TFTPServer.java#
 
 It has been assumed that for the purpose of compiling, 
 the user would use the command shell in windows or 
 terminal in Mac or Ubuntu
 
 Compile the above 4 java files in a single folder 
 and run the 'TFTPServer' java application.
 
 For compiling use the following commands,
 
   javac CreateFile.java
   javac FileChunks.java
   javac SendFile.java
   javac TFTPServer.java
 
 For executing the server file,
 
   java TFTPServer
 
 Section 2 - Client
 
 1. CreateFile.java
 2. FileChunks.java
 3. TFTPClient.java#
 
 Compile the above 3 java files in a single folder 
 and run the 'TFTPClient' java application.
 
 For compiling use the following commands,
 
   javac CreateFile.java
   javac FileChunks.java
   javac TFTPClient.java
 
 For executing the client file,
 
   java TFTPClient
 
 The two can be on the same machine in which case 
 the client would address the server as "localhost" 
 at port "49152".
 OR
 The server files may be placed on a remote machine.
 
 On executing the server program(TFTPServer), the 
 server continuously listens for incoming requests 
 on port "49152".
 
 Any client can connect to the server by specifying 
 the server machine's host name and the port number 
 "49152" as follows:
 "connect abc.def.edu 49152" if the 
 server program is run on abc.def.edu.
 
 The server can handle more than one clients 
 simultaneously
 
*******************************************************
 
 # programs to be executed after compilation
 
