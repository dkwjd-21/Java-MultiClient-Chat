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

        chatBody.clearChat();

        // 아직 로그인 정보가 세팅 안 됐다면 복원 패스
        if (Service.getInstance().getUser() == null) {
            chatBody.revalidate();
            chatBody.repaint();
            return;
        }

        // 선택한 이 방의 고유 ID 추출 (비어있으면 기본 광장인 SQUARE)
        String targetRoomID = (user.getImage() != null && !user.getImage().isEmpty()) ? user.getImage() : "SQUARE";

        try {
            // 서버의 REST API로부터 이 방의 과거 대화 리스트 수신 [[fromUserID, text], [fromUserID, text], ...]
            List<String[]> history = Service.getInstance().getChatHistoryFromREST(targetRoomID);

            // 현재 로그인한 '유저'의 ID 가져오기
            int myUid = Service.getInstance().getUser().getUserID();

            // 내역을 한 줄씩 돌면서 내가 보낸 건지, 남이 보낸 건지 판단해서 정렬 렌더링
            for (String[] msg : history) {
                int fromUid = Integer.parseInt(msg[0]); // 메시지를 보낸 사람의 ID
                String text = msg[1];                   // 메시지 내용

                if (fromUid == myUid) {
                    // 🔵 내가 보낸 메시지라면 -> 대화창 우측(Right)에 정렬 렌더링
                    Model_Send_Message legacySend = new Model_Send_Message(
                            com.raven.app.MessageType.TEXT,
                            myUid,
                            0,
                            text
                    );
                    chatBody.addItemRight(legacySend);
                } else {
                    // 🟢 상대방이 보낸 메시지라면 -> 대화창 좌측(Left)에 정렬 렌더링
                    Model_Receive_Message legacyRecv = new Model_Receive_Message(
                            com.raven.app.MessageType.TEXT,
                            fromUid,
                            text,
                            targetRoomID
                    );
                    chatBody.addItemLeft(legacyRecv);
                }
            }
        } catch (Exception e) {
            System.err.println("[REST ERROR] 과거 대화 내용 복원 프로세스 실패");
            e.printStackTrace();
        }

        // UI 새로고침 반영
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