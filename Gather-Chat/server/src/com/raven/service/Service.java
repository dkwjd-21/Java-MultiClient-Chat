package com.raven.service;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.raven.model.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.swing.JTextArea;

public class Service {

    private static Service instance;
    private SocketIOServer server;
    private List<Model_Client> listClient;
    private JTextArea textArea;
    private final int PORT_NUMBER = 9999;
    private int nextUserID = 1;

    public static Service getInstance(JTextArea textArea) {
        if (instance == null) {
            instance = new Service(textArea);
        }
        return instance;
    }

    private Service(JTextArea textArea) {
        this.textArea = textArea;
        listClient = new ArrayList<>();
    }

    public void startServer() {
        Configuration config = new Configuration();
        config.setPort(PORT_NUMBER);
        server = new SocketIOServer(config);

        // [1] 클라이언트 연결 시
        server.addConnectListener(sioc -> textArea.append("새로운 클라이언트 접속\n"));

        // [2] 로그인 (닉네임으로 입장) 로직
        server.addEventListener("login", String.class, (sioc, userName, ar) -> {
            DatabaseManager db = DatabaseManager.getInstance();
            String cleanUserName = userName.replaceAll("[^가-힣a-zA-Z0-9]", "");

            if (db.isUsernameTaken(cleanUserName)) {
                ar.sendAckData(false, "FAIL_DUPLICATE");
                textArea.append("⚠ [시스템] 입장 거부 (닉네임 중복): " + cleanUserName + "\n");
                return;
            }

            int currentID = nextUserID++;
            Model_User_Account login = new Model_User_Account(currentID, cleanUserName, "", "", true);
            db.registerUser(String.valueOf(currentID), cleanUserName);

            ar.sendAckData(true, login);
            addClient(sioc, login);

            textArea.append("▶ [시스템] " + cleanUserName + "님이 채팅방에 입장하셨습니다.\n");
            broadcastUserList();
            userConnect(login.getUserID());
        });

        // [3] 접속자 명단 요청 시
        server.addEventListener("list_user", Integer.class, (sioc, userID, ar) -> {
            List<Model_User_Account> users = new ArrayList<>();
            for (Model_Client c : listClient) {
                users.add(c.getUser());
            }
            sioc.sendEvent("list_user", users.toArray());
        });

        // [3번 확장] 클라이언트가 요청한 새 방 이름을 DB에 INSERT 소켓 리스너
        server.addEventListener("create_room", String.class, (sioc, roomName, ar) -> {
            // 방 고유 ID는 타임스탬프 해시 등을 활용해 겹치지 않게 난수화 생성
            String uniqueRoomID = "ROOM_" + System.currentTimeMillis();

            try (Connection conn = DatabaseManager.getInstance().getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                         "INSERT INTO ROOMS (room_id, room_name, creator_id) VALUES (?, ?, ?)")) {

                pstmt.setString(1, uniqueRoomID);
                pstmt.setString(2, roomName);
                pstmt.setString(3, "USER"); // 임시 생성자 태그
                pstmt.executeUpdate();

                textArea.append("🚪 [ROOM] 신규 채팅방 개설 완료: " + roomName + " (" + uniqueRoomID + ")\n");
            } catch (Exception e) {
                System.out.println("[DB ERROR] 채팅방 개설 실패");
                e.printStackTrace();
            }
        });

        // [4] 단톡방 메시지 전송 리스너 내부 교체
        server.addEventListener("send_to_user", java.util.Map.class, (sioc, data, ar) -> {
            try {
                int messageType = Integer.parseInt(data.get("messageType").toString());
                int fromUserID = Integer.parseInt(data.get("fromUserID").toString());
                String text = data.get("text").toString();

                // roomID가 없으면 광장(SQUARE)으로 강제 고정
                String realRoomID = "SQUARE";
                if (data.containsKey("roomID") && data.get("roomID") != null) {
                    realRoomID = data.get("roomID").toString();
                }

                System.out.println("📩 [소켓 수신 통과] 보낸이 ID: " + fromUserID + " | 방 ID: " + realRoomID + " | 메시지: " + text);

                com.raven.model.Model_Receive_Message receiveMsg = new com.raven.model.Model_Receive_Message(
                        messageType,
                        fromUserID,
                        text,
                        realRoomID
                );
                server.getBroadcastOperations().sendEvent("receive_ms", receiveMsg);

                try (java.sql.Connection conn = DatabaseManager.getInstance().getConnection();
                     java.sql.PreparedStatement pstmt = conn.prepareStatement(
                             "INSERT INTO MESSAGES (room_id, from_user_id, message_text, timestamp) VALUES (?, ?, ?, datetime('now', 'localtime'))")) {

                    pstmt.setString(1, realRoomID);
                    pstmt.setString(2, String.valueOf(fromUserID));
                    pstmt.setString(3, text);

                    int result = pstmt.executeUpdate();
                    System.out.println("💾 [DB 인서트 최종 성공] 반영된 행 수: " + result + " | 저장된 내용: " + text);

                } catch (Exception dbEx) {
                    System.err.println("❌ [MESSAGES 테이블 인서트 최종 실패]");
                    dbEx.printStackTrace();
                }

            } catch (Exception e) {
                System.err.println("❌ [패킷 파싱 실패 및 데이터 에러]");
                e.printStackTrace();
            }
        });

