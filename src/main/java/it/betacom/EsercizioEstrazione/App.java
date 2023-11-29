package it.betacom.EsercizioEstrazione;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import java.util.Scanner;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Hello world!
 *
 */
public class App {
	 private static final Logger logger = LogManager.getLogger(App.class);
	public static void main(String[] args) {
		Connection con = null;
		Statement stm = null;
		ResultSet rs;
		int scelta;
		
		

		
		try {
			DbHandler dbHandler = DbHandler.getInstance();
            con = dbHandler.getConnection();
            stm = con.createStatement();

			scelta = menu();
			

			if (scelta == 1) {

				String sql = "CREATE TABLE IF NOT EXISTS partecipante("
						+ "id_partecipante INT PRIMARY KEY NOT NULL AUTO_INCREMENT," + "nome VARCHAR(255) NOT NULL,"
						+ "sede VARCHAR(255) NOT NULL);";

				stm.executeUpdate(sql);

				sql = "CREATE TABLE IF NOT EXISTS estrazioni("
						+ "id_estrazione INT PRIMARY KEY NOT NULL AUTO_INCREMENT,"
						+ "data_estrazione TIMESTAMP DEFAULT CURRENT_TIMESTAMP," + "id_partecipante INT NOT NULL,"
						+ "KEY id_partecipante(id_partecipante),"
						+ "CONSTRAINT id_partecipante FOREIGN KEY (id_partecipante ) REFERENCES partecipante (id_partecipante));";
				stm.executeUpdate(sql);

				String filePath = new File("./esercizioPartecipanti.CSV").getAbsolutePath();
				boolean isFirstLine = true;

				BufferedReader br = new BufferedReader(new FileReader(filePath));
				String line;
				while ((line = br.readLine()) != null) {

					String[] columns = line.split(";");
					String nomePartecipante = columns[0];
					String sede = columns[1];

					sql = "INSERT INTO partecipante(nome,sede) VALUES ('" + nomePartecipante + "','" + sede + "');";
					stm.executeUpdate(sql);

				}

			} else if (scelta == 2) {

				rs = stm.executeQuery("SELECT COUNT(id_partecipante) FROM partecipante;");
				rs.next();
				int numPartecipanti = rs.getInt(1);

				Random generator = new Random();

				int estrazione = generator.nextInt(numPartecipanti) + 1;

				String sql = "INSERT INTO estrazioni (id_partecipante) VALUES ( " + estrazione + ");";
				stm.executeUpdate(sql);

				rs = stm.executeQuery(
						"SELECT nome, sede FROM partecipante WHERE id_partecipante = " + estrazione + ";");
				rs.next();
				String nome, sede;
				nome = rs.getString("nome");
				sede = rs.getString("sede");
				System.out.println("Il partecipante estratto è: " + nome + " " + sede);
			} else if (scelta == 3) {

				rs = stm.executeQuery("Select count(id_estrazione) AS numero_estrazioni, id_partecipante "
						+ "from estrazioni " + "group by id_partecipante " + "order by numero_estrazioni desc;");

				while (rs.next()) {
					int numero_estrazioni = rs.getInt("numero_estrazioni");
					int id_partecipante = rs.getInt("id_partecipante");
					System.out.println(
							"Numero estrazioni: " + numero_estrazioni + ", ID partecipante: " + id_partecipante);

				}

			} else if (scelta == 4) {
				generatePdf(con, stm);

			} else if (scelta == 5) {
			    String sql = "DROP TABLE partecipante, estrazioni;";
			    stm.executeUpdate(sql);
			}

			/*
			 * stm = con.createStatement(); ResultSet rs =
			 * stm.executeQuery("select * from genere"); while(rs.next()) {
			 * System.out.println(rs.getInt("codiceG") + "||" +
			 * rs.getString("descrizione"));
			 * 
			 * }
			 */

		} catch (SQLException | IOException e) {

			e.printStackTrace();
		}

		finally {
			if (con != null)
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (stm != null)
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}

	}
	 private static void generatePdf(Connection con, Statement stm) {
	        try {
	            ResultSet rs = stm.executeQuery("SELECT e.id_partecipante, COUNT(e.id_estrazione) AS numero_estrazioni, p.nome, p.sede "
	                    + "FROM estrazioni e "
	                    + "JOIN partecipante p ON e.id_partecipante = p.id_partecipante "
	                    + "GROUP BY e.id_partecipante "
	                    + "ORDER BY numero_estrazioni DESC;");

	            Document document = new Document();
	            PdfWriter.getInstance(document, new FileOutputStream("Situazione_Estrazioni.pdf"));
	            document.open();

	            document.add(new Paragraph("Situazione Estrazioni:\n\n"));

	            while (rs.next()) {
	                int numero_estrazioni = rs.getInt("numero_estrazioni");
	                String nomePartecipante = rs.getString("nome");
	                String sedePartecipante = rs.getString("sede");
	                document.add(new Paragraph("Numero estrazioni: " + numero_estrazioni +
	                        ", Nome partecipante: " + nomePartecipante +
	                        ", Sede partecipante: " + sedePartecipante));
	                document.add(new Paragraph("\n"));
	            }

	            document.close();
	            logger.info("PDF generato con successo.");

	        } catch (SQLException | DocumentException | IOException e) {
	            logger.error("Errore durante la generazione del PDF.", e);
	        }
	    }

	public static int menu() {
		int scelta;
		Scanner scanner = new Scanner(System.in);
		System.out.println("|------------------------------------------------------|");
		System.out.println("| 1. Inizializzare                                     |");
		System.out.println("| 2. Estrazione                                        |");
		System.out.println("| 3. Stampa situazione estrazioni                      |");
		System.out.println("| 4. Scrittura su file pdf della situazione estrazioni |");
		System.out.println("| 5. Riinizializzazione del processo                   |");
		System.out.println("|------------------------------------------------------|");
		System.out.print("| Inserisci l'operazione: ");
		scelta = scanner.nextInt();

		return scelta;
	}

}
