import java.sql.*;

public class BaseDades {

    private static final String URL    = "jdbc:mysql://localhost:3306/buscamines";
    private static final String USER   = "root";
    private static final String PASSWD = "Maria906090";

    private Connection connexio;

    // Connectar a la base de dades
    public boolean connectar() {
        try {
            connexio = DriverManager.getConnection(URL, USER, PASSWD);
            System.out.println("Connexió OK");
            return true;
        } catch (SQLException e) {
            System.out.println("Error de connexió: " + e.getMessage());
            return false;
        }
    }

    // Guardar partida quan guanyes o perds
    public boolean guardarPartida(String nomJugador, int idUsuari, int tempsSeg, String resultat, int banderes) {
        try {
            String sql = "INSERT INTO PARTIDA (nom_jugador, id_usuari, temps_segons, resultat, banderes_posades) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = connexio.prepareStatement(sql);
            ps.setString(1, nomJugador);
            ps.setInt(2, idUsuari);
            ps.setInt(3, tempsSeg);
            ps.setString(4, resultat);
            ps.setInt(5, banderes);
            ps.executeUpdate();
            System.out.println("Partida guardada!");
            return true;
        } catch (SQLException e) {
            System.out.println("Error guardant partida: " + e.getMessage());
            return false;
        }
    }

    // Mostrar top 10 jugadors més ràpids
    public void mostrarRecords() {
        try {
            String sql = "SELECT nom_jugador, temps_segons, data_partida FROM PARTIDA WHERE resultat = 'GUANYAT' ORDER BY temps_segons ASC LIMIT 10";
            PreparedStatement ps = connexio.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            System.out.println("=== TOP 10 ===");
            int pos = 1;
            while (rs.next()) {
                System.out.println(pos + ". " + rs.getString("nom_jugador") + " - " + rs.getInt("temps_segons") + "s");
                pos++;
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public String obtenirRecords() {
        StringBuilder sb = new StringBuilder();
        try {
            String sql = "SELECT nom_jugador, temps_segons FROM PARTIDA WHERE resultat = 'GUANYAT' ORDER BY temps_segons ASC LIMIT 10";
            PreparedStatement ps = connexio.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            int pos = 1;
            while (rs.next()) {
                sb.append(pos).append(".  ")
                        .append(rs.getString("nom_jugador"))
                        .append("  -  ")
                        .append(rs.getInt("temps_segons"))
                        .append("s\n");
                pos++;
            }
            if (sb.length() == 0) sb.append("Encara no hi ha records!");
        } catch (SQLException e) {
            sb.append("Error carregant records.");
        }
        return sb.toString();
    }
}