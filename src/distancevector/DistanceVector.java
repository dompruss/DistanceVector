/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package distancevector;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 Nicolas Moore
 Dominik Pruss
 Networks 466 Lab 5
 */
class Router {
    private int port;                               // hold port number
    private int[] vector;                           // hold vector info
    private char name;                              // hold router name
    
    /*
     Constructor for Router object
    */
    public Router(char name, int port, int size){
        this.name = name;
        this.port = port;
        vector = new int[size];
    }
    
    /*
     Returns port number
    */
    public int getPort(){
        return port;
    }
    
    /*
     Returns vector info
    */
    public int[] getVector(){
        return vector;
    }
    
    /*
     Returns name
    */
    public char getName(){
        return name;
    }
    
}

public class DistanceVector {

    Scanner in = new Scanner(System.in);                            //initialize scanner
   
    /*
    Constructor
    */
    public static void main(String[] args){
        DistanceVector dv = new DistanceVector();
    }
    public DistanceVector() {
        
        
        char ID;                                                    //store router name
        int routerNum = 0;                                          //asign number to router
        boolean acceptableInput = false;                            //boolean to see if input is valid
        do {
            System.out.print("Enter This Router's ID:");            //prompt user for info
            ID = in.nextLine().charAt(0);                           //store information
            switch (ID) {                                           //switch on input to read appropriate lines from file

                case 'X':
                case 'x':
                    routerNum = 1;
                    acceptableInput = true;
                    break;
                case 'Y':
                case 'y':
                    routerNum = 2;
                    acceptableInput = true;
                    break;
                case 'Z':
                case 'z':
                    routerNum = 3;
                    acceptableInput = true;
                    break;
                default:
                    System.out.println("This input is incorrect, please enter X,Y, or Z.");
            }
        } while (acceptableInput == false);                         //run until input is valid
        
        Router rout = setupRouter(ID, routerNum);                   //initialize router with given info
        System.out.println("Router " + ID + " is running on port " + rout.getPort()); //print out info
        
        DatagramSocket senderSocket = null;                         //create socket to send on
        DatagramSocket receiverSocket =null;                        //create socket to receive on
        try {
            receiverSocket = new DatagramSocket(rout.getPort()+1000);   //initialize receiver
            senderSocket = new DatagramSocket(rout.getPort());          //initialize sender
        } catch (SocketException ex) {                                  //catch exceptions
            Logger.getLogger(DistanceVector.class.getName()).log(Level.SEVERE, null, ex);
        }
        String vec = vectorToString(rout.getVector());                  //get vector
        System.out.println("Distance Vector on router " + ID + " is:\n" + vec); //print info
        System.out.println("Press enter to send data once all routers are setup."); //prompt user
        in.nextLine();
        sendVector(rout, senderSocket);                                 // send out this vector to all other routers
        receiveVector(rout, receiverSocket, senderSocket);              //wait to receive data

    
    }


    /*
     This method sets up a router object and returns it. The router object will have a port number
     and will have an integer array called vector.
     */
    public Router setupRouter(char ID, int routerNum) {
        String str = null; // will be used to store the first line
        BufferedReader buff = null; // will be used to read the file.
        Router router = new Router('a', 1, 1); // setting up a dummy router
        try {
            buff = new BufferedReader(new FileReader(new File("res/configuration.txt")));
            str = buff.readLine(); // reads the first line of the array;
            String[] line = str.split("\t"); // splits the first line of the array based on spaces
            int port = Integer.parseInt(line[routerNum - 1]);  // grabs the port number
            int size = line.length; // sets up the number of routers in this vector
            router = new Router(ID, port, size); // initializes this router.
            int[] vector = router.getVector(); // grabs the location of this vector
            int n = 1; // holds the row we are on
            while ((str = buff.readLine()) != null) {
                if (n == routerNum) {// if this vector line matches our router
                    line = str.split("\t"); // split on spaces
                    for (int i = 0; i < size; i++) { // going to move the values in this row into the vector as ints
                        vector[i] = Integer.parseInt(line[i]);
                    }
                }
                n++;
            }
        } catch (Exception e) {

        }
        return router;

    }
    /*
     This method takes the vector receieved, and turns it into a purdy string
     */

    public String vectorToString(int[] vector) {
        String string = "<";                        //start of string
        for (int i = 0; i < vector.length; i++) {   //loop through array
            if (i != vector.length - 1) {
                string += vector[i] + ", ";         //concadinate string
            } else {
                string += vector[i] + ">";
            }
        }

        return string;                              //return final string
    }

    /*
     This takes the router's data and sends its vector info to all other routers
     */
    public void sendVector(Router r, DatagramSocket s) {
        try {
            InetAddress IPAddress = InetAddress.getByName("localhost");     //get IP
            byte[] sendData = new byte[128];                                //set up byte array
            sendData[0] = (byte) r.getName();                               //set first spot to router name
            int[] v = r.getVector();                                        //get distance vector
            for (int i = 0; i < v.length; i++) {
                sendData[i + 1] = (byte) v[i];                              //set up rest of byte array
            }
            for (int i = 6666; i < 6666 + v.length; i++) {                  //send to the other routers
                if (r.getPort() != i) {
                    DatagramPacket sendPkt = new DatagramPacket(sendData, sendData.length, IPAddress, i + 1000);
                    s.send(sendPkt);
                }
            }
        } catch (IOException exc) {

        }

    }

    /*
     This method will run and listen for new information 
     */
    public void receiveVector(Router r1, DatagramSocket rec, DatagramSocket sender) {
        Router r2 = null;
        try {
            byte[] rcvData = new byte[128];                                             //create array to receive data
            DatagramPacket rcvPkt = new DatagramPacket(rcvData, rcvData.length);        //initialize packet
            do{
            rec.receive(rcvPkt);                                                        //get data
            rcvData = rcvPkt.getData();
            char name = (char) rcvData[0];
            r2 = new Router(name, 9999, r1.getVector().length);
            int[] vec = r2.getVector();
            for (int i = 0; i < r1.getVector().length; i++) {
                vec[i] = (int) rcvData[i + 1];
            }
            System.out.println("Received distance vector from router "+name+":"+vectorToString(r2.getVector()));    //print info
            if(updateInfo(r1,r2)){                                                                                  //if updated print and resend new info
                System.out.println("Distance vector on router "+r1.getName()+" is updated to:\n"+vectorToString(r1.getVector()));
                sendVector(r1,sender);
            }
            else{
                System.out.println("Distance vector on router "+r1.getName()+" is not updated.");
            }
            
            } while(true);
        } catch (IOException exc) {

        }
       
    }

    /*
     This method will take in two routers, and update the distance vectors if applicable.
     */
    public boolean updateInfo(Router r1, Router r2) {
        boolean changed = false;                                //boolean for updated or not
        int[] v1 = r1.getVector();                              //get two vwctors to compare
        int[] v2 = r2.getVector();
        int secLoc = 999;                                       //hold which router we are at

        for (int i = 0; i < v2.length; i++) {                   //see which router we are at
            if (v2[i] == 0) {
                secLoc = i;
            }
        }
        for (int i = 0; i < v1.length; i++) {                   //do distance vector calculation
            if (v1[i] > v1[secLoc] + v2[i]) {
                v1[i] = v1[secLoc] + v2[i];
                changed = true;
            }

        }
        return changed;
    }

}
