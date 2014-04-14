package com.example.avalanchegame;

import android.os.Parcel;
import android.os.Parcelable;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

public class Player implements Parcelable
{
    private RectF       playerRect;
    private RectF       startRect;
    private Paint       playerPaint;
    private int         canvasWidth;
    private int         canvasHeight;
    private float       width;
    private float       height;
    private float       ay;
    private float       ax;
    private float       vy                         = 0;
    private float       vx                         = 0;
    private float       py;
    private float       px;
    private boolean     sideSwitched               = false;
    private float startingSideJumpVelocity   = 750;
    private float       additionalSideJumpVelocity = 0f;
    private float jumpVelocity               = 1500f;
    private boolean     midJump                    = false;


    public Player(RectF r, int cW, int cH)
    {
        startRect = r;
        playerRect = r;
        width = r.right - r.left;
        height = r.top - r.bottom;
        canvasWidth = cW;
        canvasHeight = cH;
        py = playerRect.centerY();
        px = playerRect.centerX();
        ax = 10;
        ay = -canvasHeight * 1.5f;
        playerPaint = new Paint();
        playerPaint.setColor(Color.BLACK);
        playerPaint.setStyle(Style.FILL);
    }

    public Player(Parcel parcel)
    {
        playerRect = parcel.readParcelable(null);
        startRect = parcel.readParcelable(null);
        canvasWidth = parcel.readInt();
        canvasHeight = parcel.readInt();
        width = parcel.readFloat();
        height = parcel.readFloat();
        ay = parcel.readFloat();
        ax = parcel.readFloat();
        vy = parcel.readFloat();
        vx = parcel.readFloat();
        py = parcel.readFloat();
        px = parcel.readFloat();
        sideSwitched = parcel.readByte() != 0;
        startingSideJumpVelocity = parcel.readFloat();
        additionalSideJumpVelocity = parcel.readFloat();
        jumpVelocity = parcel.readFloat();
        midJump = parcel.readByte() != 0;
        playerPaint.setColor(Color.BLACK);
        playerPaint.setStyle(Style.FILL);
    }


    public void restart()
    {
        playerRect = startRect;
        py = playerRect.centerY();
        px = playerRect.centerX();
        vy = 0;
        vx = 0;
        sideSwitched = false;
        additionalSideJumpVelocity = 0f;
        midJump = false;
    }


    public void draw(Canvas c)
    {
// float ground = c.getHeight() * 0.8f;
// float cutoff = c.getHeight() * 0.5f;
//
// RectF localRect;
// if (playerRect.bottom < cutoff)
// {
// localRect =
// new RectF(
// playerRect.left,
// ground - playerRect.top,
// playerRect.right,
// ground - playerRect.bottom);
// }
// else
// {
// localRect =
// new RectF(
// playerRect.left,
// ground - playerRect.top - (playerRect.bottom - cutoff - ground),
// playerRect.right,
// ground - playerRect.bottom - (playerRect.bottom - cutoff - ground));
// System.out.println(localRect + "");
// }
//
// c.drawRect(localRect, playerPaint);

        float ground = c.getHeight() * 0.8f;
        float cutoff = c.getHeight() * 0.5f;
        RectF localRect =
            new RectF(
                playerRect.left,
                ground - playerRect.top,
                playerRect.right,
                ground - playerRect.bottom);
        localRect.offset(0, playerRect.bottom - (ground - cutoff));

        c.drawRect(localRect, playerPaint);
    }


