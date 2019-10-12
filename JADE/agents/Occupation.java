package agents;

import java.util.Random;

public class Occupation {
    private int maxValue;
    private int minValue;
    private int workTime;
    private String occup;
    private static int nOccup=10;

    Occupation(){
        Random rand = new Random();
        int n = rand.nextInt(10);

        //Define de worker function
        switch(n){
            case 0:
                this.occup = "plumber";
                this.minValue = 30;
                this.maxValue = 150;
                // this.workTime = 30;
                this.workTime = 1;
                break;
            case 1:
                this.occup = "electrician";
                this.minValue = 30;
                this.maxValue = 100;
                // this.workTime = 25;
                this.workTime = 1;
                break;
            case 2:
                this.occup = "builder";
                this.minValue = 80;
                this.maxValue = 1000;
                // this.workTime = 80;
                this.workTime = 1;
                break;
            case 3:
                this.occup = "baker";
                this.minValue = 20;
                this.maxValue = 100;
                this.workTime = 1;
                // this.workTime = 10;
                break;
            case 4:
                this.occup = "hairdresser";
                this.minValue = 10;
                this.maxValue = 90;
                this.workTime = 1;
                // this.workTime = 10;
                break;
            case 5:
                this.occup = "gardner";
                this.minValue = 30;
                this.maxValue = 200;
                // this.workTime = 40;
                this.workTime = 1;
                break;
            case 6:
                this.occup = "mechanic";
                this.minValue = 40;
                this.maxValue = 1000;
                // this.workTime = 30;
                this.workTime = 1;
                break;
            case 7:
                this.occup = "painter";
                this.minValue = 25;
                this.maxValue = 400;
                // this.workTime = 50;
                this.workTime = 1;
                break;
            case 8:
                this.occup = "engineer";
                this.minValue = 400;
                this.maxValue = 4000;
                // this.workTime = 80;
                this.workTime = 1;
                break;
            case 9:
                this.occup = "doctor";
                this.minValue = 400;
                this.maxValue = 4500;
                // this.workTime = 20;
                this.workTime = 1;
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
        // Random rand = new Random();
        // return (int)(this.workTime + 10 * rand.nextFloat()); 
        return 1;
    }

    public static int nOpccupations(){
        return nOccup;
    }

    public static String num2func(int i){
        String fun = "";
        //Define de worker function
        switch(i){
            case 0:
                fun = "plumber";
                break;
            case 1:
                fun = "electrician";
                break;
            case 2:
                fun = "builder";
                break;
            case 3:
                fun = "baker";
                break;
            case 4:
                fun = "hairdresser";
                break;
            case 5:
                fun = "gardner";
                break;
            case 6:
                fun = "mechanic";
                break;
            case 7:
                fun = "painter";
                break;
            case 8:
                fun = "engineer";
                break;
            case 9:
                fun =  "doctor";
                break;
        }
        return fun;
    }

}