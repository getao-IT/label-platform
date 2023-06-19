package cn.iecas.geoai.labelplatform.common.config;

import cn.iecas.geoai.labelplatform.common.interceptor.WebSocketInterceptor;
import cn.iecas.geoai.labelplatform.common.typehandler.WebSocketPushHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;


/**
 * WebSocket配置类
 */
@Configuration
@EnableWebSocket
public class MyWebSocketConfig implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry.addHandler(createWebSocketPushHandler(), "/webSocket")
                .addInterceptors(createWebSocketInterceptor()).setAllowedOrigins("*");
        webSocketHandlerRegistry.addHandler(createWebSocketPushHandler(), "/withSockJs")
                .addInterceptors(createWebSocketInterceptor()).withSockJS();
    }

    @Bean
    public HandshakeInterceptor createWebSocketInterceptor() {
        return new WebSocketInterceptor();
    }

    @Bean
    public WebSocketHandler createWebSocketPushHandler() {
        return new WebSocketPushHandler();
    }

}
