# CS 655 Programming Assignment 1
## Anne Turmel and Noah Picarelli-Kombert

This is the Github repository for Programming Assignment 1. It includes a simple Java program that codes a client and a server program that can send messages between each other. 

There are two parts:
* Echo: Text entered in the client's console is sent to the server and printed on the server's console. 
* Measure: Network statistics of the connection are measured, such as throughput and round-trip time, using client/server network information. 

Run instructions: 
* Clone the Github repository onto the machine that will be used to test, if not already done.
* Navigate to the src directory and run the command 'javac *.java'
* Find a valid port number on your machine that can be used to connect the client and server. 
* Run a program as follows:
  * Echo: java Echo -t server -p {port_number} \~or~ java Echo -t client {server_name} -p {port_number}
    * Client can enter any text into their console and the text will display on the server's console.
    * Type 'quit' to terminate the program. 
  * Measure: java Measure -t server -p {port_number} \~or~ java Measure -t client {server_name} -p {port_number}
    * Measurement of the network using the test statistics will commence, print, and finish. 
