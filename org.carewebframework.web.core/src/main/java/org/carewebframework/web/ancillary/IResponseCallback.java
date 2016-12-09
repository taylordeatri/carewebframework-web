package org.carewebframework.web.ancillary;

/**
 * Callback interface used to return a simple response.
 *
 * @param <T> Type of response
 */
public interface IResponseCallback<T> {
    
    void onComplete(T confirm);
}
