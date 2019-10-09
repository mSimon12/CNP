/* Possible Occupations:
    'cod - function - number of workers
*/

occupation(0,plumber, 0).
occupation(1,electrician, 0).
occupation(2,builder, 0).
occupation(3,baker, 0).
occupation(4,hairdresser, 0).
occupation(5,gardener, 0).
occupation(6,mechanic, 0).
occupation(7,painter, 0).
occupation(8,engineer, 0).
occupation(9,doctor, 0).

nOccupations(10).
nFinished(0).
nClients(0).
search(1).

!searchClients.

+!searchClients : search(S) & S == 1 & nFinished(N) <- 
    .df_search(client, LP);
    .length(LP, NC);
    -nClients(_);
    +nClients(NC);
    .print("Number of clients: ", NC, ". Finished: ", N);
    !checkClients;
    .wait(10000);
    !searchClients.

+!searchClients <-
    .wait(1).

@lc1[atomic]
+finished[source(A)] : nFinished(N) <-
    -nFinished(N);
    +nFinished(N+1);
    .print(A, " is done").

+!checkClients : nClients(NC) & nFinished(N) & NC > 0 & N >= NC <-
    -search(_);
    +search(0);
    .print("All clients done, checking workers...");
    !countWorkers.

+!checkClients <-
    .wait(1).

+!countWorkers <-
    .wait(5000);
    !countWorkers(0).

+!countWorkers(Id) : nOccupations(N) & Id < N & occupation(Id, OCC, _) <-
    .print("Counting: ", OCC);
    .df_search(OCC, LP);
    .length(LP, NW);
    -occupation(Id, OCC, _);
    +occupation(Id, OCC, NW);
    !countWorkers(Id + 1).


+!countWorkers(Id) <- 
    !printWorker(0, 0).

+!printWorker(Id, CW) : nOccupations(N) & Id < N & occupation(Id, OCC, NW) <-
    .print("Worker: ", OCC, "; Count: ", NW);
    !printWorker(Id + 1, CW + NW).

+!printWorker(Id, CW) <-
    .print("Total workers: ", CW).