        // [5] 연결 종료 시
        server.addDisconnectListener(sioc -> {
            int userID = removeClient(sioc);
            if (userID != 0) {
                userDisconnect(userID);
                broadcastUserList();
                textArea.append("◀ [시스템] 유저 퇴장 (ID: " + userID + ") -> 현재 접속자 수: " + listClient.size() + "명\n");
            }
        });

        server.start();
        textArea.append("Gather-Chat 서버가 포트 " + PORT_NUMBER + "에서 시작되었습니다.\n");

        startConsoleInput();

        // 방 목록 조회를 위한 HTTP REST API 서버 개설 (포트: 8080)
        try {
            HttpServer httpServer = HttpServer.create(new InetSocketAddress(8080), 0);

            // 주소 맵핑: http://localhost:8080/api/rooms
            httpServer.createContext("/api/rooms", (HttpExchange exchange) -> {
                List<Model_Room> rooms = DatabaseManager.getInstance().getRoomList();

                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < rooms.size(); i++) {
                    json.append(String.format("{\"roomID\":\"%s\",\"roomName\":\"%s\"}",
                            rooms.get(i).getRoomID(), rooms.get(i).getRoomName()));
                    if (i < rooms.size() - 1) json.append(",");
                }
                json.append("]");

                byte[] response = json.toString().getBytes("UTF-8");
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, response.length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response);
                }
            });

            httpServer.setExecutor(null);
            httpServer.start();
            textArea.append("▶ [REST API] HTTP 서버 가동 완료 (Port: 8080 / 경로: /api/rooms)\n");

        } catch (Exception e) {
            textArea.append("[REST ERROR] HTTP 서버 초기화 실패\n");
            e.printStackTrace();
        }
    }

    private void broadcastUserList() {
        List<Model_User_Account> users = new ArrayList<>();
        for (Model_Client c : listClient) {
            users.add(c.getUser());
        }
        server.getBroadcastOperations().sendEvent("list_user", users.toArray());
    }

    private void userConnect(int userID) {
        server.getBroadcastOperations().sendEvent("user_status", userID, true);
    }

    private void userDisconnect(int userID) {
        server.getBroadcastOperations().sendEvent("user_status", userID, false);
    }

    private void addClient(SocketIOClient client, Model_User_Account user) {
        listClient.add(new Model_Client(client, user));
    }

    public int removeClient(SocketIOClient client) {
        for (Model_Client d : listClient) {
            if (d.getClient() == client) {
                listClient.remove(d);
                return d.getUser().getUserID();
            }
        }
        return 0;
    }

    public List<Model_Client> getListClient() { return listClient; }

    private void startConsoleInput() {
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            System.out.println("[서버 콘솔] 명령어 모드 활성화 (/list 입력 가능)");
            while (true) {
                if (scanner.hasNextLine()) {
                    String input = scanner.nextLine();
                    executeCommand(input);
                }
            }
        }).start();
    }

    public void executeCommand(String command) {
        if (command.equalsIgnoreCase("/list")) {
            StringBuilder log = new StringBuilder("\n========== 현재 접속자 목록 (" + listClient.size() + "명) ==========\n");
            for (Model_Client c : listClient) {
                log.append("- ").append(c.getUser().getUserName()).append(" (ID: ").append(c.getUser().getUserID()).append(")\n");
            }
            log.append("============================================\n");

            textArea.append(log.toString());
            System.out.println(log.toString());
        } else if (command.equalsIgnoreCase("/clear")) {
            DatabaseManager.getInstance().clearTables();
            String clearLog = "\n[SYSTEM LOG] 관리자에 의해 DB 원장이 초기화되었습니다.\n";
            textArea.append(clearLog);
            System.out.println(clearLog);
        } else {
            System.out.println("알 수 없는 명령어입니다: " + command);
        }
    }
}