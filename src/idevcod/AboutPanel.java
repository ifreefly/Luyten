package idevcod;

import com.strobel.Procyon;
import us.deathmarine.luyten.Luyten;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

public class AboutPanel extends JPanel {
    private class LinkListener extends MouseAdapter {
        String link;
        JLabel label;

        public LinkListener(String link, JLabel label) {
            this.link = link;
            this.label = label;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            try {
                Desktop.getDesktop().browse(new URI(link));
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            label.setText("<HTML><FONT color=\"#00aa99\"><U>" + link + "</U></FONT></HTML>");
        }

        @Override
        public void mouseExited(MouseEvent e) {
            label.setText("<HTML><FONT color=\"#000099\"><U>" + link + "</U></FONT></HTML>");
        }

    }

    public AboutPanel() {
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        JLabel title = new JLabel("Luyten " + Luyten.getVersion());
        title.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
        this.add(title);
        this.add(new JLabel("by Deathmarine"));
        String project = "https://github.com/deathmarine/Luyten/";
        JLabel link = new JLabel("<HTML><FONT color=\"#000099\"><U>" + project + "</U></FONT></HTML>");
        link.setCursor(new Cursor(Cursor.HAND_CURSOR));
        link.addMouseListener(new LinkListener(project, link));
        this.add(link);
        this.add(new JLabel("Contributions By:"));
        this.add(new JLabel("zerdei, toonetown, dstmath"));
        this.add(new JLabel("virustotalop, xtrafrancyz,"));
        this.add(new JLabel("mbax, quitten, mstrobel,"));
        this.add(new JLabel("FisheyLP, and Syquel"));
        this.add(new JLabel(" "));
        this.add(new JLabel("Powered By:"));
        String procyon = "https://bitbucket.org/mstrobel/procyon";
        link = new JLabel("<HTML><FONT color=\"#000099\"><U>" + procyon + "</U></FONT></HTML>");
        link.setCursor(new Cursor(Cursor.HAND_CURSOR));
        link.addMouseListener(new LinkListener(procyon, link));
        this.add(link);
        this.add(new JLabel("Version: " + Procyon.version()));
        this.add(new JLabel("(c) 2016 Mike Strobel"));
        String rsyntax = "https://github.com/bobbylight/RSyntaxTextArea";
        link = new JLabel("<HTML><FONT color=\"#000099\"><U>" + rsyntax + "</U></FONT></HTML>");
        link.setCursor(new Cursor(Cursor.HAND_CURSOR));
        link.addMouseListener(new LinkListener(rsyntax, link));
        this.add(link);
        this.add(new JLabel("Version: 2.6.1"));
        this.add(new JLabel("(c) 2017 Robert Futrell"));
        this.add(new JLabel(" "));
    }
}
