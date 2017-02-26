/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.web.highcharts;

import java.util.HashMap;
import java.util.Map;

import org.carewebframework.web.annotation.ComponentScanner;

/**
 * Static utility methods.
 */
public class Util {
    
    private static final Map<String, Class<? extends PlotOptions>> plotTypes = new HashMap<String, Class<? extends PlotOptions>>();
    
    /**
     * Returns the plot type from its text identifier.
     *
     * @param type The text identifier for the plot type.
     * @return An instance of the specified plot type.
     */
    public static PlotOptions getPlotType(String type) {
        try {
            return plotTypes.get(type).newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid plot type: " + type);
        }
    }
    
    /**
     * Load time initializations.
     */
    public static void init() {
        ComponentScanner.getInstance().scanPackage(Util.class.getPackage());
        plotTypes.put("area", PlotArea.class);
        plotTypes.put("arearange", PlotAreaRange.class);
        plotTypes.put("areaspline", PlotAreaSpline.class);
        plotTypes.put("areasplinerange", PlotAreaSplineRange.class);
        plotTypes.put("bar", PlotBar.class);
        plotTypes.put("boxplot", PlotBox.class);
        plotTypes.put("bubble", PlotBubble.class);
        plotTypes.put("column", PlotColumn.class);
        plotTypes.put("columnrange", PlotColumnRange.class);
        plotTypes.put("errorbar", PlotErrorBar.class);
        plotTypes.put("funnel", PlotFunnel.class);
        plotTypes.put("gauge", PlotGauge.class);
        plotTypes.put("line", PlotLine.class);
        plotTypes.put("pie", PlotPie.class);
        plotTypes.put("scatter", PlotScatter.class);
        plotTypes.put("solidgauge", PlotSolidGauge.class);
        plotTypes.put("spline", PlotSpline.class);
        plotTypes.put("waterfall", PlotWaterfall.class);
    }
    
    /**
     * Enforce static class.
     */
    private Util() {
    }
    
}
