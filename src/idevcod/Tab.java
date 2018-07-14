package idevcod;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;

public class Tab extends JPanel {
    private static final long serialVersionUID = -514663009333644974L;
    private JLabel closeButton = new JLabel(new ImageIcon(
            Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/resources/icon_close.png"))));
    private JLabel tabTitle;
    private String title;

    public Tab(String t) {
        super(new GridBagLayout());
        this.setOpaque(false);

        this.title = t;
        this.tabTitle = new JLabel(title);

        this.createTab();
    }

    public JLabel getButton() {
        return this.closeButton;
    }

    public void createTab() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        this.add(tabTitle, gbc);
        gbc.gridx++;
        gbc.insets = new Insets(0, 5, 0, 0);
        gbc.anchor = GridBagConstraints.EAST;
        this.add(closeButton, gbc);
    }
}
