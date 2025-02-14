package com.asu.EduMentor.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Mentee extends User {

    private int numberOfAttendedSessions;
    private double learningHours;


    public Mentee() {
        super("", "", UserType.MENTEE, "", "");
    }

    public Mentee(String firstName, String lastName, String email, String password) {
        super(firstName, lastName, UserType.MENTEE, email, password);
        this.numberOfAttendedSessions = 0;
        this.learningHours = 0;
    }

    public Mentee(String firstName, String lastName, String email, String password, int numberOfAttendedSessions, double learningHours) {
        super(firstName, lastName, UserType.MENTEE, email, password);
        List<Session> sessions = getAttendedSessions();
        this.numberOfAttendedSessions = sessions.size();
        this.learningHours = sessions.stream().mapToDouble(Session::getDuration).sum();
    }

    public double getLearningHours() {
        return getAttendedSessions().stream().mapToDouble(Session::getDuration).sum();
    }

    public void setLearningHours(double learningHours) {
        this.learningHours = learningHours;
    }

    public int getNumberOfAttendedSessions() {
        return getAttendedSessions().size();
    }

    public void setNumberOfAttendedSessions(int numberOfAttendedSessions) {
        this.numberOfAttendedSessions = numberOfAttendedSessions;
    }

    @Override
    public Object create() {

        String userQuery = "INSERT INTO public.\"User\" (\"FirstName\", \"LastName\", \"Email\", \"Password\", \"Role\") VALUES (?, ?, ?, ?, ?)";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(userQuery, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, this.getFirstName());
            stmt.setString(2, this.getLastName());
            stmt.setString(3, this.getEmail());
            stmt.setString(4, this.getPassword());
            stmt.setInt(5, UserType.MENTEE.getRoleId());

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                this.setUserID(rs.getInt("UserID"));
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            throw new RuntimeException("Error creating user", e);
        }

        String menteeQuery = "INSERT INTO public.\"Mentee\" (\"MenteeID\", \"LearningHours\", \"NumberOfAttandedSessions\") VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(menteeQuery)) {
            stmt.setInt(1, this.getUserID());
            stmt.setDouble(2, this.getLearningHours());
            stmt.setInt(3, this.getNumberOfAttendedSessions());

            stmt.executeUpdate();

        } catch (SQLException e) {
            //e.printStackTrace();
            throw new RuntimeException("Error creating mentee", e);
        }

        return this;

    }

    @Override
    public Object update(Object updatedObject) {

        if (!(updatedObject instanceof User)) {
            throw new IllegalArgumentException("Invalid object type");
        }

        Mentee updatedMentee = (Mentee) updatedObject;

        String userQuery = "UPDATE public.\"User\" SET \"FirstName\" = ?, \"LastName\" = ?, \"Email\" = ?, \"Password\" = ? WHERE \"UserID\" = ? AND \"IsDeleted\" = FALSE";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(userQuery)) {
            stmt.setString(1, updatedMentee.getFirstName());
            stmt.setString(2, updatedMentee.getLastName());
            stmt.setString(3, updatedMentee.getEmail());
            stmt.setString(4, updatedMentee.getPassword());
            stmt.setInt(5, updatedMentee.getUserID());

            stmt.executeUpdate();
        } catch (SQLException e) {
            //e.printStackTrace();
            throw new RuntimeException("Error updating mentee", e);
        }

        String menteeQuery = "UPDATE public.\"Mentee\" SET \"LearningHours\" = ?, \"NumberOfAttandedSessions\" = ? WHERE \"MenteeID\" = ?";
        try (PreparedStatement adminStmt = conn.prepareStatement(menteeQuery)) {
            adminStmt.setDouble(1, updatedMentee.getLearningHours());
            adminStmt.setInt(2, updatedMentee.getNumberOfAttendedSessions());
            adminStmt.setInt(3, updatedMentee.getUserID());

            adminStmt.executeUpdate();
        } catch (SQLException e) {
            //e.printStackTrace();
            throw new RuntimeException("Error updating mentee", e);
        }

        return updatedMentee;

    }

    @Override
    public Object read(int id)
    // TODO: Refactor this method to be static
    {

        String sqlQuery = "SELECT u.\"UserID\", u.\"FirstName\", u.\"LastName\", u.\"Email\", u.\"Password\", m.\"LearningHours\", m.\"NumberOfAttandedSessions\" FROM public.\"Mentee\" m JOIN public.\"User\" u ON m.\"MenteeID\" = u.\"UserID\" WHERE u.\"UserID\" = ? AND u.\"IsDeleted\" = FALSE";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sqlQuery)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int userID = rs.getInt("UserID");
                String firstName = rs.getString("FirstName");
                String lastName = rs.getString("LastName");
                String email = rs.getString("Email");
                String password = rs.getString("Password");
                double hours = rs.getDouble("LearningHours");
                int numberOfAttendedSessions = rs.getInt("NumberOfAttandedSessions");

                Mentee m = new Mentee(firstName, lastName, email, password, numberOfAttendedSessions, hours);
                m.setUserID(userID);
                return m;
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            throw new RuntimeException("Error reading mentee", e);
        }
        return null;
    }

    @Override
    public List<Object> readAll() {

        List<Object> mentees = new ArrayList<>();
        String sqlQuery = "SELECT u.\"UserID\", u.\"FirstName\", u.\"LastName\", u.\"Email\", u.\"Password\", m.\"LearningHours\", m.\"NumberOfAttandedSessions\" FROM public.\"Mentee\" m JOIN public.\"User\" u ON m.\"MenteeID\" = u.\"UserID\" WHERE u.\"IsDeleted\" = FALSE";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sqlQuery)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int userID = rs.getInt("UserID");
                String firstName = rs.getString("FirstName");
                String lastName = rs.getString("LastName");
                String email = rs.getString("Email");
                String password = rs.getString("Password");
                double hours = rs.getDouble("LearningHours");
                int numberOfAttendedSessions = rs.getInt("NumberOfAttandedSessions");

                Mentee m = new Mentee(firstName, lastName, email, password, numberOfAttendedSessions, hours);
                m.setUserID(userID);
                mentees.add(m);
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            throw new RuntimeException("Error reading all mentees", e);
        }

        return mentees;
    }


    public List<Session> getAttendedSessions() {
        List<Session> givenSessions = new ArrayList<>();
        String query = "SELECT s.\"SessionID\", s.\"SessionName\", s.\"Date\", s.\"Duration\" " +
                "FROM public.\"Session\" s " +
                "JOIN public.\"SMTT_Takes\" st ON s.\"SessionID\" = st.\"SessionID\" " +
                "WHERE st.\"MenteeID\" = ? AND s.\"IsDeleted\" = FALSE";

        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, this.getUserID());

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                int sessionID = rs.getInt("SessionID");
                String name = rs.getString("SessionName");
                Date date = rs.getDate("Date");
                double duration = rs.getDouble("Duration");

                Session session = new Session(date, duration, name);
                session.setSessionID(sessionID);
                givenSessions.add(session);
            }

        } catch (SQLException e) {
            //e.printStackTrace();
            throw new RuntimeException("Error retrieving given sessions", e);
        }
        return givenSessions;
    }
}
