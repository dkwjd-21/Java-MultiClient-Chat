package com.raven.model;

import com.raven.app.MessageType;
import org.json.JSONException;
import org.json.JSONObject;

public class Model_Receive_Message {

    private MessageType messageType;
    private int fromUserID;
    private String text;
    private Model_Receive_Image dataImage;
    private String roomID;

    public MessageType getMessageType() { return messageType; }
    public void setMessageType(MessageType messageType) { this.messageType = messageType; }

    public int getFromUserID() { return fromUserID; }
    public void setFromUserID(int fromUserID) { this.fromUserID = fromUserID; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Model_Receive_Image getDataImage() { return dataImage; }
    public void setDataImage(Model_Receive_Image dataImage) { this.dataImage = dataImage; }

    public String getRoomID() { return roomID; }
    public void setRoomID(String roomID) { this.roomID = roomID; }

    public Model_Receive_Message(Object json) {
        JSONObject obj = (JSONObject) json;
        try {
            messageType = MessageType.toMessageType(obj.getInt("messageType"));
            fromUserID = obj.getInt("fromUserID");
            text = obj.getString("text");

            if (obj.has("roomID")) {
                roomID = obj.getString("roomID");
            } else {
                roomID = "SQUARE"; // 기본값
            }

            if (!obj.isNull("dataImage")) {
                dataImage = new Model_Receive_Image(obj.get("dataImage"));
            }
        } catch (JSONException e) {
            System.err.println(e);
        }
    }

    // JSON 복원 생성자 추가 (REST 내역 드로잉 연동용)
    public Model_Receive_Message(MessageType messageType, int fromUserID, String text, String roomID) {
        this.messageType = messageType;
        this.fromUserID = fromUserID;
        this.text = text;
        this.roomID = roomID;
    }

    public JSONObject toJsonObject() {
        try {
            JSONObject json = new JSONObject();
            json.put("messageType", messageType.getValue());
            json.put("fromUserID", fromUserID);
            json.put("text", text);
            json.put("roomID", roomID);
            if (dataImage != null) {
                json.put("dataImage", dataImage.toJsonObject());
            }
            return json;
        } catch (JSONException e) {
            return null;
        }
    }
}