package com.raven.main;

import com.raven.app.MessageType;
import com.raven.event.PublicEvent;
import com.raven.model.Model_Send_Message;
import com.raven.service.Service;
import java.awt.EventQueue;

public class TestLauncher {

    public static void main(String[] args) {
        // [1] 봇의 닉네임 설정
        String botName = "광장_테스트봇";

        // [2] Main의 자동 로그인 기능을 활용해 창 띄우기
        Main.TEST_NAME = botName;

        EventQueue.invokeLater(() -> {
            Main frame = new Main();
            frame.setVisible(true);

            // 봇 창은 구석에 배치 (확인용)
            frame.setLocation(10, 10);
            frame.setTitle(botName + " (자동 발신 중...)");

            // [3] 창이 뜨고 서비스가 초기화된 후 메시지 발송 시작
            startBotBehavior(botName);
        });
    }

    private static void startBotBehavior(String botName) {
        new Thread(() -> {
            try {
                // 로그인 처리가 완료될 때까지 대기
                Thread.sleep(3000);

                int myRealID = Service.getInstance().getUser().getUserID();

                String[] randomMessages = {
                        "안녕하세요! 실시간 동기화 테스트 중입니다. ✨",
                        "숭실대 글로벌미디어학부 프로젝트 화이팅! 🔥",
                        "ㄹㅇㅋㅋ",
                        "제 메시지 잘 보이시나요?"
                };

                while (true) {
                    Thread.sleep((long) (Math.random() * 3000) + 3000);

                    Model_Send_Message data = new Model_Send_Message();
                    data.setMessageType(MessageType.TEXT);
                    data.setToUserID(0);
                    data.setFromUserID(myRealID); // 내 ID 주입
                    data.setText(randomMessages[(int)(Math.random() * randomMessages.length)]);

                    // [1] 서버로 전송
                    Service.getInstance().getClient().emit("send_to_user", data.toJsonObject());

                    // [2] 내 화면에도 강제로 그리기
                    java.awt.EventQueue.invokeLater(() -> {
                        if (PublicEvent.getInstance().getEventChat() != null) {
                            PublicEvent.getInstance().getEventChat().sendMessage(data);
                        }
                    });
                    System.out.println("🤖 발송 완료: " + data.getText());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}