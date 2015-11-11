/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package distancevector;

/**
 * Nicolas Moore
 * Dominik Pruss
 * Networks 266 Lab 5
 */

public class Router {
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
