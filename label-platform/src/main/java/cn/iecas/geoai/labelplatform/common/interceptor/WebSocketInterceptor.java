package cn.iecas.geoai.labelplatform.common.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 握手请求与响应处理
 */
@Slf4j
public class WebSocketInterceptor implements HandshakeInterceptor {

    /**
     * 握手之前执行该方法，继续握手返回true、中断握手返回false
     * @param serverHttpRequest
     * @param serverHttpResponse
     * @param webSocketHandler
     * @param map
     * @return
     * @throws Exception
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Map<String, Object> map) throws Exception {
        if (serverHttpRequest instanceof ServletServerHttpRequest) {
            String userId = ((ServletServerHttpRequest) serverHttpRequest).getServletRequest().getParameter("userId");
            String taskTokenId = ((ServletServerHttpRequest) serverHttpRequest).getServletRequest().getParameter("taskTokenId");
            String name = ((ServletServerHttpRequest) serverHttpRequest).getServletRequest().getParameter("name");
            String realName = ((ServletServerHttpRequest) serverHttpRequest).getServletRequest().getParameter("realName");
            String headImg = ((ServletServerHttpRequest) serverHttpRequest).getServletRequest().getParameter("headImg");
            String labelInfo = ((ServletServerHttpRequest) serverHttpRequest).getServletRequest().getParameter("labelInfo");
            map.put("userId", userId);
            map.put("taskTokenId", taskTokenId);
            map.put("name", name);
            map.put("realName", realName);
            map.put("headImg", headImg);
            map.put("labelInfo", labelInfo);
            map.put("operation", "INTO");
            log.info("用户 {} 正在建立连接...", userId);
            return true;
        }
        return false;
    }

    /**
     * 握手之后执行该方法，无论是否握手成功，都指明了响应头和状态码
     * @param serverHttpRequest
     * @param serverHttpResponse
     * @param webSocketHandler
     * @param e
     */
    @Override
    public void afterHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Exception e) {
        log.info("用户连接中...");
    }
}
