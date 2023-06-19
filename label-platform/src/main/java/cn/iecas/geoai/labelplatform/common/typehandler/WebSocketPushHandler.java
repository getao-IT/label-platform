package cn.iecas.geoai.labelplatform.common.typehandler;

import cn.iecas.geoai.labelplatform.entity.dto.LabelCommitInfo;
import cn.iecas.geoai.labelplatform.entity.dto.UserInfo;
import cn.iecas.geoai.labelplatform.service.LabelTaskService;
import cn.iecas.geoai.labelplatform.service.UserInfoService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.util.JSONBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * WebSocket 消息处理器
 */
@Slf4j
public class WebSocketPushHandler extends AbstractWebSocketHandler {

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private LabelTaskService labelTaskService;

    private static final Vector<WebSocketSession> userList = new Vector<>();

    private static final Map<String, Vector<WebSocketSession>> labelTaskAndUserRelation = new ConcurrentHashMap<>();

    /**
     * 用户进入监听
     *
     * @param session
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("用户 {} 进入系统..", session.getAttributes());

        // 保存标注与用户的对应信息
        Map<String, Object> attributes = session.getAttributes();
        log.info("标注任务标识 {}", attributes.get("taskTokenId"));
        for (String key : attributes.keySet()) {
            if (key.contains("labelInfo") || key.contains("headImg")) {
                log.info("key: {}, value: {} ", key, attributes.get(key) == null || attributes.get(key) == "" ? "无" : "有");
            } else {
                log.info("key: {}, value: {} ", key, attributes.get(key));
            }

        }
        String taskTokenId = String.valueOf(attributes.get("taskTokenId"));
        String userId = String.valueOf(attributes.get("userId"));
        if (labelTaskAndUserRelation.containsKey(taskTokenId)) {
            if (!isExistUserIntask(taskTokenId, userId)) {
                labelTaskAndUserRelation.get(taskTokenId).add(session);
            } else {
                this.sendAllToUser(taskTokenId, "INTO", userId, userId);
                return;
            }

        } else {
            Vector<WebSocketSession> userList = new Vector<>();
            userList.add(session);
            labelTaskAndUserRelation.put(taskTokenId, userList);
        }

        // 广播当前标注任务的所有用户标注
        this.send(taskTokenId, session, "INTO", userId,userId);
    }

    /**
     * 判断进入的用户是否存在于标注任务中
     * @param taskTokenId
     * @return
     */
    private boolean isExistUserIntask(String taskTokenId, String userId) {
        Vector<WebSocketSession> webSocketSessions = labelTaskAndUserRelation.get(taskTokenId);
        if (webSocketSessions == null)
            return false;
        List<WebSocketSession> result = webSocketSessions.stream().filter(e -> e.getAttributes().get("userId").equals(userId)).collect(Collectors.toList());
        return result.size() > 0;
    }

