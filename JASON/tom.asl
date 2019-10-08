//Beliefs

/* Possible Occupations:
    'cod - function - minValue - maxValue'

occupation(0,plumper,30,150).
occupation(0,plumper,40,150).
occupation(0,casas,40,150).
occupation(1,electrician,30,100).
occupation(2,builder,80,1000).

price(0).
*/

count(0).

//Initial desire.

!start1.

/* Plans */

//Defines the worker function
+!start1 <- .wait(200); .print("Start1"); 
            .print("1 starting !!!!!!!!"); 
            !start2;
            !start3; 
            .print("1 ending !!!!!!!!").

+!start2 : count(X) & X<10 
        <- -+count(X+1); 
            .wait(1000); 
            .print("Start2"); 
            !start2.

+!start2 <- -+count(0); .print("FINISH 2").

+!start3 : count(X) & X<10 
        <- -+count(X+1); 
            .wait(1000); 
            .print("Start3"); 
            !start3.

+!start3 <- -+count(0); .print("FINISH 3").

