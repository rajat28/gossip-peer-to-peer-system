import java.sql.*;

public class Database {
    private String path;

    public Database(String file) {
        this.path = file;
    }

    private Connection connect(){
        Connection connection = null;

        try {
            connection = DriverManager.getConnection
                    ("jdbc:sqlite:" + path);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return connection;
    }

    public void createTableGossip() {
        Connection connection = null;
        Statement statement = null;

        try {
            connection = this.connect();
            statement = connection.createStatement();
            statement.executeUpdate("DROP TABLE IF EXISTS Gossip");
            statement.executeUpdate("CREATE TABLE Gossip (Message string)");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void createTablePeers() {
        Connection connection = null;
        Statement statement = null;

        try {
            connection = this.connect();
            statement = connection.createStatement();
            statement.executeUpdate("DROP TABLE IF EXISTS Peers");
            statement.executeUpdate("CREATE TABLE Peers (Name string, Port integer, IP text)");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean insertTableGossip(String message) {
        Connection connection = null;
        Statement statement = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = this.connect();
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM  Gossip");
            while (resultSet.next()) {
                if (message.equals(resultSet.getString("Message"))) {
                    System.err.println("DISCARDED");
                    return false;
                }
            }

            preparedStatement = connection.prepareStatement("INSERT INTO Gossip VALUES (?)");
            preparedStatement.setString(1, message);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return true;
    }



    public void insertTablePeers(String name, int port, String ip) {
        Connection connection = null;
        Statement statement = null;
        PreparedStatement preparedStatement1 = null, preparedStatement2 = null;
        ResultSet resultSet = null;

        try {
            connection = this.connect();
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM Peers");
            while (resultSet.next()) {
                if (((name.equals(resultSet.getString("Name"))) && (port == resultSet.getInt("Port")))
                        || (ip.equals(resultSet.getString("IP")))) {
                    preparedStatement1 = connection.prepareStatement("UPDATE Peers SET IP = ? " +
                            "WHERE Name = ? AND Port = ?");
                    preparedStatement1.setString(1, ip);
                    preparedStatement1.setString(2, name);
                    preparedStatement1.setInt(3, port);
                    preparedStatement1.executeUpdate();
                    return;
                }
            }

            preparedStatement2 = connection.prepareStatement("INSERT INTO Peers VALUES (?,?,?)");
            preparedStatement2.setString(1, name);
            preparedStatement2.setInt(2, port);
            preparedStatement2.setString(3, ip);
            preparedStatement2.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (preparedStatement1 != null) {
                    preparedStatement1.close();
                }
                if (preparedStatement2 != null) {
                    preparedStatement2.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public String printTablePeers() {
        Connection connection = null;
        Statement statement1 = null, statement2 = null;
        ResultSet resultSet1 = null, resultSet2 = null;
        String peerResults = "";

        try {
            connection = this.connect();
            statement1 = connection.createStatement();
            statement2 = connection.createStatement();
            resultSet1 = statement1.executeQuery("SELECT * FROM Peers");
            resultSet2 = statement2.executeQuery("SELECT COUNT(*) FROM Peers");
            peerResults = "PEERS|" + resultSet2.getInt("Count(*)") + "|";
            while (resultSet1.next()) {
                peerResults +=  resultSet1.getString("Name") + ":PORT=" +
                        resultSet1.getInt("Port") + ":IP=" + resultSet1.getString("IP") + "|";
            }
            peerResults += "%";
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet1 != null) {
                    resultSet1.close();
                }
                if (resultSet2 != null) {
                    resultSet2.close();
                }
                if (statement1 != null) {
                    statement1.close();
                }
                if (statement2 != null) {
                    statement2.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return peerResults;
    }

    public String[] returnPeerName() {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        String name[] = new String[50];
        int i = 0;

        try {
            connection = this.connect();
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT Name FROM Peers");
            while (resultSet.next()) {
                name[i] = resultSet.getString("Name");
                i++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return name;
    }

    public int[] returnPeerPort() {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        int port[] =  new int[50];
        int i = 0;

        try {
            connection = this.connect();
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT Port FROM Peers");
            while (resultSet.next()) {
                port[i] = resultSet.getInt("Port");
                i++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return port;
    }

    public String[] returnPeerIP() {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        String ip[] =  new String[50];
        int i = 0;

        try {
            connection = this.connect();
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT IP FROM Peers");
            while (resultSet.next()) {
                ip[i] = resultSet.getString("IP");
                i++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return ip;
    }

}