    /**
     * 用户退出监听
     *
     * @param session
     * @param status
     * @throws Exception
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        if (session.isOpen()) {
            session.close();
        }
        Map<String, Object> attributes = session.getAttributes();
        String taskTokenId = String.valueOf(attributes.get("taskTokenId"));
        String userId = String.valueOf(attributes.get("userId"));
        if (labelTaskAndUserRelation.containsKey(taskTokenId)) {
            labelTaskAndUserRelation.get(taskTokenId).remove(session);
            this.send(taskTokenId, session,"OUT", userId,null);
            log.info("用户 {} 退出标注任务 {} ...", userId, taskTokenId);
        } else {
            log.error("没有查询到TokenId为 {} 的标注任务信息", taskTokenId);
        }
    }

    /**
     * 接收消息
     *
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        log.info("系统正在处理用户的请求 {} ... ", session.getAttributes());
        JSONObject messageJson = JSONObject.parseObject(String.valueOf(message.getPayload()));
        String taskTokenId = String.valueOf(messageJson.get("taskTokenId"));
        String userId = String.valueOf(messageJson.get("userId"));
        String labelInfo = String.valueOf(messageJson.get("labelInfo"));
        String operation = String.valueOf(messageJson.get("operation"));

        Vector<WebSocketSession> webSocketSessions = labelTaskAndUserRelation.get(taskTokenId);
        for (WebSocketSession webSocketSession : webSocketSessions) {
            if (webSocketSession.getAttributes().get("userId").equals(userId)) {
                webSocketSession.getAttributes().put("labelInfo", labelInfo);
                webSocketSession.getAttributes().put("operation", operation);
            }
        }

        session.getAttributes().put("labelInfo", labelInfo);
        session.getAttributes().put("operation", operation);
        this.send(taskTokenId, session,operation, userId,null);
    }

    /**
     * 推送消息
     * @param operation 发送消息的操作类型
     * @param message 发送的消息
     * @param taskTokenId 任务ID
     * @param currentUser 当前用户，该参数为null,则向当前用户推送；不为null，则不想该用户推送消息
     * @param userId 指定向哪位用户推送消息
     */
    public void sendMessage(String operation, WebSocketMessage message, String taskTokenId, String currentUser, String userId) {
        try {
            Vector<WebSocketSession> userSessions = labelTaskAndUserRelation.get(taskTokenId);
            Assert.isTrue(userSessions != null, "没有该任务信息，taskTokenId: " + taskTokenId);
            if (userId != null && userId != "") { // 推送指定用户
                for (WebSocketSession session : userSessions) {
                    if (session.getAttributes().get("userId").equals(userId)) {
                        session.sendMessage(message);
                        log.info("任务 {} 向用户 {} 推送消息成功...", taskTokenId, session.getAttributes().get("userId"));
                    }
                }
            } else { // 推送全部用户
                for (WebSocketSession session : userSessions) {
                    if (session.getAttributes().get("userId").equals(currentUser) && !operation.equals("INTO"))
                        continue;
                    session.sendMessage(message);
                    log.info("任务 {} 向用户 {} 推送消息成功...", taskTokenId, session.getAttributes().get("userId"));
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    /**
     * 推送全部信息
     * 指定用户，或者推送给当前标注任务全部用户
     *
     * @param taskTokenId 当前标注任务ID
     * @param operation 用户操作类型 INTO || OUT || LABELING
     * @param currentUser 当前用户
     * @param targetUser  指定用户，为null则推送给当前标注任务全部用户
     */
    private void send(String taskTokenId, WebSocketSession session, String operation, String currentUser, String targetUser) throws IOException {
        // 进入标注任务，对进入的用户广播所有标注信息；同任务其他用户，广播进入用户信息
        if (operation.equalsIgnoreCase("INTO")) {
            this.sendAllToUser(taskTokenId, operation, currentUser, targetUser);
            this.sendCurrentToAllUser(taskTokenId, session, currentUser, targetUser);
        }

        // 标注操作时，对同任务其他用户，广播推送的消息
        if (operation.equalsIgnoreCase("LABELING") || operation.equalsIgnoreCase("OUT")) {
            this.sendCurrentToAllUser(taskTokenId, session, operation, currentUser);
        }
    }

    /**
     * 发送所有消息到指定用户
     */
    private void sendAllToUser(String taskTokenId, String operation, String currentUser, String targetUser) {
        Vector<WebSocketSession> userList = labelTaskAndUserRelation.get(taskTokenId);
        JSONObject currentTaskInfo = new JSONObject();
        JSONArray taskInfoJson = new JSONArray();
        // 进入标注任务，对进入的用户广播所有标注信息；同任务其他用户，广播进入用户信息
        for (WebSocketSession socketSession : userList) {
            Map<String, Object> params = socketSession.getAttributes();
            taskInfoJson.add(params);
        }
        currentTaskInfo.put(taskTokenId, taskInfoJson);
        currentTaskInfo.put("operation", operation);
        TextMessage message = new TextMessage(currentTaskInfo.toJSONString());
        this.sendMessage(operation, message, taskTokenId, currentUser, targetUser);
    }

    /**
     * 发送当前消息到所有用户
     */
    private void sendCurrentToAllUser(String taskTokenId, WebSocketSession session, String operation, String currentUser) throws IOException {
        Vector<WebSocketSession> userList = labelTaskAndUserRelation.get(taskTokenId);
        JSONObject currentTaskInfo = new JSONObject();
        JSONArray current = null;
        JSONArray taskInfoJson = new JSONArray();
        // 进入标注任务，对进入的用户广播所有标注信息；同任务其他用户，广播进入用户信息
        for (WebSocketSession socketSession : userList) {
            Map<String, Object> params = socketSession.getAttributes();
            taskInfoJson.add(params);
            if (!params.get("userId").equals(currentUser)) {
                current = new JSONArray();
                current.add(session.getAttributes());
                currentTaskInfo.put(taskTokenId, current);
                currentTaskInfo.put("operation", operation);
                TextMessage message = new TextMessage(currentTaskInfo.toJSONString());
                socketSession.sendMessage(message);
                log.info("任务 {} 向用户 {} 推送消息成功...", taskTokenId, socketSession.getAttributes().get("userId"));
            }
        }
    }

    /**
     * 推送当前用户的信息
     * 指定用户，或者推送给当前标注任务全部用户
     * @param taskTokenId 当前标注任务ID
     * @param userId      当前用户
     * @param labelInfo   标注信息
     * @param targetUser  指定用户，为null则推送给当前标注任务全部用户
     */
    private void sendCurrent(String taskTokenId, String userId, String labelInfo, String targetUser) {
        JSONObject currentUserInfo = new JSONObject();
        currentUserInfo.put("userId", userId);
        currentUserInfo.put("labelInfo", labelInfo);
        JSONObject currentLabelUserInfo = new JSONObject();
        currentLabelUserInfo.put(taskTokenId, currentUserInfo);
        TextMessage currentUserMessage = new TextMessage(currentLabelUserInfo.toJSONString());
        this.sendMessage(null, currentUserMessage, taskTokenId, null, targetUser);
    }

    /**
     * 转化协同标注前端推送的标注格式为样本数据系统中标注格式
     * @param labelInfo
     * @return
     */
    private JSONArray parseLabelInfo(String labelInfo) {
        JSONObject result = new JSONObject();
        JSONObject jsonObject = JSONObject.parseObject(labelInfo);
        JSONArray latlngs = jsonObject.getJSONArray("latlngs");
        JSONArray point = new JSONArray();
        for (Object latlng : latlngs) {
            JSONObject latlg = JSONObject.parseObject(String.valueOf(latlng));
            point.add(latlg.get("lng")+","+latlg.get("lat"));
        }
        result.put("id", jsonObject.getIntValue("id"));
        result.put("type", jsonObject.getString("type"));
        result.put("checkStatus", jsonObject.getIntValue("checkStatus"));
        result.put("note", "");
        String coordinate = jsonObject.getString("coordinate");
        String description = coordinate.equals("geodegree") ? "经纬度坐标" : (coordinate.equals("pixel") ? "像素坐标" : "投影坐标");
        result.put("coordinate", coordinate);
        result.put("description", description);
        JSONObject possibleresult = new JSONObject();
        possibleresult.put("name", jsonObject.get("name"));
        possibleresult.put("probability", "1");
        JSONArray possresult = new JSONArray();
        possresult.add(possibleresult);
        result.put("possibleresult", possresult);
        JSONObject pointRestlt = new JSONObject();
        pointRestlt.put("point", point);
        result.put("points",pointRestlt);
        JSONArray resultArr = new JSONArray();
        resultArr.add(result);
        return resultArr;
    }
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        super.handleBinaryMessage(session, message);
    }

    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        super.handlePongMessage(session, message);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        super.handleTransportError(session, exception);
    }

    @Override
    public boolean supportsPartialMessages() {
        return super.supportsPartialMessages();
    }

    public static void main(String[] args) {
        /*Map<String, List> map = new HashMap<>();
        ArrayList<Object> objects = new ArrayList<>();
        objects.add("ddd");
        objects.add("eee");
        map.put("aaa", objects);
        System.out.println(map);
        map.get("aaa").add("fff");
        System.out.println(map);
        map.get("aaa").remove("ddd");
        System.out.println(map);*/
        JSONObject a = new JSONObject();
        a.put("aaa", 111);
        a.put("bbb", 222);
        JSONObject b = new JSONObject();
        b.put("ddd", a);
        b.put("eee", 444);
        System.out.println(a);
        System.out.println(b);
        b.getJSONObject("ddd").remove("aaa");
        System.out.println("111111111111111111111111111");
        System.out.println(a);
        System.out.println(b);

    }
}

