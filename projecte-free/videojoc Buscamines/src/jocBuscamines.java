import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;
import javax.swing.border.*;

public class jocBuscamines {

    static final Color GRIS_INDUSTRIAL = new Color(0xBDC3C7);
    static final Color GRIS_FOSC       = new Color(0x7F8C8D);
    static final Color BLAU_LOGIC      = new Color(0x2980B9);
    static final Color VERD_EXIT       = new Color(0x27AE60);
    static final Color VERMELL_INTENS  = new Color(0xFF1800);
    static final Color GRIS_BLAVOS     = new Color(0x2C3E50);
    static final Color BLANC_CLAR      = new Color(0xECF0F1);

    int midaCasella = 60;
    int numFiles    = 8;
    int numColu     = 8;
    int ComptadorMines = 10;

    JFrame frame = new JFrame("Buscamines");

    JPanel headerPanel = new JPanel();
    JLabel logoLabel   = new JLabel("💣 BUSCAMINES");
    JLabel minesLabel  = new JLabel();
    JLabel tempsLabel  = new JLabel();

    JPanel boardPanel = new JPanel();

    JPanel footerPanel    = new JPanel();
    JButton btnReiniciar  = new JButton("🙂  REINICIAR");
    JButton btnTop10      = new JButton("🏆  TOP 10");

    CasellaMinat[][] tauler = new CasellaMinat[numFiles][numColu];
    ArrayList<CasellaMinat> MinatList;
    Random rand = new Random();

    int MinesClicades = 0;
    int banderes      = 0;
    boolean JocPerdut = false;

    Timer cronometreTimer;
    int segons = 0;

    BaseDades bd = new BaseDades();
    String nomJugador = "Anònim";

    jocBuscamines() {
        carregarFonts();
        bd.connectar();

        String nom = JOptionPane.showInputDialog(
                null,
                "Entra el teu nom:",
                "Buscamines",
                JOptionPane.QUESTION_MESSAGE
        );
        if (nom != null && !nom.trim().isEmpty()) {
            nomJugador = nom.trim();
        }

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(GRIS_BLAVOS);

        construirHeader();
        construirTauler();
        construirFooter();

        frame.add(headerPanel, BorderLayout.NORTH);
        frame.add(boardPanel,  BorderLayout.CENTER);
        frame.add(footerPanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        setMinat();
        actualitzarMinesLabel();
        iniciarCronòmetre();
    }

    void construirHeader() {
        headerPanel.setBackground(GRIS_BLAVOS);
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(10, 16, 10, 16));

        logoLabel.setFont(new Font("Orbitron", Font.BOLD, 16));
        logoLabel.setForeground(GRIS_INDUSTRIAL);

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        statsPanel.setOpaque(false);

        minesLabel.setFont(new Font("Orbitron", Font.BOLD, 16));
        minesLabel.setForeground(VERMELL_INTENS);

        tempsLabel.setFont(new Font("Orbitron", Font.BOLD, 16));
        tempsLabel.setForeground(GRIS_INDUSTRIAL);
        tempsLabel.setText("⏱ 00:00");

        statsPanel.add(minesLabel);
        statsPanel.add(tempsLabel);

        headerPanel.add(logoLabel,  BorderLayout.WEST);
        headerPanel.add(statsPanel, BorderLayout.EAST);
    }

