/**
 * @CuriosityDanube
 * DanubePad 1.1
 */
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class Notepad extends JFrame implements ActionListener
{
    private JTextPane textPane;
    private JFileChooser fileChooser;
    private JMenuItem newItem;
    private JMenuItem openItem;
    private JMenuItem saveItem;
    private JMenuItem exitItem;
    private JMenuItem cutItem;
    private JMenuItem copyItem;
    private JMenuItem pasteItem;
    private JRadioButtonMenuItem yamlItem;
    private JRadioButtonMenuItem jsItem;
    private JRadioButtonMenuItem htmlItem;
    private JRadioButtonMenuItem cssItem;
    private JRadioButtonMenuItem sassItem;
    private JRadioButtonMenuItem jsonItem;
    private JRadioButtonMenuItem pythonItem;
    private JRadioButtonMenuItem tsItem;
    private JRadioButtonMenuItem phpItem;
    private JRadioButtonMenuItem goItem;
    private JRadioButtonMenuItem plainItem;
    private JRadioButtonMenuItem darkTheme;
    private JRadioButtonMenuItem lightTheme;
    private JRadioButtonMenuItem pastelTheme;
    private JRadioButtonMenuItem moonTheme;
    private JRadioButtonMenuItem directxTheme;
    private JRadioButtonMenuItem linuxTheme;
    private JRadioButtonMenuItem htmlTheme;
    private JRadioButtonMenuItem grubTheme;
    private JRadioButtonMenuItem milkTheme;
    private JPanel sideBarPanel;
    private JTree folderTree;
    private JScrollPane treeScrollPane;
    private JMenuItem openFolderItem;
    private boolean isSidebarVisible = false;
    private File activeFile = null;
    private SyntaxHighlighter currentHighlighter;
    private ThemeManager themeManager;
    private File currentWorkingDir = null;

    public Notepad()
    {
        super("DanubePad - Untitled");

        textPane = new JTextPane();
        textPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textPane.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        fileChooser = new JFileChooser();
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        JMenu fileMenu = new JMenu("File");
        JMenu editMenu = new JMenu("Edit");
        JMenu syntaxMenu = new JMenu("Syntax");
        JMenu themesMenu = new JMenu("Themes");
        newItem = new JMenuItem("New");
        openItem = new JMenuItem("Open");
        saveItem = new JMenuItem("Save");
        openFolderItem = new JMenuItem("Open Folder...");
        exitItem = new JMenuItem("Exit");
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        cutItem = new JMenuItem("Cut");
        copyItem = new JMenuItem("Copy");
        pasteItem = new JMenuItem("Paste");
        ButtonGroup syntaxGroup = new ButtonGroup();
        plainItem = new JRadioButtonMenuItem("Plain Text", true);
        yamlItem = new JRadioButtonMenuItem("Yaml");
        jsItem = new JRadioButtonMenuItem("JS");
        htmlItem = new JRadioButtonMenuItem("HTML");
        cssItem = new JRadioButtonMenuItem("CSS");
        sassItem = new JRadioButtonMenuItem("Sass");
        jsonItem = new JRadioButtonMenuItem("Json");
        pythonItem = new JRadioButtonMenuItem("Python");
        tsItem = new JRadioButtonMenuItem("TypeScript");
        phpItem = new JRadioButtonMenuItem("PHP");
        goItem = new JRadioButtonMenuItem("Go");

        JRadioButtonMenuItem[] syntaxItems = {
            plainItem, yamlItem, jsItem, htmlItem, cssItem, 
            sassItem, jsonItem, pythonItem, tsItem, phpItem, goItem
        };

        for (JRadioButtonMenuItem item : syntaxItems)
        {
            syntaxGroup.add(item);
            item.addActionListener(this);
            syntaxMenu.add(item);
        }

        ButtonGroup themeGroup = new ButtonGroup();
        lightTheme = new JRadioButtonMenuItem("Light", true);
        darkTheme = new JRadioButtonMenuItem("Dark");
        pastelTheme = new JRadioButtonMenuItem("Pastel");
        moonTheme = new JRadioButtonMenuItem("Moon");
        directxTheme = new JRadioButtonMenuItem("DirectX");
        linuxTheme = new JRadioButtonMenuItem("Linux");
        htmlTheme = new JRadioButtonMenuItem("Html");
        grubTheme = new JRadioButtonMenuItem("Grub");
        milkTheme = new JRadioButtonMenuItem("Milk");

        JRadioButtonMenuItem[] themeItems = {
            lightTheme, darkTheme, pastelTheme, moonTheme, 
            directxTheme, linuxTheme, htmlTheme, grubTheme, milkTheme
        };

        for (JRadioButtonMenuItem item : themeItems)
        {
            themeGroup.add(item);
            item.addActionListener(this);
            themesMenu.add(item);
        }

        newItem.addActionListener(this);
        openItem.addActionListener(this);
        saveItem.addActionListener(this);
        openFolderItem.addActionListener(this);
        exitItem.addActionListener(this);
        cutItem.addActionListener(this);
        copyItem.addActionListener(this);
        pasteItem.addActionListener(this);
        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(openFolderItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        editMenu.add(cutItem);
        editMenu.add(copyItem);
        editMenu.add(pasteItem);
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(syntaxMenu);
        menuBar.add(themesMenu);

        setJMenuBar(menuBar);

        sideBarPanel = new JPanel(new BorderLayout());
        sideBarPanel.setPreferredSize(new Dimension(240, 0));
        
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("No Folder Opened");
        folderTree = new JTree(rootNode);
        folderTree.setRowHeight(24);
        treeScrollPane = new JScrollPane(folderTree);
        treeScrollPane.setBorder(BorderFactory.createEmptyBorder());
        sideBarPanel.add(treeScrollPane, BorderLayout.CENTER);
        
        add(sideBarPanel, BorderLayout.WEST);
        sideBarPanel.setVisible(false);

        add(scrollPane, BorderLayout.CENTER);

        setSize(1100, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        currentHighlighter = new SyntaxHighlighter(textPane, "Plain");
        themeManager = new ThemeManager(textPane, scrollPane, menuBar, sideBarPanel, folderTree, treeScrollPane);
        themeManager.applyTheme("Light");
    }

    public void actionPerformed(ActionEvent e)
    {
        Object source = e.getSource();

        if (source == newItem)
        {
            textPane.setText("");
            activeFile = null;
            setTitle("DanubePad - Untitled");
        }
        else if (source == openItem)
        {
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            {
                File file = fileChooser.getSelectedFile();
                openFileContent(file);
            }
        }
        else if (source == openFolderItem)
        {
            JFolderChooser folderChooser = new JFolderChooser();
            if (folderChooser.showOpenDialog(this) == JFolderChooser.APPROVE_OPTION)
            {
                currentWorkingDir = folderChooser.getSelectedFile();
                populateFolderTree(currentWorkingDir);
                sideBarPanel.setVisible(true);
                isSidebarVisible = true;
                revalidate();
            }
        }
        else if (source == saveItem)
        {
            saveCurrentFile();
        }
        else if (source == exitItem)
        {
            System.exit(0);
        }
        else if (source == cutItem)
        {
            textPane.cut();
        }
        else if (source == copyItem)
        {
            textPane.copy();
        }
        else if (source == pasteItem)
        {
            textPane.paste();
        }
        else 
        {
            String lang = getSyntaxLanguageSelection(source);
            if (lang != null)
            {
                currentHighlighter.setLanguage(lang);
                return;
            }

            String theme = getThemeSelection(source);
            if (theme != null)
            {
                themeManager.applyTheme(theme);
                currentHighlighter.updateThemeStyles(themeManager.getKeywordColor(), themeManager.getCommentColor(), 
                                                   themeManager.getStringColor(), themeManager.getNumberColor(), 
                                                   themeManager.getDefaultColor());
            }
        }
    }

    private void saveCurrentFile()
    {
        if (activeFile != null)
        {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(activeFile)))
            {
                writer.write(textPane.getText());
                setTitle("DanubePad - " + activeFile.getName() + " (Saved)");
            }
            catch (IOException ex)
            {
                JOptionPane.showMessageDialog(this, "Could not save file to: " + activeFile.getAbsolutePath());
            }
        }
        else
        {
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
            {
                activeFile = fileChooser.getSelectedFile();
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(activeFile)))
                {
                    writer.write(textPane.getText());
                    setTitle("DanubePad - " + activeFile.getName());
                }
                catch (IOException ex)
                {
                    JOptionPane.showMessageDialog(this, "Could not save file.");
                }
            }
        }
    }

    private void openFileContent(File file)
    {
        try (BufferedReader reader = new BufferedReader(new FileReader(file)))
        {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
            {
                sb.append(line).append("\n");
            }
            textPane.setText(sb.toString());
            activeFile = file;
            setTitle("DanubePad - " + file.getName());
            
            String name = file.getName().toLowerCase();
            if (name.endsWith(".js")) jsItem.setSelected(true);
            else if (name.endsWith(".html")) htmlItem.setSelected(true);
            else if (name.endsWith(".css")) cssItem.setSelected(true);
            else if (name.endsWith(".scss") || name.endsWith(".sass")) sassItem.setSelected(true);
            else if (name.endsWith(".json")) jsonItem.setSelected(true);
            else if (name.endsWith(".py")) pythonItem.setSelected(true);
            else if (name.endsWith(".ts")) tsItem.setSelected(true);
            else if (name.endsWith(".php")) phpItem.setSelected(true);
            else if (name.endsWith(".go")) goItem.setSelected(true);
            else if (name.endsWith(".yml") || name.endsWith(".yaml")) yamlItem.setSelected(true);
            else plainItem.setSelected(true);
            
            currentHighlighter.setLanguage(getCurrentlySelectedLanguage());
        }
        catch (IOException ex)
        {
            JOptionPane.showMessageDialog(this, "Could not open file.");
        }
    }

    private String getCurrentlySelectedLanguage()
    {
        if (jsItem.isSelected()) return "JS";
        if (htmlItem.isSelected()) return "HTML";
        if (cssItem.isSelected()) return "CSS";
        if (sassItem.isSelected()) return "Sass";
        if (jsonItem.isSelected()) return "Json";
        if (pythonItem.isSelected()) return "Python";
        if (tsItem.isSelected()) return "TypeScript";
        if (phpItem.isSelected()) return "PHP";
        if (goItem.isSelected()) return "Go";
        if (yamlItem.isSelected()) return "Yaml";
        return "Plain";
    }

    private String getSyntaxLanguageSelection(Object source)
    {
        if (source == yamlItem) return "Yaml";
        if (source == jsItem) return "JS";
        if (source == htmlItem) return "HTML";
        if (source == cssItem) return "CSS";
        if (source == sassItem) return "Sass";
        if (source == jsonItem) return "Json";
        if (source == pythonItem) return "Python";
        if (source == tsItem) return "TypeScript";
        if (source == phpItem) return "PHP";
        if (source == goItem) return "Go";
        if (source == plainItem) return "Plain";
        return null;
    }

    private String getThemeSelection(Object source)
    {
        if (source == lightTheme) return "Light";
        if (source == darkTheme) return "Dark";
        if (source == pastelTheme) return "Pastel";
        if (source == moonTheme) return "Moon";
        if (source == directxTheme) return "DirectX";
        if (source == linuxTheme) return "Linux";
        if (source == htmlTheme) return "Html";
        if (source == grubTheme) return "Grub";
        if (source == milkTheme) return "Milk";
        return null;
    }

    private void populateFolderTree(File dir)
    {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(new FileNode(dir));
        buildTreeNodes(dir, root);
        folderTree.setModel(new DefaultTreeModel(root));
        
        folderTree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) folderTree.getLastSelectedPathComponent();
            if (node != null && node.getUserObject() instanceof FileNode) {
                FileNode fNode = (FileNode) node.getUserObject();
                if (fNode.file.isFile()) {
                    openFileContent(fNode.file);
                }
            }
        });
    }

    private void buildTreeNodes(File file, DefaultMutableTreeNode parent)
    {
        File[] files = file.listFiles();
        if (files != null)
        {
            for (File f : files)
            {
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(new FileNode(f));
                parent.add(node);
                if (f.isDirectory())
                {
                    buildTreeNodes(f, node);
                }
            }
        }
    }

    public static void main(String[] args)
    {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new Notepad().setVisible(true));
    }
}

