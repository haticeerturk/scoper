package burp;

import java.awt.*;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.datatransfer.DataFlavor;
import java.awt.Dimension;
import java.awt.event.*;

import java.io.IOException;
import java.lang.Boolean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;

import javax.swing.*;
import javax.swing.border.*;

import org.json.JSONArray;
import org.json.JSONObject;

@SuppressWarnings("all")
public class BurpExtender implements IBurpExtender, ITab {
    static IBurpExtenderCallbacks callbacks;
    private static IExtensionHelpers helpers;
    static final JTextArea textArea = new JTextArea();
    static String[] scope = null;
    static MyTableModel myTable;
    static Boolean overwriteScope = false;

    private static void createOverwriteCheckbox(JPanel frame) {
        JCheckBox overwrightCheckbox = new JCheckBox("Overwrite current scope");
        overwrightCheckbox.setBorder(new EmptyBorder(8, 16, 0, 0));

        overwrightCheckbox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    overwriteScope = true;
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    overwriteScope = false;
                }
            }
        });

        frame.add(overwrightCheckbox, BorderLayout.PAGE_START);
    }

    private static void createPasteVerifyClearButtons(JPanel panel) {
        JPanel leadingPane = new JPanel();

        leadingPane.setLayout(new BoxLayout(leadingPane, BoxLayout.PAGE_AXIS));
        leadingPane.setPreferredSize(new Dimension(130, 170));
        leadingPane.setBorder(new EmptyBorder(12, 16, 16, 0));

        // Create Paste button
        JButton pasteButton = new JButton("Paste");
        pasteButton.setMaximumSize(new Dimension(110, 27));

        pasteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String copiedData = "";

                try {
                    copiedData = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
                } catch (UnsupportedFlavorException ex) {
                    throw new RuntimeException(ex);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

                textArea.setText(copiedData);
            }
        });

        leadingPane.add(pasteButton);
        leadingPane.add(Box.createVerticalStrut(8));

        // Create Verify button
        JButton verifyButton = new JButton("Verify");
        verifyButton.setMaximumSize(new Dimension(110, 27));

        verifyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Pattern regex = Pattern.compile("(?:(?:https?|ftp):\\/\\/)?(\\*)?[\\w/\\-?=%.]+\\.[\\w/\\-&?=%.]+");
                Matcher regexMatcher = regex.matcher("a.com " + textArea.getText());

                if (regexMatcher.find()) {
                    String aa = regexMatcher.group();
                    scope = regexMatcher.results().map(MatchResult::group).toArray(String[]::new);
                    myTable.setNewData(scope);
                }
            }
        });

        leadingPane.add(verifyButton);
        leadingPane.add(Box.createVerticalStrut(8));

        // Create Clear button
        JButton clearButton = new JButton("Clear");
        clearButton.setMaximumSize(new Dimension(110, 27));

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.setText("");
            }
        });

        leadingPane.add(clearButton);
        panel.add(leadingPane, BorderLayout.LINE_START);
    }

    private static void createTextArea(JPanel panel) {
        JPanel trailingPane = new JPanel();

        trailingPane.setLayout(new BoxLayout(trailingPane, BoxLayout.PAGE_AXIS));
        trailingPane.setBorder(new EmptyBorder(12, 4, 16, 0));

        textArea.setMinimumSize(new Dimension(500, 300));
        textArea.setSize(new Dimension(500, 300));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane sp = new JScrollPane(textArea);
        sp.setMaximumSize(new Dimension(500, 300));
        sp.setSize(new Dimension(500, 300));
        sp.setMinimumSize(new Dimension(500, 300));
        sp.setPreferredSize(new Dimension(500, 300));

        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        trailingPane.add(sp);
        panel.add(trailingPane);
    }

    private static void createAddToScopeClearButtons(JPanel panel) {
        JPanel tableButtonsPane = new JPanel();

        tableButtonsPane.setLayout(new BoxLayout(tableButtonsPane, BoxLayout.PAGE_AXIS));
        tableButtonsPane.setBorder(new EmptyBorder(12, 20, 16, 0));

        // Add To Scope Button
        JButton addToScopeButton = new JButton("Add To Scope");
        addToScopeButton.setMaximumSize(new Dimension(160, 27));
        tableButtonsPane.add(addToScopeButton);
        tableButtonsPane.add(Box.createVerticalStrut(8));

        addToScopeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<String> burpInScopeList = new ArrayList<String>();
                List<String> burpExcludeScopeList = new ArrayList<String>();

                // If overwrite checkbox is selected, fetch the user's scopes and add them to related lists
                if (!overwriteScope) {
                    String scopeOptions = callbacks.saveConfigAsJson("target.scope");
                    JSONObject jsonScopeOptionsObject = new JSONObject(scopeOptions);

                    JSONArray includeList = jsonScopeOptionsObject.getJSONObject("target").getJSONObject("scope").getJSONArray("include");
                    for (Object object : includeList) {
                        burpInScopeList.add(object.toString());
                    }

                    JSONArray excludeList = jsonScopeOptionsObject.getJSONObject("target").getJSONObject("scope").getJSONArray("exclude");
                    for (Object object : excludeList) {
                        burpExcludeScopeList.add(object.toString());
                    }
                }

                // Get data from the table
                Object[][] tableData = myTable.getData();

                // Prepare include and exclude scopes
                for (int i = 0; i < tableData.length; i++) {
                    Integer fullScopeColumnIndex = myTable.getColumnIndex("Full Scope");
                    Integer ignoreColumnIndex = myTable.getColumnIndex("Ignore");
                    Integer scopeColumnIndex = myTable.getColumnIndex("Scope");
                    Integer excludeColumnIndex = myTable.getColumnIndex("OOS");
                    Integer formatColumnIndex = myTable.getColumnIndex("Format");

                    // If the ignore checkbox is selected, do not do anything
                    if ((Boolean) tableData[i][ignoreColumnIndex]) {
                        continue;
                    }

                    String scope = tableData[i][scopeColumnIndex].toString();
                    String domain = "";
                    String host;
                    String file = "";

                    // Change the domain string to the type of scope.
                    if ((Boolean) tableData[i][formatColumnIndex]) { // If the row is format for OOS
                        String format = scope.replaceAll(",", "|").replaceAll(" ", "");
                        file = "^/.*\\.(" + format + ").*";
                    } else if ((Boolean) tableData[i][fullScopeColumnIndex]) { // If scope is a full scope
                        domain = scope.replaceAll("\\.", "\\\\.");
                    } else { // If scope is not a full scope
                        String[] splittedDomain = scope.split("//");
                        domain = splittedDomain.length > 1 ? splittedDomain[1] : splittedDomain[0];

                        if (domain.endsWith("/")) {
                            domain = domain.subSequence(0, domain.length() - 1).toString();
                        }
                        domain = domain.replaceAll("\\.", "\\\\.");
                    }

                    host = domain.length() > 0 ? String.format("^%s$", domain) : "";

                    // Create a Scope object with given values
                    Scope sc = new Scope(host, file);
                    JSONObject jsonScope = sc.toJSON();

                    Boolean excludeScope = (Boolean) tableData[i][excludeColumnIndex];

                    // Ignore duplicate scopes
                    if ((!excludeScope && burpInScopeList.contains(jsonScope.toString())) ||
                            (excludeScope && burpExcludeScopeList.contains(jsonScope.toString()))) {
                        continue;
                    }

                    if (excludeScope) {
                        burpExcludeScopeList.add(jsonScope.toString());
                    } else {
                        burpInScopeList.add(jsonScope.toString());
                    }
                }

                // Final scope options
                String includeScopeOptionsText = "";
                if (burpInScopeList.size() != 0) {
                    includeScopeOptionsText = ", \"include\":" + burpInScopeList.toString();
                }

                String excludeScopeOptionsText = "";
                if (burpExcludeScopeList.size() != 0) {
                    excludeScopeOptionsText = ", \"exclude\":" + burpExcludeScopeList.toString();
                }

                // Add values to Burp Suite's include scope
                callbacks.loadConfigFromJson("{\"target\":{\"scope\":{\"advanced_mode\":true"+ includeScopeOptionsText + excludeScopeOptionsText + "}}}");
            }
        });

        // Clear Button
        JButton scopeClearButton = new JButton("Clear");
        scopeClearButton.setMaximumSize(new Dimension(160, 27));

        scopeClearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                myTable.setNewData(null);
            }
        });

        tableButtonsPane.add(scopeClearButton);
        panel.add(tableButtonsPane, BorderLayout.LINE_START);
    }

    private static void createTable(JPanel panel) {
        JPanel tablePane = new JPanel();

        tablePane.setLayout(new BoxLayout(tablePane, BoxLayout.PAGE_AXIS));
        tablePane.setBorder(new EmptyBorder(12, 8, 16, 0));

        myTable = new MyTableModel();
        JTable table = new JTable(myTable);
        table.setAutoCreateRowSorter(true);
        table.removeColumn( table.getColumn("Format") ); // Do not show format column

        table.setMaximumSize(new Dimension(600, 300));
        table.setMinimumSize(new Dimension(600, 300));
        table.setSize(new Dimension(600, 300));
        table.setFillsViewportHeight(true);

        table.getColumnModel().getColumn(0).setPreferredWidth(300);

        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setMaximumSize(new Dimension(600, 300));
        scrollPane.setMinimumSize(new Dimension(600, 300));
        scrollPane.setSize(new Dimension(600, 300));
        scrollPane.setPreferredSize(new Dimension(600, 300));
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setAlignmentX(JComponent.LEFT_ALIGNMENT);

        //Add the scroll pane to this panel.
        tablePane.add(scrollPane, BoxLayout.X_AXIS);
        panel.add(tablePane);
    }

    public void registerExtenderCallbacks (IBurpExtenderCallbacks callbacks) {
        //Callback Objects
        BurpExtender.callbacks = callbacks;
        BurpExtender.helpers = callbacks.getHelpers();

        //Extension Name
        callbacks.setExtensionName("Scoper");

        callbacks.addSuiteTab(this);

        callbacks.printOutput("Thank you for your installed!");
    }

    public Component getUiComponent() {
        // Create and set up the main Panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        JScrollPane scrollPanel = new JScrollPane(mainPanel);
        scrollPanel.setBorder(null);
        scrollPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

        // First Part
        createOverwriteCheckbox(mainPanel);

        // Left Part
        JPanel leftPartPanel = new JPanel(new BorderLayout());

        createPasteVerifyClearButtons(leftPartPanel);
        createTextArea(leftPartPanel);

        mainPanel.add(leftPartPanel, BorderLayout.LINE_START);

        // Right Part
        JPanel rightPartPanel = new JPanel(new BorderLayout());

        createAddToScopeClearButtons(rightPartPanel);
        createTable(rightPartPanel);

        mainPanel.add(rightPartPanel, BorderLayout.CENTER);

        return scrollPanel;
    }

    public String getTabCaption() {
        return "Scoper";
    }
}