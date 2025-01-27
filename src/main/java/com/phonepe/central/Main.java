package com.phonepe.central;

/**
 * @author ankush.nakaskar
 */
//https://dev.to/pmgysel/learn-aspect-oriented-programming-by-example-m8o
    
public class Main {

    public static void main(String[] args) {
        System.out.println("Ankush");
        HelloWorld helloWorld =new HelloWorld();
        helloWorld.sayHelloWithAnnotation();
    }

}
