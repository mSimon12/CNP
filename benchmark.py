#!/usr/bin/env python

import psutil
import threading
import subprocess
import os
import time
import pandas as pd

class cd:
    """Context manager for changing the current working directory"""
    def __init__(self, newPath):
        self.newPath = os.path.expanduser(newPath)

    def __enter__(self):
        self.savedPath = os.getcwd()
        os.chdir(self.newPath)

    def __exit__(self, etype, value, traceback):
        os.chdir(self.savedPath)

def popenAndCall(onExit, popenArgs):
    """
    Runs the given args in a subprocess.Popen, and then calls the function
    onExit when the subprocess completes.
    onExit is a callable object, and popenArgs is a list/tuple of args that 
    would give to subprocess.Popen.
    """
    def runInThread(onExit, popenArgs):
        proc = subprocess.Popen(*popenArgs)
        proc.wait()
        onExit()
        return
    thread = threading.Thread(target=runInThread, args=(onExit, popenArgs))
    thread.start()
    # returns immediately after the thread starts
    return thread

class MeasureThread:
    def __init__(self, interval = 1.0):
        self.interval = interval
        self.memory = []
        self.cpu = []
        self.time = 0

    def start(self):
        self.running = True
        self.thread = threading.Thread(target = self.__thread)
        self.thread.start()

    def stop(self):
        self.running = False
        self.thread.join()

    def reset(self):
        self.memory = []
        self.cpu = []
        self.time = 0

    def getMemory(self):
        return self.memory

    def getCPU(self):
        return self.cpu

    def getTime(self):
        return self.time
        
    def __thread(self):
        print("Start collecting...")
        while self.running:
            self.cpu.append( psutil.cpu_percent() )
            self.memory.append( psutil.virtual_memory()[2] )
            time.sleep(self.interval)
            self.time = self.time + self.interval
        print("Finished collecting")

        

nWorkers = [1, 10, 20, 30, 40, 50]
#nWorkers = [1]
nClients = [1, 10, 25, 50, 75, 100, 125, 150, 175, 200]
#nClients = [1]
nCNPS = [1, 3, 5, 7, 10]
#nCNPS = [1]

cnp = {'workers': [],
        'clients': [],
        'cnps'   : [],
        'cpu_jason':[],
        'memory_jason':[],
        'time_jason':[],
        'cpu_jade':[],
        'memory_jade':[],
        'time_jade':[]
        }

df = pd.DataFrame(cnp)

for nW in nWorkers:
    for nC in nClients:
        for nCNP in nCNPS:
            thread = MeasureThread()
            
            maxJadeCPU = 0
            maxJadeMEM = 0
            jadeTime = 0
            maxJasonCPU = 0
            maxJasonMEM = 0
            jasonTime = 0

            with cd("JADE"):
                thread.reset()
                thread.start()    
                subprocess.call(["sh", "run.sh", str(nW), str(nC), str(nCNP)])
                thread.stop()
                maxJadeCPU = max(thread.getCPU())
                maxJadeMEM = max(thread.getMemory())
                jadeTime = thread.getTime()

            with cd("JASON"):
                thread.reset()
                thread.start()   
                subprocess.call(["sh", "run.sh", str(nW), str(nC), str(nCNP)])
                thread.stop()
                maxJasonCPU = max(thread.getCPU())
                maxJasonMEM = max(thread.getMemory())
                jasonTime = thread.getTime()

            df.loc[len(df)] = [nW, nC, nCNP, maxJasonCPU, maxJasonMEM, jasonTime, maxJadeCPU, maxJadeMEM, jadeTime]

df.to_csv('benchmark.csv', index = None, header=True)