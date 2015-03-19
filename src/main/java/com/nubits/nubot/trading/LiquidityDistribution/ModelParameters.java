/*
 * Copyright (C) 2015 Nu Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.nubits.nubot.trading.LiquidityDistribution;


import java.util.Objects;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class ModelParameters {

    private static final Logger LOG = LoggerFactory.getLogger(ModelParameters.class.getName());
    private double offset; //Distance from 1$ target price, expressed in USD
    private double wallHeight; //amount of base-liquidity available at the best price, expressed in USD
    private double wallWidth; //otal width of the order book, expressed in USD
    private double density; //distance between orders, expressed in USD
    private LiquidityCurve curve; //type of model

    public ModelParameters(double offset, double wallHeight, double wallWidth, double density, LiquidityCurve curve) {
        this.offset = offset;
        this.wallHeight = wallHeight;
        this.wallWidth = wallWidth;
        this.density = density;
        this.curve = curve;
    }

    public double getOffset() {
        return offset;
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }

    public double getWallHeight() {
        return wallHeight;
    }

    public void setWallHeight(double wallHeight) {
        this.wallHeight = wallHeight;
    }

    public double getWallWidth() {
        return wallWidth;
    }

    public void setWallWidth(double wallWidth) {
        this.wallWidth = wallWidth;
    }

    public double getDensity() {
        return density;
    }

    public void setDensity(double density) {
        this.density = density;
    }

    public LiquidityCurve getCurve() {
        return curve;
    }

    public void setCurve(LiquidityCurve curve) {
        this.curve = curve;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (int) (Double.doubleToLongBits(this.offset) ^ (Double.doubleToLongBits(this.offset) >>> 32));
        hash = 41 * hash + (int) (Double.doubleToLongBits(this.wallHeight) ^ (Double.doubleToLongBits(this.wallHeight) >>> 32));
        hash = 41 * hash + (int) (Double.doubleToLongBits(this.wallWidth) ^ (Double.doubleToLongBits(this.wallWidth) >>> 32));
        hash = 41 * hash + (int) (Double.doubleToLongBits(this.density) ^ (Double.doubleToLongBits(this.density) >>> 32));
        hash = 41 * hash + Objects.hashCode(this.curve);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ModelParameters other = (ModelParameters) obj;
        if (Double.doubleToLongBits(this.offset) != Double.doubleToLongBits(other.offset)) {
            return false;
        }
        if (Double.doubleToLongBits(this.wallHeight) != Double.doubleToLongBits(other.wallHeight)) {
            return false;
        }
        if (Double.doubleToLongBits(this.wallWidth) != Double.doubleToLongBits(other.wallWidth)) {
            return false;
        }
        if (Double.doubleToLongBits(this.density) != Double.doubleToLongBits(other.density)) {
            return false;
        }
        if (!Objects.equals(this.curve, other.curve)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ModelParameters{" + "offset=" + offset + ", wallHeight=" + wallHeight + ", wallWidth=" + wallWidth + ", density=" + density + ", curve=" + curve + '}';
    }
}