    void construirTauler() {
        boardPanel.setLayout(new GridLayout(numFiles, numColu, 2, 2));
        boardPanel.setBackground(GRIS_FOSC);
        boardPanel.setBorder(new EmptyBorder(3, 3, 3, 3));

        for (int f = 0; f < numFiles; f++) {
            for (int c = 0; c < numColu; c++) {
                CasellaMinat minat = new CasellaMinat(f, c);
                tauler[f][c] = minat;
                estilCasella(minat);
                minat.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        gestionarClick(e);
                    }
                });
                boardPanel.add(minat);
            }
        }
    }

    void estilCasella(CasellaMinat minat) {
        minat.setPreferredSize(new Dimension(midaCasella, midaCasella));
        minat.setFocusable(false);
        minat.setMargin(new Insets(0, 0, 0, 0));
        minat.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        minat.setBackground(GRIS_INDUSTRIAL);
        minat.setForeground(GRIS_BLAVOS);
        minat.setBorder(BorderFactory.createLineBorder(GRIS_FOSC, 1));
        minat.setOpaque(true);
        minat.setText("");
    }

    void construirFooter() {
        footerPanel.setBackground(GRIS_BLAVOS);
        footerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 16, 0));
        footerPanel.setBorder(new EmptyBorder(8, 0, 10, 0));

        // Botó reiniciar
        estilBoto(btnReiniciar);
        btnReiniciar.addActionListener(e -> reiniciar());
        btnReiniciar.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnReiniciar.setBackground(BLANC_CLAR); }
            public void mouseExited(MouseEvent e)  { btnReiniciar.setBackground(GRIS_INDUSTRIAL); }
        });

        // Botó top 10
        estilBoto(btnTop10);
        btnTop10.addActionListener(e -> mostrarTop10());
        btnTop10.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnTop10.setBackground(BLANC_CLAR); }
            public void mouseExited(MouseEvent e)  { btnTop10.setBackground(GRIS_INDUSTRIAL); }
        });

        footerPanel.add(btnReiniciar);
        footerPanel.add(btnTop10);
    }

    void estilBoto(JButton btn) {
        btn.setFont(new Font("Orbitron", Font.BOLD, 13));
        btn.setBackground(GRIS_INDUSTRIAL);
        btn.setForeground(GRIS_BLAVOS);
        btn.setFocusable(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GRIS_FOSC, 1),
                new EmptyBorder(8, 20, 8, 20)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    void mostrarTop10() {
        String records = bd.obtenirRecords();

        JDialog dialog = new JDialog(frame, "🏆 TOP 10", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(GRIS_BLAVOS);

        // Títol
        JLabel titol = new JLabel("🏆  TOP 10 RÈCORDS", JLabel.CENTER);
        titol.setFont(new Font("Orbitron", Font.BOLD, 16));
        titol.setForeground(GRIS_INDUSTRIAL);
        titol.setBorder(new EmptyBorder(16, 0, 8, 0));
        dialog.add(titol, BorderLayout.NORTH);

        // Llista de rècords
        JTextArea textArea = new JTextArea(records);
        textArea.setFont(new Font("Roboto Mono", Font.PLAIN, 14));
        textArea.setBackground(new Color(0x34495E));
        textArea.setForeground(BLANC_CLAR);
        textArea.setEditable(false);
        textArea.setBorder(new EmptyBorder(12, 20, 12, 20));
        dialog.add(textArea, BorderLayout.CENTER);

        // Botó tancar
        JButton btnTancar = new JButton("TANCAR");
        estilBoto(btnTancar);
        btnTancar.addActionListener(e -> dialog.dispose());
        JPanel panelBoto = new JPanel();
        panelBoto.setBackground(GRIS_BLAVOS);
        panelBoto.setBorder(new EmptyBorder(8, 0, 12, 0));
        panelBoto.add(btnTancar);
        dialog.add(panelBoto, BorderLayout.SOUTH);

        dialog.setSize(350, 400);
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    void gestionarClick(MouseEvent e) {
        if (JocPerdut) return;

        CasellaMinat minat = (CasellaMinat) e.getSource();

        if (e.getButton() == MouseEvent.BUTTON1) {
            if (minat.getText().equals("") && !minat.revelada) {
                if (MinatList.contains(minat)) {
                    revelarMines();
                } else {
                    checkMina(minat.f, minat.c);
                }
            }
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            if (minat.getText().equals("") && !minat.revelada) {
                minat.setText("🚩");
                banderes++;
            } else if (minat.getText().equals("🚩")) {
                minat.setText("");
                banderes--;
            }
            actualitzarMinesLabel();
        }
    }

    void setMinat() {
        MinatList = new ArrayList<CasellaMinat>();
        int MinesRestants = ComptadorMines;
        while (MinesRestants > 0) {
            int f = rand.nextInt(numFiles);
            int c = rand.nextInt(numColu);
            CasellaMinat minat = tauler[f][c];
            if (!MinatList.contains(minat)) {
                MinatList.add(minat);
                MinesRestants--;
            }
        }
    }

    void revelarMines() {
        for (CasellaMinat minat : MinatList) {
            minat.setText("💣");
            minat.setBackground(VERMELL_INTENS);
            minat.setForeground(Color.WHITE);
        }
        JocPerdut = true;
        cronometreTimer.stop();
        btnReiniciar.setText("😢  REINICIAR");

        headerPanel.setBackground(VERMELL_INTENS);
        footerPanel.setBackground(VERMELL_INTENS);
        minesLabel.setText("HAS PERDUT!");
        minesLabel.setForeground(Color.WHITE);
        tempsLabel.setForeground(Color.WHITE);

        bd.guardarPartida(nomJugador, 1, segons, "PERDUT", banderes);
    }

    void checkMina(int f, int c) {
        if (f < 0 || f >= numFiles || c < 0 || c >= numColu) return;

        CasellaMinat minat = tauler[f][c];
        if (minat.revelada) return;
        minat.revelada = true;
        MinesClicades++;

        int MinesTrobades = 0;
        MinesTrobades += MinesContades(f-1, c-1);
        MinesTrobades += MinesContades(f-1, c);
        MinesTrobades += MinesContades(f-1, c+1);
        MinesTrobades += MinesContades(f,   c-1);
        MinesTrobades += MinesContades(f,   c+1);
        MinesTrobades += MinesContades(f+1, c-1);
        MinesTrobades += MinesContades(f+1, c);
        MinesTrobades += MinesContades(f+1, c+1);

        if (MinesTrobades > 0) {
            minat.setText(Integer.toString(MinesTrobades));
            minat.setBackground(BLANC_CLAR);
            switch (MinesTrobades) {
                case 1: minat.setForeground(BLAU_LOGIC);     break;
                case 2: minat.setForeground(VERD_EXIT);      break;
                case 3: minat.setForeground(VERMELL_INTENS); break;
                default: minat.setForeground(GRIS_BLAVOS);   break;
            }
        } else {
            minat.setText("");
            minat.setBackground(BLANC_CLAR);
            checkMina(f-1, c-1); checkMina(f-1, c); checkMina(f-1, c+1);
            checkMina(f,   c-1);                    checkMina(f,   c+1);
            checkMina(f+1, c-1); checkMina(f+1, c); checkMina(f+1, c+1);
        }

        if (MinesClicades == numFiles * numColu - MinatList.size()) {
            JocPerdut = true;
            cronometreTimer.stop();
            btnReiniciar.setText("😊  REINICIAR");

            headerPanel.setBackground(VERD_EXIT);
            footerPanel.setBackground(VERD_EXIT);
            minesLabel.setText("MINES TROBADES!");
            minesLabel.setForeground(Color.WHITE);
            tempsLabel.setForeground(Color.WHITE);

            bd.guardarPartida(nomJugador, 1, segons, "GUANYAT", banderes);
        }
    }

    int MinesContades(int f, int c) {
        if (f < 0 || f >= numFiles || c < 0 || c >= numColu) return 0;
        return MinatList.contains(tauler[f][c]) ? 1 : 0;
    }

    void iniciarCronòmetre() {
        segons = 0;
        cronometreTimer = new Timer(1000, e -> {
            segons++;
            int m = segons / 60;
            int s = segons % 60;
            tempsLabel.setText(String.format("⏱ %02d:%02d", m, s));
        });
        cronometreTimer.start();
    }

    void reiniciar() {
        MinesClicades = 0;
        banderes = 0;
        JocPerdut = false;
        btnReiniciar.setText("🙂  REINICIAR");

        headerPanel.setBackground(GRIS_BLAVOS);
        footerPanel.setBackground(GRIS_BLAVOS);
        minesLabel.setForeground(VERMELL_INTENS);
        tempsLabel.setForeground(GRIS_INDUSTRIAL);

        boardPanel.removeAll();
        tauler = new CasellaMinat[numFiles][numColu];
        for (int f = 0; f < numFiles; f++) {
            for (int c = 0; c < numColu; c++) {
                CasellaMinat minat = new CasellaMinat(f, c);
                tauler[f][c] = minat;
                estilCasella(minat);
                minat.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) { gestionarClick(e); }
                });
                boardPanel.add(minat);
            }
        }
        boardPanel.revalidate();
        boardPanel.repaint();

        if (cronometreTimer != null) cronometreTimer.stop();
        iniciarCronòmetre();
        setMinat();
        actualitzarMinesLabel();
    }

    void actualitzarMinesLabel() {
        int restants = ComptadorMines - banderes;
        minesLabel.setText("💣 " + restants);
    }

    void carregarFonts() {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Font orbitron = Font.createFont(Font.TRUETYPE_FONT,
                    new java.io.File("C:\\Users\\maria\\Downloads\\Orbitron\\Orbitron-VariableFont_wght.ttf"));
            Font roboto = Font.createFont(Font.TRUETYPE_FONT,
                    new java.io.File("C:\\Users\\maria\\Downloads\\Roboto_Mono\\RobotoMono-VariableFont_wght.ttf"));
            ge.registerFont(orbitron);
            ge.registerFont(roboto);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class CasellaMinat extends JButton {
        int f, c;
        boolean revelada = false;
        CasellaMinat(int f, int c) {
            this.f = f;
            this.c = c;
        }
    }
}