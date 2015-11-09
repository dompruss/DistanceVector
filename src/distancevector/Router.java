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
    private int port;
    private int[] vector;
    private char name;
    
    public Router(char name, int port, int size){
        this.name = name;
        this.port = port;
        vector = new int[size];
    }
    
    public int getPort(){
        return port;
    }
    
    public int[] getVector(){
        return vector;
    }
    
    public char getName(){
        return name;
    }
    
}
