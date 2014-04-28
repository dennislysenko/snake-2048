package com.frenchtoastmafia.snake2048;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

public class Player
{
    private List<Box> boxes;
    private RectF       startRect;
    private Paint       playerPaint;
    private Paint textPaint;
    private int         canvasWidth;
    private int         canvasHeight;
    private float       width;
    private float       height;
    private int movingDirection = -1; // 0 = up, 1 = right, 2 = down, 3 = left
    private float       py;
    private float       px;
    private boolean     sideSwitched               = false;

    public static final int VELOCITY = 48;

    public Player(RectF r, int cW, int cH)
    {
        Log.d("PLAYER_CREATION", "player created");
        startRect = r;
        Box playerRect = new Box(r.centerX(), r.centerY(), 2);
        width = r.right - r.left;
        height = r.top - r.bottom;
        canvasWidth = cW;
        canvasHeight = cH;
        py = playerRect.centerY();
        px = playerRect.centerX();
        playerPaint = new Paint();
        playerPaint.setColor(Color.BLACK);
        playerPaint.setStyle(Style.FILL);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setStyle(Style.FILL);
        textPaint.setTextSize(60);
        textPaint.setFakeBoldText(true);
        boxes = new ArrayList<Box>();
        boxes.add(playerRect);

        Log.d("Player create", "" + boxes);
    }
    public void restart()
    {
        boxes = new ArrayList<Box>();
        boxes.add(new Box(startRect.centerX(), startRect.centerY(), 2));
        py = startRect.centerY();
        px = startRect.centerX();
        sideSwitched = false;
        movingDirection = -1;
    }


    public void draw(Canvas c)
    {
        for (Box box : boxes) {
            box.draw(c, playerPaint, textPaint);
        }
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
        Box head = head();
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
        Box head = head();

        boxes.add(0, other);

        if (movingDirection == 0) { // Up
            other.left = head.left;
            other.right = head.right;
            other.bottom = head.top;
            other.top = other.bottom + VELOCITY;
        } else if (movingDirection == 1) {
            // Right
            other.top = head.top;
            other.bottom = head.bottom;
            other.left = head.right;
            other.right = other.left + VELOCITY;
        } else if (movingDirection == 2) {
            // Down
            other.left = head.left;
            other.right = head.right;
            other.top = head.bottom;
            other.bottom = other.top - VELOCITY;
        } else if (movingDirection == 3) {
            // Left
            other.top = head.top;
            other.bottom = head.bottom;
            other.right = head.left;
            other.left = other.right - VELOCITY;
        }
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
        Box head = head();

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

        mergeNumbersTogether();
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

    public void mergeNumbersTogether() {
        // TODO move all of the following boxes up in the snake when you merge two together

        // Reverse the boxes first so that it merges from the end (e.g. 222 -> 24 instead of 42)
        Collections.reverse(boxes);

        ListIterator<Box> it = boxes.listIterator();
        while (it.hasNext()) {
            Box current = it.next();
            if (!it.hasNext()) {
                break;
            }

            Box next = it.next();

            if (current.getValue() == next.getValue()) {
                current.setValue(current.getValue() * 2);
                it.remove();
            } else {
                // If you didn't merge two tiles, move the cursor back so you can re-process the "next" tile
                it.previous();
            }
        }

        // Re-correctly order the snake
        Collections.reverse(boxes);
    }

    public boolean isDead() {
        // First check if you "ran into yourself"
        Box head = head();
        for (Box box : boxes) {
            if (!box.equals(head) && intersects(box) > -1) { // remember that intersects() only checks collisions with the head
                return true;
            }
        }

        // Then check if you're off the screen
        return px < 0 || py < 0 || px > canvasWidth || py > canvasHeight;
    }

    public Box head() {
        if (boxes == null || boxes.size() == 0 || !(boxes.get(0) instanceof Box)) {
            restart();
            return null;
        }

        return boxes.get(0);
    }

    public int headValue() {
        Box head = head();
        if (head == null) {
            return 0;
        }
        return head.getValue();
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
    public int getMovingDirection() { return movingDirection; }
}
