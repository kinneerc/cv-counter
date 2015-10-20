package org.ccfls.counter;

import com.pi4j.io.gpio.*;

public class Indicators extends Thread {

        // create gpio controller
        GpioController gpio;

        // pin objects
        GpioPinDigitalOutput green, orange, red;

        // stores timestamp of last heartbeat for detecting lack of responsiveness
        long alive;

        // how frequently we need to hear a heartbeat
        final long heartbeatThreashold = 5000;

        // we'll use this to broadcast error codes
        public ErrorCode error;



    public static void main(String[] args){
        (new Indicators()).start();
    }

    public Indicators(){

        System.out.println("<--Pi4J--> GPIO Control Started");
        
         // initialize gpio api
         gpio = GpioFactory.getInstance();

         // schedule gpio to be terminated if the program is stopped
         Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run(){
                System.out.println("--> ShutdownHook initialized");
                System.out.println("--> Only red light on");
                error.shutdown();
                try{
                error.join();
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
                green.low();
                orange.low();
                System.out.println("--> Shutdown gpio controller");
                gpio.shutdown();
            }
         });

         // lock down pin locations for leds
          green = gpio.provisionDigitalOutputPin(
                  RaspiPin.GPIO_03, "MyLED", PinState.HIGH);
          orange = gpio.provisionDigitalOutputPin(
                  RaspiPin.GPIO_04, "MyLED", PinState.HIGH);
          red = gpio.provisionDigitalOutputPin(
                  RaspiPin.GPIO_17, "MyLED", PinState.HIGH);

          // start listening for error codes
          error = new ErrorCode(red);
          error.start();

        System.out.println("--> All indicators: ON");
    }

    public void count() {
        orange.pulse(3000,false);
    }

    // Main thread should notify us that it is still doing ok
    // if not, then we will sound the alarm after heartbeatThreashold
    public void heartbeat(){
        alive = System.currentTimeMillis();
    }

    public void run() {

        try{

            // set lights to normal running mode
            Thread.sleep(3000);
            red.low();
            orange.low();

        // begin indicator update loop
        while (true){
            if (System.currentTimeMillis() - alive > heartbeatThreashold){
                green.low();
                orange.low();
                error.unresponsive();
            }
           Thread.sleep(1000); 
            
        }

               // stop all GPIO activity/threads by shutting down the GPIO controller
        // (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
        /* gpio.shutdown(); */

        } catch (InterruptedException e){
            e.printStackTrace();
        }

    }


}
