package com.raven.model;

public class Model_Room {
    private String roomID;
    private String roomName;

    public Model_Room(String roomID, String roomName) {
        this.roomID = roomID;
        this.roomName = roomName;
    }

    public String getRoomID() { return roomID; }
    public String getRoomName() { return roomName; }
}