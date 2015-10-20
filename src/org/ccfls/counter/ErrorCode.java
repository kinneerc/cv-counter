package org.ccfls.counter;

import com.pi4j.io.gpio.GpioPinDigitalOutput;

public class ErrorCode extends Thread {
    
    // error code
    // solid = shutdown
    // 1 = unresponsive, 2 = no wifi, 3 = other database accessability problem
    // 4 = other uncaught exception, 0 = all is well
    public int error = 0;

    public boolean shutdown = false;

    GpioPinDigitalOutput led;

    public ErrorCode(GpioPinDigitalOutput led){
        this.led = led;
        error = 0;
    }

    // if out of range, default to error code 4
    public void setError(int e){
        if (e >= 0 && e <= 4){
            error = e;
        }else{
            error = 4;
        }
    }

    public void unresponsive(){
        setError(1);
    }
    public void connectivity(){
        setError(2);
    }
    public void database(){
        setError(3);
    }
    public void other(){
        setError(4);
    }
    public void shutdown(){
        shutdown = true;
    }
    public void allIsWell(){
        setError(0);
    }

    public void run(){
        
        while (true){

            try{
            Thread.sleep(1000);
            }catch(InterruptedException e){
            e.printStackTrace();
            }

            try {

            while (error == 1 && !shutdown){
                led.pulse(250,true);
                Thread.sleep(250);
            }
            while (error == 2 && !shutdown){
                led.pulse(1000,true);
                Thread.sleep(1000);
            }
            while (error == 3 && !shutdown){
                led.pulse(1000,true);
                Thread.sleep(1000);
                led.pulse(250,true);
                Thread.sleep(750);
            }
            while (error == 4 && !shutdown){
                led.pulse(1000,true);
                Thread.sleep(1000);
                led.pulse(250,true);
                Thread.sleep(250);
                led.pulse(250,true);
                Thread.sleep(250);
            }

            }catch (InterruptedException e){
                e.printStackTrace();
            }

            if (shutdown){
                led.high();
                return;
            }
                
        }
        

    }
}
