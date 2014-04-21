package com.frenchtoastmafia.snake2048;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Player
{
    private List<Box> boxes;
    private RectF       startRect;
    private Paint       playerPaint;
    private int         canvasWidth;
    private int         canvasHeight;
    private float       width;
    private float       height;
    private int movingDirection = -1; // 0 = up, 1 = right, 2 = down, 3 = left
    private float       py;
    private float       px;
    private boolean     sideSwitched               = false;

    public static final int VELOCITY = 16;

    public Player(RectF r, int cW, int cH)
    {
        Log.d("PLAYER_CREATION", "player created");
        startRect = r;
        Box playerRect = new Box(r.centerX(), r.centerY(), r.width());
        width = r.right - r.left;
        height = r.top - r.bottom;
        canvasWidth = cW;
        canvasHeight = cH;
        py = playerRect.centerY();
        px = playerRect.centerX();
        playerPaint = new Paint();
        playerPaint.setColor(Color.BLACK);
        playerPaint.setStyle(Style.FILL);
        boxes = new ArrayList<Box>();
        boxes.add(playerRect);

        Log.d("Player create", "" + boxes);
    }
    public void restart()
    {
        boxes = new ArrayList<Box>();
        boxes.add(new Box(startRect.centerX(), startRect.centerY(), startRect.width()));
        py = startRect.centerY();
        px = startRect.centerX();
        sideSwitched = false;
    }


    public void draw(Canvas c)
    {
        // TODO implement
        for (Box box : boxes) {
            c.drawRect(box, playerPaint);
        }
    }


    public RectF getRect()
    {
        return boxes.get(0);
    }


    /**
     * This method is solely for determining whether or not the player has
     * collided with anything. This method should not change anything about the
     * state of the player or the RectF argument.
     *
     * @param collided
     *            the rectangle that should be tested to see if it collides with
     *            the player
     * @return -1 for no collision, 0-4 for a collision on a side of the player,
     *         starting with 0 at the top and going clockwise
     */
    public int intersects(RectF collided)
    {
        int minimumIntersectIndex = -1;
        float minimumIntersect = Math.max(canvasHeight, canvasWidth);

        // We only need to check collisions on the head box.
        Box head = boxes.get(0);
        if (head.bottom < collided.top
            && head.bottom > collided.bottom
            && ((head.right < collided.right && head.right > collided.left) || (head.left > collided.left && head.left < collided.right)))
        {
            float intersectBot = collided.top - head.bottom;
            float intersectRight = head.right - collided.left;
            float intersectLeft = collided.right - head.left;
            if (intersectBot > 0 && intersectBot < minimumIntersect)
            {
                minimumIntersectIndex = 2;
                minimumIntersect = intersectBot;
            }
            if (intersectRight > 0 && intersectRight < minimumIntersect)
            {
                minimumIntersectIndex = 1;
                minimumIntersect = intersectRight;
            }
            if (intersectLeft > 0 && intersectLeft < minimumIntersect)
            {
                minimumIntersectIndex = 3;
                minimumIntersect = intersectLeft;
            }
            // side switch collision trumps all
            // if ((intersectRight > 0 || intersectLeft > 0) && sideSwitched)
            // {
            // minimumIntersectIndex = 4;
            // }
            if (sideSwitched)
            minimumIntersectIndex = 4;
        }
        else if (head.top < collided.top
            && head.top > collided.bottom
            && ((head.right < collided.right && head.right > collided.left) || (head.left > collided.left && head.left < collided.right)))
        {
            float intersectBot = head.top - collided.bottom;
            float intersectRight = head.right - collided.left;
            float intersectLeft = collided.right - head.left;
            if (intersectBot > 0 && intersectBot < minimumIntersect)
            {
                minimumIntersectIndex = 0;
                minimumIntersect = intersectBot;
            }
            if (intersectRight > 0 && intersectRight < minimumIntersect)
            {
                minimumIntersectIndex = 1;
                minimumIntersect = intersectRight;
            }
            if (intersectLeft > 0 && intersectLeft < minimumIntersect)
            {
                minimumIntersectIndex = 3;
                minimumIntersect = intersectLeft;
            }
            // side switch collision trumps all
// if ((intersectRight > 0 || intersectLeft > 0) && sideSwitched)
// {
// minimumIntersectIndex = 4;
// }
            if (sideSwitched)
                minimumIntersectIndex = 4;
        }
        return minimumIntersectIndex;
    }


    /**
     * This method should be called when the player collides with a box. It algorithmically determines whether the
     * collided box should be pushed onto the player's stack of boxes or if it should be combined with his head,
     * repeatedly combining blocks further down the chain if need be.
     *
     * PRE: collisionIndicator > -1 (i.e. there was a collision)
     *
     * @param other
     *            the RectF the player collided with
     * @param collisionIndicator
     *            an integer indicating what side of the player collided. See
     *            documentation for -intersects(RectF)
     */
    public void fixIntersection(Box other, int collisionIndicator)
    {
        boxes.add(0, other);
        //adjustPosition(0);
    }


    /**
     * Move the player based on the amount of time that passed since the last
     * frame (for smooth movement)
     *
     * @param deltaT
     *            the amount of time in milliseconds since the last time
     *            adjustPosition was called
     */
    public void adjustPosition(int deltaT)
    {
        if (boxes == null || !(boxes.get(0) instanceof Box)) {
            restart();
        }
        Box head = boxes.get(0);

        int vx = 0;
        int vy = 0;

        switch (movingDirection) {
            case 0: // up
                vy = VELOCITY;
                break;
            case 1: // right
                vx = VELOCITY;
                break;
            case 2: // down
                vy = -VELOCITY;
                break;
            case 3: // left
                vx = -VELOCITY;
                break;
        }

        // From the tail, move every box to the one above it, and finish by moving the in the movingDirection
        for (int i = boxes.size() - 1; i >= 1; i--) {
            Box currentBox = boxes.get(i);
            Box previousBox = boxes.get(i-1);

            currentBox.offset(previousBox.centerX() - currentBox.centerX(), previousBox.centerY() - currentBox.centerY());
        }
        head.offset(vx, vy);

        py = head.centerY();
        px = head.centerX();
    }

    public float getY()
    {
        return py;
    }


    public float getX()
    {
        return px;
    }

    public boolean switchedSides()
    {
        return sideSwitched;
    }

    public List<Box> getBoxes() {
        return boxes;
    }

    public boolean isDead() {
        // TODO implement
        return false;
    }

    public void setBoxes(List<Box> boxes) {
        this.boxes = boxes;
    }

    public void toggleMovingDirection() {
        movingDirection = ++movingDirection % 4;
    }
    public void setMovingDirection(int movingDirection) {
        this.movingDirection = movingDirection;
    }
}
