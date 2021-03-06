package us.deathmarine.luyten;

import com.strobel.assembler.metadata.MetadataSystem;
import idevcod.CodeTabbedPane;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;
import javax.swing.border.BevelBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.dnd.DropTarget;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Vector;

/**
 * Dispatcher
 */
public class MainWindow extends JFrame {
    private static final long serialVersionUID = 5265556630724988013L;

    private static final String TITLE = "ng-jd-gui";

    private JProgressBar progressBar;
    private JLabel statusLabel;
    FindBox findBox;
    private FindAllBox findAllBox;
    private ConfigSaver configSaver;
    private WindowPosition windowPosition;
    private LuytenPreferences luytenPrefs;
    private FileDialog fileDialog;
    private FileSaver fileSaver;
    private CodeTabbedPane contentTabbedPane;
    private MainMenuBar mainMenuBar;

    public MainWindow() {
        configSaver = ConfigSaver.getLoadedInstance();
        windowPosition = configSaver.getMainWindowPosition();
        luytenPrefs = configSaver.getLuytenPreferences();

        mainMenuBar = new MainMenuBar(this);
        this.setJMenuBar(mainMenuBar);
        this.adjustWindowPositionBySavedState();
        this.setHideFindBoxOnMainWindowFocus();
        this.setShowFindAllBoxOnMainWindowFocus();
        this.setQuitOnWindowClosing();
        this.setTitle(TITLE);
        this.setIconImage(new ImageIcon(
                Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/resources/Luyten.png"))).getImage());


        JSplitPane statusSplitPane = createStatusSplitPane();
        this.add(statusSplitPane, BorderLayout.SOUTH);

        try {
            DropTarget dt = new DropTarget();
            dt.addDropTargetListener(new DropListener(this));
            this.setDropTarget(dt);
        } catch (Exception e) {
            Luyten.showExceptionDialog("Exception!", e);
        }

        fileDialog = new FileDialog(this);
        fileSaver = new FileSaver(progressBar, statusLabel);

        contentTabbedPane = new CodeTabbedPane(this);
        this.getContentPane().add(contentTabbedPane);
    }

    private JSplitPane createStatusSplitPane() {
        statusLabel = new JLabel();
        statusLabel.setHorizontalAlignment(JLabel.LEFT);

        JPanel bottomStatusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomStatusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        bottomStatusPanel.setPreferredSize(new Dimension(this.getWidth() / 2, 20));
        bottomStatusPanel.add(statusLabel);

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setOpaque(false);
        progressBar.setVisible(false);

        JPanel progressPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        progressPanel.setPreferredSize(new Dimension(this.getWidth() / 3, 20));
        progressPanel.add(progressBar);

        JSplitPane statusSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, bottomStatusPanel, progressPanel) {
            private static final long serialVersionUID = 2189946972124687305L;
            private final int location = 400;

            {
                setDividerLocation(location);
            }

            @Override
            public int getDividerLocation() {
                return location;
            }

            @Override
            public int getLastDividerLocation() {
                return location;
            }
        };
        statusSplitPane.setBorder(new BevelBorder(BevelBorder.LOWERED));
        statusSplitPane.setPreferredSize(new Dimension(this.getWidth(), 24));
        return statusSplitPane;
    }

    public void onOpenFileMenu() {
        File selectedFile = fileDialog.doOpenDialog();
        if (selectedFile != null) {
            System.out.println("[Open]: Opening " + selectedFile.getAbsolutePath());

            this.getModel().loadFile(selectedFile);
        }
    }

    public void updateRecentFiles(File file) {
        mainMenuBar.addRecentFile(file);
    }

    public void onSaveAsMenu() {
        RSyntaxTextArea pane = this.getModel().getCurrentTextArea();
        if (pane == null)
            return;
        String tabTitle = this.getModel().getCurrentTabTitle();
        if (tabTitle == null)
            return;

        String recommendedFileName = tabTitle.replace(".class", ".java");
        File selectedFile = fileDialog.doSaveDialog(recommendedFileName);
        if (selectedFile != null) {
            fileSaver.saveText(pane.getText(), selectedFile);
        }
    }

    public void onSaveAllMenu() {
        File openedFile = this.getModel().getOpenedFile();
        if (openedFile == null)
            return;

        String fileName = openedFile.getName();
        if (fileName.endsWith(".class")) {
            fileName = fileName.replace(".class", ".java");
        } else if (fileName.toLowerCase().endsWith(".jar")) {
            fileName = "decompiled-" + fileName.replaceAll("\\.[jJ][aA][rR]", ".zip");
        } else {
            fileName = "saved-" + fileName;
        }

        File selectedFileToSave = fileDialog.doSaveAllDialog(fileName);
        if (selectedFileToSave != null) {
            fileSaver.saveAllDecompiled(openedFile, selectedFileToSave);
        }
    }

    public void onExitMenu() {
        quit();
    }

    public void onSelectAllMenu() {
        try {
            RSyntaxTextArea pane = this.getModel().getCurrentTextArea();
            if (pane != null) {
                pane.requestFocusInWindow();
                pane.setSelectionStart(0);
                pane.setSelectionEnd(pane.getText().length());
            }
        } catch (Exception e) {
            Luyten.showExceptionDialog("Exception!", e);
        }
    }

    public void onFindMenu() {
        try {
            RSyntaxTextArea pane = this.getModel().getCurrentTextArea();
            if (pane != null) {
                if (findBox == null)
                    findBox = new FindBox(this);
                findBox.showFindBox();
            }
        } catch (Exception e) {
            Luyten.showExceptionDialog("Exception!", e);
        }
    }

    public void onFindAllMenu() {
        try {
            if (findAllBox == null)
                findAllBox = new FindAllBox(this);
            findAllBox.showFindBox();

        } catch (Exception e) {
            Luyten.showExceptionDialog("Exception!", e);
        }
    }

    public void onLegalMenu() {
        new Thread() {
            public void run() {
                try {
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                    String legalStr = getLegalStr();
                    MainWindow.this.getModel().showLegal(legalStr);
                } finally {
                    progressBar.setIndeterminate(false);
                    progressBar.setVisible(false);
                }
            }
        }.start();
    }

    public void onListLoadedClasses() {
        try {
            StringBuilder sb = new StringBuilder();
            ClassLoader myCL = Thread.currentThread().getContextClassLoader();
            progressBar.setVisible(true);
            progressBar.setIndeterminate(true);
            while (myCL != null) {
                sb.append("ClassLoader: " + myCL + "\n");
                for (Iterator<?> iter = list(myCL); iter.hasNext(); ) {
                    sb.append("\t" + iter.next() + "\n");
                }
                myCL = myCL.getParent();
            }
            MainWindow.this.getModel().show("Debug", sb.toString());
        } finally {
            progressBar.setIndeterminate(false);
            progressBar.setVisible(false);
        }
    }

    private static Iterator<?> list(ClassLoader CL) {
        Class<?> CL_class = CL.getClass();
        while (CL_class != java.lang.ClassLoader.class) {
            CL_class = CL_class.getSuperclass();
        }
        java.lang.reflect.Field ClassLoader_classes_field;
        try {
            ClassLoader_classes_field = CL_class.getDeclaredField("classes");
            ClassLoader_classes_field.setAccessible(true);
            Vector<?> classes = (Vector<?>) ClassLoader_classes_field.get(CL);
            return classes.iterator();
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            Luyten.showExceptionDialog("Exception!", e);
        }
        return null;
    }

    private String getLegalStr() {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(getClass().getResourceAsStream("/distfiles/Procyon.License.txt")));
            String line;
            while ((line = reader.readLine()) != null)
                sb.append(line).append("\n");
            sb.append("\n\n\n\n\n");
            reader = new BufferedReader(
                    new InputStreamReader(getClass().getResourceAsStream("/distfiles/RSyntaxTextArea.License.txt")));
            while ((line = reader.readLine()) != null)
                sb.append(line).append("\n");
        } catch (IOException e) {
            Luyten.showExceptionDialog("Exception!", e);
        }
        return sb.toString();
    }

    public void onThemesChanged() {
        String themeXml = luytenPrefs.getThemeXml();

        contentTabbedPane.changeTheme(themeXml);
        //  luytenPrefs.setFont_size(this.getModel().getTheme().baseFont.getSize());
    }

    public void onSettingsChanged() {
        this.getModel().updateOpenClasses();
    }

    public void onTreeSettingsChanged() {
        this.getModel().updateTree();
    }

    public void onFileDropped(File file) {
        if (file != null) {
            contentTabbedPane.loadFile(file);
        }
    }

    public void onFileLoadEnded(String fullName, boolean isSuccess) {
        try {
            if (fullName != null && isSuccess) {
                this.setWindowTitle(fullName);
            } else {
                this.setWindowTitle(null);
            }
        } catch (Exception e) {
            Luyten.showExceptionDialog("Exception!", e);
        }
    }

    public void setWindowTitle(String title) {
        if (title == null) {
            this.setTitle(TITLE);
        } else {
            this.setTitle(TITLE + " - " + title);
        }
    }

    public void onNavigationRequest(String uniqueStr) {
        this.getModel().navigateTo(uniqueStr);
    }

    public void closeFile() {
        contentTabbedPane.closeFile();
    }

    public MetadataSystem getMetadataSystem() {
        return contentTabbedPane.getMetadataSystem();
    }

    private void adjustWindowPositionBySavedState() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        if (!windowPosition.isSavedWindowPositionValid()) {
            final Dimension center = new Dimension((int) (screenSize.width * 0.75), (int) (screenSize.height * 0.75));
            final int x = (int) (center.width * 0.2);
            final int y = (int) (center.height * 0.2);
            this.setBounds(x, y, center.width, center.height);

        } else if (windowPosition.isFullScreen()) {
            int heightMinusTray = screenSize.height;
            if (screenSize.height > 30)
                heightMinusTray -= 30;
            this.setBounds(0, 0, screenSize.width, heightMinusTray);
            this.setExtendedState(JFrame.MAXIMIZED_BOTH);

            this.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    if (MainWindow.this.getExtendedState() != JFrame.MAXIMIZED_BOTH) {
                        windowPosition.setFullScreen(false);
                        if (windowPosition.isSavedWindowPositionValid()) {
                            MainWindow.this.setBounds(windowPosition.getWindowX(), windowPosition.getWindowY(),
                                    windowPosition.getWindowWidth(), windowPosition.getWindowHeight());
                        }
                        MainWindow.this.removeComponentListener(this);
                    }
                }
            });

        } else {
            this.setBounds(windowPosition.getWindowX(), windowPosition.getWindowY(), windowPosition.getWindowWidth(),
                    windowPosition.getWindowHeight());
        }
    }

    private void setHideFindBoxOnMainWindowFocus() {
        this.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                if (findBox != null && findBox.isVisible()) {
                    findBox.setVisible(false);
                }
            }
        });
    }

    private void setShowFindAllBoxOnMainWindowFocus() {
        this.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                if (findAllBox != null && findAllBox.isVisible()) {
                    findAllBox.setVisible(false);
                }
            }
        });
    }

    private void setQuitOnWindowClosing() {
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                quit();
            }
        });
    }

    public void quit() {
        try {
            windowPosition.readPositionFromWindow(this);
            configSaver.saveConfig();
        } catch (Exception e) {
            Luyten.showExceptionDialog("Exception!", e);
        } finally {
            try {
                this.dispose();
            } finally {
                System.exit(0);
            }
        }
    }

    public Model getModel() {
        return contentTabbedPane.getCodeModel();
    }

    public void loadFile(File file) {
        contentTabbedPane.loadFile(file);
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    public JLabel getStatusLabel() {
        return statusLabel;
    }

    public LuytenPreferences getLuytenPrefs() {
        return luytenPrefs;
    }
}
