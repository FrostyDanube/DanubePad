/**
 * Copyright FrostyDanube - All Rights Reserved.
 * DanubePad 1.2
 * ----------------------------------------
 * This program is free software, you can distribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as published by
 * the Free Software Foundation.
 */

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import javax.swing.tree.*;

public class Notepad extends JFrame implements ActionListener {
    private JTextPane textPane;
    private JFileChooser fileChooser;
    private JMenuItem newItem, openItem, saveItem, openFolderItem, exitItem;
    private JMenuItem cutItem, copyItem, pasteItem;
    private JMenuItem creditsItem, licenseItem;

    private JRadioButtonMenuItem plainItem, yamlItem, jsItem, htmlItem, cssItem,
            sassItem, jsonItem, pythonItem, tsItem, phpItem, goItem;
    private JRadioButtonMenuItem lightTheme, darkTheme, pastelTheme, moonTheme,
            directxTheme, linuxTheme, htmlTheme, grubTheme, milkTheme;

    private JPanel sideBarPanel;
    private JTree folderTree;
    private JScrollPane treeScrollPane;
    private File activeFile = null;
    private SyntaxHighlighter currentHighlighter;
    private ThemeManager themeManager;
    private AutoCompleteManager autoCompleteManager;

    public Notepad() {
        super("DanubePad - Untitled");

        textPane = new JTextPane();
        textPane.setFont(new Font("Consolas", Font.PLAIN, 14));
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
        JMenu helpMenu = new JMenu("Help");

        newItem = new JMenuItem("New");
        openItem = new JMenuItem("Open");
        saveItem = new JMenuItem("Save");
        openFolderItem = new JMenuItem("Open Folder...");
        exitItem = new JMenuItem("Exit");
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));

        cutItem = new JMenuItem("Cut");
        copyItem = new JMenuItem("Copy");
        pasteItem = new JMenuItem("Paste");

        creditsItem = new JMenuItem("Credits");
        licenseItem = new JMenuItem("License (GPLv2)");

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

        for (JRadioButtonMenuItem item : syntaxItems) {
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
        htmlTheme = new JRadioButtonMenuItem("Paper");
        grubTheme = new JRadioButtonMenuItem("BIOS");
        milkTheme = new JRadioButtonMenuItem("Milk");

        JRadioButtonMenuItem[] themeItems = {
            lightTheme, darkTheme, pastelTheme, moonTheme,
            directxTheme, linuxTheme, htmlTheme, grubTheme, milkTheme
        };

        for (JRadioButtonMenuItem item : themeItems) {
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
        creditsItem.addActionListener(this);
        licenseItem.addActionListener(this);

        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(openFolderItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        editMenu.add(cutItem);
        editMenu.add(copyItem);
        editMenu.add(pasteItem);

        helpMenu.add(creditsItem);
        helpMenu.add(licenseItem);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(syntaxMenu);
        menuBar.add(themesMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        sideBarPanel = new JPanel(new BorderLayout());
        sideBarPanel.setPreferredSize(new Dimension(240, 0));

        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("No Folder Opened");
        folderTree = new JTree(rootNode);
        folderTree.setRowHeight(22);
        folderTree.setCellRenderer(new FileTreeCellRenderer());
        folderTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    TreePath path = folderTree.getPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                        if (node.getUserObject() instanceof FileNode) {
                            FileNode fNode = (FileNode) node.getUserObject();
                            if (fNode.file.isFile()) {
                                openFileContent(fNode.file);
                            }
                        }
                    }
                }
            }
        });

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

        autoCompleteManager = new AutoCompleteManager(textPane);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == newItem) {
            textPane.setText("");
            activeFile = null;
            setTitle("DanubePad - Untitled");
        } else if (source == openItem) {
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                openFileContent(fileChooser.getSelectedFile());
            }
        } else if (source == openFolderItem) {
            JFolderChooser folderChooser = new JFolderChooser();
            if (folderChooser.showOpenDialog(this) == JFolderChooser.APPROVE_OPTION) {
                populateFolderTree(folderChooser.getSelectedFile());
                sideBarPanel.setVisible(true);
                revalidate();
            }
        } else if (source == saveItem) {
            saveCurrentFile();
        } else if (source == exitItem) {
            System.exit(0);
        } else if (source == cutItem) {
            textPane.cut();
        } else if (source == copyItem) {
            textPane.copy();
        } else if (source == pasteItem) {
            textPane.paste();
        } else if (source == creditsItem) {
            showCreditsDialog();
        } else if (source == licenseItem) {
            showLicenseDialog();
        } else {
            String lang = getSyntaxLanguageSelection(source);
            if (lang != null) {
                currentHighlighter.setLanguage(lang);
                autoCompleteManager.setLanguage(lang);
                return;
            }

            String theme = getThemeSelection(source);
            if (theme != null) {
                themeManager.applyTheme(theme);
                currentHighlighter.updateThemeStyles(
                        themeManager.getKeywordColor(),
                        themeManager.getCommentColor(),
                        themeManager.getStringColor(),
                        themeManager.getNumberColor(),
                        themeManager.getDefaultColor(),
                        themeManager.getErrorColor()
                );
            }
        }
    }

    private void showCreditsDialog() {
        String credits = "<html><body style='width: 300px; padding: 10px; font-family: Segoe UI, sans-serif;'>"
                + "<h2 style='margin-bottom: 5px; color: #005c8a;'>DanubePad 1.2</h2>"
                + "<p><b>Created by:</b> FrostyDanube</p>"
                + "<p>A text editor with syntax</p>"
                + "<hr>"
                + "<p style='font-size: 10px; color: #666;'>© Copyright FrostyDanube. All Rights Reserved.</p>"
                + "</body></html>";
        JOptionPane.showMessageDialog(this, credits, "Credits - DanubePad", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showLicenseDialog() {
        String licenseText = "GNU GENERAL PUBLIC LICENSE\n"
                + "Version 2, June 1991\n\n"
                + "Copyright (C) 1989, 1991 Free Software Foundation, Inc.\n"
                + "51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA\n\n"
                + "Everyone is permitted to copy and distribute verbatim copies\n"
                + "of this license document, but changing it is not allowed.\n\n"
                + "Preamble:\n"
                + "The licenses for most software are designed to take away your\n"
                + "freedom to share and change it. By contrast, the GNU General\n"
                + "Public License is intended to guarantee your freedom to share\n"
                + "and change free software--to make sure the software is free for\n"
                + "all its users.\n\n"
                + "This program is dwistributed in the hope that it will be useful,\n"
                + "but WITHOUT ANY WARRANTY; without even the implied warranty of\n"
                + "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the\n"
                + "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the\n"
                + "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the\n"
                + "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the\n"
                + "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the\n"
                + "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the\n"
                + "GNU General Public License for more details.";

        JTextArea textArea = new JTextArea(licenseText);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setPreferredSize(new Dimension(500, 350));

        JOptionPane.showMessageDialog(this, scroll, "License - GNU GPLv2", JOptionPane.PLAIN_MESSAGE);
    }

    private void saveCurrentFile() {
        if (activeFile != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(activeFile))) {
                writer.write(textPane.getText());
                setTitle("DanubePad - " + activeFile.getName() + " (Saved)");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Could not save file to: " + activeFile.getAbsolutePath());
            }
        } else {
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                activeFile = fileChooser.getSelectedFile();
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(activeFile))) {
                    writer.write(textPane.getText());
                    setTitle("DanubePad - " + activeFile.getName());
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Could not save file.");
                }
            }
        }
    }

    private void openFileContent(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
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

            String selectedLang = getCurrentlySelectedLanguage();
            currentHighlighter.setLanguage(selectedLang);
            autoCompleteManager.setLanguage(selectedLang);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not open file.");
        }
    }

    private String getCurrentlySelectedLanguage() {
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

    private String getSyntaxLanguageSelection(Object source) {
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

    private String getThemeSelection(Object source) {
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

    private void populateFolderTree(File dir) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(new FileNode(dir));
        buildTreeNodes(dir, root);
        folderTree.setModel(new DefaultTreeModel(root));
    }

    private void buildTreeNodes(File file, DefaultMutableTreeNode parent) {
        File[] files = file.listFiles();
        if (files != null) {
            // Sort: directories first, then files
            Arrays.sort(files, (f1, f2) -> {
                if (f1.isDirectory() && !f2.isDirectory()) return -1;
                if (!f1.isDirectory() && f2.isDirectory()) return 1;
                return f1.getName().compareToIgnoreCase(f2.getName());
            });

            for (File f : files) {
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(new FileNode(f));
                parent.add(node);
                if (f.isDirectory()) {
                    buildTreeNodes(f, node);
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new Notepad().setVisible(true));
    }
}

/* Custom File Tree Renderer for better iconography */
class FileTreeCellRenderer extends DefaultTreeCellRenderer {
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean sel, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        if (node.getUserObject() instanceof FileNode) {
            FileNode fileNode = (FileNode) node.getUserObject();
            File file = fileNode.file;
            if (file.isDirectory()) {
                setIcon(expanded ? UIManager.getIcon("Tree.openIcon") : UIManager.getIcon("Tree.closedIcon"));
            } else {
                setIcon(UIManager.getIcon("Tree.leafIcon"));
            }
        }
        return this;
    }
}

class FileNode {
    File file;
    public FileNode(File file) { this.file = file; }
    @Override
    public String toString() { return file.getName().isEmpty() ? file.getPath() : file.getName(); }
}

class JFolderChooser extends JFileChooser {
    public JFolderChooser() {
        super();
        setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    }
}

/* Syntax Highlighting & Syntax Error Detection */
class SyntaxHighlighter {
    private JTextPane textPane;
    private String language;
    private boolean updating = false;

    private final StyleContext cont = StyleContext.getDefaultStyleContext();
    private AttributeSet defaultStyle;
    private AttributeSet keywordStyle;
    private AttributeSet stringStyle;
    private AttributeSet commentStyle;
    private AttributeSet numberStyle;
    private AttributeSet errorStyle;

    public SyntaxHighlighter(JTextPane textPane, String language) {
        this.textPane = textPane;
        this.language = language;

        updateThemeStyles(
                new Color(0, 102, 204),
                new Color(128, 128, 128),
                new Color(34, 139, 34),
                new Color(178, 34, 34),
                new Color(50, 50, 50),
                new Color(255, 0, 0)
        );

        textPane.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { process(); }
            public void removeUpdate(DocumentEvent e) { process(); }
            public void changedUpdate(DocumentEvent e) { process(); }
        });
    }

    public void updateThemeStyles(Color kw, Color comm, Color str, Color num, Color def, Color err) {
        keywordStyle = cont.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, kw);
        commentStyle = cont.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, comm);
        stringStyle = cont.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, str);
        numberStyle = cont.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, num);
        defaultStyle = cont.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, def);

        SimpleAttributeSet errAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(errAttr, err);
        StyleConstants.setBold(errAttr, true);
        StyleConstants.setUnderline(errAttr, true);
        errorStyle = errAttr;

        rehighlight();
    }

    public void setLanguage(String language) {
        this.language = language;
        rehighlight();
    }

    public void rehighlight() {
        process();
    }

    private synchronized void process() {
        if (updating) return;
        updating = true;

        SwingUtilities.invokeLater(() -> {
            StyledDocument doc = textPane.getStyledDocument();
            String text;
            try {
                text = doc.getText(0, doc.getLength());
            } catch (BadLocationException e) {
                updating = false;
                return;
            }

            doc.setCharacterAttributes(0, text.length(), defaultStyle, true);

            if (!language.equals("Plain")) {
                applyHighlighting(doc, text);
            }
            highlightSyntaxErrors(doc, text);

            updating = false;
        });
    }

    private void applyHighlighting(StyledDocument doc, String text) {
        String keywords = getKeywords(language);
        String comments = getCommentPattern(language);

        String regex = "(?<KEYWORD>\\b(" + keywords + ")\\b)" +
                "|(?<COMMENT>" + comments + ")" +
                "|(?<STRING>\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*')" +
                "|(?<NUMBER>\\b\\d+(\\.\\d+)?\\b)";

        Matcher matcher = Pattern.compile(regex, Pattern.MULTILINE).matcher(text);

        while (matcher.find()) {
            if (matcher.group("KEYWORD") != null) {
                doc.setCharacterAttributes(matcher.start("KEYWORD"), matcher.end("KEYWORD") - matcher.start("KEYWORD"), keywordStyle, true);
            } else if (matcher.group("COMMENT") != null) {
                doc.setCharacterAttributes(matcher.start("COMMENT"), matcher.end("COMMENT") - matcher.start("COMMENT"), commentStyle, true);
            } else if (matcher.group("STRING") != null) {
                doc.setCharacterAttributes(matcher.start("STRING"), matcher.end("STRING") - matcher.start("STRING"), stringStyle, true);
            } else if (matcher.group("NUMBER") != null) {
                doc.setCharacterAttributes(matcher.start("NUMBER"), matcher.end("NUMBER") - matcher.start("NUMBER"), numberStyle, true);
            }
        }
    }

    /* Syntax Error Highlighting for unmatched brackets/quotes & trailing tokens */
    private void highlightSyntaxErrors(StyledDocument doc, String text) {
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '(' || ch == '{' || ch == '[') {
                stack.push(i);
            } else if (ch == ')' || ch == '}' || ch == ']') {
                if (stack.isEmpty()) {
                    doc.setCharacterAttributes(i, 1, errorStyle, true);
                } else {
                    char open = text.charAt(stack.peek());
                    if ((ch == ')' && open == '(') || (ch == '}' && open == '{') || (ch == ']' && open == '[')) {
                        stack.pop();
                    } else {
                        doc.setCharacterAttributes(i, 1, errorStyle, true);
                    }
                }
            }
        }

        // Highlight unclosed opening brackets
        while (!stack.isEmpty()) {
            int pos = stack.pop();
            doc.setCharacterAttributes(pos, 1, errorStyle, true);
        }

        // Check for unclosed string quotes per line
        String[] lines = text.split("\n", -1);
        int currentOffset = 0;
        for (String line : lines) {
            boolean inDouble = false, inSingle = false;
            int doubleStart = -1, singleStart = -1;

            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (c == '\\') {
                    i++; // skip escaped char
                    continue;
                }
                if (c == '"' && !inSingle) {
                    inDouble = !inDouble;
                    if (inDouble) doubleStart = i;
                } else if (c == '\'' && !inDouble) {
                    inSingle = !inSingle;
                    if (inSingle) singleStart = i;
                }
            }

            if (inDouble && doubleStart != -1) {
                doc.setCharacterAttributes(currentOffset + doubleStart, line.length() - doubleStart, errorStyle, false);
            }
            if (inSingle && singleStart != -1) {
                doc.setCharacterAttributes(currentOffset + singleStart, line.length() - singleStart, errorStyle, false);
            }

            currentOffset += line.length() + 1;
        }
    }

    private String getKeywords(String lang) {
        switch (lang) {
            case "JS":
            case "TypeScript":
                return "var|let|const|function|return|if|else|for|while|class|import|export|default|switch|case|break|new|this|typeof|async|await";
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

    private String getCommentPattern(String lang) {
        switch (lang) {
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

/* Auto-complete Popup Manager */
class AutoCompleteManager {
    private JTextPane textPane;
    private JPopupMenu popupMenu;
    private JList<String> suggestionList;
    private DefaultListModel<String> listModel;
    private String language = "Plain";

    private static final Map<String, List<String>> DICTIONARIES = new HashMap<>();

    static {
        DICTIONARIES.put("JS", Arrays.asList("function", "const", "let", "var", "return", "document", "window", "console.log", "addEventListener", "async", "await", "import", "export"));
        DICTIONARIES.put("TypeScript", Arrays.asList("function", "const", "let", "interface", "type", "namespace", "return", "async", "await", "implements", "readonly"));
        DICTIONARIES.put("Python", Arrays.asList("def", "class", "return", "import", "from", "print", "self", "elif", "except", "lambda", "with"));
        DICTIONARIES.put("HTML", Arrays.asList("<div>", "<span>", "<p>", "<a href=\"\">", "<script>", "<style>", "<link>", "<meta>", "<html>", "<body>"));
        DICTIONARIES.put("CSS", Arrays.asList("background-color", "color", "margin", "padding", "display: flex;", "border", "font-size", "position: relative;"));
        DICTIONARIES.put("Sass", Arrays.asList("@mixin", "@include", "@extend", "background-color", "color", "margin", "padding"));
        DICTIONARIES.put("PHP", Arrays.asList("function", "public", "private", "protected", "echo", "foreach", "namespace", "use", "$this"));
        DICTIONARIES.put("Go", Arrays.asList("func", "package", "import", "struct", "interface", "fmt.Println", "make", "append", "return"));
        DICTIONARIES.put("Json", Arrays.asList("\"name\":", "\"version\":", "\"description\":", "true", "false", "null"));
        DICTIONARIES.put("Yaml", Arrays.asList("version:", "services:", "image:", "ports:", "environment:"));
    }

    public AutoCompleteManager(JTextPane textPane) {
        this.textPane = textPane;
        this.popupMenu = new JPopupMenu();
        this.listModel = new DefaultListModel<>();
        this.suggestionList = new JList<>(listModel);
        this.suggestionList.setFocusable(false);

        popupMenu.add(new JScrollPane(suggestionList));
        popupMenu.setFocusable(false);

        suggestionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    insertSelectedCompletion();
                }
            }
        });

        textPane.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN && popupMenu.isVisible()) {
                    int next = Math.min(suggestionList.getSelectedIndex() + 1, listModel.getSize() - 1);
                    suggestionList.setSelectedIndex(next);
                    return;
                }
                if (e.getKeyCode() == KeyEvent.VK_UP && popupMenu.isVisible()) {
                    int prev = Math.max(suggestionList.getSelectedIndex() - 1, 0);
                    suggestionList.setSelectedIndex(prev);
                    return;
                }
                if ((e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_TAB) && popupMenu.isVisible()) {
                    insertSelectedCompletion();
                    e.consume();
                    return;
                }
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE && popupMenu.isVisible()) {
                    popupMenu.setVisible(false);
                    return;
                }

                checkForSuggestions();
            }
        });
    }

    public void setLanguage(String lang) {
        this.language = lang;
    }

    private void checkForSuggestions() {
        List<String> keywords = DICTIONARIES.get(language);
        if (keywords == null || keywords.isEmpty()) {
            popupMenu.setVisible(false);
            return;
        }

        String word = getCurrentWord();
        if (word.length() < 2) {
            popupMenu.setVisible(false);
            return;
        }

        listModel.clear();
        for (String kw : keywords) {
            if (kw.toLowerCase().startsWith(word.toLowerCase()) && !kw.equalsIgnoreCase(word)) {
                listModel.addElement(kw);
            }
        }

        if (!listModel.isEmpty()) {
            suggestionList.setSelectedIndex(0);
            try {
                Rectangle caretRect = textPane.modelToView(textPane.getCaretPosition());
                if (caretRect != null) {
                    popupMenu.show(textPane, caretRect.x, caretRect.y + caretRect.height);
                    textPane.requestFocusInWindow();
                }
            } catch (BadLocationException ignored) {}
        } else {
            popupMenu.setVisible(false);
        }
    }

    private String getCurrentWord() {
        int caretPos = textPane.getCaretPosition();
        String text = textPane.getText();
        int start = caretPos - 1;
        while (start >= 0 && Character.isJavaIdentifierPart(text.charAt(start))) {
            start--;
        }
        return text.substring(start + 1, caretPos);
    }

    private void insertSelectedCompletion() {
        String selected = suggestionList.getSelectedValue();
        if (selected == null) return;

        String word = getCurrentWord();
        int caretPos = textPane.getCaretPosition();
        int start = caretPos - word.length();

        try {
            Document doc = textPane.getDocument();
            doc.remove(start, word.length());
            doc.insertString(start, selected, null);
        } catch (BadLocationException ignored) {}

        popupMenu.setVisible(false);
    }
}

