package com.raven.form;

import com.raven.component.Chat_Body;
import com.raven.component.Chat_Bottom;
import com.raven.component.Chat_Title;
import com.raven.event.EventChat;
import com.raven.event.PublicEvent;
import com.raven.model.Model_Receive_Message;
import com.raven.model.Model_Send_Message;
import com.raven.model.Model_User_Account;
import com.raven.service.Service;
import net.miginfocom.swing.MigLayout;
import java.util.List;

public class Chat extends javax.swing.JPanel {

    private Chat_Title chatTitle;
    private Chat_Body chatBody;
    private Chat_Bottom chatBottom;
    private Model_User_Account currentRoom;

    public Chat() {
        initComponents();
        init();
    }

    private void init() {
        setLayout(new MigLayout("fillx", "0[fill]0", "0[]0[100%, fill]0[shrink 0]0"));
        chatTitle = new Chat_Title();
        chatBody = new Chat_Body();
        chatBottom = new Chat_Bottom();

        PublicEvent.getInstance().addEventChat(new EventChat() {
            @Override
            public void sendMessage(Model_Send_Message data) {
                chatBody.addItemRight(data);
            }

            @Override
            public void receiveMessage(Model_Receive_Message data) {
                if (data.getFromUserID() != Service.getInstance().getUser().getUserID()) {
                    if (currentRoom != null && String.valueOf(currentRoom.getImage()).equals(data.getRoomID())) {
                        chatBody.addItemLeft(data);
                        chatBody.revalidate();
                        chatBody.repaint();
                    }
                }
            }
        });

        add(chatTitle, "wrap");
        add(chatBody, "wrap");
        add(addChatBottom(), "h ::50%");
    }

    public void setUser(Model_User_Account user) {
        this.currentRoom = user;

        chatTitle.updateUser(user);
        chatBottom.setUser(user);

        // 1. 화면 일단 초기화
        chatBody.clearChat();

        // 아직 내 로그인 정보(User)가 세팅 안 됐다면, 과거 내역 복원을 건너뛰고 리턴
        if (Service.getInstance().getUser() == null) {
            chatBody.revalidate();
            chatBody.repaint();
            return;
        }

        // 2. [신규 파이프라인] 유리가 선택한 이 방의 고유 ID를 기반으로 REST API 통신 실행!
        String targetRoomID = (user.getImage() != null && !user.getImage().isEmpty()) ? user.getImage() : "SQUARE";

        try {
            // 서버의 REST 엔드포인트로부터 정렬된 내역 호출 명세 실행
            List<String[]> history = Service.getInstance().getChatHistoryFromREST(targetRoomID);
            int myUid = Service.getInstance().getUser().getUserID();

            for (String[] msg : history) {
                int fromUid = Integer.parseInt(msg[0]);
                String text = msg[1];

                if (fromUid == myUid) {
                    Model_Send_Message legacySend = new Model_Send_Message(com.raven.app.MessageType.TEXT, myUid, 0, text);
                    chatBody.addItemRight(legacySend);
                } else {
                    Model_Receive_Message legacyRecv = new Model_Receive_Message(com.raven.app.MessageType.TEXT, fromUid, text, targetRoomID);
                    chatBody.addItemLeft(legacyRecv);
                }
            }
        } catch (Exception e) {
            System.err.println("[REST ERROR] 과거 대화 내용 복원 프로세스 실패");
            e.printStackTrace();
        }

        chatBody.revalidate();
        chatBody.repaint();
    }

    private javax.swing.JPanel addChatBottom() {
        return chatBottom;
    }

    public void updateUser(Model_User_Account user) {
        chatTitle.updateUser(user);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {
        setBackground(new java.awt.Color(255, 255, 255));
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 727, Short.MAX_VALUE));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 681, Short.MAX_VALUE));
    }// </editor-fold>
}