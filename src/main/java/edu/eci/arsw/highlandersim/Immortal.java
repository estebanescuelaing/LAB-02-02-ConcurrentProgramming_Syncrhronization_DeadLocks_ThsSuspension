package edu.eci.arsw.highlandersim;

import java.util.List;
import java.util.Random;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback=null;
    
    private int health;
    
    private int defaultDamageValue;

    private final List<Immortal> immortalsPopulation;

    private final String name;

    private final Object pauseLock; //como un monitor para que lo usen los hilos para sincronizarse y pausar la ejecución de los hilos

    private volatile boolean paused; //pausar el hilo

    private volatile boolean running; //reanudar


    private final Random r = new Random(System.currentTimeMillis());


    public Immortal(String name, List<Immortal> immortalsPopulation, int health, int defaultDamageValue, ImmortalUpdateReportCallback ucb, Object pauseLock) {
        super(name);
        this.updateCallback=ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = health;
        this.defaultDamageValue=defaultDamageValue;
        this.pauseLock = pauseLock;
        this.paused = false;
        this.running = true;
    }

    public boolean isPaused() {
        synchronized (pauseLock) {
            return paused;
        }
    }

    public void pause() {
        paused = true;
    }

    public void resumeImmortal() {
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll();
        }
    }

    public void stopImmortal() {
        running = false;
        interrupt();
    }

    public void run() {

        while (running) {

            synchronized (pauseLock) {
                while (paused) {
                    try {
                        pauseLock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }

            if (immortalsPopulation.size() <= 1) {
                break;
            }

            int myIndex = immortalsPopulation.indexOf(this);

            int nextFighterIndex =
                r.nextInt(immortalsPopulation.size());

            if (nextFighterIndex == myIndex) {
                nextFighterIndex =
                    (nextFighterIndex + 1)
                    % immortalsPopulation.size();
            }

            Immortal im =
                immortalsPopulation.get(nextFighterIndex);

            this.fight(im);

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    public void fight(Immortal i2) {

        int thisIndex = immortalsPopulation.indexOf(this);
        int otherIndex = immortalsPopulation.indexOf(i2);

        Immortal first;
        Immortal second;

        if (thisIndex < otherIndex) {
            first = this;
            second = i2;
        } else {
            first = i2;
            second = this;
        }

        synchronized (first) {
            synchronized (second) {

                if (i2.getHealth() > 0) {

                    i2.changeHealth(
                        i2.getHealth() - defaultDamageValue
                    );

                    this.health += defaultDamageValue;

                    updateCallback.processReport(
                        "Fight: " + this + " vs " + i2 + "\n"
                    );

                    if (i2.getHealth() <= 0) {
                        immortalsPopulation.remove(i2);
                    }

                } else {

                    updateCallback.processReport(
                        this + " says:" + i2 + " is already dead!\n"
                    );
                }
            }
        }
    }

    public void changeHealth(int v) {
        health = v;
    }

    public int getHealth() {
        return health;
    }

    @Override
    public String toString() {

        return name + "[" + health + "]";
    }

}
