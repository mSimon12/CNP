package agents;

import java.util.Random;

public class Occupation {
    private int maxValue;
    private int minValue;
    private String occup;

    Occupation(){
        Random rand = new Random();
        int n = rand.nextInt(3);

        //Define de worker function
        switch(n){
            case 0:
                this.occup = "plumper";
                this.maxValue = 150;
                this.minValue = 30;
                break;
            case 1:
                this.occup = "electrician";
                this.maxValue = 100;
                this.minValue = 30;
                break;
            case 2:
                this.occup = "builder";
                this.maxValue = 1000;
                this.minValue = 80;
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

}