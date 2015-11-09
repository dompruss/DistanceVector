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
public class DistanceVector {

    Scanner in = new Scanner(System.in);

    public DistanceVector() {
        
        
        char ID;
        int routerNum = 0;
        boolean acceptableInput = false;
        do {
            System.out.print("Enter This Router's ID:");
            ID = in.nextLine().charAt(0);
            switch (ID) {

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
        } while (acceptableInput == false);
        Router rout = setupRouter(ID, routerNum);
        System.out.println("Router " + ID + " is running on port " + rout.getPort());
        
        DatagramSocket senderSocket = null;
        DatagramSocket receiverSocket =null;
        try {
            receiverSocket = new DatagramSocket(rout.getPort()+1000);
            senderSocket = new DatagramSocket(rout.getPort());
        } catch (SocketException ex) {
            Logger.getLogger(DistanceVector.class.getName()).log(Level.SEVERE, null, ex);
        }
        String vec = vectorToString(rout.getVector());
        System.out.println("Distance Vector on router " + ID + " is:\n" + vec);
        System.out.println("Press enter to send data once all routers are setup.");
        in.nextLine();
        sendVector(rout, senderSocket); // send out this vector to all other routers
        receiveVector(rout, receiverSocket, senderSocket);


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
        String string = "<";
        for (int i = 0; i < vector.length; i++) {
            if (i != vector.length - 1) {
                string += vector[i] + ", ";
            } else {
                string += vector[i] + ">";
            }
        }

        return string;
    }

    /*
     This takes the router's data and sends its vector info to all other routers
     */
    public void sendVector(Router r, DatagramSocket s) {
        try {
            InetAddress IPAddress = InetAddress.getByName("localhost");
            byte[] sendData = new byte[128];
            sendData[0] = (byte) r.getName();
            int[] v = r.getVector();
            for (int i = 0; i < v.length; i++) {
                sendData[i + 1] = (byte) v[i];
            }
            for (int i = 6666; i < 6666 + v.length; i++) {
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
            byte[] rcvData = new byte[128];
            DatagramPacket rcvPkt = new DatagramPacket(rcvData, rcvData.length);
            do{
            rec.receive(rcvPkt);
            rcvData = rcvPkt.getData();
            char name = (char) rcvData[0];
            r2 = new Router(name, 9999, r1.getVector().length);
            int[] vec = r2.getVector();
            for (int i = 0; i < r1.getVector().length; i++) {
                vec[i] = (int) rcvData[i + 1];
            }
            System.out.println("Received distance vector from router "+name+":"+vectorToString(r2.getVector()));
            if(updateInfo(r1,r2)){
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
        boolean changed = false;
        int[] v1 = r1.getVector();
        int[] v2 = r2.getVector();
        int secLoc = 999;

        for (int i = 0; i < v2.length; i++) {
            if (v2[i] == 0) {
                secLoc = i;
            }
        }
        for (int i = 0; i < v1.length; i++) {
            if (v1[i] > v1[secLoc] + v2[i]) {
                v1[i] = v1[secLoc] + v2[i];
                changed = true;
            }

        }
        return changed;
    }

}
