/* Initial beliefs and rules */

/* Possible Occupations:
    'cod - function - minValue - maxValue'
*/
occupation(0,plumper).
occupation(1,electrician).
occupation(2,builder).

nOccupations(3).
nCNPs(3).

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

+!init <- !start |&| !start |&| !start.

//Defines the worker function
+!start : .random(R) & nOccupations(NO) & N = math.floor(NO*R) & occupation(N,F) & 
           .my_name(A) & 
           .time(H,M,S) & .date(YY,MM,DD) & .random(RS) & R10 = math.round(10000*R) & RS10 = math.round(10000*RS)
        <-  .concat(A, "-", YY, MM, DD, H, M, S, R10, RS10, Id);              // create an Id for the CNP -> name-nContract
            +myNeed(Id,F); 
            +tries(Id,0);
            .print("CNP id ",Id,": I need ",F);
            !startCNP(Id,F);                    // start CNP
            .print("After startCNP").

// start the CNP
+!startCNP(Id,Task) : tries(Id,N) & N<5
   <- -+tries(Id,N+1);
      .wait(2000);                                       // wait participants introduction
      .df_search(Task,LP);                               // look for workers in the DF
      //.print("Sending CFP to ",LP);
      .abolish(propose(CNPId,_)[source(_)]);             // clear propose memory
      .abolish(refuse(CNPId)[source(_)]);                // clear refuse memory
      .send(LP,tell,cfp(Id,Task));                       // send CFP for all workers available to do the Task
      // the deadline of the CNP is now + 4 seconds (or all proposals were received)
      .wait(all_proposals_received(Id,.length(LP)), 4000, _);
      !contract(Id).

+!startCNP(Id,Task) <- .print("Give up looking for ",Task);
                        -tries(Id,N);                                   // clear tries counter
                        -myNeed(CNPId,F).                               // clear myNeed.


+!contract(CNPId)
   :  .findall(offer(O,A),propose(CNPId,O)[source(A)],L) & L \== []     // there is a offer
   <- .print("Offers are ",L);
      .min(L,offer(WOf,WAg));                                           // the first offer is the best
      .print("Winner is ",WAg," with ",WOf);
      !announce_result(CNPId,L,WAg).

// no offer case, maintain intention for current Need
+!contract(CNPId) : myNeed(CNPId,F)
               <- .print("No offers.");
                  .wait(1000);
                  !startCNP(CNPId,F).

+!announce_result(_,[],_).

// announce to the winner
+!announce_result(CNPId,[offer(_,WAg)|T],WAg)
   <- .send(WAg,tell,accept_proposal(CNPId));
      !announce_result(CNPId,T,WAg).

// announce to others
+!announce_result(CNPId,[offer(_,LAg)|T],WAg)
   <- .send(LAg,tell,reject_proposal(CNPId));
      !announce_result(CNPId,T,WAg).

// receive inform_done from worker
+inform_done(CNPId)[source(W)] <- 
               .print("Best worker accept the service.");
               -inform_done(CNPId)[source(W)].                          //clear inform_done memory

// receive refusal from worker and maintain intention for current Need
+inform_ref(CNPId)[source(W)] : myNeed(CNPId,F)
            <- .print("Best worker busy --> restart search for ",F);
               .wait(1000);
               !startCNP(CNPId,F);
               -inform_ref(CNPId)[source(W)];                           // clear inform_ref memory
               -myNeed(CNPId,F).                                       // clear myNeed