package com.raven.model;

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

    // 서버 호환용 레거시 생성자
    public Model_Send_Message(int messageType, int fromUserID, int toUserID, String text) {
        this.messageType = messageType;
        this.fromUserID = fromUserID;
        this.toUserID = toUserID;
        this.text = text;
    }

    public Model_Send_Message() {
    }
}