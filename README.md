# CS 655 Programming Assignment 1
## Anne Turmel and Noah Picarelli-Kombert

This is the Github repository for Programming Assignment 1. It includes a simple Java program that 
codes a client and a server program that can send messages between each other. 

There are two parts:
* Echo: A user enters text on the client machine. This text is sent to the server over TCP. The server
responds with an identical TCP message.
* Measure: The client automatically sends several probes for messages of different sizes over TCP. The server
responds with identical messages. The client measures the time for the full interaction and, from it,
calculates throughput and round-trip time.

## Run instructions: 
* Clone the Github repository onto the two machines that will be used to test.
* On each machine, navigate to the src directory and run the command 'javac *.java'
* Find a port number available on both machines.
* To run the Echo program:
  * On the machine intended to be the server, run: java Echo -t server -p {port number}
  * The server will activate and begin looking for a client. Note that its IP address will print to the console.
  * On the machine intended to be the client, run: java Echo -t client -p {port number} -h {server IP address/hostname}
* To run the Measure program:
  * On the machine intended to be the server, run: java Measure -t server -p {port number}
  * Again, the server will activate and its IP address will be displayed.
  * On the machine intended to be the client, run: java Measure -t client -p {port number} -h {server IP address/hostname}
    * Optional arguments: 
      * -pn {probe number}: number of probes sent per test, default is 10
      * -sd {server delay ms}: number of milliseconds of simulated server delay per response, default is 0     
  * If the throughput numbers look odd on the csa machines (time recorded is 0ms and throughput evaluates to infinite), increase the server delay from 0. 

## Using the Echo program:  
When running Echo, enter any text into the client console and hit 'enter' to send it to the server. 
The server should receive this message, print out its contents to its own console, and send an identical 
message back to the client. The client will then print this echoed message to its console. To stop the 
connection, send the message 'quit' from the client to the server.  
Should either the client of server experience an error in attempting to send a packet, they will exit the program.  

## Using the Measure program:
When running Measure, the client will automatically send messages to the server. The server will, in turn, 
respond to each message with an echo message. First, the client will perform tests 
to check the round-trip time for messages of varying length. For each message length tested, the client 
will send ten identical messages (or however many specified in the command line argument -pn) to the server 
with the expectation that they are echoed back. It will time each round-trip in nanoseconds and print 
each result to the console as well as, at the end, the average time for that test. The client will then perform 
a similar process to check throughput for various sizes of message. For each test, it will print the throughput 
in Mb/s and then, at the end, the average result.  
As with Echo, if either the client or the server experiences an error while trying to send a packet then 
they will exit the program.
