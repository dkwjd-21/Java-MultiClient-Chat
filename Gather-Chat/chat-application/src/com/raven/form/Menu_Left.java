package com.raven.form;

import com.raven.component.Item_People;
import com.raven.event.EventMenuLeft;
import com.raven.event.PublicEvent;
import com.raven.model.Model_Room;
import com.raven.model.Model_User_Account;
import com.raven.service.Service;
import com.raven.swing.ScrollBar;
import java.awt.Component;
import java.awt.Font;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import net.miginfocom.swing.MigLayout;

public class Menu_Left extends javax.swing.JPanel {

    private List<Model_User_Account> userAccount;
    private JLabel titleLabel; // 동적 타이틀 변경을 위해 변수로 승격

    public Menu_Left() {
        initComponents();
        init();
    }

    private boolean isRoomMode = false; // 현재 방 목록 모드인지 추적하는 플래그 변수

    private void init() {
        sp.setVerticalScrollBar(new ScrollBar());
        menuList.setLayout(new MigLayout("fillx", "0[]0", "0[]0"));
        userAccount = new ArrayList<>();

        // 상단 탭 레이아웃 동적 셋업 (MigLayout 속성을 활용해 가로로 배치)
        menu.removeAll();
        menu.setLayout(new MigLayout("fillx", "10[]10[]10", "10[]10"));

        // 1. 토글 레이블
        titleLabel = new JLabel("접속자 목록 🔄");
        titleLabel.setFont(new Font("맑은 고딕", 1, 14));
        titleLabel.setForeground(new Color(40, 120, 240));
        titleLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        // 2. ➕ 방 추가 버튼 생성
        javax.swing.JButton btnAddRoom = new javax.swing.JButton("➕ 방만들기");
        btnAddRoom.setFont(new Font("맑은 고딕", 1, 11));
        btnAddRoom.setBackground(new Color(240, 240, 240));
        btnAddRoom.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        // [이벤트 A] 토글 레이블 클릭 시 모드 전환
        titleLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (isRoomMode) {
                    titleLabel.setText("접속자 목록 🔄");
                    titleLabel.setForeground(new Color(40, 120, 240));
                    isRoomMode = false;
                    refreshUserList(userAccount);
                } else {
                    titleLabel.setText("채팅방 목록 🔄");
                    titleLabel.setForeground(new Color(220, 80, 80));
                    isRoomMode = true;
                    showRESTRoomList();
                }
            }
        });

        // [이벤트 B] ➕ 방만들기 버튼 클릭 시 팝업 띄우기
        btnAddRoom.addActionListener(e -> {
            String newRoomName = javax.swing.JOptionPane.showInputDialog(this,
                    "새로 개설할 채팅방 이름을 입력해 주세요.",
                    "새 방 만들기",
                    javax.swing.JOptionPane.PLAIN_MESSAGE);

            if (newRoomName != null && !newRoomName.trim().isEmpty()) {
                // 소켓을 통해 서버에 방 생성 요청 이벤트
                Service.getInstance().getClient().emit("create_room", newRoomName.trim());

                // UX 센스: 방을 만들었으니 자동으로 방 목록 탭으로 전환해서 보여주기
                titleLabel.setText("채팅방 목록 🔄");
                titleLabel.setForeground(new Color(220, 80, 80));
                isRoomMode = true;

                // 서버가 DB에 반영할 시간을 잠시 주기 위해 0.2초 뒤 리스트 새로고침
                java.awt.EventQueue.invokeLater(() -> {
                    try { Thread.sleep(200); } catch(Exception ex){}
                    showRESTRoomList();
                });
            }
        });

        menu.add(titleLabel, "aligny center");
        menu.add(btnAddRoom, "gapleft push, aligny center");
        menu.revalidate();
        menu.repaint();

        // 메인 소켓 이벤트 연동 핸들러
        PublicEvent.getInstance().addEventMenuLeft(new EventMenuLeft() {
            @Override
            public void newUser(List<Model_User_Account> users) {
                userAccount = new ArrayList<>(users);
                if (isRoomMode) return;
                refreshUserList(users);
            }

            @Override
            public void userConnect(int id) {}

            @Override
            public void userDisconnect(int id) {
                removeUser(id);
            }
        });
    }

    // 순수 접속 유저 명단을 그리는 로직 분리
    private void refreshUserList(List<Model_User_Account> users) {
        Model_User_Account mySelf = Service.getInstance().getUser();
        menuList.removeAll();

        if (mySelf != null) {
            setMySelf(mySelf);
        }

        List<Model_User_Account> pureOthers = new ArrayList<>();
        for (Model_User_Account u : users) {
            if (mySelf != null && u.getUserID() != mySelf.getUserID()) {
                pureOthers.add(u);
            }
        }
        updateOtherUsers(pureOthers);
    }

    public void setMySelf(Model_User_Account mySelf) {
        System.out.println("==========================================");
        System.out.println("로그 [원본 mySelf]: " + mySelf.getUserName() + " (Hash: " + System.identityHashCode(mySelf) + ")");

        Model_User_Account me = new Model_User_Account(
                mySelf.getUserID(),
                mySelf.getUserName() + " (나)",
                mySelf.getGender(),
                mySelf.getImage(),
                true
        );

        System.out.println("로그 [생성된 me]: " + me.getUserName() + " (Hash: " + System.identityHashCode(me) + ")");
        System.out.println("==========================================");

        menuList.add(new Item_People(me), "wrap, x 0, y 0");
        refreshMenuList();
        System.out.println("로그: 내 정보가 리스트 최상단에 고정되었습니다.");
    }

    public void updateOtherUsers(List<Model_User_Account> users) {
        if (users != null) {
            for (Model_User_Account u : users) {
                menuList.add(new Item_People(u), "wrap");
            }
        }
        refreshMenuList();
        System.out.println("로그: 타인 리스트만 갱신 완료 (나의 항목은 유지됨)");
    }

    // REST API로 가져온 방 리스트를 화면에 바인딩하는 전용 함수
    private void showRESTRoomList() {
        menuList.removeAll();
        titleLabel.setText("개설된 채팅방 목록");

        // 서버 REST API로부터 룸 객체 리스트 수신
        List<Model_Room> rooms = Service.getInstance().getRoomListFromREST();

        for (Model_Room room : rooms) {
            Model_User_Account roomData = new Model_User_Account(0, room.getRoomName(), "", "", true);

            roomData.setImage(room.getRoomID());

            Item_People roomItem = new Item_People(roomData);
            roomItem.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

            roomItem.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    PublicEvent.getInstance().getEventMain().selectUser(roomData);
                }
            });

            menuList.add(roomItem, "wrap");
        }
        refreshMenuList();
        System.out.println("로그 [UI]: 서버 REST API로부터 " + rooms.size() + "개의 찐 방 데이터 매핑 완료.");
    }

    private void removeUser(int userID) {
        for (Component com : menuList.getComponents()) {
            if (com instanceof Item_People) {
                Item_People item = (Item_People) com;
                if (item.getUser().getUserID() == userID) {
                    menuList.remove(com);
                    break;
                }
            }
        }
        refreshMenuList();
        System.out.println("로그: 유저 퇴장(ID: " + userID + ") - 리스트에서 제거됨");
    }

    public String getUserNameById(int userID) {
        for (Model_User_Account u : userAccount) {
            if (u.getUserID() == userID) {
                return u.getUserName();
            }
        }
        return "알 수 없는 사용자";
    }

    private void refreshMenuList() {
        menuList.revalidate();
        menuList.repaint();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        menu = new javax.swing.JLayeredPane();
        menuMessage = new com.raven.component.MenuButton();
        menuGroup = new com.raven.component.MenuButton();
        menuBox = new com.raven.component.MenuButton();
        sp = new javax.swing.JScrollPane();
        menuList = new javax.swing.JLayeredPane();

        setBackground(new java.awt.Color(242, 242, 242));

        menu.setBackground(new java.awt.Color(229, 229, 229));
        menu.setOpaque(true);
        menu.setLayout(new java.awt.GridLayout(1, 3));

        menuMessage.setIconSelected(new javax.swing.ImageIcon(getClass().getResource("/com/raven/icon/message_selected.png"))); // NOI18N
        menuMessage.setIconSimple(new javax.swing.ImageIcon(getClass().getResource("/com/raven/icon/message.png"))); // NOI18N
        menuMessage.setSelected(true);
        menuMessage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuMessageActionPerformed(evt);
            }
        });
        menu.add(menuMessage);

        menuGroup.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/raven/icon/group.png"))); // NOI18N
        menuGroup.setIconSelected(new javax.swing.ImageIcon(getClass().getResource("/com/raven/icon/group_selected.png"))); // NOI18N
        menuGroup.setIconSimple(new javax.swing.ImageIcon(getClass().getResource("/com/raven/icon/group.png"))); // NOI18N
        menuGroup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuGroupActionPerformed(evt);
            }
        });
        menu.add(menuGroup);

        menuBox.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/raven/icon/box.png"))); // NOI18N
        menuBox.setIconSelected(new javax.swing.ImageIcon(getClass().getResource("/com/raven/icon/box_selected.png"))); // NOI18N
        menuBox.setIconSimple(new javax.swing.ImageIcon(getClass().getResource("/com/raven/icon/box.png"))); // NOI18N
        menuBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuBoxActionPerformed(evt);
            }
        });
        menu.add(menuBox);

        sp.setBackground(new java.awt.Color(242, 242, 242));
        sp.setBorder(null);
        sp.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        menuList.setBackground(new java.awt.Color(242, 242, 242));
        menuList.setOpaque(true);

        javax.swing.GroupLayout menuListLayout = new javax.swing.GroupLayout(menuList);
        menuList.setLayout(menuListLayout);
        menuListLayout.setHorizontalGroup(
                menuListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );
        menuListLayout.setVerticalGroup(
                menuListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 576, Short.MAX_VALUE)
        );

        sp.setViewportView(menuList);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(menu, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(sp)
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(menu, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sp)
                                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void menuMessageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuMessageActionPerformed
        if (!menuMessage.isSelected()) {
            menuMessage.setSelected(true);
            menuGroup.setSelected(false);
            menuBox.setSelected(false);
            titleLabel.setText("접속자 목록");
            refreshUserList(userAccount);
        }
    }//GEN-LAST:event_menuMessageActionPerformed

    // 두 번째 탭 단추(사람 세 명 아이콘) 클릭 시 비연결성 REST API 쿼리를 작동시킴
    private void menuGroupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuGroupActionPerformed
        if (!menuGroup.isSelected()) {
            menuMessage.setSelected(false);
            menuGroup.setSelected(true);
            menuBox.setSelected(false);

            // 수동 더미 렌더러 대신 우리가 빌드한 찐 REST 런타임 활성화
            showRESTRoomList();
        }
    }//GEN-LAST:event_menuGroupActionPerformed

    private void menuBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuBoxActionPerformed
    }//GEN-LAST:event_menuBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLayeredPane menu;
    private com.raven.component.MenuButton menuBox;
    private com.raven.component.MenuButton menuGroup;
    private javax.swing.JLayeredPane menuList;
    private com.raven.component.MenuButton menuMessage;
    private javax.swing.JScrollPane sp;
    // End of variables declaration//GEN-END:variables
}