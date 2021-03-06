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

/**
 * Supported export MIME types.
 */
public enum ExportType {
    png("image/png"), jpeg("image/jpeg"), pdf("application/pdf"), svg("image/svg+xml");
    
    private final String mimetype;
    
    private ExportType(String mimetype) {
        this.mimetype = mimetype;
    }
    
    @Override
    public String toString() {
        return mimetype;
    }
}
