/* Initial beliefs and rules */

/* Possible Occupations:
    'cod - function
*/
occupation(0,plumber).
occupation(1,electrician).
occupation(2,builder).
occupation(3,baker).
occupation(4,hairdresser).
occupation(5,gardener).
occupation(6,mechanic).
occupation(7,painter).
occupation(8,engineer).
occupation(9,doctor).

nOccupations(10).
nCNPs(3).
count(0).
finished(0).

all_proposals_received(CNPId,NP) :-              // NP = number of participants
     .count(propose(CNPId,_)[source(_)], NO) &   // number of proposes received
     .count(refuse(CNPId)[source(_)], NR) &      // number of refusals received
     NP = NO + NR.

/* Initial goals */
!init.
!register.

/* Plans */

//Register the agent as client 
+!register <- .df_register(client).

// Start CNPs
+!init : count(X) & nCNPs(N) & X < N <-
      -+count(X+1);
      !start(X+1) |&| !init.

+!init : true <- .print("No more requests").


//Defines the worker function
+!start(X) : .random(R) & nOccupations(NO) & N = math.floor(NO*R) & occupation(N,F) & 
           .my_name(A) & 
           .time(H,M,S)
        <-  .concat(A, "-", X, Id);              // create an Id for the CNP -> name-nContract
            +myNeed(Id,F); 
            +tries(Id,0);
            .print("CNP id ",Id,": I need ",F);
            !startCNP(Id,F).                    // start CNP

// start the CNP
+!startCNP(Id,Task) : tries(Id,N) & N<5
   <- -tries(Id,N);
      +tries(Id,N+1);
      //.print(Id, " for ", Task, ": Try #", N);
      .wait(2000);                                       // wait participants introduction
      .df_search(Task,LP);                               // look for workers in the DF
      //.print("Sending CFP to ",LP);
      .abolish(propose(Id,_)[source(_)]);             // clear propose memory
      .abolish(refuse(Id)[source(_)]);                // clear refuse memory
      .send(LP,tell,cfp(Id,Task));                       // send CFP for all workers available to do the Task
      // the deadline of the CNP is now + 4 seconds (or all proposals were received)
      .wait(all_proposals_received(Id,.length(LP)), 4000, _);
      !contract(Id).

+!startCNP(Id,Task) <- .print(Id, " gave up looking for ",Task, " for now");
                        -tries(Id,_);                                   // clear tries counter
                        -myNeed(Id,Task);                               // clear myNeed.
                        !findWorker(Id, Task).

+!findWorker(Id, Task) <- 
               .print("Checking if there is a ", Task);
               .df_search(Task, LP);
               .length(LP, GiveUp);
               !waitWorkerOrGiveUp(Id, Task, GiveUp).

+!waitWorkerOrGiveUp(Id, Task, GiveUp) : GiveUp \== 0 <- 
            .print(Id, " found a suitable worker for ", Task, " but he is buzy. Waiting for a while");
            +myNeed(Id,Task); 
            +tries(Id,0);
            .wait(20000);
            !startCNP(Id,Task).

+!waitWorkerOrGiveUp(Id, Task, GiveUp) : GiveUp == 0 <- 
            .print(Id, " could not found a worker for ", Task, ". Giving up!").


@lc1[atomic]
+!contract(Id)
   :  .findall(offer(O,A),propose(Id,O)[source(A)],L) & L \== []     // there is a offer
      & finished(N)
   <- .print("Offers are ",L);
      .min(L,offer(WOf,WAg));                                           // the first offer is the best
      .print("Winner is ",WAg," with ",WOf);
      !announce_result(Id,L,WAg);
      -finished(N);
      +finished(N+1);
      !announce_finished.

// no offer case, maintain intention for current Need
+!contract(Id) : myNeed(Id,F)
               <- .print("No offers.");
                  .wait(1000);
                  !startCNP(Id,F).

+!announce_finished : finished(N) & nCNPs(NM) & N >= NM
      <- .send(severino, tell, finished).

+!announce_finished : finished(N) & nCNPs(NM) & N < NM
      <- .wait(1).

+!announce_result(_,[],_).

// announce to the winner
+!announce_result(Id,[offer(_,WAg)|T],WAg)
   <- .send(WAg,tell,accept_proposal(Id));
      !announce_result(Id,T,WAg).

// announce to others
+!announce_result(Id,[offer(_,LAg)|T],WAg)
   <- .send(LAg,tell,reject_proposal(Id));
      !announce_result(Id,T,WAg).

// receive inform_done from worker
+inform_done(Id)[source(W)] <- 
               .print("Best worker accept the service (", W, ")");
               -inform_done(Id)[source(W)].                          //clear inform_done memory

// receive refusal from worker and maintain intention for current Need
+inform_ref(Id)[source(W)] : myNeed(Id,F)
            <- .print("Best worker (", W, ") busy --> restart search for ",F);
               .wait(1000);
               !startCNP(Id,F);
               -inform_ref(Id)[source(W)];                           // clear inform_ref memory
               -myNeed(Id,F).                                       // clear myNeed