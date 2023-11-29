package it.betacom.EsercizioEstrazione;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbHandler {
    private static DbHandler instance;
    private Connection connection;

    private DbHandler() {
        // Costruttore privato per evitare l'istanziazione diretta
        initializeConnection();
    }

    public static DbHandler getInstance() {
        if (instance == null) {
            synchronized (DbHandler.class) {
                if (instance == null) {
                    instance = new DbHandler();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    private void initializeConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/esercizio_estrazione", "root", "root");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore durante la connessione al database", e);
        }
    }
}