    public RectF getRect()
    {
        return playerRect;
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
        if (playerRect.bottom < collided.top
            && playerRect.bottom > collided.bottom
            && ((playerRect.right < collided.right && playerRect.right > collided.left) || (playerRect.left > collided.left && playerRect.left < collided.right)))
        {
            float intersectBot = collided.top - playerRect.bottom;
            float intersectRight = playerRect.right - collided.left;
            float intersectLeft = collided.right - playerRect.left;
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
        else if (playerRect.top < collided.top
            && playerRect.top > collided.bottom
            && ((playerRect.right < collided.right && playerRect.right > collided.left) || (playerRect.left > collided.left && playerRect.left < collided.right)))
        {
            float intersectBot = playerRect.top - collided.bottom;
            float intersectRight = playerRect.right - collided.left;
            float intersectLeft = collided.right - playerRect.left;
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
     * @param other
     *            the RectF the player collided with
     * @param whichSide
     *            an integer indicating what side of the player collided. See
     *            documentation for -intersects(RectF)
     */
    public void fixIntersection(Box other, int whichSide)
    {
        if (whichSide == 0) // top
        {
            playerRect.top = other.bottom - 0.5f; // + 10;
            playerRect.bottom = playerRect.top - height;
            vy = -canvasHeight / 4f;
            py = playerRect.centerY();
            // Log.d("CENTER", playerRect + "");
        }
        else if (whichSide == 1) // right
        {
            playerRect.right = other.left - 0.5f;
            playerRect.left = playerRect.right - width;
            vx = 0;
            if (!midJump)
            {
                vx = 20.0f;
                vy = -canvasHeight * .125f;
                px = playerRect.centerX();
            }
            // Log.d("CENTER", playerRect + "");
        }
        else if (whichSide == 2) // bottom
        {
            playerRect.bottom = other.top + 0.5f;
            playerRect.top = playerRect.bottom + height;
            vy = 0;
            py = playerRect.centerY();
            midJump = false;
        }
        else if (whichSide == 3) // left
        {
            playerRect.left = other.right + 0.5f;
            playerRect.right = playerRect.left + width;
            vx = 0;
            if (!midJump)
            {
                vx = -20.0f;
                vy = -canvasHeight * .125f;
                px = playerRect.centerX();
            }
        }
        else if (whichSide == 4) // switched side and collided
        {
            if (px > canvasWidth)
            {
                playerRect.left = -width / 2 + 1.0f;
                playerRect.right = playerRect.left + width;

            }
            else if (px < 0)
            {
                playerRect.right = canvasWidth + width / 2 - 1.0f;
                playerRect.left = playerRect.right - width;
            }
            sideSwitched = false;
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
        vy += ay * (deltaT / 1000.0f);
        if (midJump && vy <= 0)
            midJump = false;
        float pytemp =
            py + vy * (deltaT / 1000.0f)
                + (-ay * (deltaT / 1000.0f) * (deltaT / 1000.0f));

        // add amount of sideJump from jumping
        float pxtemp =
            px + (vx + additionalSideJumpVelocity) * (deltaT / 1000.0f);
        if (additionalSideJumpVelocity > 0 || additionalSideJumpVelocity < 0)
        {
            float adjustmentAmount = startingSideJumpVelocity * deltaT / 250f;
            if (Math.abs(additionalSideJumpVelocity) < adjustmentAmount)
                additionalSideJumpVelocity = 0;
            else if (additionalSideJumpVelocity > 0)
                additionalSideJumpVelocity -= adjustmentAmount;
            else
                additionalSideJumpVelocity += adjustmentAmount;
        }
// if (pytemp > py)
// grounded = false;
        playerRect.offset(pxtemp - px, pytemp - py);
        if (px < 0)
        {
            Log.d("OFF SCREEN", "YAYA TOURE");
            playerRect.right = canvasWidth + width / 2;
            playerRect.left = playerRect.right - width;
            sideSwitched = true;
        }
        else if (px > canvasWidth)
        {
            Log.d("OFF SCREEN", "YAYA TOURE");
            playerRect.left = -width / 2;
            playerRect.right = playerRect.left + width;
            sideSwitched = true;
        }
        py = playerRect.centerY();
        px = playerRect.centerX();
        // Log.d("position", "bottom: " + playerRect.bottom);
        // set grounded every frame, update via a collision. otherwise, you can
        // run off a block then jump (which shouldn't be a thing)
    }


    public void setXVelocity(float dvx)
    {
        vx = dvx;
    }

    public float getY()
    {
        return py;
    }


    public float getX()
    {
        return px;
    }


    public void setYVelocity(float blockVY)
    {
        vy = blockVY;
    }


    public boolean switchedSides()
    {
        return sideSwitched;
    }


    public int describeContents()
    {
        // TODO Auto-generated method stub
        return 0;
        //idk what this method is supposed to do
    }


    public void writeToParcel(Parcel parcel, int flags)
    {
        parcel.writeParcelable(playerRect, 0);//idk if 0
        parcel.writeParcelable(startRect, 0);
        //idk what to do about paint. just load it regardless in the reading thing?
        parcel.writeInt(canvasWidth);
        parcel.writeInt(canvasHeight);
        parcel.writeFloat(width);
        parcel.writeFloat(height);
        parcel.writeFloat(ay);
        parcel.writeFloat(ax);
        parcel.writeFloat(vy);
        parcel.writeFloat(vx);
        parcel.writeFloat(py);
        parcel.writeFloat(px);
        parcel.writeByte((byte)(sideSwitched?1:0));
        parcel.writeFloat(startingSideJumpVelocity);
        parcel.writeFloat(additionalSideJumpVelocity);
        parcel.writeFloat(jumpVelocity);
        parcel.writeByte((byte)(midJump?1:0));

    }

    public boolean isDead() {
        // TODO implement
        return false;
    }
}
