package com.ontotext.ehri.deduplication.clustering.indices.parrallelizor;

import java.util.concurrent.locks.LockSupport;

public class Parrallelizor {
    private final long WAIT_TIME_IN_NANOS = 1000;
    protected int numberOfActiveThreads;
    protected int queueFirstPosition;

    private abstract class PositionThread implements Runnable {
        protected PositionProcess positionProcess;
        protected Parrallelizor parrallelizor;
        protected int threadIndex;
        protected Thread thread;

        protected PositionThread(PositionProcess positionProcess, Parrallelizor parrallelizor, int threadIndex) {
            this.positionProcess = positionProcess;
            this.parrallelizor = parrallelizor;
            this.threadIndex = threadIndex;
            this.thread = new Thread(this);
        }

        public void start() {
            thread.start();
        }
    }

    private class ArrayPositionThread extends PositionThread {
        private int start;
        private int end;

        public ArrayPositionThread(int start, int end, PositionProcess positionProcess, Parrallelizor parrallelizor, int threadIndex) {
            super(positionProcess, parrallelizor, threadIndex);
            this.start = start;
            this.end = end;
        }

        public void run() {
            for (int position = start; position < end; position++) {
                positionProcess.process(position, threadIndex);
            }
            synchronized (parrallelizor) {
                parrallelizor.numberOfActiveThreads--;
            }
        }
    }

    private class QueuePositionThread extends PositionThread {
        private int length;

        public QueuePositionThread(int length, PositionProcess positionProcess, Parrallelizor parrallelizor, int threadIndex) {
            super(positionProcess, parrallelizor, threadIndex);
            this.length = length;
        }

        public void run() {
            int position;
            while (true) {
                synchronized (parrallelizor) {
                    if (parrallelizor.queueFirstPosition < length) {
                        position = parrallelizor.queueFirstPosition;
                        parrallelizor.queueFirstPosition++;
                    } else {
                        position = -1;
                    }
                }
                if (position == -1) {
                    break;
                }
                positionProcess.process(position, threadIndex);
            }
            synchronized (parrallelizor) {
                parrallelizor.numberOfActiveThreads--;
            }
        }
    }

    /**
     * This method starts numberOfThreads (or less) threads of type ArrayPositionThread.
     * Each such thread processes an interval of consecutive positions
     * from ArrayPositionThread.start inclusively to ArrayPositionThread.end exclusively.
     * The positions processed by all threads are 0,1,2,...,length-1.
     * The method process(int position, int threadIndex) is called by each thread T for each position that T processes.
     * One of the threads isthe thread calling this method.
     * This method waits all threads to finish their processing.
     */
    public void arrayParrallelize(int length, int numberOfThreads, PositionProcess positionProcess) {
        if (length <= 0 || numberOfThreads <= 0) {
            return;
        }
        int n = length / numberOfThreads;
        if (n == 0) {
            numberOfThreads = 1;
        }
        numberOfActiveThreads = numberOfThreads;
        PositionThread[] threads = new PositionThread[numberOfThreads];
        for (int i = 0; i < numberOfThreads; i++) {
            threads[i] = new ArrayPositionThread(i * n, ((i + 1 < numberOfThreads) ? ((i + 1) * n) : length), positionProcess, this, i);
        }
        execute(threads);
    }

    /**
     * This method starts numberOfThreads (or less) threads of type QueuePositionThread.
     * All positions from 0 to length-1 are pushed in a queue.
     * Until the queue is not empty each thread pops a position from the queue and processes the popped position
     * by calling the method process(int position, int threadIndex).
     * One of the threads is the thread calling this method.
     * This method waits all threads to finish their processing.
     */
    public void queueParrallelize(int length, int numberOfThreads, PositionProcess positionProcess) {
        if (length <= 0) {
            return;
        }
        if (length < numberOfThreads) {
            numberOfThreads = length;
        }
        numberOfActiveThreads = numberOfThreads;
        queueFirstPosition = 0;
        PositionThread[] threads = new PositionThread[numberOfThreads];
        for (int i = 0; i < numberOfThreads; i++) {
            threads[i] = new QueuePositionThread(length, positionProcess, this, i);
        }
        execute(threads);
    }

    /**
     * Starts the threads and wait until they finish.
     */
    private void execute(PositionThread[] threads) {
        for (int i = 1; i < threads.length; i++) {
            threads[i].start();
        }
        threads[0].run();
        boolean running = true;
        while (running) {
            synchronized (this) {
                if (numberOfActiveThreads == 0) {
                    running = false;
                }
            }
            if (running) {
                LockSupport.parkNanos(WAIT_TIME_IN_NANOS);
            }
        }
    }
}
