//Beliefs

/* Possible Occupations:
    'cod - function - minValue - maxValue - min working time'
*/
occupation(0,plumper,30,150,30).
occupation(1,electrician,30,100,25).
occupation(2,builder,80,1000,50).

nOccupations(3).

price(0).

working(false).
count(0).
workTime(0).
thinking(false).


//Initial goals.
!start.

/* Plans */

//Defines the worker function
+!start : .random(R) & nOccupations(NO) & N = math.floor(NO*R) & occupation(N,F,Min,Max,MWT) 
        <- +myFunction(F, Min, Max,MWT); 
            .print("I am ",F);
            !getPrice; !register.

//Get the price for the service to be done
+!getPrice : myFunction(F, Min, Max, _) & .random(R) & V = math.round(Min + (Max - Min)*R)
            <- -+price(V).

//Register the worker in the Yellow page
+!register : myFunction(F,_,_,_) 
            <- .df_register(F);
               .df_subscribe("client").

//Plan for working
+!work : count(C) & workTime(WT) & C<WT <- .wait(200);
                            //.print("Work count: ", C, " of ", WT);
                            -+count(C+1);
                            //.wait(200);
                            !work.

+!work <- -+workTime(0);
          -+count(0); 
          .print("Finished working!!!"); 
          -+working(false).                      // turn the worker to free state


// ---------------------------------------- CNP ------------------------------------------------
// answer to Call For Proposal
@c1 +cfp(CNPId,Task)[source(A)]
   :  provider(A,"client") & 
      myFunction(Task,_,_,_) &
      working(false) &
      price(Offer) &
      thinking(false)
   <- -+thinking(true);
      +proposal(CNPId,Task,Offer);              // remember my proposal
      //.print("offer ",Offer," for ",A);    //testes
      .send(A,tell,propose(CNPId,Offer));
      -cfp(CNPId,Task)[source(A)];              // clear cfp memory
      //.print("Offer sended to ",A);              //teste
      !getPrice;                              // recalculate the price for next proposal
      -+thinking(false).

// refuse Call For Proposal if worker already busy
+cfp(CNPId,Task)[source(A)] 
   <- .send(A,tell,refuse(CNPId));
      //.print("refusing work on ",Offer," for ",A);
      -cfp(CNPId,Task)[source(A)].              // clear cfp memory

// Receives accept for porposal
@r1 +accept_proposal(CNPId)[source(A)]
   :  proposal(CNPId,Task,Offer) &
      working(false) & 
      myFunction(_,_,_,MWT) & .random(R) & WT = math.round(MWT + 10*R)
   <- -+working(true);                          // turn the worker to busy state
      -+workTime(WT);
      .print("Working for ",A);
      .send(A,tell,inform_done(CNPId));
      .abolish(proposal(CNPId,_,_));            // clear proposal memory
      -accept_proposal(CNPId)[source(A)];       // clear accept_proposal memory
      !work.                                    // do the task and report to client
                                          
// refuse Proposal if worker already busy
+accept_proposal(CNPId)[source(A)]
   :   working(true)
   <- .send(A,tell,inform_ref(CNPId));             // refuse the task and report to initiator
      .abolish(proposal(CNPId,_,_));               // clear proposal memory
      -accept_proposal(CNPId)[source(A)].          // clear accept_proposal memory

@r2 +reject_proposal(CNPId)[source(A)]
   <- //.print("I lost CNP ",CNPId, ".");
      .abolish(proposal(CNPId,_,_));               // clear proposal memory
      -reject_proposal(CNPId).                     // clear reject_proposal memory
