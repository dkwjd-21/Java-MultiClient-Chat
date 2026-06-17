package com.raven.main;

import com.formdev.flatlaf.intellijthemes.FlatArcIJTheme;
import com.raven.event.EventImageView;
import com.raven.event.EventMain;
import com.raven.event.PublicEvent;
import com.raven.form.Menu_Left;
import com.raven.model.Model_User_Account;
import com.raven.service.Service;
import com.raven.swing.ComponentResizer;
import io.socket.client.Ack;

import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class Main extends javax.swing.JFrame {

    private static Main instance;

    // 테스트용 고정 이름 변수
    public static String TEST_NAME = "";

    public Main() {
        instance = this;
        initComponents();
        init();
    }

    public static Menu_Left getMenuLeft() {
        return instance.home.getMenuLeft(); // home 안에 있는 getMenuLeft() 호출
    }

    private void init() {
        setIconImage(new ImageIcon(getClass().getResource("/com/raven/icon/icon.png")).getImage());

        // [1] 리사이저 설정 보강
        ComponentResizer com = new ComponentResizer();
        com.registerComponent(this);
        com.setMinimumSize(new Dimension(800, 600));
        com.setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());
        com.setSnapSize(new Dimension(10, 10));

        // [2] 화면 초기 상태 설정
        login.setVisible(false);
        loading.setVisible(false);
        vIew_Image.setVisible(false);
        home.setVisible(true);

        initEvent();

        // [3] 닉네임 입력 및 로그인

        String userName;

        if (TEST_NAME != null && !TEST_NAME.trim().isEmpty()) {
            // 외부(TestLauncher 등)에서 이름을 미리 지정한 경우 팝업 없이 진행
            userName = TEST_NAME;
            System.out.println("로그: 테스트 모드 자동 입장 (" + userName + ")");
            TEST_NAME = ""; // 다음 인스턴스를 위해 초기화
        } else {
            // 일반 실행 시 팝업창 띄우기
            userName = javax.swing.JOptionPane.showInputDialog(this,
                    "Gather-Chat에 오신 것을 환영합니다!\n사용하실 닉네임을 입력해주세요.",
                    "입장하기",
                    javax.swing.JOptionPane.PLAIN_MESSAGE);
        }

        // 공백이나 취소 클릭 시 익명 처리
        if (userName == null || userName.trim().isEmpty()) {
            userName = "익명유저" + (int)(Math.random() * 1000);
        }

        Model_User_Account user = new Model_User_Account(0, userName, "", "", true);
        Service.getInstance().setUser(user);

        // [4] 서버 연결
        Service.getInstance().startServer();

        // 서버의 수락/거절 응답(Ack) 판별
        Service.getInstance().getClient().emit("login", userName, (Ack) os -> {
            if (os.length > 0 && (boolean) os[0]) {
                // [성공 케이스] 중복 통과 시 정상 진입 프로세스 실행
                Model_User_Account loginUser = new Model_User_Account(os[1]);
                Service.getInstance().setUser(loginUser);

                java.awt.EventQueue.invokeLater(() -> {
                    // 로그인 성공 직후 서버에 접속유저 리스트 요청 및 채팅창 이니셜라이즈
                    Service.getInstance().getClient().emit("list_user", loginUser.getUserID());
                    PublicEvent.getInstance().getEventMain().initChat();

                    Model_User_Account groupChat = new Model_User_Account(0, "Gather-Chat 광장", "", "", true);
                    home.setUser(groupChat);
                });
            } else {
                // [실패 케이스] 중복 걸렸을 때 처리
                String reason = os.length > 1 ? os[1].toString() : "";

                if ("FAIL_DUPLICATE".equals(reason)) {
                    // UX 디테일: 유저에게 중복 알림 팝업 발생 후 프로그램 종료 또는 재입장 유도
                    java.awt.EventQueue.invokeLater(() -> {
                        javax.swing.JOptionPane.showMessageDialog(this,
                                "이미 존재하는 닉네임입니다!\n다른 닉네임으로 다시 로그인해 주세요.",
                                "입장 거부",
                                javax.swing.JOptionPane.WARNING_MESSAGE);

                        // 현재 창 닫고 프로세스 정리 (또는 원할 경우 복귀 설계 가능)
                        System.exit(0);
                    });
                }
            }
        });

        this.setSize(new Dimension(850, 600));
        this.revalidate();
    }

    private void initEvent() {
        PublicEvent.getInstance().addEventMain(new EventMain() {
            @Override
            public void showLoading(boolean show) {
                loading.setVisible(show);
            }

            @Override
            public void initChat() {
                home.setVisible(true);
                login.setVisible(false);
                Service.getInstance().getClient().emit("list_user", Service.getInstance().getUser().getUserID());
            }

            @Override
            public void selectUser(Model_User_Account user) {
                home.setUser(user);
            }

            @Override
            public void updateUser(Model_User_Account user) {
                home.updateUser(user);
            }

        });
        PublicEvent.getInstance().addEventImageView(new EventImageView() {
            @Override
            public void viewImage(Icon image) {
                vIew_Image.viewImage(image);
            }

            @Override
            public void saveImage(Icon image) {
                System.out.println("Save Image next update");
            }

        });
    }

    /**
     * [GUI 빌더 생성 코드] 화면 디자인과 컴포넌트 배치를 담당하는 메소드
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        border = new javax.swing.JPanel();
        background = new javax.swing.JPanel();
        title = new javax.swing.JPanel();
        cmdMinimize = new javax.swing.JButton();
        cmdClose = new javax.swing.JButton();
        body = new javax.swing.JLayeredPane();
        loading = new com.raven.form.Loading();
        login = new com.raven.form.Login();
        vIew_Image = new com.raven.form.VIew_Image();
        home = new com.raven.form.Home();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);

        border.setBackground(new java.awt.Color(229, 229, 229));

        background.setBackground(new java.awt.Color(255, 255, 255));

        title.setBackground(new java.awt.Color(229, 229, 229));
        title.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                titleMouseDragged(evt);
            }
        });
        title.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                titleMousePressed(evt);
            }
        });

        cmdMinimize.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/raven/icon/minimize.png"))); // NOI18N
        cmdMinimize.setBorder(null);
        cmdMinimize.setContentAreaFilled(false);
        cmdMinimize.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        cmdMinimize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdMinimizeActionPerformed(evt);
            }
        });

        cmdClose.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/raven/icon/close.png"))); // NOI18N
        cmdClose.setBorder(null);
        cmdClose.setContentAreaFilled(false);
        cmdClose.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        cmdClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdCloseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout titleLayout = new javax.swing.GroupLayout(title);
        title.setLayout(titleLayout);
        titleLayout.setHorizontalGroup(
            titleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, titleLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(cmdMinimize)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmdClose)
                .addContainerGap())
        );
        titleLayout.setVerticalGroup(
            titleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(titleLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(titleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cmdClose, javax.swing.GroupLayout.DEFAULT_SIZE, 20, Short.MAX_VALUE)
                    .addComponent(cmdMinimize, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0))
        );

        body.setLayout(new java.awt.CardLayout());
        body.add(loading, "card5");
        body.add(login, "card4");
        body.setLayer(vIew_Image, javax.swing.JLayeredPane.POPUP_LAYER);
        body.add(vIew_Image, "card3");
        body.add(home, "card2");

        javax.swing.GroupLayout backgroundLayout = new javax.swing.GroupLayout(background);
        background.setLayout(backgroundLayout);
        backgroundLayout.setHorizontalGroup(
            backgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(title, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(backgroundLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(body, javax.swing.GroupLayout.DEFAULT_SIZE, 1210, Short.MAX_VALUE)
                .addContainerGap())
        );
        backgroundLayout.setVerticalGroup(
            backgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(title, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(body, javax.swing.GroupLayout.DEFAULT_SIZE, 606, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout borderLayout = new javax.swing.GroupLayout(border);
        border.setLayout(borderLayout);
        borderLayout.setHorizontalGroup(
            borderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(borderLayout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addComponent(background, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(1, 1, 1))
        );
        borderLayout.setVerticalGroup(
            borderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(borderLayout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addComponent(background, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(1, 1, 1))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(border, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(border, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private int pX;
    private int pY;
    private void titleMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_titleMouseDragged
        this.setLocation(this.getLocation().x + evt.getX() - pX, this.getLocation().y + evt.getY() - pY);
    }//GEN-LAST:event_titleMouseDragged

    private void titleMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_titleMousePressed
        pX = evt.getX();
        pY = evt.getY();
    }//GEN-LAST:event_titleMousePressed

    private void cmdCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdCloseActionPerformed
        System.exit(0);
    }//GEN-LAST:event_cmdCloseActionPerformed

    private void cmdMinimizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdMinimizeActionPerformed
        this.setState(JFrame.ICONIFIED);
    }//GEN-LAST:event_cmdMinimizeActionPerformed

    public static void main(String args[]) {
        FlatArcIJTheme.setup();
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Main().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel background;
    private javax.swing.JLayeredPane body;
    private javax.swing.JPanel border;
    private javax.swing.JButton cmdClose;
    private javax.swing.JButton cmdMinimize;
    private com.raven.form.Home home;
    private com.raven.form.Loading loading;
    private com.raven.form.Login login;
    private javax.swing.JPanel title;
    private com.raven.form.VIew_Image vIew_Image;
    // End of variables declaration//GEN-END:variables
}
