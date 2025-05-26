import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.Timestamp;

public class FeedbackDAO {

    public List<Feedback> getAllFeedback() throws SQLException {
        List<Feedback> feedbackList = new ArrayList<>();
        String sql = "SELECT * FROM feedback ORDER BY submission_date DESC";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement pstmt = connection.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Feedback feedback = new Feedback();
                feedback.setId(rs.getInt("id"));
                feedback.setCustomerName(rs.getString("name"));
                feedback.setRating(rs.getInt("rating"));
                feedback.setComment(rs.getString("comment"));
                Timestamp timestamp = rs.getTimestamp("submission_date");
                feedback.setDate(timestamp != null ? timestamp.toString() : "");
                feedbackList.add(feedback);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving feedback: " + e.getMessage());
            throw e;
        }
        return feedbackList;
    }

    public boolean deleteFeedback(int feedbackId) throws SQLException {
        String sql = "DELETE FROM feedback WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setInt(1, feedbackId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting feedback: " + e.getMessage());
            throw e;
        }
    }

    public void addFeedback(Feedback feedback) throws SQLException {
        String sql = "INSERT INTO feedback (name, phone, comment, rating, submission_date) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, feedback.getCustomerName());
            pstmt.setString(2, ""); // Since phone is required in DB but not in our model
            pstmt.setString(3, feedback.getComment());
            pstmt.setInt(4, feedback.getRating());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding feedback: " + e.getMessage());
            throw e;
        }
    }
}