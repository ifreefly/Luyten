package idevcod;

import com.strobel.assembler.metadata.MetadataSystem;
import us.deathmarine.luyten.Luyten;
import us.deathmarine.luyten.MainWindow;
import us.deathmarine.luyten.Model;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

public class CodeTabbedPane extends JTabbedPane {
    private MainWindow window;

    public CodeTabbedPane(MainWindow window) {
        this.window = window;
        addChangeListener(event -> {
            {
                Model model = (Model) getSelectedComponent();
                if (model == null) {
                    return;
                }

                window.setWindowTitle(model.getFullName());
            }
        });
    }

    private void setExitOnEscWhenEnabled(JComponent mainComponent) {
        Action escapeAction = new AbstractAction() {
            private static final long serialVersionUID = -3460391555954575248L;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (window.getLuytenPrefs().isExitByEscEnabled()) {
                    window.quit();
                }
            }
        };
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        mainComponent.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(escapeKeyStroke, "ESCAPE");
        mainComponent.getActionMap().put("ESCAPE", escapeAction);
    }

    public Model getCodeModel() {
        if (getTabCount() == 0) {
            return null;
        }

        return (Model) getComponentAt(getSelectedIndex());
    }

    public MetadataSystem getMetadataSystem() {
        if (getTabCount() == 0) {
            return null;
        }

        return ((Model) getComponentAt(getSelectedIndex())).getMetadataSystem();
    }

    public void loadFile(File file) {
        try {
            String fullName = file.getCanonicalPath();
            int index = indexOfTab(fullName);
            if (index < 0) {
                addModel(file, fullName);
            } else {
                setSelectedIndex(index);
                window.setWindowTitle(fullName);
            }
        } catch (IOException e) {
            Luyten.showExceptionDialog("Exception!", e);
        }
    }

    void addModel(File file, String fullName) {
        Model model = new Model(fullName, window);
        model.startWarmUpThread();
        setExitOnEscWhenEnabled(model);

        this.add(fullName, model);

        int index = indexOfTab(fullName);
        Tab ct = new Tab(file.getName());
        ct.getButton().addMouseListener(new CloseTab(model));
        setTabComponentAt(index, ct);

        model.loadFile(file);

        setSelectedIndex(index);
    }

    public void closeFile() {
        Model model = getCodeModel();
        if (model == null) {
            return;
        }

        if (model.getTabCount() > 0) {
            model.closeCurrentTab();
        } else {
            closeModel(model);
        }
    }

    public void changeTheme(String themeXml) {
        int count = getTabCount();
        if (count == 0) {
            return;
        }

        for (int i = 0; i < count; i++) {
            Model model = (Model) getComponentAt(i);
            model.changeTheme(themeXml);
        }
    }

    private class CloseTab extends MouseAdapter {
        private Model model;

        public CloseTab(Model model) {
            this.model = model;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            closeModel(model);
        }
    }

    private void closeModel(Model model) {
        model.closeFile();
        remove(model);
    }
}
