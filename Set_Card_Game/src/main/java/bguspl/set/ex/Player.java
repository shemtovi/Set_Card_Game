package bguspl.set.ex;



import java.util.Queue;

import java.util.concurrent.ConcurrentLinkedQueue;

import java.util.concurrent.atomic.AtomicBoolean;

import java.util.concurrent.atomic.AtomicInteger;



import bguspl.set.Env;



/**

 * This class manages the players' threads and data

 *

 * @inv id >= 0

 * @inv score >= 0

 */

public class Player implements Runnable {



    /**

     * The game environment object.

     */

    private final Env env;



    /**

     * Game entities.

     */

    private final Table table;



    /**

     * The id of the player (starting from 0).

     */

    public final int id;



    /**

     * The thread representing the current player.

     */

    private Thread playerThread;



    /**

     * The thread of the AI (computer) player (an additional thread used to generate key presses).

     */

    private Thread aiThread;



    /**

     * True iff the player is human (not a computer player).

     */

    private final boolean human;



    /**

     * True iff game should be terminated due to an external event.

     */

    private volatile boolean terminate;



    /**

     * The current score of the player.

     */

    private int score;



    private Dealer dealer;



    private Queue <Integer> queue;



    private AtomicInteger numOfTokens =  new AtomicInteger();

    private AtomicBoolean keyAccess = new AtomicBoolean();

    private Object lock = new Object();



    /**

     * The class constructor.

     *

     * @param env    - the environment object.

     * @param dealer - the dealer object.

     * @param table  - the table object.

     * @param id     - the id of the player.

     * @param human  - true iff the player is a human player (i.e. input is provided manually, via the keyboard).

     */

    public Player(Env env, Dealer dealer, Table table, int id, boolean human) {

        this.env = env;

        this.table = table;

        this.id = id;

        this.human = human;

        this.dealer = dealer;

        queue = new ConcurrentLinkedQueue<Integer>();

        

    }



    /**

     * The main player thread of each player starts here (main loop for the player thread).

     */

    @Override

    public void run() {

        playerThread = Thread.currentThread();

        System.out.printf("Info: Thread %s starting.%n", Thread.currentThread().getName());

        if (!human) createArtificialIntelligence();

       // try{playerThread.wait();}

        //atch(InterruptedException ignore){}

        while (!terminate) {

            // TODO implement main player loop

            if(numOfTokens.get() >= env.config.featureSize){

                //if player need to wait to the dealer to check if set is leagal

                try{playerThread.wait();}

                catch(InterruptedException ignore){}

            } 

            while(!queue.isEmpty()){

                int slot = queue.remove();

                if(table.hasToken(id,slot)){

                    table.removeToken(id, slot);

                    //TODO comperAndSet?

                    int i = numOfTokens.get();

                    numOfTokens.compareAndSet(i, i--);

                }

                else{

                    table.placeToken(id, slot);

                    int i = numOfTokens.get();

                    numOfTokens.compareAndSet(i, i++);   

                }

                        

            }



            

        }

        if (!human) try { aiThread.join(); } catch (InterruptedException ignored) {}

        System.out.printf("Info: Thread %s terminated.%n", Thread.currentThread().getName());

    }



    /**

     * Creates an additional thread for an AI (computer) player. The main loop of this thread repeatedly generates

     * key presses. If the queue of key presses is full, the thread waits until it is not full.

     */

    private void createArtificialIntelligence() {

        // note: this is a very very smart AI (!)

        aiThread = new Thread(() -> {

            System.out.printf("Info: Thread %s starting.%n", Thread.currentThread().getName());

            while (!terminate) {

                // TODO implement player key press simulator

                try {

                    synchronized (this) { wait(); }

                } catch (InterruptedException ignored) {}

            }

            System.out.printf("Info: Thread %s terminated.%n", Thread.currentThread().getName());

        }, "computer-" + id);

        aiThread.start();

    }



    /**

     * Called when the game should be terminated due to an external event.

     */

    public void terminate() {

        // TODO implement

    }



    /**

     * This method is called when a key is pressed.

     *

     * @param slot - the slot corresponding to the key pressed.

     */

    public void keyPressed(int slot) {

        /*if(keyAccess.get()){

            queue.add(slot);

        }*/

        synchronized(lock){

            queue.add(slot);

        }

    }



    /**

     * Award a point to a player and perform other related actions.

     *

     * @post - the player's score is increased by 1.

     * @post - the player's score is updated in the ui.

     */

    public void point() {

        //int ignored = table.countCards(); // this part is just for demonstration in the unit tests

        synchronized(lock){

            env.ui.setScore(id, ++score);

            try{

                wait(env.config.pointFreezeMillis);

            }catch (InterruptedException ignored) {};

                

        }

    }



    /**

     * Penalize a player and perform other related actions.

     */

    public void penalty() {

        synchronized(lock){     

            try{

                wait(env.config.penaltyFreezeMillis);

            }catch (InterruptedException ignored) {};

        }

    }



    public int getScore() {

        return score;

    }

}

