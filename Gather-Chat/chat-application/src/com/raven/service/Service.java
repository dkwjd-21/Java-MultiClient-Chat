package com.raven.service;

import com.raven.event.PublicEvent;
import com.raven.model.Model_Receive_Message;
import com.raven.model.Model_Room;
import com.raven.model.Model_User_Account;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * [Gather-Chat 클라이언트 서비스]
 * 서버와 직접 소켓 통신을 하며 데이터를 주고받는 안테나 역할을 수행
 */
public class Service {

    private static Service instance;
    private Socket client;
    private final int PORT_NUMBER = 9999;
    private final String IP = "localhost";
    private Model_User_Account user;

    public static Service getInstance() {
        if (instance == null) {
            instance = new Service();
        }
        return instance;
    }

    private Service() {
    }

    // 서버와의 소켓 연결을 시작하고 이벤트 리스너를 등록
    public void startServer() {
        try {
            client = IO.socket("http://" + IP + ":" + PORT_NUMBER);

            // [1] 서버로부터 전체 유저 명단을 받았을 때
            client.on("list_user", new Emitter.Listener() {
                @Override
                public void call(Object... os) {
                    try {
                        List<Model_User_Account> users = new ArrayList<>();
                        for (Object obj : os) {
                            users.add(new Model_User_Account(obj));
                        }

                        java.awt.EventQueue.invokeLater(() -> {
                            PublicEvent.getInstance().getEventMenuLeft().newUser(users);
                        });

                        System.out.println("로그 [Service]: 서버로부터 명단 " + users.size() + "명 수신 완료!");
                    } catch (Exception e) {
                        System.err.println("로그 [Service]: 명단 수신 에러 -> " + e.getMessage());
                    }
                }
            });

            // [2] 특정 유저의 온라인/오프라인 상태가 변경되었을 때
            client.on("user_status", new Emitter.Listener() {
                @Override
                public void call(Object... os) {
                    int userID = (Integer) os[0];
                    boolean status = (Boolean) os[1];
                    if (status) {
                        PublicEvent.getInstance().getEventMenuLeft().userConnect(userID);
                    } else {
                        PublicEvent.getInstance().getEventMenuLeft().userDisconnect(userID);
                    }
                }
            });

            // [3] 서버로부터 새로운 채팅 메시지를 받았을 때
            client.on("receive_ms", new Emitter.Listener() {
                @Override
                public void call(Object... os) {
                    Model_Receive_Message message = new Model_Receive_Message(os[0]);

                    java.awt.EventQueue.invokeLater(() -> {
                        PublicEvent.getInstance().getEventChat().receiveMessage(message);
                    });
                }
            });

            client.open();
        } catch (URISyntaxException e) {
            error(e);
        }
    }

    // 수신 함수
    public List<Model_Room> getRoomListFromREST() {
        List<Model_Room> roomList = new ArrayList<>();
        try {
            URL url = new URL("http://localhost:8080/api/rooms");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) response.append(line);

                    String jsonStr = response.toString();

                    // JSON 데이터 쪼개서 진짜 roomID와 roomName을 정밀 타격 추출
                    String[] tokens = jsonStr.split("\\{\"" + "roomID\":\"");
                    for (int i = 1; i < tokens.length; i++) {
                        String id = tokens[i].split("\",\"roomName\":\"")[0];
                        String name = tokens[i].split("\",\"roomName\":\"")[1].split("\"")[0];
                        roomList.add(new Model_Room(id, name));
                    }
                }
            }
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return roomList;
    }

    // 과거 대화 내역을 정렬해서 가져오는 REST API 호출 함수
    public List<String[]> getChatHistoryFromREST(String roomID) {
        List<String[]> list = new ArrayList<>();
        try {
            // 서버의 REST API 메시지 조회 엔드포인트 찌르기
            URL url = new URL("http://localhost:8080/api/rooms/" + roomID + "/messages");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) response.append(line);

                    String jsonStr = response.toString();
                    String[] tokens = jsonStr.split("\\{\"" + "fromUserID\":");

                    for (int i = 1; i < tokens.length; i++) {
                        String fromUser = tokens[i].split(",")[0].trim();
                        String text = tokens[i].split("\"text\":\"")[1].split("\"")[0];

                        list.add(new String[]{fromUser, text});
                    }
                }
            }
            conn.disconnect();
        } catch (Exception e) {
            System.err.println("[REST CLIENT ERROR] 과거 메시지 내역 로드 실패: " + e.getMessage());
        }
        return list;
    }

    public Socket getClient() {
        return client;
    }

    public Model_User_Account getUser() {
        return user;
    }

    public void setUser(Model_User_Account user) {
        this.user = user;
    }

    private void error(Exception e) {
        System.err.println("통신 에러 발생: " + e);
    }
}