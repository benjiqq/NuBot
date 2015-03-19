/*
 * Copyright (C) 2014-2015 Nu Development Team
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

package com.nubits.nubot.utils;

import easyjcckit.Graphics2DPlotCanvas;
import easyjcckit.GraphicsPlotCanvas;
import easyjcckit.data.*;
import easyjcckit.util.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/**
 * You can import this statically "import static easyjcckit.QuickPlot.*;
 */
public class LiquidityPlot {

    static JFrame jframe;

    public static class CurveData {

        public double[] xaxis;
        public double[] yvalues;
        public boolean line = true;
        public boolean symbol = true;

        public CurveData(double[] xaxis, double[] yvalues) {
            this.xaxis = xaxis;
            this.yvalues = yvalues;
        }

        public CurveData(double[] xaxis, double[] yvalues, boolean line) {
            this.xaxis = xaxis;
            this.yvalues = yvalues;
            this.line = line;
        }

        public LiquidityPlot.CurveData symbol(boolean symbol) {
            this.symbol = symbol;
            return this;
        }
    }
    static final ArrayList<easyjcckit.QuickPlot.CurveData> curves = new ArrayList<easyjcckit.QuickPlot.CurveData>();

    static void _plot() {
        double xmin, xmax, ymin, ymax;
        xmin = xmax = ymin = ymax = 0;
        for (int set = 0; set < curves.size(); set++) {
            easyjcckit.QuickPlot.CurveData curveData = curves.get(set);
            int N = curveData.xaxis.length;
            if (curveData.yvalues.length != N) {
                throw new RuntimeException("xaxis and yvalues should have same length");
            }
            if (N == 0) {
                throw new RuntimeException("xaxis and yvalues were empty");
            }
            if (set == 0) {
                xmin = xmax = curveData.xaxis[0];
                ymin = ymax = curveData.yvalues[0];
            }
            for (int i = 1; i < N; i++) {
                xmin = Math.min(xmin, curveData.xaxis[i]);
                xmax = Math.max(xmax, curveData.xaxis[i]);
                ymin = Math.min(ymin, curveData.yvalues[i]);
                ymax = Math.max(ymax, curveData.yvalues[i]);
            }

        }
        if (ymin == ymax) {
            if (ymin > 0) {
                ymin = 0;
            } else if (ymin < 0) {
                ymax = 0;
            } else {
                ymin -= 1;
                ymax += 1;
            }
        }

        Properties props = new Properties();
        ConfigParameters config = new ConfigParameters(new PropertiesBasedConfigData(props));
        props.put("foreground", "0");
        props.put("background", "0xffffff");
        props.put("paper", "0 0 1 1");
        props.put("horizontalAnchor", "left");
        props.put("verticalAnchor", "bottom");
        props.put("plot/legendVisible", "false");
        props.put("plot/coordinateSystem/xAxis/minimum", "" + xmin * 0.9);
        props.put("plot/coordinateSystem/xAxis/maximum", "" + xmax * 1.1);
        props.put("plot/coordinateSystem/xAxis/axisLabel", "Price (cent$)");
        props.put("plot/coordinateSystem/xAxis/ticLabelFormat", "%d");
        props.put("plot/coordinateSystem/yAxis/axisLabel", "NBT Depth");
        props.put("plot/coordinateSystem/yAxis/minimum", "" + 0);
        props.put("plot/coordinateSystem/yAxis/maximum", "" + ymax * 1.1);
        props.put("plot/coordinateSystem/yAxis/axisLength", "0.8");
        props.put("plot/coordinateSystem/xAxis/axisLength", "1.15");
        props.put("plot/coordinateSystem/yAxis/ticLabelFormat", "%d");

        String definitions = "";
        for (int set = 0; set < curves.size(); set++) {
            if (set != 0) {
                definitions += " ";
            }
            definitions += "y" + set;
        }
        String[] colors = new String[]{"0xff0000", "0x00ff00", "0x0000ff", "0xffff00", "0xff00ff", "0x00ffff"};
        props.put("plot/curveFactory/definitions", definitions);
        for (int set = 0; set < curves.size(); set++) {
            if (curves.get(set).line) {
                props.put("plot/curveFactory/y" + set + "/withLine", "true");
            } else {
                props.put("plot/curveFactory/y" + set + "/withLine", "false");
            }
            if (curves.get(set).symbol) {
                props.put("plot/curveFactory/y" + set + "/symbolFactory/className",
                        "easyjcckit.plot.CircleSymbolFactory");
                props.put("plot/curveFactory/y" + set + "/symbolFactory/size", "0.01");
                props.put("plot/curveFactory/y" + set + "/symbolFactory/attributes/className",
                        "easyjcckit.graphic.ShapeAttributes");
                props.put("plot/curveFactory/y" + set + "/symbolFactory/attributes/fillColor", colors[set % colors.length]);
                props.put("plot/curveFactory/y" + set + "/lineAttributes/className",
                        "easyjcckit.graphic.ShapeAttributes");
                props.put("plot/curveFactory/y" + set + "/lineAttributes/lineColor", colors[set % colors.length]);
            }
        }

        final GraphicsPlotCanvas plotCanvas = new Graphics2DPlotCanvas(config);

        DataPlot _dataPlot = new DataPlot();
        for (int set = 0; set < curves.size(); set++) {
            easyjcckit.QuickPlot.CurveData curveData = curves.get(set);
            DataCurve curve = new DataCurve("y" + set);
            for (int i = 0; i < curveData.xaxis.length; i++) {
                curve.addElement(new DataPoint(curveData.xaxis[i], curveData.yvalues[i]));
            }
            _dataPlot.addElement(curve);
        }
        plotCanvas.connect(_dataPlot);

        if (jframe != null) {
            jframe.setVisible(false);
            jframe.dispose();
            jframe = null;
        }
        jframe = new JFrame();
        jframe.setTitle("Test Liquidity Distribution");
        jframe.setSize(800, 600);
        jframe.setLocationRelativeTo(null);
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.add(plotCanvas.getGraphicsCanvas());
        jframe.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        jframe.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
                jframe.dispose();
                System.exit(0);
            }
        });
        jframe.setVisible(true);
    }

    public static void scatter(double[] xaxis, double[] yvalues) {
        curves.clear();
        curves.add(new easyjcckit.QuickPlot.CurveData(xaxis.clone(), yvalues.clone(), false));
        _plot();
    }

    public static void addScatter(double[] xaxis, double[] yvalues) {
        curves.add(new easyjcckit.QuickPlot.CurveData(xaxis.clone(), yvalues.clone(), false));
        _plot();
    }

    public static void plot(double[] xaxis, double[] yvalues) {
        curves.clear();
        curves.add(new easyjcckit.QuickPlot.CurveData(xaxis.clone(), yvalues.clone(), true));
        _plot();
    }

    public static void addPlot(double[] xaxis, double[] yvalues) {
        curves.add(new easyjcckit.QuickPlot.CurveData(xaxis.clone(), yvalues.clone()));
        _plot();
    }

    public static void line(double[] xaxis, double[] yvalues) {
        curves.clear();
        curves.add(new easyjcckit.QuickPlot.CurveData(xaxis.clone(), yvalues.clone(), true).symbol(false));
        _plot();
    }

    public static void addLine(double[] xaxis, double[] yvalues) {
        curves.add(new easyjcckit.QuickPlot.CurveData(xaxis.clone(), yvalues.clone()).symbol(false));
        _plot();
    }

    public static void clearPlots() {
        curves.clear();
        if (jframe != null) {
            jframe.setVisible(false);
            jframe.dispose();
            jframe = null;
        }
    }

    public static void waitGraphs() {
        if (jframe == null) {
            return;
        }
        while (jframe.isVisible()) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                throw new RuntimeException("thread interrupted");
            }
        }
    }
}
