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

public class Chat extends javax.swing.JPanel {

    private Chat_Title chatTitle;
    private Chat_Body chatBody;
    private Chat_Bottom chatBottom;

    public Chat() {
        initComponents();
        init();
    }

    private void init() {
        // 레이아웃 설정: 타이틀(상단), 바디(중앙), 바텀(하단 입력창)
        setLayout(new MigLayout("fillx", "0[fill]0", "0[]0[100%, fill]0[shrink 0]0"));
        chatTitle = new Chat_Title();
        chatBody = new Chat_Body();
        chatBottom = new Chat_Bottom();

        // 채팅 이벤트 리스너 등록
        PublicEvent.getInstance().addEventChat(new EventChat() {
            @Override
            public void sendMessage(Model_Send_Message data) {
                // 내가 보낸 메시지를 화면 오른쪽에 추가
                chatBody.addItemRight(data);
            }

            @Override
            public void receiveMessage(Model_Receive_Message data) {
                // [중요] 남이 보낸 거라면 무조건 왼쪽에 그리기!
                if (data.getFromUserID() != Service.getInstance().getUser().getUserID()) {
                    chatBody.addItemLeft(data);
                    chatBody.revalidate();
                    chatBody.repaint();
                }
            }
        });

        add(chatTitle, "wrap");
        add(chatBody, "wrap");
        add(chatBottom, "h ::50%");
    }

     // 유저를 선택했을 때 호출되는 메소드
     // (단톡방 모드에서는 'Gather-Chat 전체 대화방' 등으로 타이틀을 고정할 수 있습니다)
    public void setUser(Model_User_Account user) {
        chatTitle.setUserName(user);
        chatBottom.setUser(user);
        // 새로운 방에 들어왔을 때 이전 대화내역 삭제 (필요 시)
        // chatBody.clearChat();
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