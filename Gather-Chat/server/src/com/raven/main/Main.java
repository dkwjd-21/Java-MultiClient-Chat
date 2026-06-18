package com.raven.main;

// 필요한 라이브러리들 불러오기
import com.raven.connection.DatabaseConnection;
import com.raven.service.Service;
import java.sql.SQLException;

/**
 * Gather-Chat 서버의 메인 관제창 클래스
 */
public class Main extends javax.swing.JFrame {

    /**
     * 서버 실행 시 화면 구성을 초기화
     */
    public Main() {
        initComponents();
    }

    // --- GUI 설정 부분 ---
    @SuppressWarnings("unchecked")
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        txt = new javax.swing.JTextArea(); // 서버 로그가 찍힐 텍스트 영역

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        // 윈도우 창이 열릴 때 실행될 이벤트 설정
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        txt.setEditable(false); // 로그창은 수정 불가능하게 설정
        txt.setColumns(20);
        txt.setRows(5);
        jScrollPane1.setViewportView(txt);

        // 레이아웃 구성 (화면 배치 관련)
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 879, Short.MAX_VALUE)
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(29, 29, 29)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE)
                                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null); // 화면 중앙에 배치
    }

    /**
     * [핵심] 서버 창이 뜨자마자 실행되는 로직
     */
    private void formWindowOpened(java.awt.event.WindowEvent evt) {
        try {
            com.raven.service.DatabaseManager.getInstance();
            txt.append("[DB LOG] SQLite 인프라 및 3대 테이블 연결 완료!\n");

            Service.getInstance(txt).startServer();

            txt.append("✅ Gather-Chat 서버가 실시간 정상 가동되었습니다!\n");
            txt.append("🖥️ [작동 모드] SQLite DB 영속화 원장 동기화 완료\n");
            txt.append("📢 현재 포트 9999(소켓) / 8080(HTTP)에서 대기 중입니다...\n");

        } catch (Exception e) {
            txt.append("에러 발생 : " + e.getMessage() + "\n");
        }
    }

    /**
     * 프로그램의 시작점 (메인 메소드)
     */
    public static void main(String args[]) {
        /* 테마 설정 (Nimbus UI 적용) */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        /* 서버 관리창 띄우기 */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Main().setVisible(true);
            }
        });
    }

    // 화면 UI 변수 선언
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea txt;
}