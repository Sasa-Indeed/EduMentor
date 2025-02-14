package com.asu.EduMentor.model;

import lombok.NoArgsConstructor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class Mentor extends User {

    private double totalHours;

    public Mentor(String firstName, String lastName, String email, String password) {
        super(firstName, lastName, UserType.MENTOR, email, password);
        this.totalHours = 0;
    }

    public Mentor(String firstName, String lastName, String email, String password, double totalHours) {
        super(firstName, lastName, UserType.MENTOR, email, password);
        List<Session> sessions = getGivenSessions();
        this.totalHours = sessions.stream().mapToDouble(Session::getDuration).sum();
    }

    public double getTotalHours() {
        return getGivenSessions()
                .stream()
                .mapToDouble(Session::getDuration)
                .sum();
    }

    public void setTotalHours(double totalHours) {
        this.totalHours = totalHours;
    }

    @Override
    public Object create() {

        String userQuery = "INSERT INTO public.\"User\" (\"FirstName\", \"LastName\", \"Email\", \"Password\", \"Role\", \"IsDeleted\") VALUES (?, ?, ?, ?, ?, FALSE)";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(userQuery, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, this.getFirstName());
            stmt.setString(2, this.getLastName());
            stmt.setString(3, this.getEmail());
            stmt.setString(4, this.getPassword());
            stmt.setInt(5, UserType.MENTOR.getRoleId());

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                this.setUserID(rs.getInt("UserID"));
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            throw new RuntimeException("Error creating user", e);
        }

        String mentorQuery = "INSERT INTO public.\"Mentor\" (\"MentorID\") VALUES (?)";
        try (PreparedStatement stmt = conn.prepareStatement(mentorQuery)) {
            stmt.setInt(1, this.getUserID());

            stmt.executeUpdate();
        } catch (SQLException e) {
            //e.printStackTrace();
            throw new RuntimeException("Error creating mentor", e);
        }

        return this;

    }

    @Override
    public Object update(Object updatedObject) {


        if (!(updatedObject instanceof User)) {
            throw new IllegalArgumentException("Invalid object type");
        }

        Mentor updatedMentor = (Mentor) updatedObject;

        String userQuery = "UPDATE public.\"User\" SET \"FirstName\" = ?, \"LastName\" = ?, \"Email\" = ?, \"Password\" = ? WHERE \"UserID\" = ? AND \"IsDeleted\" = FALSE";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(userQuery)) {
            stmt.setString(1, updatedMentor.getFirstName());
            stmt.setString(2, updatedMentor.getLastName());
            stmt.setString(3, updatedMentor.getEmail());
            stmt.setString(4, updatedMentor.getPassword());
            stmt.setInt(5, updatedMentor.getUserID());

            stmt.executeUpdate();
        } catch (SQLException e) {
            //e.printStackTrace();
            throw new RuntimeException("Error updating mentor", e);
        }

        return updatedMentor;
    }

    @Override
    public Object read(int id) {

        String sqlQuery = "SELECT u.\"UserID\", u.\"FirstName\", u.\"LastName\", u.\"Email\", u.\"Password\" FROM public.\"Mentor\" m JOIN public.\"User\" u ON m.\"MentorID\" = u.\"UserID\" WHERE u.\"UserID\" = ? AND u.\"IsDeleted\" = FALSE";
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

                Mentor m = new Mentor(firstName, lastName, email, password);
                m.setUserID(userID);
                return m;
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            throw new RuntimeException("Error reading mentor", e);
        }
        return null;

    }

    @Override
    public List<Object> readAll() {

        List<Object> mentors = new ArrayList<>();
        String sqlQuery = "SELECT u.\"UserID\", u.\"FirstName\", u.\"LastName\", u.\"Email\", u.\"Password\" FROM public.\"Mentor\" m JOIN public.\"User\" u ON m.\"AdminID\" = u.\"UserID\" WHERE u.\"IsDeleted\" = FALSE";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sqlQuery)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int userID = rs.getInt("UserID");
                String firstName = rs.getString("FirstName");
                String lastName = rs.getString("LastName");
                String email = rs.getString("Email");
                String password = rs.getString("Password");

                Mentor mentor = new Mentor(firstName, lastName, email, password);
                mentor.setUserID(userID);
                mentors.add(mentor);
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            throw new RuntimeException("Error reading all mentors", e);
        }

        return mentors;

    }

    public List<Session> getGivenSessions() {

        List<Session> sessions = new ArrayList<>();
        String query = "SELECT s.\"SessionID\", s.\"SessionName\", s.\"Date\",  s.\"Duration\"" +
                "FROM public.\"Session\" s " +
                "JOIN public.\"SM_Gives\" sg ON s.\"SessionID\" = sg.\"SessionID\" " +
                "WHERE sg.\"MentorID\" = ? AND s.\"IsDeleted\" = FALSE";

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
                sessions.add(session);
            }

        } catch (SQLException e) {
            //e.printStackTrace();
            throw new RuntimeException("Error retrieving attended sessions", e);
        }

        return sessions;
    }

    public List<Availability> getMentorAvailabilities() {
        List<Availability> availabilityList = new ArrayList<>();

        String query = "SELECT a.\"Availability\", a.\"AvailabilityDuration\" " +
                "FROM public.\"Mentor_Availability\" a " +
                "WHERE a.\"MentorID\" = ? AND a.\"IsDeleted\" = FALSE";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, this.getUserID());

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                Timestamp time = rs.getTimestamp("Availability");
                double duration = rs.getDouble("AvailabilityDuration");

                Availability availability = new Availability(time, duration);

                availabilityList.add(availability);
            }

        } catch (SQLException e) {
            //e.printStackTrace();
            throw new RuntimeException("Error retrieving availability", e);
        }
        return availabilityList;
    }

    public boolean addAvailability(Availability availability) {

        String sqlQuery = "INSERT INTO public.\"Mentor_Availability\" (\"MentorID\", \"Availability\", \"AvailabilityDuration\", \"IsDeleted\") VALUES (?, ?, ?, ?)";

        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sqlQuery)) {

            Timestamp utcTimestamp = new Timestamp(availability.getTime().getTime());

            stmt.setInt(1, this.getUserID());
            stmt.setTimestamp(2, utcTimestamp);
            stmt.setDouble(3, availability.getDuration());
            stmt.setBoolean(4, availability.isDeleted());

            int rowsAffected = stmt.executeUpdate();

            return rowsAffected > 0;

        } catch (SQLException e) {
            //e.printStackTrace();
            throw new RuntimeException("Error adding availability for mentor", e);
        }

    }

    public boolean deleteAvailability(Availability availability) {

        String sqlQuery = "UPDATE public.\"Mentor_Availability\" " +
                "SET \"IsDeleted\" = TRUE " +
                "WHERE \"MentorID\" = ? AND \"Availability\" = ? AND \"AvailabilityDuration\" = ? AND \"IsDeleted\" = FALSE";

        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sqlQuery)) {

            stmt.setInt(1, this.getUserID());
            stmt.setTimestamp(2, availability.getTime());
            stmt.setDouble(3, availability.getDuration());

            int rowsAffected = stmt.executeUpdate();

            return rowsAffected > 0;

        } catch (SQLException e) {
            //e.printStackTrace();
            throw new RuntimeException("Error deleting availability for mentor", e);
        }

    }

    public static List<Mentor> findAvailableMentors(java.util.Date sessionDate, double sessionDuration) {
        List<Mentor> availableMentors = new ArrayList<>();

        String sqlQuery =
                "WITH MaxMentorAvailability AS (" +
                        "    SELECT m.\"MentorID\", ma.\"Availability\", MAX(ma.\"AvailabilityDuration\") as \"AvailabilityDuration\" " +
                        "    FROM public.\"Mentor\" m " +
                        "    JOIN public.\"Mentor_Availability\" ma ON m.\"MentorID\" = ma.\"MentorID\" " +
                        "    WHERE ma.\"IsDeleted\" = FALSE " +
                        "    GROUP BY m.\"MentorID\", ma.\"Availability\" " +
                        ") " +
                        "SELECT DISTINCT u.\"UserID\", u.\"FirstName\", u.\"LastName\", u.\"Email\", u.\"Password\", " +
                        "mma.\"Availability\", mma.\"AvailabilityDuration\" " +
                        "FROM public.\"User\" u " +
                        "JOIN public.\"Mentor\" m ON u.\"UserID\" = m.\"MentorID\" " +
                        "JOIN MaxMentorAvailability mma ON m.\"MentorID\" = mma.\"MentorID\" " +
                        "WHERE u.\"IsDeleted\" = FALSE " +
                        "AND DATE(mma.\"Availability\") = DATE(?::timestamp) " +
                        "AND ?::timestamp >= mma.\"Availability\" " +
                        "AND ?::timestamp <= mma.\"Availability\" + " +
                        "INTERVAL '1 hour' * mma.\"AvailabilityDuration\" " +
                        "AND mma.\"AvailabilityDuration\" >= ? " +
                        "AND NOT EXISTS (" +
                        "    SELECT 1 FROM public.\"SM_Gives\" sg " +
                        "    JOIN public.\"Session\" s ON sg.\"SessionID\" = s.\"SessionID\" " +
                        "    WHERE sg.\"MentorID\" = m.\"MentorID\" " +
                        "    AND s.\"Date\" = ? " +
                        "    AND s.\"IsDeleted\" = FALSE" +
                        ")";

        Connection conn = DBConnection.getInstance().getConnection();
        try {
            try (PreparedStatement stmt = conn.prepareStatement(sqlQuery)) {
                Timestamp sessionTimestamp = new Timestamp(sessionDate.getTime());

                stmt.setTimestamp(1, sessionTimestamp);
                stmt.setTimestamp(2, sessionTimestamp);
                stmt.setTimestamp(3, sessionTimestamp);
                stmt.setDouble(4, sessionDuration);
                stmt.setDate(5, new java.sql.Date(sessionDate.getTime()));

                ResultSet rs = stmt.executeQuery();

                boolean hasResults = false;

                while (rs.next()) {
                    hasResults = true;
                    int mentorID = rs.getInt("UserID");
                    String firstName = rs.getString("FirstName");
                    String lastName = rs.getString("LastName");
                    Timestamp availability = rs.getTimestamp("Availability");
                    double availDuration = rs.getDouble("AvailabilityDuration");

                    Mentor mentor = new Mentor(firstName, lastName,
                            rs.getString("Email"), rs.getString("Password"));
                    mentor.setUserID(mentorID);
                    availableMentors.add(mentor);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error finding available mentors", e);
        }

        return availableMentors;
    }
}
