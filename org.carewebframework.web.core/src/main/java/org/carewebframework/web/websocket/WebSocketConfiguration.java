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
package org.carewebframework.web.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

/**
 * Configurer for the web socket connection.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfiguration implements WebSocketConfigurer {

    private static long keepaliveInterval;
    
    @Autowired
    private WebSocketHandler cwf_WebSocketHandler;
    
    /**
     * Returns the keep-alive interval, in milliseconds. The client will transmit a ping packet when
     * no transmission has occurred within this interval. A value of <= 0 disables this feature.
     *
     * @return The keep-alive interval.
     */
    public static long getKeepaliveInterval() {
        return keepaliveInterval;
    }

    /**
     * Register the web socket handler and add a handshake interceptor to copy attributes from the
     * http session to the web socket.
     *
     * @see org.springframework.web.socket.config.annotation.WebSocketConfigurer#registerWebSocketHandlers(org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry)
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(cwf_WebSocketHandler, "/ws/**").addInterceptors(new HttpSessionHandshakeInterceptor());
    }

    /**
     * Sets the keep-alive interval, in milliseconds. The client will transmit a ping packet when no
     * transmission has occurred within this interval.
     *
     * @param value The keep-alive interval, in milliseconds. A value of <= 0 disables this feature.
     */
    @Value("${org.carewebframework.web.websocket.keepaliveInterval}")
    private void setKeepaliveInterval(long value) {
        keepaliveInterval = value;
    }
}
