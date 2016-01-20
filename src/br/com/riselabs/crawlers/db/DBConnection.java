package br.com.riselabs.crawlers.db;
import java.io.File;
import java.sql.*;

/**
 * TODO Creates the connection with the database (MySQL).
 * @author Alcemir R. Santos
 *
 */
public class DBConnection {
	
	public static final String PRODUCTION = "info"+File.separator+"simules-production.db";
	public static final String TEST 	  = "info"+File.separator+"simules-test.db";
	//Variável para carregar o driver
    private static final String STR_DRIVER = "org.sqlite.JDBC";
    //Variável para passar o caminho da conexão juntando com IP e o database
    private static final String STR_CON = "jdbc:sqlite:";
    // indica o ambiente que está sendo utilizado. TEST ou PRODUCTION 
    public static String ENVIROMENT = TEST;

	public DBConnection() {	}
    
    public static Connection getConnection() throws ClassNotFoundException {
        //Variável de conexão
        Connection conn = null;
        try {
            //Carregando o driver
            Class.forName(STR_DRIVER);
            //Estabelecendo conexão
            conn = DriverManager.getConnection(STR_CON+ENVIROMENT);
            //Retornando a conexão
            return conn;
        } catch (ClassNotFoundException e) {
            //Tratando caso não carregue o driver
            String errorMsg = "Driver nao encontrado";
            throw new ClassNotFoundException(errorMsg, e);
        } catch (SQLException e) {
            //Tratando casso não consiga obter a conexão
            String errorMsg = "Erro ao obter a conexao";
            throw new ClassNotFoundException(errorMsg, e);
        }
    }
	
	public static void closeConnection(Connection conn){
		try {
			conn.close();
		} catch (SQLException e) {
			System.err.println("Não foi possível fechar a conexão!"+e.getCause());
		}
	}

	public static void closeConnection(Connection conn, Statement stm){
		try {
			stm.close();
		} catch (SQLException e) {
			System.err.println("Não foi possível fechar a conexão!"+e.getCause());
		}
		closeConnection(conn);		
	}
	
	public static void closeConnection(Connection conn, Statement stm, ResultSet rs){
		try {
			rs.close();
		} catch (SQLException e) {
			System.err.println("Não foi possível fechar a conexão!"+e.getCause());
		}
		closeConnection(conn, stm);		
	}
	
		
}
