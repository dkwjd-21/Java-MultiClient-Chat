package com.raven.form;

import com.raven.model.Model_User_Account;
import net.miginfocom.swing.MigLayout;

public class Home extends javax.swing.JLayeredPane {

    private Chat chat;
    private Menu_Left menuLeft;

    public Home() {
        initComponents();
        init();
    }

    private void init() {
        // 레이아웃 설정
        // 왼쪽 메뉴 200px 고정, 채팅창 600px 고정, 나머지는 빈 공간(grow)
        setLayout(new MigLayout("fill, insets 10", "0[200!]15[600!]0[grow]", "0[fill]0"));

        menuLeft = new Menu_Left();
        this.add(menuLeft);

        // 채팅창 추가
        chat = new Chat();
        this.add(chat);

        // [수정] 입장하자마자 광장 채팅방이 보이도록 설정
        Model_User_Account community = new Model_User_Account(0, "Gather-Chat 광장", "", "", true);
        setUser(community);
    }

    public void setUser(Model_User_Account user) {
        chat.setUser(user);
        chat.setVisible(true);
    }

    public void updateUser(Model_User_Account user) {
        chat.updateUser(user);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1007, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 551, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    public Menu_Left getMenuLeft() {
        return menuLeft; // GUI 빌더가 생성한 Menu_Left 변수명 (보통 menu_Left1 일 거예요)
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
