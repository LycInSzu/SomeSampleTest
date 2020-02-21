package com.wtk.charge;

public class BubbleBean {
    private float randomY = 3;
    private float x;
    private float y;
    private int index;

    public BubbleBean(float x, float y, float randomY, int index) {
        this.x = x;
        this.y = y;
        this.randomY = randomY;
        this.index = index;
    }

    public void set(float x, float y, float randomY, int index) {
        this.x = x;
        this.y = y;
        this.randomY = randomY;
        this.index = index;
    }

    public void setMove(int screenHeight, int maxDistance) {
        this.y -= randomY;
        /*
        if (y - maxDistance < 110) {
            this.y -= 2;
            return;
        }

        if (maxDistance <= y && screenHeight - y > 110) {
            this.y -= randomY;
        } else {
            this.y -= 1;
        }*/

        /*if (index == 0) {
            this.x -= 0.4;
        } else if (index == 2) {
            this.x += 0.4;
        }*/
    }


    public int getIndex() {
        return index;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }
}
