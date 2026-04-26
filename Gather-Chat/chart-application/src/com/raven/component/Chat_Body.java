package com.raven.component;

import com.raven.app.MessageType;
import com.raven.emoji.Emogi;
import com.raven.model.Model_Receive_Message;
import com.raven.model.Model_Send_Message;
import com.raven.swing.ScrollBar;
import java.awt.Adjustable;
import java.awt.Color;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.JScrollBar;
import net.miginfocom.swing.MigLayout;

public class Chat_Body extends javax.swing.JPanel {

    public Chat_Body() {
        initComponents();
        init();
    }

    private void init() {
        body.setLayout(new MigLayout("fillx, ins 0 30 0 30", "", "5[bottom]5"));
        sp.setVerticalScrollBar(new ScrollBar());
        sp.getVerticalScrollBar().setBackground(Color.WHITE);

        // 배경 이미지 적용을 위해 투명하게 설정 가능
        // body.setOpaque(false);
    }

    private String getCurrentFullDateTime() {
        return LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 a h:mm", Locale.KOREAN)
        );
    }

    // 상대방 메시지 추가
    public void addItemLeft(Model_Receive_Message data) {
        String fullTime = getCurrentFullDateTime();
        Chat_Left item = new Chat_Left();

        // 1. 서버에서 받은 '번호(ID)'로 '이름' 찾아오기
        // Main을 통해 Menu_Left에 접근해서 해당 ID의 이름을 가져옵니다.
        String userName = com.raven.main.Main.getMenuLeft().getUserNameById(data.getFromUserID());

        if (data.getMessageType() == MessageType.TEXT) {
            item.setText(data.getText());
        } else if (data.getMessageType() == MessageType.EMOJI) {
            item.setEmoji(Emogi.getInstance().getImoji(Integer.valueOf(data.getText())).getIcon());
        } else if (data.getMessageType() == MessageType.IMAGE) {
            item.setText("");
            item.setImage(data.getDataImage());
        }

        // 2. 공통 정보 세팅 (찾아온 userName을 직접 넣어줍니다)
        item.setUserProfile(userName);
        item.setTime(fullTime);

        body.add(item, "wrap, w 100::80%, gaptop 10");
        repaint();
        revalidate();
    }

    // 내가 보낸 메시지 추가
    public void addItemRight(Model_Send_Message data) {
        String fullTime = getCurrentFullDateTime();
        Chat_Right item = new Chat_Right();

        if (data.getMessageType() == MessageType.TEXT) {
            item.setText(data.getText());
        } else if (data.getMessageType() == MessageType.EMOJI) {
            item.setEmoji(Emogi.getInstance().getImoji(Integer.valueOf(data.getText())).getIcon());
        } else if (data.getMessageType() == MessageType.IMAGE) {
            item.setText("");
            item.setImage(data.getFile());
        }

        item.setTime(fullTime);
        body.add(item, "wrap, al right, w 100::80%");

        repaint();
        revalidate();
        scrollToBottom();
    }

    // 채팅창 초기화
    public void clearChat() {
        body.removeAll();
        repaint();
        revalidate();
    }

    // 날짜 구분선 추가
    public void addDate(String date) {
        Chat_Date item = new Chat_Date();
        item.setDate(date);
        body.add(item, "wrap, al center");
        body.repaint();
        body.revalidate();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        sp = new javax.swing.JScrollPane();
        body = new javax.swing.JPanel();

        sp.setBorder(null);
        sp.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        body.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout bodyLayout = new javax.swing.GroupLayout(body);
        body.setLayout(bodyLayout);
        bodyLayout.setHorizontalGroup(
                bodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 826, Short.MAX_VALUE)
        );
        bodyLayout.setVerticalGroup(
                bodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 555, Short.MAX_VALUE)
        );

        sp.setViewportView(body);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(sp)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(sp)
        );
    }
    // </editor-fold>

    private void scrollToBottom() {
        JScrollBar verticalBar = sp.getVerticalScrollBar();
        AdjustmentListener downScroller = new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                Adjustable adjustable = e.getAdjustable();
                adjustable.setValue(adjustable.getMaximum());
                verticalBar.removeAdjustmentListener(this);
            }
        };
        verticalBar.addAdjustmentListener(downScroller);
    }

    // Variables declaration - do not modify
    private javax.swing.JPanel body;
    private javax.swing.JScrollPane sp;
    // End of variables declaration
}