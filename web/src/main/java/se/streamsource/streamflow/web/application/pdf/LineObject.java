/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
