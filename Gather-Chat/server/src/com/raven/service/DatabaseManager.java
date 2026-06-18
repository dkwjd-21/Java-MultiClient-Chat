package com.raven.service;

import com.raven.model.Model_Room;
import com.raven.model.Model_User_Account;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:gather_chat.db";
    private static DatabaseManager instance;

    private DatabaseManager() {
        initDatabase();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    // DB 연결 커넥션을 가져오는 함수
    public Connection getConnection() throws Exception {
        return DriverManager.getConnection(DB_URL);
    }

    // 서버 켜질 때 테이블 3개를 자동으로 빌드하는 핵심 로직
    private void initDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

            // 1. 유저 테이블 (USERS) -> 규격 통일: user_id, username
            stmt.execute("CREATE TABLE IF NOT EXISTS USERS (" +
                    "user_id TEXT PRIMARY KEY, " +
                    "username TEXT NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // 2. 채팅방 테이블 (ROOMS)
            stmt.execute("CREATE TABLE IF NOT EXISTS ROOMS (" +
                    "room_id TEXT PRIMARY KEY, " +
                    "room_name TEXT NOT NULL, " +
                    "creator_id TEXT)");

            // 3. 메시지 테이블 (MESSAGES)
            stmt.execute("CREATE TABLE IF NOT EXISTS MESSAGES (" +
                    "message_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "room_id TEXT, " +
                    "from_user_id TEXT, " +
                    "message_text TEXT, " +
                    "timestamp TEXT)");

            // 기본 'Gather-Chat 광장' 방이 없을 때만 자동으로 하나 파주기
            stmt.execute("INSERT OR IGNORE INTO ROOMS(room_id, room_name, creator_id) " +
                    "VALUES('SQUARE', 'Gather-Chat 광장', 'SYSTEM')");

            System.out.println("[DB LOG] SQLite 인프라 및 3대 테이블 빌드업 완료.");
        } catch (Exception e) {
            System.out.println("[DB ERROR] 테이블 생성 중 에러 발생!");
            e.printStackTrace();
        }
    }

    // 닉네임이 DB에 이미 존재하는지 검증하는 함수
    public boolean isUsernameTaken(String username) {
        String sql = "SELECT COUNT(*) FROM USERS WHERE username = ?";
        try (Connection conn = this.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0; // 0보다 크면 중복된 닉네임!
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 🎯 [완벽 수정] 테이블 정의에 맞게 쿼리 컬럼명을 user_id, username 으로 정밀 일치 매핑!
    public void registerUser(String userID, String userName) {
        String sql = "INSERT OR IGNORE INTO USERS (user_id, username) VALUES (?, ?)";

        try (Connection conn = this.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userID);
            pstmt.setString(2, userName);

            pstmt.executeUpdate();
            System.out.println("👤 [USERS 영속화 안전 통과] 로그인 처리 완료 ID: " + userID);

        } catch (Exception e) {
            System.err.println("❌ [DatabaseManager] registerUser 처리 중 에러 발생");
            e.printStackTrace();
        }
    }

    // DB 테이블 데이터 CLEAR 함수
    public void clearTables() {
        try (Connection conn = this.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("DELETE FROM USERS");
            stmt.execute("DELETE FROM ROOMS");
            stmt.execute("DELETE FROM MESSAGES");

            stmt.execute("DELETE FROM sqlite_sequence WHERE name='MESSAGES'");

            stmt.execute("INSERT OR IGNORE INTO ROOMS(room_id, room_name, creator_id) " +
                    "VALUES('SQUARE', 'Gather-Chat 광장', 'SYSTEM')");

            System.out.println("[DB LOG] 모든 테이블 데이터가 성공적으로 초기화(Clear)되었습니다.");
        } catch (Exception e) {
            System.out.println("[DB ERROR] 테이블 초기화 중 에러 발생");
            e.printStackTrace();
        }
    }

    // 방 목록 조회
    public List<Model_Room> getRoomList() {
        List<Model_Room> rooms = new ArrayList<>();
        String sql = "SELECT room_id, room_name FROM ROOMS";

        try (Connection conn = this.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                rooms.add(new Model_Room(
                        rs.getString("room_id"),
                        rs.getString("room_name")
                ));
            }
        } catch (Exception e) {
            System.out.println("[DB ERROR] 방 목록 쿼리 실행 실패");
            e.printStackTrace();
        }
        return rooms;
    }
}