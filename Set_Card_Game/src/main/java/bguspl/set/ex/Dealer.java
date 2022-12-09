package bguspl.set.ex;



import bguspl.set.Config;

import bguspl.set.Env;



import java.util.Collections;

import java.util.List;

import java.util.Queue;

import java.util.concurrent.atomic.AtomicInteger;

import java.util.stream.Collectors;

import java.util.stream.IntStream;



/**

 * This class manages the dealer's threads and data

 */

public class Dealer implements Runnable {



    /**

     * The game environment object.

     */

    private final Env env;



    /**

     * Game entities.

     */

    private final Table table;

    private final Player[] players;



    /**

     * The list of card ids that are left in the dealer's deck.

     */

    private final List<Integer> deck;



    /**

     * True iff game should be terminated due to an external event.

     */

    private volatile boolean terminate;



    /**

     * The time when the dealer needs to reshuffle the deck due to turn timeout.

     */

    private long reshuffleTime = Long.MAX_VALUE;

    //helping verify correcntess of set

    AtomicInteger  senNum = new AtomicInteger();

    int[] lastSet;

    //The set testing queue of the dealer

    Queue<int[]> queue;



    Thread [] playersThreads;



    public Dealer(Env env, Table table, Player[] players) {

        this.env = env;

        this.table = table;

        this.players = players;

        deck = IntStream.range(0, env.config.deckSize).boxed().collect(Collectors.toList());

        lastSet = new int[env.config.featureSize + 1];

        playersThreads = new Thread[players.length];

    }        



    /**

     * The dealer thread starts here (main loop for the dealer thread).

     */

    

    @Override

    public void run() {

        System.out.printf("Info: Thread %s starting.%n", Thread.currentThread().getName());  

        for(int i = 0; i< players.length; i++){

            playersThreads[i] = new Thread(players[i]);

            playersThreads[i].start();

        }

        reshuffleTime = System.currentTimeMillis() + env.config.turnTimeoutMillis ;

        while (!shouldFinish()) {

            Collections.shuffle(deck);

            placeCardsOnTable();

            //players notifyall

            //notifyAll();     

            timerLoop();

            updateTimerDisplay(false);

            removeAllCardsFromTable();

        }

        announceWinners();

        System.out.printf("Info: Thread %s terminated.%n", Thread.currentThread().getName());

    }



    /**

     * The inner loop of the dealer thread that runs as long as the countdown did not time out.

     */

    private void timerLoop() {

        while (!terminate && System.currentTimeMillis() < reshuffleTime) {

            sleepUntilWokenOrTimeout();

            updateTimerDisplay(false);

            removeCardsFromTable();

            placeCardsOnTable();

            

        }

        //players wait

        //Tokens counter + queue resets

        for(Thread t: playersThreads){

            try{

                t.wait();

            }catch(InterruptedException ignored){}

            

        }

        

    }



    /**

     * Called when the game should be terminated due to an external event.

     */

    public void terminate() {

        // TODO implement

    }



    /**

     * Check if the game should be terminated or the game end conditions are met.

     *

     * @return true iff the game should be finished.

     */

    private boolean shouldFinish() {

        return terminate || env.util.findSets(deck, 1).size() == 0;

    }



    /**

     * Checks if any cards should be removed from the table.

     */

    //synchronize

    private void removeCardsFromTable() {

        // TODO implement

    }



    /**

     * Check if any cards can be removed from the deck and placed on the table.

     */

    private void placeCardsOnTable() {

        if(table.countCards() != env.config.tableSize && !deck.isEmpty()){

            for(int i =0 ;i < env.config.tableSize ;i++){

                if(table.slotToCard[i] == null){

                    if(!deck.isEmpty()){

                        try {

                            Thread.currentThread().sleep(100);

                        } catch (InterruptedException ignored) {}

                        table.placeCard(deck.remove(0), i);

                    }    

                }

                

            }

        }

    }



    /**

     * Sleep for a fixed amount of time or until the thread is awakened for some purpose.

     */

    private void sleepUntilWokenOrTimeout() {

        // TODO implement

    }



    /**

     * Reset and/or update the countdown and the countdown display.

     * false if need to update the countdoun

     * true if needed to reset the timer (60 seconds in our game)

     */

    private void updateTimerDisplay(boolean reset) {

        // TODO implement

        env.ui.setCountdown( reshuffleTime- System.currentTimeMillis(), reset);



    }



    /**

     * Returns all the cards from the table to the deck.

     */

    private void removeAllCardsFromTable() {

        // TODO implement

    }



    /**

     * Check who is/are the winner/s and displays them.

     */

    private void announceWinners() {

        // TODO implement

    }

}

