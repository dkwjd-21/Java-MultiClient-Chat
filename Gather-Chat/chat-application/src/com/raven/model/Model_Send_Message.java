package com.raven.model;

import com.raven.app.MessageType;
import org.json.JSONException;
import org.json.JSONObject;

public class Model_Send_Message {

    private int messageType;
    private int fromUserID;
    private int toUserID;
    private String text;
    private String roomID;

    public int getMessageType() { return messageType; }
    public void setMessageType(int messageType) { this.messageType = messageType; }

    public int getFromUserID() { return fromUserID; }
    public void setFromUserID(int fromUserID) { this.fromUserID = fromUserID; }

    public int getToUserID() { return toUserID; }
    public void setToUserID(int toUserID) { this.toUserID = toUserID; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getRoomID() { return roomID; }
    public void setRoomID(String roomID) { this.roomID = roomID; }

    // Chat_Bottom.java 호환용 생성자 (enum 수용)
    public Model_Send_Message(MessageType messageType, int fromUserID, int toUserID, String text) {
        this.messageType = messageType.getValue();
        this.fromUserID = fromUserID;
        this.toUserID = toUserID;
        this.text = text;
    }

    public Model_Send_Message() {
    }

    // 클라이언트 전용 정석 직렬화 메서드
    public JSONObject toJsonObject() {
        try {
            JSONObject json = new JSONObject();
            json.put("messageType", messageType);
            json.put("fromUserID", fromUserID);
            json.put("toUserID", toUserID);
            json.put("text", text);
            json.put("roomID", roomID);
            return json;
        } catch (JSONException e) {
            return null;
        }
    }
}