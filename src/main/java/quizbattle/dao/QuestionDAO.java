package quizbattle.dao;

import quizbattle.exception.DataAccessException;
import quizbattle.model.Question;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for questions table.
 * Demonstrates: PreparedStatements and try-with-resources to prevent SQL injection and resource leaks.
 */
public class QuestionDAO {

    public List<Question> findRandom(int count) {
        String sql = "SELECT id, text, option_a, option_b, option_c, option_d, correct_option, category " +
                     "FROM questions ORDER BY RAND() LIMIT ?";
        List<Question> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, count);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch random questions", e);
        }
    }

    public List<Question> findRandomByCategory(String category, int count) {
        String sql = "SELECT id, text, option_a, option_b, option_c, option_d, correct_option, category " +
                     "FROM questions WHERE LOWER(category) = LOWER(?) ORDER BY RAND() LIMIT ?";
        List<Question> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, category.trim());
            ps.setInt(2, count);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch questions for category: " + category, e);
        }
    }

    public List<String> findCategories() {
        String sql = "SELECT DISTINCT category FROM questions WHERE category IS NOT NULL AND category != '' ORDER BY category ASC";
        List<String> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(rs.getString("category"));
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch categories", e);
        }
    }

    public List<Question> findAll() {
        String sql = "SELECT id, text, option_a, option_b, option_c, option_d, correct_option, category " +
                     "FROM questions ORDER BY id ASC";
        List<Question> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch all questions", e);
        }
    }

    public void insert(Question q) {
        String sql = "INSERT INTO questions (text, option_a, option_b, option_c, option_d, correct_option, category) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, q.getText());
            ps.setString(2, q.getOptions().get(0));
            ps.setString(3, q.getOptions().get(1));
            ps.setString(4, q.getOptions().get(2));
            ps.setString(5, q.getOptions().get(3));
            ps.setString(6, String.valueOf(q.getCorrectOptionLetter()));
            ps.setString(7, q.getCategory());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert question", e);
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM questions WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete question with id: " + id, e);
        }
    }

    private Question mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String text = rs.getString("text");
        List<String> options = List.of(
                rs.getString("option_a"),
                rs.getString("option_b"),
                rs.getString("option_c"),
                rs.getString("option_d")
        );
        char correctChar = rs.getString("correct_option").charAt(0);
        int correctIndex = Character.toUpperCase(correctChar) - 'A';
        String category = rs.getString("category");

        return new Question(id, text, options, correctIndex, category);
    }
}
