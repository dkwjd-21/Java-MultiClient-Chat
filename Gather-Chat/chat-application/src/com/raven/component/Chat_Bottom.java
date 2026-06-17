package com.raven.component;

import com.raven.app.MessageType;
import com.raven.event.PublicEvent;
import com.raven.model.Model_Send_Message;
import com.raven.model.Model_User_Account;
import com.raven.service.Service;
import com.raven.swing.JIMSendTextPane;
import com.raven.swing.ScrollBar;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import net.miginfocom.swing.MigLayout;

public class Chat_Bottom extends javax.swing.JPanel {

    public Model_User_Account getUser() { return user; }

    public void setUser(Model_User_Account user) {
        this.user = user;
        panelMore.setUser(user);

        if (user != null && user.getImage() != null && !user.getImage().isEmpty()) {
            this.currentActiveRoomID = user.getImage();
        } else {
            this.currentActiveRoomID = "SQUARE"; // 기본 광장
        }
    }

    private Model_User_Account user;
    private MigLayout mig;
    private Panel_More panelMore;
    private String currentActiveRoomID = "SQUARE";

    public void setActiveRoomID(String roomID) {
        this.currentActiveRoomID = roomID;
    }

    public Chat_Bottom() {
        initComponents();
        init();
    }

    private void init() {
        mig = new MigLayout("fillx, filly", "0[fill]0[]0[]2", "2[fill]2[]0");
        setLayout(mig);
        JScrollPane scroll = new JScrollPane();
        scroll.setBorder(null);
        JIMSendTextPane txt = new JIMSendTextPane();
        txt.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent ke) {
                refresh();
                if (ke.getKeyChar() == 10 && ke.isControlDown()) {
                    eventSend(txt);
                }
            }
        });
        txt.setBorder(new EmptyBorder(5, 5, 5, 5));
        txt.setHintText("Write Message Here ...");
        scroll.setViewportView(txt);
        ScrollBar sb = new ScrollBar();
        sb.setBackground(new Color(229, 229, 229));
        sb.setPreferredSize(new Dimension(2, 10));
        scroll.setVerticalScrollBar(sb);
        add(sb);
        add(scroll, "w 100%");
        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("filly", "0[]3[]0", "0[bottom]0"));
        panel.setPreferredSize(new Dimension(30, 28));
        panel.setBackground(Color.WHITE);
        JButton cmd = new JButton();
        cmd.setBorder(null);
        cmd.setContentAreaFilled(false);
        cmd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cmd.setIcon(new ImageIcon(getClass().getResource("/com/raven/icon/send.png")));
        cmd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                eventSend(txt);
            }
        });
        JButton cmdMore = new JButton();
        cmdMore.setBorder(null);
        cmdMore.setContentAreaFilled(false);
        cmdMore.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cmdMore.setIcon(new ImageIcon(getClass().getResource("/com/raven/icon/more_disable.png")));
        cmdMore.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (panelMore.isVisible()) {
                    cmdMore.setIcon(new ImageIcon(getClass().getResource("/com/raven/icon/more_disable.png")));
                    panelMore.setVisible(false);
                    mig.setComponentConstraints(panelMore, "dock south,h 0!");
                    revalidate();
                } else {
                    cmdMore.setIcon(new ImageIcon(getClass().getResource("/com/raven/icon/more.png")));
                    panelMore.setVisible(true);
                    mig.setComponentConstraints(panelMore, "dock south,h 170!");
                    revalidate();
                }
            }
        });
        panel.add(cmdMore);
        panel.add(cmd);
        add(panel, "wrap");
        panelMore = new Panel_More();
        panelMore.setVisible(false);
        add(panelMore, "dock south,h 0!");
    }

    private String filterBadWords(String rawText) {
        if (rawText == null) return "";
        String[] badWords = {"바보", "멍청이", "뚱땅이", "킹받네", "개짜증"};
        String filteredText = rawText;
        for (String word : badWords) {
            if (filteredText.contains(word)) {
                StringBuilder hearts = new StringBuilder();
                for (int i = 0; i < word.length(); i++) {
                    hearts.append("♡");
                }
                filteredText = filteredText.replace(word, hearts.toString());
            }
        }
        return filteredText;
    }

    private void eventSend(com.raven.swing.JIMSendTextPane txt) {
        String text = txt.getText().trim();
        if (!text.equals("")) {
            String cleanText = text;
            try {
                java.lang.reflect.Method filterMethod = this.getClass().getDeclaredMethod("filterBadWords", String.class);
                filterMethod.setAccessible(true);
                cleanText = (String) filterMethod.invoke(this, text);
            } catch (Exception e) {
            }

            org.json.JSONObject json = new org.json.JSONObject();
            try {
                json.put("messageType", com.raven.app.MessageType.TEXT.getValue()); // int 값(1) 주입
                json.put("fromUserID", Service.getInstance().getUser().getUserID());
                json.put("toUserID", 0);
                json.put("text", cleanText);

                String currentRoomID = "SQUARE";
                if (user != null && user.getImage() != null && !user.getImage().isEmpty()) {
                    currentRoomID = user.getImage();
                }
                json.put("roomID", currentRoomID);

            } catch (org.json.JSONException e) {
                e.printStackTrace();
            }

            if (Service.getInstance().getClient() != null && Service.getInstance().getClient().connected()) {
                Service.getInstance().getClient().emit("send_to_user", json);
                System.out.println("🚀 [클라이언트 소켓 방출 성공] 데이터: " + json.toString());
            } else {
                System.err.println("❌ [클라이언트 에러] 서버 소켓과 연결되어 있지 않습니다!");
            }

            com.raven.model.Model_Send_Message message = new com.raven.model.Model_Send_Message(
                    com.raven.app.MessageType.TEXT,
                    Service.getInstance().getUser().getUserID(),
                    0,
                    cleanText
            );
            if (user != null && user.getImage() != null) {
                message.setRoomID(user.getImage());
            }

            PublicEvent.getInstance().getEventChat().sendMessage(message);
            txt.setText("");
            txt.grabFocus();
            refresh();
        }
    }

    private void refresh() {
        revalidate();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {
        setBackground(new java.awt.Color(229, 229, 229));
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 400, Short.MAX_VALUE));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 40, Short.MAX_VALUE));
    }// </editor-fold>
}