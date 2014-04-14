package com.example.avalanchegame;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.hardware.SensorEvent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

// -------------------------------------------------------------------------
/**
 * The view on which the game will take place.
 *
 * @author Andriy
 * @version Jan 10, 2014
 */
public class CustomSurfaceView
    extends SurfaceView
    implements SurfaceHolder.Callback
{

    // -------------------------------------------------------------------------
    /**
     * This is the thread on which the game runs. The thread will be
     * continuously updating the screen and redrawing the game.
     *
     * @author Andriy
     * @version Jan 10, 2014
     */
    class GameThread
        extends Thread
    {
        /*
         * These are used for frame timing
         */
        private final static int MAX_FPS                  = 50;
        private final static int MAX_FRAME_SKIPS          = 5;
        private final static int FRAME_PERIOD             = 1000 / MAX_FPS;

        /** The drawable to use as the background of the animation canvas */
        private Bitmap           mBackgroundImage;

        /**
         * Current height of the surface/canvas.
         *
         * @see #setSurfaceSize
         */
        private int              mCanvasHeight            = 1;

        /**
         * Current width of the surface/canvas.
         *
         * @see #setSurfaceSize
         */
        private int              mCanvasWidth             = 1;

        /** Indicate whether the surface has been created & is ready to draw */
        private boolean          mRun                     = false;

        /** Prevents multiple threads from accessing the canvas */
        private final Object     mRunLock                 = new Object();

        /** Handle to the surface manager object we interact with */
        // this is how we get the canvas
        private SurfaceHolder    mSurfaceHolder;

        private Paint            mBlackPaint              = new Paint();
        private Player           player;
        private List<Box>        boxes                    =
                                                              new ArrayList<Box>();
        /**
         * Defines the N predominant column structures that govern where new
         * boxes spawn.
         */
        private Box[]            columns                  = new Box[6];

        private int              minWidth;

        private int              maxWidth;

        private long             lastTime                 = System.currentTimeMillis();

        // the amount the accelerometer value should be multiplied by before
        // being passed to the player object
        private int              accelerometerCoefficient = -100;

        private long             seed                     = (long)(Long.MAX_VALUE * Math.random());

        private Random           seededRandom;

        private boolean          triedToJump              = false;

        private float            spawnCutoff              = 0;

        // assign after size of screen is determined
        private float            spawnIncrements;
        private float            maxBlockHeight;
        private int              blocksAbovePlayer        = 0;

        private final int        MIN_BLOCKS_ABOVE         = 40;

        private boolean          firstTime                = true;


        // ----------------------------------------------------------
        /**
         * Create a new GameThread object.
         *
         * @param surfaceHolder
         */
        public GameThread(SurfaceHolder surfaceHolder)
        {
            mSurfaceHolder = surfaceHolder;

            mBlackPaint.setColor(Color.BLACK);
            mBlackPaint.setStyle(Style.FILL);

            // creates a seeded random object
            // TODO fill this in with a real seed later
            seededRandom = new Random(seed);
        }


        private int randInt(int min, int max)
        {
            return (int)(seededRandom.nextDouble() * (max - min) + min);
        }
        public void generateBoxes(float startingHeight, float additionalHeight)
        {
            // TODO implement
        }

        /**
         * Pauses the physics update & animation.
         */
        public void pause()
        {
            synchronized (mSurfaceHolder)
            {
                mRun = false;
            }
        }

        private long beginTime; // the time when the cycle began


        @Override
        public void run()
        {
            long timeDiff; // the time it took for the cycle to execute
            int sleepTime = 0; // ms to sleep (<0 if we're behind)
            int framesSkipped; // number of frames being skipped

            // while its running, which is determined by the mode constants
            // defined at the beginning
            while (mRun)
            {
                Canvas c = null;
                try
                {
                    // get a reference to the canvas
                    c = mSurfaceHolder.lockCanvas();
                    synchronized (mSurfaceHolder)
                    {
                        beginTime = System.currentTimeMillis();
                        framesSkipped = 0;
                        /*
                         * Critical section. Do not allow mRun to be set false
                         * until we are sure all canvas draw operations are
                         * complete. If mRun has been toggled false, inhibit
                         * canvas operations.
                         */
                        synchronized (mRunLock)
                        {
                            // if its running, update the canvas through doDraw
                            if (mRun)
                            {
                                updateLogic(); // moves everything without
                                // drawing it yet (basically a buffer)
                                doDraw(c); // renders everything

                                timeDiff =
                                    System.currentTimeMillis() - beginTime;
                                sleepTime = (int)(FRAME_PERIOD - timeDiff);

                                if (sleepTime > 0)
                                {
                                    try
                                    {
                                        Thread.sleep(sleepTime);
                                    }
                                    catch (InterruptedException e)
                                    {
                                        // this should probably never fail...
                                    }
                                }
                                while (sleepTime < 0
                                    && framesSkipped < MAX_FRAME_SKIPS)
                                {
                                    // catch up, so update without rendering
                                    updateLogic();
                                    sleepTime += FRAME_PERIOD;
                                    framesSkipped++;
                                }
                            }
                        }
                    }
                }
                finally
                {
                    // do this in a finally so that if an exception is thrown
                    // during the above, we don't leave the Surface in an
                    // inconsistent state
                    if (c != null)
                    {
                        mSurfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }


        /**
         * Restores game state. Typically called when the Activity is being restored after having been previously
         * destroyed.
         */
        public synchronized void restoreState()
        {
            synchronized (mSurfaceHolder)
            {
                Gson g = new Gson();
                SharedPreferences prefs =
                    getContext()
                        .getSharedPreferences(MainActivity.PREF_FILE, 0);
                Editor e = prefs.edit();
                player =
                    g.fromJson(prefs.getString("player", "null"), Player.class);
                if (player == null)
                    System.out.println("PLAYER NULL");
                triedToJump = prefs.getBoolean("triedToJump", false);
                blocksAbovePlayer = prefs.getInt("blocksAbovePlayer", 0);
                firstTime = prefs.getBoolean("firstTime", false);
                e.putBoolean("gameSaved", false);
                e.commit();
            }
        }


        /**
         * Dump game state to the provided Bundle. Typically called when the
         * Activity is being suspended.
         */
        public synchronized void saveState()
        {
            synchronized (mSurfaceHolder)
            {
                Gson g = new Gson();
                SharedPreferences prefs =
                    getContext()
                        .getSharedPreferences(MainActivity.PREF_FILE, 0);
                Editor e = prefs.edit();
                // System.out.println(g.toJson(player, Player.class));
                e.putString("player", g.toJson(player, Player.class));
// Type listType = new TypeToken<List<Box>>() {
// }.getType();
// e.putString("boxes", g.toJson(boxes, listType));
                e.putBoolean("triedToJump", triedToJump);
                e.putInt("blocksAbovePlayer", blocksAbovePlayer);
                e.putBoolean("firstTime", firstTime);
                e.putBoolean("gameSaved", true);
                e.commit();
            }
        }


        /**
         * Used to signal the thread whether it should be running or not.
         * Passing true allows the thread to run; passing false will shut it
         * down if it's already running. Calling start() after this was most
         * recently called with false will result in an immediate shutdown.
         *
         * @param b
         *            true to run, false to shut down
         */
        public void setRunning(boolean b)
        {
            // Do not allow mRun to be modified while any canvas operations
            // are potentially in-flight. See doDraw().
            synchronized (mRunLock)
            {
                mRun = b;
            }
        }


        public boolean isRunning()
        {
            return mRun;
        }


        /** Callback invoked when the surface dimensions change. */
        public void setSurfaceSize(int width, int height)
        {
            Log.d("CHANGING SURFACE SIZE", width + ", " + height);
            // synchronized to make sure these all change atomically
            synchronized (mSurfaceHolder)
            {
                mCanvasWidth = width;
                mCanvasHeight = height;

                // don't forget to resize the background image
// mBackgroundImage =
// Bitmap.createScaledBitmap(
// mBackgroundImage,
// width,
// height,
// true);

                player =
                    new Player(new RectF(
                        mCanvasWidth / 2 - 50,
                        900,
                        mCanvasWidth / 2 + 50,
                        800), mCanvasWidth, mCanvasHeight);

                minWidth = 2 * (mCanvasWidth / 30); // Even

                maxWidth = 2 * (mCanvasWidth / 15); // Even

                mBlackPaint.setColor(Color.BLACK);
                mBlackPaint.setStyle(Style.FILL);

                spawnIncrements = mCanvasHeight * 6;
                maxBlockHeight = mCanvasHeight;
            }
        }


        /**
         * Resumes from a pause.
         */
        public void unpause()
        {
            synchronized (mSurfaceHolder)
            {
                // setState(STATE_RUNNING);
                mRun = true;
            }
        }


        public void restart()
        {
            SharedPreferences prefs =
                getContext().getSharedPreferences(MainActivity.PREF_FILE, 0);
            Editor e = prefs.edit();
            e.putBoolean("gameSaved", false);
            e.commit();

            boxes.clear();
            firstTime = true;
            player.restart();
            triedToJump = false;
            blocksAbovePlayer = 0;
            maxBlockHeight = mCanvasHeight;
            // use new seed
            seededRandom = new Random((long)(Long.MAX_VALUE * Math.random()));
            lastTime = System.currentTimeMillis();
        }

        private void updateLogic()
        {
            if (firstTime)
            {
                player.getRect()
                    .offsetTo(
                        mCanvasWidth / 2,
                        mCanvasHeight / 2);
                // generateBoxes(mCanvasHeight, mCanvasHeight * 40);
            }

            firstTime = false;
            player.adjustPosition((int)(System.currentTimeMillis() - lastTime));
            for (Box block : boxes)
            {
//                for (Box possibleCollisionBlock : boxes)
//                {
//                    // Check if the player's head has collided with the block, if so, do combination logic
//                    // And generate another block
//
//                    // With a low probability, remove a random block from the board and place another one
//                }
//                if (block.top > maxBlockHeight)
//                    maxBlockHeight = block.top;

                // adjust block
//                int collisionIndicator = player.intersects(block);
//                if (collisionIndicator > -1)
//                {
//                    player.fixIntersection(block, collisionIndicator);
//                    // if (block.width() > 9000)
//                    // Log.d("ground", "colliding with ground");
//                    // fix grounding within player
//                }

//                if (collisionIndicator == 0)
//                    if (!player.switchedSides())
//                        topHit = true;
//                if (collisionIndicator == 2)
//                {
//                    if (!player.switchedSides())
//                        bottomHit = true;
//                    player.setYVelocity(block.getVy());
//                }
//                if (player.getY() < block.top)
//                    blocksAbovePlayer++;
            }
            if (player.isDead())
            {
                restart();
                return;
            }

            lastTime = System.currentTimeMillis();
        }


        /**
         * Draws the player snake and number blocks to the provided canvas.
         */
        private void doDraw(Canvas canvas)
        {
            // Log.d("doDraw", "drawing");
            // Draw the background image. Operations on the Canvas accumulate
            // so this is like clearing the screen.
            // canvas.drawBitmap(mBackgroundImage, 0, 0, null);
            canvas.drawColor(Color.WHITE);
            // canvas.drawRect(mGrassRect, mGrassPaint);
            // canvas.drawRect(testBlock, mBlackPaint);
            player.draw(canvas);

            for (Box box : boxes)
            {
                box.draw(canvas, player.getRect().bottom);
            }
            // canvas.drawRect(mBlockRect, mBlockPaint);
        }

        /**
         * variables to store previous touch down positions as well as calculate
         * scroll
         */
        private float downX, downY, currentX;


        // ----------------------------------------------------------
        /**
         * This is where we will check where the user touches and respond
         * accordingly
         *
         * @param e
         *            the motion event
         * @return true
         */
        public boolean onTouchEvent(MotionEvent e)
        {
            // synchronized (mSurfaceHolder)
            // {
            switch (e.getAction())
            {
                // TODO implement
            }
            return true;
            // }
        }


        public void onSensorChanged(SensorEvent event)
        {
            if (event != null && event.values != null && player != null)
                player.setXVelocity(accelerometerCoefficient * event.values[0]);
        }
    }

    /** The thread that actually draws the animation */
    private GameThread thread;


    // ----------------------------------------------------------
    /**
     * Create a new CustomSurfaceView object.
     *
     * @param context
     * @param attrs
     */
    public CustomSurfaceView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        // register our interest in hearing about changes to our surface
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        // create thread only; it's started in surfaceCreated()
        thread = new GameThread(holder);

        setFocusable(true); // make sure we get key events
    }


    /**
     * Fetches the animation thread corresponding to this LunarView.
     *
     * @return the animation thread
     */
    public GameThread getThread()
    {
        return thread;
    }


    @Override
    public boolean onTouchEvent(MotionEvent e)
    {
        return thread.onTouchEvent(e);
    }


    /**
     * Standard window-focus override. Notice focus lost so we can pause on
     * focus lost. e.g. user switches to take a call.
     */
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus)
    {
        if (!hasWindowFocus)
            thread.pause();
        else
            // TODO: make this open pause screen
            thread.unpause();
    }


    /* Callback invoked when the surface dimensions change. */
    public void surfaceChanged(
        SurfaceHolder holder,
        int format,
        int width,
        int height)
    {
        thread.setSurfaceSize(width, height);
    }


    /*
     * Callback invoked when the Surface has been created and is ready to be
     * used.
     */
    public void surfaceCreated(SurfaceHolder holder)
    {
        // start the thread here so that we don't busy-wait in run()
        // waiting for the surface to be created
        if (thread.getState() == Thread.State.TERMINATED)
        {
            System.out.println("creating a new thread because terminated");
            thread = new GameThread(getHolder());
        }
        thread.setRunning(true);
        // thread.start();
    }


    /*
     * Callback invoked when the Surface has been destroyed and must no longer
     * be touched. WARNING: after this method returns, the Surface/Canvas must
     * never be touched again!
     */
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        System.out.println("TIME TO DIE!");
        boolean retry = true;
        thread.setRunning(false);
        while (retry)
        {
            try
            {
                thread.join();
                retry = false;
            }
            catch (InterruptedException e)
            {
                // don't worry about it
            }
        }
    }

}
