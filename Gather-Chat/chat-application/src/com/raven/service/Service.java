package com.raven.service;

import com.raven.event.PublicEvent;
import com.raven.model.Model_Receive_Message;
import com.raven.model.Model_User_Account;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
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
    private final int PORT_NUMBER = 9999; // 서버와 동일한 포트
    private final String IP = "localhost"; // 로컬 테스트용 IP
    private Model_User_Account user; // 현재 내 계정 정보

    public static Service getInstance() {
        if (instance == null) {
            instance = new Service();
        }
        return instance;
    }

    private Service() {
        // 파일 전송 기능은 사용하지 않으므로 리스트 초기화 제거
    }

    // 서버와의 소켓 연결을 시작하고 이벤트 리스너를 등록
    public void startServer() {
        try {
            client = IO.socket("http://" + IP + ":" + PORT_NUMBER);

            // [1] 서버로부터 전체 유저 명단을 받았을 때
            client.on("list_user", new io.socket.emitter.Emitter.Listener() {
                @Override
                public void call(Object... os) {
                    try {
                        List<Model_User_Account> users = new ArrayList<>();
                        // 서버가 보낸 명단 데이터를 하나씩 꺼내서 리스트에 담기
                        for (Object obj : os) {
                            users.add(new Model_User_Account(obj));
                        }

                        // Menu_Left에게 명단이 왔다고 알려줌
                        java.awt.EventQueue.invokeLater(() -> {
                            com.raven.event.PublicEvent.getInstance().getEventMenuLeft().newUser(users);
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
                        // 온라인 표시
                        PublicEvent.getInstance().getEventMenuLeft().userConnect(userID);
                    } else {
                        // 오프라인 표시
                        PublicEvent.getInstance().getEventMenuLeft().userDisconnect(userID);
                    }


                }
            });

            // [3] 서버로부터 새로운 채팅 메시지를 받았을 때
            client.on("receive_ms", new io.socket.emitter.Emitter.Listener() {
                @Override
                public void call(Object... os) {
                    // 서버가 쏜 데이터를 모델로 변환 (유리님 모델 생성자 활용)
                    Model_Receive_Message message = new Model_Receive_Message(os[0]);

                    // UI 스레드에서 실행
                    java.awt.EventQueue.invokeLater(() -> {
                        PublicEvent.getInstance().getEventChat().receiveMessage(message);
                    });
                }
            });

            client.open(); // 소켓 연결 실제 개시
        } catch (URISyntaxException e) {
            error(e);
        }
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