class FileNode
{
    File file;
    public FileNode(File file) { this.file = file; }
    public String toString() { return file.getName(); }
}

class JFolderChooser extends JFileChooser
{
    public JFolderChooser()
    {
        super();
        setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    }
}

class SyntaxHighlighter
{
    private JTextPane textPane;
    private String language;
    private boolean updating = false;

    private final StyleContext cont = StyleContext.getDefaultStyleContext();
    private AttributeSet defaultStyle;
    private AttributeSet keywordStyle;
    private AttributeSet stringStyle;
    private AttributeSet commentStyle;
    private AttributeSet numberStyle;

    public SyntaxHighlighter(JTextPane textPane, String language)
    {
        this.textPane = textPane;
        this.language = language;

        updateThemeStyles(new Color(0, 102, 204), new Color(128, 128, 128), 
                          new Color(34, 139, 34), new Color(178, 34, 34), new Color(50, 50, 50));

        textPane.getDocument().addDocumentListener(new javax.swing.event.DocumentListener()
        {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { process(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { process(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { process(); }
        });
    }

    public void updateThemeStyles(Color kw, Color comm, Color str, Color num, Color def)
    {
        keywordStyle = cont.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, kw);
        commentStyle = cont.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, comm);
        stringStyle = cont.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, str);
        numberStyle = cont.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, num);
        defaultStyle = cont.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, def);
        rehighlight();
    }

    public void setLanguage(String language)
    {
        this.language = language;
        rehighlight();
    }

    public void rehighlight()
    {
        process();
    }

    private synchronized void process()
    {
        if (updating || language.equals("Plain")) return;
        updating = true;

        SwingUtilities.invokeLater(() -> {
            StyledDocument doc = textPane.getStyledDocument();
            String text = "";
            try
            {
                text = doc.getText(0, doc.getLength());
            }
            catch (BadLocationException e)
            {
                updating = false;
                return;
            }

            doc.setCharacterAttributes(0, text.length(), defaultStyle, true);
            applyHighlighting(doc, text);
            updating = false;
        });
    }

    private void applyHighlighting(StyledDocument doc, String text)
    {
        String keywords = getKeywords(language);
        String comments = getCommentPattern(language);
        
        String regex = "(?<KEYWORD>\\b(" + keywords + ")\\b)" +
                       "|(?<COMMENT>" + comments + ")" +
                       "|(?<STRING>\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*')" +
                       "|(?<NUMBER>\\b\\d+(\\.\\d+)?\\b)";

        Matcher matcher = Pattern.compile(regex, Pattern.MULTILINE).matcher(text);

        while (matcher.find())
        {
            if (matcher.group("KEYWORD") != null)
            {
                doc.setCharacterAttributes(matcher.start("KEYWORD"), matcher.end("KEYWORD") - matcher.start("KEYWORD"), keywordStyle, true);
            }
            else if (matcher.group("COMMENT") != null)
            {
                doc.setCharacterAttributes(matcher.start("COMMENT"), matcher.end("COMMENT") - matcher.start("COMMENT"), commentStyle, true);
            }
            else if (matcher.group("STRING") != null)
            {
                doc.setCharacterAttributes(matcher.start("STRING"), matcher.end("STRING") - matcher.start("STRING"), stringStyle, true);
            }
            else if (matcher.group("NUMBER") != null)
            {
                doc.setCharacterAttributes(matcher.start("NUMBER"), matcher.end("NUMBER") - matcher.start("NUMBER"), numberStyle, true);
            }
        }
    }

    private String getKeywords(String lang)
    {
        switch (lang)
        {
            case "JS":
            case "TypeScript":
                return "var|let|const|function|return|if|else|for|while|class|import|export|default|switch|case|break|new|this|typeof";
            case "Python":
                return "def|return|if|elif|else|for|while|class|import|from|as|try|except|with|lambda|True|False|None|in|is|not|and|or";
            case "PHP":
                return "echo|function|return|if|else|foreach|class|public|private|protected|include|require|namespace|use";
            case "Go":
                return "func|return|if|else|for|range|var|const|package|import|struct|interface|go|chan|defer";
            case "HTML":
                return "html|head|body|div|span|a|p|script|style|link|meta|title|table|tr|td";
            case "CSS":
            case "Sass":
                return "color|background|margin|padding|font|display|flex|border|width|height|position|top|left";
            case "Yaml":
            case "Json":
                return "true|false|null";
            default:
                return "";
        }
    }

    private String getCommentPattern(String lang)
    {
        switch (lang)
        {
            case "Python":
            case "Yaml":
                return "(#.*)";
            case "HTML":
                return "(<!--[\\s\\S]*?-->)";
            case "CSS":
            case "Sass":
                return "(/\\*[\\s\\S]*?\\*/)";
            case "JS":
            case "TypeScript":
            case "PHP":
            case "Go":
                return "(//.*|/\\*[\\s\\S]*?\\*/)";
            default:
                return "($^)";
        }
    }
}

