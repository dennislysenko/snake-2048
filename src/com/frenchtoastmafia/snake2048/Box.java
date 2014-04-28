package com.frenchtoastmafia.snake2048;

import android.graphics.*;
import android.graphics.Paint.Style;

public class Box
    extends RectF
{
    private float x;
    private float y;
    public static final float SIZE = 48;

    private int value;

    private Paint fillPaint;
    private Paint textPaint;


    /**
     * Constructor for the box class
     *
     * @param x
     *            the center x of the box
     * @param y
     *            the center y of the box
     */
    public Box(float x, float y, int value)
    {
        super(x - SIZE / 2, y + SIZE / 2, x + SIZE / 2, y - SIZE / 2);

        this.x = x;
        this.y = y;
        this.value = value;

        fillPaint = new Paint();
        fillPaint.setColor(Color.BLUE);
        fillPaint.setStyle(Style.FILL);
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setStyle(Style.FILL);
        textPaint.setTextSize(60);
        textPaint.setFakeBoldText(true);

        adjustToGrid();
    }


    public void updateBounds()
    {
        set(x - SIZE / 2, y + SIZE / 2, x + SIZE / 2, y - SIZE / 2);
        adjustToGrid();
    }


    public void draw(Canvas c, Paint fillPaint, Paint textPaint)
    {
       c.drawRect(this, fillPaint);
       if (value > 0) {
           String sValue = ""+value;

           // TODO set the font SIZE of the textPaint to fit sValue

           Rect bounds = new Rect();
           textPaint.getTextBounds(sValue, 0, sValue.length(), bounds);

           float paddingX = 0.5f * (width() - bounds.width());
           float paddingY = 0.5f * (height() - bounds.height());

           c.drawText(sValue, left + paddingX, top, textPaint);
       }
    }

    public void draw(Canvas c) {
        this.draw(c, this.fillPaint, this.textPaint);
    }

    // ----------------------------------------------------------
    /**
     * @return the x
     */
    public float getX()
    {
        return this.centerX();
    }


    // ----------------------------------------------------------
    /**
     * @param x
     *            the x to set
     */
    public void setX(float x)
    {
        this.x = x;
        updateBounds();
    }


    // ----------------------------------------------------------
    /**
     * @return the y
     */
    public float getY()
    {
        return this.centerY();
    }


    // ----------------------------------------------------------
    /**
     * @param y
     *            the y to set
     */
    public void setY(float y)
    {
        this.y = y;
        updateBounds();
    }

    public int intersects(RectF collided)
    {
        int minimumIntersectIndex = -1;
        float minimumIntersect = Float.MAX_VALUE;
        if (this.bottom < collided.top
            && this.bottom > collided.bottom
            && ((this.right < collided.right && this.right > collided.left)
                || (this.left > collided.left && this.left < collided.right) || (this.right > collided.right && this.left < collided.left)))
        {
            float intersectBot = collided.top - this.bottom;
            float intersectRight = this.right - collided.left;
            float intersectLeft = collided.right - this.left;
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
        }
// else if (this.top < collided.top
// && this.top > collided.bottom
// && ((this.right < collided.right && this.right > collided.left) || (this.left
// > collided.left && this.left < collided.right)))
// {
// minimumIntersectIndex = 0;
// }
        return minimumIntersectIndex;
    }


    public void fixIntersection(RectF other, int whichSide)
    {
        if (whichSide == 1)
        {
            float amount = other.left - this.right - 30.5f;
            offset(amount, 0);

            x += amount;
        }
        else if (whichSide == 2) // bottom
        {
            float amount = other.top - this.bottom + 0.5f;
            offset(0, amount);

            y += amount;
        }
        else if(whichSide == 3)
        {
            float amount = other.right - this.left + 30.5f;
            offset(amount, 0);
            x += amount;
        }

        adjustToGrid();
    }

    private void adjustToGrid() {
        float leftAdjust = this.left % 48;
        float topAdjust = this.top % 48;

        // TODO figure out why this gridding code was breaking everything
        if (false) {
            this.left -= leftAdjust;
            this.right -= leftAdjust;
            this.x -= leftAdjust;

            this.top -= topAdjust;
            this.bottom -= topAdjust;
            this.y -= topAdjust;
        }
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
