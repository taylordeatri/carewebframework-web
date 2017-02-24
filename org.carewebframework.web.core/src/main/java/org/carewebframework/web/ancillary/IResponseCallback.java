/*
 * #%L
 * carewebframework
 * %%
 * Copyright (C) 2008 - 2016 Regenstrief Institute, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This Source Code Form is also subject to the terms of the Health-Related
 * Additional Disclaimer of Warranty and Limitation of Liability available at
 *
 *      http://www.carewebframework.org/licensing/disclaimer.
 *
 * #L%
 */
package org.carewebframework.web.ancillary;

/**
 * Callback interface used to return a simple response.
 *
 * @param <T> Type of response
 */
public interface IResponseCallback<T> {
    
    /**
     * Reports the response.
     *
     * @param response The response.
     */
    void onComplete(T response);
    
    /**
     * Convenience method for invoking a callback unless it is null.
     *
     * @param callback The callback (may be null).
     * @param response The response to report.
     */
    static <T> void invoke(IResponseCallback<T> callback, T response) {
        if (callback != null) {
            callback.onComplete(response);
        }
    }
}
