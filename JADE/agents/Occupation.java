package agents;

import java.util.Random;

public class Occupation {
    private int maxValue;
    private int minValue;
    private int workTime;
    private String occup;

    Occupation(){
        Random rand = new Random();
        int n = rand.nextInt(3);

        //Define de worker function
        switch(n){
            case 0:
                this.occup = "plumper";
                this.minValue = 30;
                this.maxValue = 150;
                this.workTime = 30;
                break;
            case 1:
                this.occup = "electrician";
                this.minValue = 30;
                this.maxValue = 100;
                this.workTime = 25;
                break;
            case 2:
                this.occup = "builder";
                this.minValue = 80;
                this.maxValue = 1000;
                this.workTime = 80;
                break;
            case 3:
                this.occup = "baker";
                this.minValue = 20;
                this.maxValue = 100;
                this.workTime = 10;
                break;
            case 4:
                this.occup = "hairdresser";
                this.minValue = 10;
                this.maxValue = 90;
                this.workTime = 10;
                break;
            case 5:
                this.occup = "gardner";
                this.minValue = 30;
                this.maxValue = 200;
                this.workTime = 40;
                break;
            case 6:
                this.occup = "mechanic";
                this.minValue = 40;
                this.maxValue = 1000;
                this.workTime = 30;
                break;
            case 7:
                this.occup = "painter";
                this.minValue = 25;
                this.maxValue = 400;
                this.workTime = 50;
                break;
            case 8:
                this.occup = "engineer";
                this.minValue = 400;
                this.maxValue = 4000;
                this.workTime = 80;
                break;
            case 9:
                this.occup = "doctor";
                this.minValue = 400;
                this.maxValue = 4500;
                this.workTime = 20;
                break;
        }
    }

    public String getOccup(){
        return this.occup;
    }

    public int getPrice(){
        Random rand = new Random();
        return (this.minValue + (int)((this.maxValue - this.minValue) * rand.nextFloat())); 
    }

    public int getTime(){
        Random rand = new Random();
        return (int)(this.workTime + 10 * rand.nextFloat()); 
    }

}