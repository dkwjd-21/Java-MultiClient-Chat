package com.raven.service;


import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.raven.model.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTextArea;

public class Service {

    // 싱글톤 패턴을 위한 인스턴스 변수
    private static Service instance;
    // Socket.io 서버 객체
    private SocketIOServer server;
    // 현재 접속 중인 클라이언트 명단 (메모리 DB 역할)
    private List<Model_Client> listClient;
    // 서버 관리창에 로그를 찍기 위한 컴포넌트
    private JTextArea textArea;
    // 서버가 사용할 포트 번호
    private final int PORT_NUMBER = 9999;
    // 입장순으로 부여할 ID
    private int nextUserID = 1;

    public static Service getInstance(JTextArea textArea) {
        if (instance == null) {
            instance = new Service(textArea);
        }
        return instance;
    }

    private Service(JTextArea textArea) {
        this.textArea = textArea;
        // 메모리 세션 리스트 초기화
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
            // DB 조회 대신, 닉네임만 가지고 유저 객체 생성
            Model_User_Account login = new Model_User_Account(nextUserID++, userName, "", "", true);

            ar.sendAckData(true, login); // 클라이언트에게 성공 알림
            addClient(sioc, login);      // 메모리 리스트에 추가

            // [로그 보강] 관리자 창에 "ㅇㅇ님이 입장함" 출력
            textArea.append("▶ [시스템] " + userName + "님이 채팅방에 입장하셨습니다.\n");

            // 입장하자마자 현재 접속자 명단을 다시 뿌려줌
            broadcastUserList();
            userConnect(login.getUserID());
        });

        // [3] 접속자 명단 요청 시
        server.addEventListener("list_user", Integer.class, (sioc, userID, ar) -> {
            List<Model_User_Account> users = new ArrayList<>();
            for (Model_Client c : listClient) {
                // 모든 접속자를 리스트에 다 담아서 보냄
                users.add(c.getUser());
            }
            sioc.sendEvent("list_user", users.toArray());
        });

        // [4] 1:다 단톡방 메시지 전송
        server.addEventListener("send_to_user", Model_Send_Message.class, (sioc, t, ar) -> {
            // 1:1이 아닌 브로드캐스팅으로 변경 (나를 제외한 모두에게 전송)
            Model_Receive_Message receiveMsg = new Model_Receive_Message(t.getMessageType(), t.getFromUserID(), t.getText(), null);

            // 모든 접속자에게 메시지 발송 (Broadcasting)
            server.getBroadcastOperations().sendEvent("receive_ms", receiveMsg);

            textArea.append("[메시지] " + t.getFromUserID() + ": " + t.getText() + "\n");
        });

        // [5] 연결 종료 시 (퇴장 로그 보강)
        server.addDisconnectListener(sioc -> {
            int userID = removeClient(sioc);
            if (userID != 0) {
                userDisconnect(userID);
                broadcastUserList(); // 나갔으니까 명단 갱신

                // 서버 로그에 퇴장 인원 출력
                textArea.append("◀ [시스템] 유저 퇴장 (ID: " + userID + ") -> 현재 접속자 수: " + listClient.size() + "명\n");
            }
        });

        server.start();
        textArea.append("Gather-Chat 서버가 포트 " + PORT_NUMBER + "에서 시작되었습니다.\n");

        // 콘솔 명령어 입력 스레드 시작
        startConsoleInput();
    }

    // 모든 클라이언트에게 최신 유저 리스트를 뿌려주는 메소드
    private void broadcastUserList() {
        List<Model_User_Account> users = new ArrayList<>();
        for (Model_Client c : listClient) {
            users.add(c.getUser());
        }
        server.getBroadcastOperations().sendEvent("list_user", users.toArray());
    }

    // 특정 유저가 온라인이 되었음을 브로드캐스팅 알림
    private void userConnect(int userID) {
        server.getBroadcastOperations().sendEvent("user_status", userID, true);
    }

    // 특정 유저가 오프라인이 되었음을 브로드캐스팅 알림
    private void userDisconnect(int userID) {
        server.getBroadcastOperations().sendEvent("user_status", userID, false);
    }

    // 클라이언트 소켓과 유저 정보를 매핑하여 리스트에 추가
    private void addClient(SocketIOClient client, Model_User_Account user) {
        listClient.add(new Model_Client(client, user));
    }

    // 연결이 끊긴 클라이언트를 리스트에서 찾아 제거하고 해당 ID를 반환
    public int removeClient(SocketIOClient client) {
        for (Model_Client d : listClient) {
            if (d.getClient() == client) {
                listClient.remove(d);
                return d.getUser().getUserID();
            }
        }
        return 0;
    }

    // 현재 접속자 리스트를 반환
    public List<Model_Client> getListClient() { return listClient; }

    // 인텔리제이 콘솔에서 직접 입력받는 스레드
    private void startConsoleInput() {
        new Thread(() -> {
            java.util.Scanner scanner = new java.util.Scanner(System.in);
            System.out.println("[서버 콘솔] 명령어 모드 활성화 (/list 입력 가능)");
            while (true) {
                if (scanner.hasNextLine()) {
                    String input = scanner.nextLine();
                    executeCommand(input);
                }
            }
        }).start();
    }

    // 명령어 실행 로직
    public void executeCommand(String command) {
        if (command.equalsIgnoreCase("/list")) {
            String log = "\n========== 현재 접속자 목록 (" + listClient.size() + "명) ==========\n";
            for (Model_Client c : listClient) {
                log += "- " + c.getUser().getUserName() + " (ID: " + c.getUser().getUserID() + ")\n";
            }
            log += "============================================\n";

            textArea.append(log); // GUI에 출력
            System.out.println(log); // 콘솔에도 출력
        } else {
            System.out.println("알 수 없는 명령어입니다: " + command);
        }
    }
}