/* Distinct Theme Manager */
class ThemeManager {
    private JTextPane textPane;
    private JScrollPane scrollPane;
    private JMenuBar menuBar;
    private JPanel sideBarPanel;
    private JTree folderTree;
    private JScrollPane treeScrollPane;

    private Color keywordColor, commentColor, stringColor, numberColor, defaultColor, errorColor;

    public ThemeManager(JTextPane textPane, JScrollPane scrollPane, JMenuBar menuBar,
                        JPanel sideBarPanel, JTree folderTree, JScrollPane treeScrollPane) {
        this.textPane = textPane;
        this.scrollPane = scrollPane;
        this.menuBar = menuBar;
        this.sideBarPanel = sideBarPanel;
        this.folderTree = folderTree;
        this.treeScrollPane = treeScrollPane;
    }

    public void applyTheme(String themeName) {
        Color bg, fg, menuBg, menuFg;
        errorColor = new Color(255, 65, 54);

        switch (themeName) {
            case "Dark":
                bg = new Color(30, 30, 30);
                fg = new Color(220, 220, 220);
                menuBg = new Color(45, 45, 48);
                menuFg = new Color(220, 220, 220);
                keywordColor = new Color(86, 156, 214);
                commentColor = new Color(87, 166, 74);
                stringColor = new Color(214, 157, 133);
                numberColor = new Color(181, 206, 168);
                defaultColor = fg;
                break;

            case "Pastel":
                bg = new Color(253, 246, 227);
                fg = new Color(101, 123, 131);
                menuBg = new Color(238, 232, 213);
                menuFg = new Color(101, 123, 131);
                keywordColor = new Color(38, 139, 210);
                commentColor = new Color(147, 161, 161);
                stringColor = new Color(42, 161, 152);
                numberColor = new Color(211, 54, 130);
                defaultColor = fg;
                break;

            case "Moon":
                bg = new Color(15, 18, 25);
                fg = new Color(169, 177, 214);
                menuBg = new Color(26, 30, 43);
                menuFg = new Color(169, 177, 214);
                keywordColor = new Color(187, 154, 247);
                commentColor = new Color(86, 95, 137);
                stringColor = new Color(156, 207, 120);
                numberColor = new Color(255, 158, 100);
                defaultColor = fg;
                break;

            case "DirectX":
                bg = new Color(5, 5, 25);
                fg = new Color(0, 255, 204);
                menuBg = new Color(10, 10, 50);
                menuFg = new Color(0, 255, 204);
                keywordColor = new Color(255, 0, 128);
                commentColor = new Color(70, 90, 130);
                stringColor = new Color(255, 230, 0);
                numberColor = new Color(0, 255, 100);
                defaultColor = fg;
                break;

            case "Linux":
                bg = new Color(12, 12, 12);
                fg = new Color(57, 255, 20);
                menuBg = new Color(25, 25, 25);
                menuFg = new Color(57, 255, 20);
                keywordColor = new Color(255, 184, 108);
                commentColor = new Color(98, 114, 164);
                stringColor = new Color(241, 250, 140);
                numberColor = new Color(189, 147, 249);
                defaultColor = fg;
                break;

            case "Html":
                bg = new Color(255, 253, 240);
                fg = new Color(40, 40, 40);
                menuBg = new Color(240, 235, 210);
                menuFg = new Color(40, 40, 40);
                keywordColor = new Color(160, 32, 240);
                commentColor = new Color(100, 149, 237);
                stringColor = new Color(178, 34, 34);
                numberColor = new Color(205, 133, 63);
                defaultColor = fg;
                break;

            case "Grub":
                bg = new Color(0, 0, 128);
                fg = new Color(255, 255, 255);
                menuBg = new Color(0, 0, 80);
                menuFg = new Color(255, 255, 255);
                keywordColor = new Color(255, 255, 0);
                commentColor = new Color(0, 255, 255);
                stringColor = new Color(0, 255, 0);
                numberColor = new Color(255, 0, 255);
                defaultColor = fg;
                break;

            case "Milk":
                bg = new Color(250, 248, 242);
                fg = new Color(80, 70, 60);
                menuBg = new Color(235, 230, 220);
                menuFg = new Color(80, 70, 60);
                keywordColor = new Color(180, 100, 50);
                commentColor = new Color(140, 135, 125);
                stringColor = new Color(70, 130, 80);
                numberColor = new Color(190, 80, 80);
                defaultColor = fg;
                break;

            case "Light":
            default:
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
        }

        textPane.setBackground(bg);
        textPane.setCaretColor(fg);
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
    public Color getErrorColor() { return errorColor; }
}
