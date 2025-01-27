package com.phonepe.central;

import io.appform.opentracing.TracingAnnotation;

/**
 * @author ankush.nakaskar
 */
public class HelloWorld {
    public void sayHello(){
        System.out.println("Hello Ankush...!!!");
    }

    @TracingAnnotation
    public void sayHelloWithAnnotation(){
        System.out.println("sayHelloWithAnnotation Ankush...!!!");
    }
}
