package se.streamsource.streamflow.web.application.pdf;

import java.awt.*;

/**
 * Created by 3emluk on 06.05.16.
 */
public class LineObject {
    private float yPosition;
    private float endX;
    private Color color;

    public LineObject(float endX) {
        this.endX = endX;
    }

    public LineObject(float yPosition, float endX) {
        this.yPosition = yPosition;
        this.endX = endX;
    }

    public LineObject(float yPosition, float endX, Color color) {
        this.yPosition = yPosition;
        this.endX = endX;
        this.color = color;
    }

    public float getyPosition() {
        return yPosition;
    }

    public void setyPosition(float yPosition) {
        this.yPosition = yPosition;
    }

    public float getEndX() {
        return endX;
    }

    public void setEndX(float endX) {
        this.endX = endX;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
