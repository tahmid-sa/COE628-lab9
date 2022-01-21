/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lab9;

/**
 *
 * @author Tahmid Sajin, Section 2 COE628, Lab 9
 */

import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DiningPhilosophers {
    final static int n = 5;
    final static Philosopher[] philosophers = new Philosopher[n];
    final static Semaphore mutex = new Semaphore(1);
    
    public static void main(String[] args) {
        philosophers[0] = new Philosopher(0);
        
        for (int i = 1; i < n; i++) {
            philosophers[i] = new Philosopher(i);
        }
        
        for (Thread t : philosophers) {
            t.start();
        }
    }
    
    public static int numberDone = 0;
    
    public static class Philosopher extends Thread {
        private enum State {THINKING, HUNGRY, EATING};
        public static int counter = 0;
        private final int id;
        public State state;
        private final Semaphore self;
        private int done = 0;
        
        Philosopher(int id) {
            this.id = id;
            self = new Semaphore(0);
            state = State.THINKING;
        }
        
        private Philosopher left() {
            return philosophers[id == 0 ? n - 1 : id - 1];
        }
        
        private Philosopher right() {
            return philosophers[(id + 1) % n];
        }
        
        @Override
        public void run() {
            try {
                while (numberDone < n) {
                    printState();
                    counter++;
                    
                    switch(state) {
                        case THINKING:
                            pause();
                            
                            if (done != 1) {
                                mutex.acquire();
                                state = State.HUNGRY;
                            } else {
                                System.out.println("Philosopher " + this.id + " completed his dinner");
                            }
                            
                            break;
                        case HUNGRY:
                            get_forks(this);
                            mutex.release();
                            self.acquire();
                            state = State.EATING;
                            break;
                        case EATING:
                            numberDone++;
                            pause();
                            mutex.acquire();
                            state = State.THINKING;
                            
                            put_forks(this);
                            mutex.release();
                            this.done = 1;
                            break;
                    }
                }
                
                if (this.id == 0) {
                    pause();
                    System.out.println("Till now number of philosophers completed dinner are " + numberDone);
                }
            } 
            catch(InterruptedException e) {}
        }
        
        static private void get_forks(Philosopher p) {
            if (p.left().state != State.EATING && p.state == State.HUNGRY && p.right().state != State.EATING) {
                p.state = State.EATING;
                p.self.release();
            }
        }
        
        static private void put_forks(Philosopher p) {
            get_forks(p.left());
            get_forks(p.right());
        }
        
        private void pause() {
            try {
                Thread.sleep((long)Math.round(Math.random() * 5000));
            } catch (InterruptedException e) {}
        }
        
        private void printState() {
            if (state != State.HUNGRY) {
                System.out.println("Fork " + this.id + " taken by Philosopher " + this.id);
            }
            
            if (state == State.EATING) {
                System.out.println("Philosopher " + this.id + " released Fork " + this.id + " and Fork " + left().id);
                System.out.println("Til now the number of philosophers completed dinner are " + numberDone);
            }
            
            if (state == State.HUNGRY) {
                System.out.println("Philosopher " + this.id + " waiting for Fork " + left().id);
            }
        }
    }
}