/* Theme manager */
class ThemeManager
{
    private JTextPane textPane;
    private JScrollPane scrollPane;
    private JMenuBar menuBar;
    private JPanel sideBarPanel;
    private JTree folderTree;
    private JScrollPane treeScrollPane;

    private Color keywordColor, commentColor, stringColor, numberColor, defaultColor;

    public ThemeManager(JTextPane textPane, JScrollPane scrollPane, JMenuBar menuBar, 
                        JPanel sideBarPanel, JTree folderTree, JScrollPane treeScrollPane)
    {
        this.textPane = textPane;
        this.scrollPane = scrollPane;
        this.menuBar = menuBar;
        this.sideBarPanel = sideBarPanel;
        this.folderTree = folderTree;
        this.treeScrollPane = treeScrollPane;
    }

    public void applyTheme(String themeName)
    {
        Color bg = Color.WHITE;
        Color fg = Color.BLACK;
        Color menuBg = new Color(245, 245, 247);
        Color menuFg = Color.BLACK;

        switch (themeName)
        {
            case "Light":
                bg = new Color(255, 255, 255);
                fg = new Color(38, 38, 38);
                menuBg = new Color(242, 242, 247);
                menuFg = new Color(38, 38, 38);
                keywordColor = new Color(0, 92, 197);
                commentColor = new Color(112, 128, 144);
                stringColor = new Color(0, 128, 0);
                numberColor = new Color(139, 0, 139);
                defaultColor = fg;
                break;

            case "Dark":
                bg = new Color(18, 18, 18);
                fg = new Color(224, 224, 224);
                menuBg = new Color(30, 30, 30);
                menuFg = new Color(224, 224, 224);
                keywordColor = new Color(86, 156, 214);
                commentColor = new Color(92, 143, 92);
                stringColor = new Color(214, 157, 133);
                numberColor = new Color(181, 206, 168);
                defaultColor = fg;
                break;

            case "Pastel":
                bg = new Color(250, 247, 240);
                fg = new Color(84, 94, 107);
                menuBg = new Color(236, 230, 218);
                menuFg = new Color(84, 94, 107);
                keywordColor = new Color(74, 144, 226);
                commentColor = new Color(155, 166, 177);
                stringColor = new Color(65, 175, 135);
                numberColor = new Color(230, 105, 145);
                defaultColor = fg;
                break;

            case "Moon":
                bg = new Color(21, 23, 33);
                fg = new Color(196, 203, 230);
                menuBg = new Color(28, 31, 45);
                menuFg = new Color(196, 203, 230);
                keywordColor = new Color(199, 146, 234);
                commentColor = new Color(103, 110, 140);
                stringColor = new Color(195, 232, 141);
                numberColor = new Color(247, 140, 108);
                defaultColor = fg;
                break;

            case "DirectX":
                bg = new Color(10, 10, 48);
                fg = new Color(230, 242, 255);
                menuBg = new Color(0, 0, 96);
                menuFg = new Color(255, 255, 255);
                keywordColor = new Color(51, 204, 255);
                commentColor = new Color(140, 140, 170);
                stringColor = new Color(51, 255, 153);
                numberColor = new Color(255, 255, 102);
                defaultColor = fg;
                break;

            case "Linux":
                bg = new Color(24, 10, 24);
                fg = new Color(51, 255, 51);
                menuBg = new Color(42, 14, 42);
                menuFg = new Color(51, 255, 51);
                keywordColor = new Color(255, 255, 102);
                commentColor = new Color(120, 120, 120);
                stringColor = new Color(255, 102, 255);
                numberColor = new Color(102, 255, 255);
                defaultColor = fg;
                break;

            case "Html":
                bg = new Color(254, 254, 245);
                fg = new Color(34, 34, 34);
                menuBg = new Color(235, 235, 245);
                menuFg = new Color(0, 0, 102);
                keywordColor = new Color(178, 34, 34);
                commentColor = new Color(46, 139, 87);
                stringColor = new Color(0, 0, 205);
                numberColor = new Color(210, 105, 30);
                defaultColor = fg;
                break;

            case "Grub":
                bg = new Color(12, 12, 12);
                fg = new Color(200, 200, 200);
                menuBg = new Color(0, 0, 139);
                menuFg = new Color(255, 255, 255);
                keywordColor = new Color(255, 255, 0);
                commentColor = new Color(0, 255, 255);
                stringColor = new Color(0, 255, 0);
                numberColor = new Color(255, 0, 255);
                defaultColor = fg;
                break;

            case "Milk":
                bg = new Color(252, 250, 245);
                fg = new Color(74, 68, 61);
                menuBg = new Color(240, 235, 224);
                menuFg = new Color(74, 68, 61);
                keywordColor = new Color(196, 118, 62);
                commentColor = new Color(153, 143, 133);
                stringColor = new Color(92, 140, 92);
                numberColor = new Color(194, 92, 92);
                defaultColor = fg;
                break;
        }

        textPane.setBackground(bg);
        textPane.setForeground(fg);
        scrollPane.setBackground(bg);
        
        menuBar.setBackground(menuBg);
        menuBar.setForeground(menuFg);
        
        sideBarPanel.setBackground(menuBg);
        folderTree.setBackground(bg);
        folderTree.setForeground(fg);
        treeScrollPane.setBackground(bg);
    }

    public Color getKeywordColor() { return keywordColor; }
    public Color getCommentColor() { return commentColor; }
    public Color getStringColor() { return stringColor; }
    public Color getNumberColor() { return numberColor; }
    public Color getDefaultColor() { return defaultColor; }
}