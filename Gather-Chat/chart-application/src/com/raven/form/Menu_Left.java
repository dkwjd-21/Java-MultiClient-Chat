package com.raven.form;

import com.raven.component.Item_People;
import com.raven.event.EventMenuLeft;
import com.raven.event.PublicEvent;
import com.raven.model.Model_User_Account;
import com.raven.service.Service;
import com.raven.swing.ScrollBar;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import net.miginfocom.swing.MigLayout;

public class Menu_Left extends javax.swing.JPanel {

    private List<Model_User_Account> userAccount;

    public Menu_Left() {
        initComponents();
        init();
    }

    private void init() {
        sp.setVerticalScrollBar(new ScrollBar());
        // 리스트 레이아웃 설정
        menuList.setLayout(new MigLayout("fillx", "0[]0", "0[]0"));
        userAccount = new ArrayList<>();

        // [1] 상단 탭 텍스트 제목 추가
        menu.removeAll();
        menu.setLayout(new MigLayout("fill", "15[]10", "10[]10"));

        javax.swing.JLabel title = new javax.swing.JLabel("접속자 목록");
        title.setFont(new java.awt.Font("맑은 고딕", 1, 14));
        title.setForeground(new java.awt.Color(51, 51, 51));

        menu.add(title); // 텍스트 추가
        menu.revalidate();
        menu.repaint();

        // 메인 이벤트 리스너: 입장이 완료되면 실행됨
        PublicEvent.getInstance().addEventMenuLeft(new EventMenuLeft() {
            @Override
            public void newUser(List<Model_User_Account> users) {
                userAccount = new ArrayList<>(users);

                Model_User_Account mySelf = Service.getInstance().getUser();

                if (menuList.getComponentCount() == 0 && mySelf != null) {
                    setMySelf(mySelf);
                }

                List<Model_User_Account> pureOthers = new ArrayList<>();
                for (Model_User_Account u : users) {
                    if (mySelf != null && u.getUserID() != mySelf.getUserID()) {
                        pureOthers.add(u);
                    }
                }

                // 4. 타인 리스트 갱신
                updateOtherUsers(pureOthers);
            }

            @Override
            public void userConnect(int id) {
                // [추가] 새로 들어온 유저가 있으면 리스트 끝에 붙이기
                // Service나 서버로부터 받은 유저 객체가 있다면 addUser(user) 호출
            }

            @Override
            public void userDisconnect(int id) {
                // [삭제] 나간 유저가 있으면 리스트에서 즉시 제거
                removeUser(id);
            }
        });
    }

    // 로그인 직후 '나'를 딱 한 번만 추가하는 메소드
    public void setMySelf(Model_User_Account mySelf) {
        menuList.removeAll(); // 초기화 시점에만 전체 삭제

        // [추적 로그 추가]
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

        // 첫 번째(index 0)에 '나'를 고정
        menuList.add(new com.raven.component.Item_People(me), "wrap, x 0, y 0");

        menuList.revalidate();
        menuList.repaint();
        System.out.println("로그: 내 정보가 리스트 최상단에 고정되었습니다.");
    }

    // 본인을 제외한 나머지 인원만 갱신하는 메소드
    public void updateOtherUsers(List<Model_User_Account> users) {
        Model_User_Account mySelf = Service.getInstance().getUser();

        // index 0(나)만 남기고 나머지(타인들)를 지움
        // menuList의 컴포넌트가 1개보다 많을 때만 작동
        while (menuList.getComponentCount() > 1) {
            menuList.remove(1); // 1번 인덱스부터 끝까지 계속 지움
        }

        // 전달받은 리스트를 순회하며 그대로 추가
        if (users != null) {
            for (Model_User_Account u : users) {
                // 외부에서 필터링해서 줬으므로 여기서 중복 체크(if)는 생략
                menuList.add(new com.raven.component.Item_People(u), "wrap");
            }
        }

        menuList.revalidate();
        menuList.repaint();
        System.out.println("로그: 타인 리스트만 갱신 완료 (나의 항목은 유지됨)");
    }

    // 나간 사람만 리스트에서 빼기
    private void removeUser(int userID) {
        for (Component com : menuList.getComponents()) {
            if (com instanceof Item_People) {
                Item_People item = (Item_People) com;
                if (item.getUser().getUserID() == userID) {
                    menuList.remove(com); // 화면에서 삭제
                    break;
                }
            }
        }
        menuList.revalidate();
        menuList.repaint();
        System.out.println("로그: 유저 퇴장(ID: " + userID + ") - 리스트에서 제거됨");
    }

    private void updateStatus(int id, boolean s) {
        Model_User_Account mySelf = Service.getInstance().getUser();
        List<Model_User_Account> pureOthers = new ArrayList<>();

        // 이미 추가한 유저 ID를 추적해서 중복 방지
        List<Integer> addedIds = new ArrayList<>();

        for (Model_User_Account u : userAccount) {
            if (u.getUserID() == id) {
                u.setStatus(s);
            }

            // 1. 나(mySelf)는 제외
            // 2. 이미 pureOthers에 담긴 ID가 아닐 때만 추가
            if (mySelf != null && u.getUserID() != mySelf.getUserID()) {
                if (!addedIds.contains(u.getUserID())) {
                    pureOthers.add(u);
                    addedIds.add(u.getUserID());
                }
            }
        }

        // 이제 정말로 깨끗한 1인 1계정 명단만 전달됨
        updateOtherUsers(pureOthers);
    }

    private void showMessage() {
        //  test data
        menuList.removeAll();
        for (Model_User_Account d : userAccount) {
            menuList.add(new Item_People(null), "wrap");
        }
        refreshMenuList();
    }

    private void showGroup() {
        //  test data
        menuList.removeAll();
        for (int i = 0; i < 5; i++) {
            menuList.add(new Item_People(null), "wrap");
        }
        refreshMenuList();
    }

    private void showBox() {
        //  test data
        menuList.removeAll();
        for (int i = 0; i < 10; i++) {
            menuList.add(new Item_People(null), "wrap");
        }
        refreshMenuList();
    }

    private void refreshMenuList() {
        menuList.repaint();
        menuList.revalidate();
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
            showMessage();
        }
    }//GEN-LAST:event_menuMessageActionPerformed

    private void menuGroupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuGroupActionPerformed
        /*
        if (!menuGroup.isSelected()) {
            menuMessage.setSelected(false);
            menuGroup.setSelected(true);
            menuBox.setSelected(false);
            showGroup();
        }
        */
        System.out.println("단체 채팅방 모드 활성화");
    }//GEN-LAST:event_menuGroupActionPerformed

    private void menuBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuBoxActionPerformed
       /*
        if (!menuBox.isSelected()) {
            menuMessage.setSelected(false);
            menuGroup.setSelected(false);
            menuBox.setSelected(true);
            showBox();
        }
        */
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
