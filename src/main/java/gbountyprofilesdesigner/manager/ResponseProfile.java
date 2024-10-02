/*
Copyright 2018 Eduardo Garcia Melia <wagiro@gmail.com>

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package gbountyprofilesdesigner.manager;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import gbountyprofilesdesigner.gui.GBountyProfilesGui;
import gbountyprofilesdesigner.gui.NewTag;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.RowSorter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author eduardogarcia
 */
public class ResponseProfile extends javax.swing.JPanel {

    /**
     * Creates new form RequestProfile
     */
    DefaultListModel encoder;
    DefaultListModel tag;
    DefaultListModel tagmanager;
    DefaultTableModel modelgrep;
    String filename;
    JComboBox operator;
    JComboBox match_type;
    JComboBox options;

    public ResponseProfile() {

        encoder = new DefaultListModel();
        tag = new DefaultListModel();
        tagmanager = new DefaultListModel();
        modelgrep = new DefaultTableModel();
        operator = new JComboBox();
        match_type = new JComboBox();
        options = new JComboBox();

        modelgrep = new DefaultTableModel() {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                Class clazz = String.class;
                switch (columnIndex) {
                    case 0:
                        clazz = Boolean.class;
                        break;
                }
                return clazz;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                if (row == 0 && column == 1) {
                    return false;
                } else {
                    return true;
                }
            }

        };

        initComponents();

        filename = GBountyProfilesGui.filename;

        showTags();
        showGrepsTable();

    }

    public void showGrepsTable() {

        modelgrep.setNumRows(0);
        modelgrep.setColumnCount(0);
        modelgrep.addColumn("Enabled");
        modelgrep.addColumn("Operator");
        modelgrep.addColumn("Match Type");
        modelgrep.addColumn("Options");
        modelgrep.addColumn("Value");
        
        

        operator.addItem("AND");
        operator.addItem("AND NOT");
        operator.addItem("OR");
        operator.addItem("OR NOT");
        
        
        match_type.addItem("Simple String");
        match_type.addItem("Regex");
        match_type.addItem("Status Code");
        match_type.addItem("Content Type");
        match_type.addItem("Content Length");
        match_type.addItem("URL Extension");
        
        
        options.addItem("");
        options.addItem("Case sensitive");
        options.addItem("Only in Headers");
        options.addItem("Not in Headers");

        table4.getColumnModel().getColumn(0).setPreferredWidth(5);
        table4.getColumnModel().getColumn(1).setPreferredWidth(15);
        table4.getColumnModel().getColumn(2).setPreferredWidth(90);
        table4.getColumnModel().getColumn(3).setPreferredWidth(55);
        table4.getColumnModel().getColumn(4).setPreferredWidth(280);
        table4.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(operator));
        table4.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(match_type));
        table4.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(options));

        TableRowSorter<TableModel> sorter = new TableRowSorter<>(table4.getModel());
        table4.setRowSorter(sorter);
        table4.getTableHeader().setReorderingAllowed(false);
        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sorter.setSortKeys(sortKeys);
        sorter.sort();

    }

    public void showGreps(List<String> greps) {

        for (String grepline : greps) {
            List<String> array = Arrays.asList(grepline.split(",", 5));
            if (array.size() > 1) {
                if (modelgrep.getRowCount() == 0) {
                    if (array.get(0).equals("true")) {
                        modelgrep.addRow(new Object[]{true, "", array.get(2),array.get(3),array.get(4)});
                    } else {
                        modelgrep.addRow(new Object[]{false, "", array.get(2),array.get(3),array.get(4)});
                    }
                } else {
                    if (array.get(0).equals("true")) {
                        modelgrep.addRow(new Object[]{true, array.get(1), array.get(2),array.get(3),array.get(4)});
                    } else {
                        modelgrep.addRow(new Object[]{false, array.get(1), array.get(2),array.get(3),array.get(4)});
                    }
                }
            } else {
                if (modelgrep.getRowCount() == 0) {
                    modelgrep.addRow(new Object[]{true, "", "Simple String","", grepline});
                } else {
                    modelgrep.addRow(new Object[]{true, "OR","Simple String","", grepline});
                }
            }
        }
    }

    public void addNewTag(String str) {
        if (!str.isEmpty()) {
            try {
                BufferedWriter out = new BufferedWriter(new FileWriter(filename.concat("tags.txt"), true));
                out.write(str.concat("\n"));
                out.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public void removeTag(String tag) {
        String file = filename.concat("tags.txt");
        try {

            File inFile = new File(file);

            if (!inFile.isFile()) {
                return;
            }

            //Construct the new file that will later be renamed to the original filename.
            File tempFile = new File(inFile.getAbsolutePath().concat(".tmp"));

            BufferedReader br = new BufferedReader(new FileReader(file));
            PrintWriter pw = new PrintWriter(new FileWriter(tempFile));

            String line = null;

            //Read from the original file and write to the new
            //unless content matches data to be removed.
            while ((line = br.readLine()) != null) {

                if (!line.trim().equals(tag)) {
                    pw.println(line);
                    pw.flush();
                }
            }
            pw.close();
            br.close();

        } catch (FileNotFoundException ex) {
            System.out.println(ex.getMessage());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void showTags() {

        List<String> tags = readFile(filename.concat("tags.txt"));

        newTagCombo.removeAllItems();
        tagmanager.removeAllElements();
        for (String tag : tags) {
            newTagCombo.addItem(tag);
            tagmanager.addElement(tag);
        }
    }

    public String getClipboardContents() {
        //Get clipboard contents for implement grep and match paste button
        String result = "";
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = clipboard.getContents(null);
        boolean hasTransferableText = (contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor);

        if (hasTransferableText) {
            try {
                result = (String) contents.getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException | IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
        return result;
    }

    public void loadGrepsFile(DefaultTableModel model) {
        //Load file for implement payloads and match load button
        List<String> grep = new ArrayList();
        String line;
        JFrame parentFrame = new JFrame();
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Specify a file to load");

        int userSelection = fileChooser.showOpenDialog(parentFrame);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileload = fileChooser.getSelectedFile();
            textgreps.setText(fileload.getAbsolutePath());
            try {
                BufferedReader bufferreader = new BufferedReader(new FileReader(fileload.getAbsolutePath()));
                line = bufferreader.readLine();

                while (line != null) {
                    grep.add(line);
                    line = bufferreader.readLine();
                }
                bufferreader.close();
                showGreps(grep);
            } catch (FileNotFoundException ex) {
                System.out.println(ex.getMessage());
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    public void swap(int a, int b) {
        Object aObject = encoder.getElementAt(a);
        Object bObject = encoder.getElementAt(b);
        encoder.set(a, bObject);
        encoder.set(b, aObject);
    }

    public JsonArray initJson() {
        //Init json form filename
        FileReader fr;

        try {
            JsonArray data = new JsonArray();
            File f = new File(filename);
            if (f.exists() && f.isDirectory()) {
                for (File file : f.listFiles()) {
                    if (file.getName().endsWith(".bb2")) {
                        fr = new FileReader(file.getAbsolutePath());
                        JsonReader json = new JsonReader((fr));
                        JsonParser parser = new JsonParser();
                        data.addAll(parser.parse(json).getAsJsonArray());
                        fr.close();
                    }

                }
            }
            return data;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    private List<String> readFile(String filename) {
        List<String> records = new ArrayList();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null) {
                records.add(line);
            }
            reader.close();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return records;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();
        buttonGroup5 = new javax.swing.ButtonGroup();
        buttonGroup6 = new javax.swing.ButtonGroup();
        buttonGroup7 = new javax.swing.ButtonGroup();
        text1 = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        textauthor = new javax.swing.JTextField();
        headerstab = new javax.swing.JTabbedPane();
        jScrollPane6 = new javax.swing.JScrollPane();
        jPanel11 = new javax.swing.JPanel();
        button10 = new javax.swing.JButton();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        button19 = new javax.swing.JButton();
        button18 = new javax.swing.JButton();
        jScrollPane14 = new javax.swing.JScrollPane();
        table4 = new javax.swing.JTable();
        textgreps = new javax.swing.JTextField();
        button8 = new javax.swing.JButton();
        button7 = new javax.swing.JButton();
        jScrollPane10 = new javax.swing.JScrollPane();
        jPanel12 = new javax.swing.JPanel();
        jLabel32 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        radio5 = new javax.swing.JRadioButton();
        radio6 = new javax.swing.JRadioButton();
        radio7 = new javax.swing.JRadioButton();
        radio8 = new javax.swing.JRadioButton();
        jLabel7 = new javax.swing.JLabel();
        radio9 = new javax.swing.JRadioButton();
        radio10 = new javax.swing.JRadioButton();
        radio11 = new javax.swing.JRadioButton();
        text4 = new javax.swing.JTextField();
        jSeparator7 = new javax.swing.JSeparator();
        jLabel34 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jScrollPane7 = new javax.swing.JScrollPane();
        textarea2 = new javax.swing.JTextArea();
        jLabel13 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        jSeparator8 = new javax.swing.JSeparator();
        jLabel38 = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        jSeparator9 = new javax.swing.JSeparator();
        jScrollPane1 = new javax.swing.JScrollPane();
        textarea1 = new javax.swing.JTextArea();
        jLabel9 = new javax.swing.JLabel();
        jScrollPane8 = new javax.swing.JScrollPane();
        textarea3 = new javax.swing.JTextArea();
        jLabel14 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        jLabel41 = new javax.swing.JLabel();
        jSeparator10 = new javax.swing.JSeparator();
        jScrollPane9 = new javax.swing.JScrollPane();
        textarea4 = new javax.swing.JTextArea();
        jLabel15 = new javax.swing.JLabel();
        first_match = new javax.swing.JRadioButton();
        all_matches = new javax.swing.JRadioButton();
        jLabel5 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        removetag = new javax.swing.JButton();
        addTag = new javax.swing.JButton();
        newTagCombo = new javax.swing.JComboBox<>();
        jScrollPane11 = new javax.swing.JScrollPane();
        listtag = new javax.swing.JList<>();
        jLabel46 = new javax.swing.JLabel();
        jLabel47 = new javax.swing.JLabel();
        newTagb = new javax.swing.JButton();

        text1.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N

        jLabel18.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        jLabel18.setText("Author:");

        jLabel12.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        jLabel12.setText("Name:");

        textauthor.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N

        headerstab.setAutoscrolls(true);
        headerstab.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        headerstab.setPreferredSize(new java.awt.Dimension(800, 600));
        headerstab.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                headerstabStateChanged(evt);
            }
        });

        jScrollPane6.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane6.getVerticalScrollBar().setUnitIncrement(20);

        jPanel11.setAutoscrolls(true);

        button10.setText("Clear");
        button10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button10removeAllGrep(evt);
            }
        });

        jLabel24.setText("You can define one or more greps.");

        jLabel25.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
        jLabel25.setForeground(new java.awt.Color(229, 92, 58));
        jLabel25.setText("Grep");

        button19.setText("Add");
        button19.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addGrep(evt);
            }
        });

        button18.setText("Remove");
        button18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button18removeMatchReplace(evt);
            }
        });

        table4.setFont(new java.awt.Font("Lucida Grande", 0, 13)); // NOI18N
        table4.setModel(modelgrep);
        table4.setShowGrid(false);
        jScrollPane14.setViewportView(table4);

        button8.setText("Load File");
        button8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button8loadGrep(evt);
            }
        });

        button7.setText("Paste");
        button7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button7pasteGrep(evt);
            }
        });

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addComponent(button8, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(textgreps))
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel25)
                            .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 769, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 48, Short.MAX_VALUE))
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(button18, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(button10, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(button7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(button19, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane14)))
                .addContainerGap())
        );

        jPanel11Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {button10, button18, button19, button7, button8});

        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel25)
                .addGap(18, 18, 18)
                .addComponent(jLabel24)
                .addGap(18, 18, 18)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textgreps, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(button8))
                .addGap(18, 18, 18)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addComponent(button19)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(button7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(button10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(button18)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane14, javax.swing.GroupLayout.DEFAULT_SIZE, 391, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanel11Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {button10, button18, button19, button7, button8});

        JScrollPane responseresScroll = new JScrollPane(jPanel11,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        jScrollPane6.setViewportView(jPanel11);

        headerstab.addTab("     Response     ", jScrollPane6);

        jScrollPane10.getVerticalScrollBar().setUnitIncrement(20);

        jPanel12.setAutoscrolls(true);

        jLabel32.setText("You can define the issue properties.");

        jLabel33.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
        jLabel33.setForeground(new java.awt.Color(229, 92, 58));
        jLabel33.setText("Issue Properties");

        jLabel3.setText("Issue Name:");

        jLabel4.setText("Severity:");

        buttonGroup5.add(radio5);
        radio5.setText("High");

        buttonGroup5.add(radio6);
        radio6.setText("Medium");

        buttonGroup5.add(radio7);
        radio7.setText("Low");

        buttonGroup5.add(radio8);
        radio8.setText("Information");

        jLabel7.setText("Confidence:");

        buttonGroup6.add(radio9);
        radio9.setText("Certain");

        buttonGroup6.add(radio10);
        radio10.setText("Firm");

        buttonGroup6.add(radio11);
        radio11.setText("Tentative");

        jLabel34.setText("You can define the issue details.");

        jLabel35.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
        jLabel35.setForeground(new java.awt.Color(229, 92, 58));
        jLabel35.setText("Issue Detail");

        textarea2.setColumns(20);
        textarea2.setRows(5);
        jScrollPane7.setViewportView(textarea2);

        jLabel13.setText("Description:");

        jLabel36.setText("You can define the issue background.");

        jLabel37.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
        jLabel37.setForeground(new java.awt.Color(229, 92, 58));
        jLabel37.setText("Issue Background");

        jLabel38.setText("You can define the remediation detail.");

        jLabel39.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
        jLabel39.setForeground(new java.awt.Color(229, 92, 58));
        jLabel39.setText("Remediation Detail");

        textarea1.setColumns(20);
        textarea1.setRows(5);
        jScrollPane1.setViewportView(textarea1);

        jLabel9.setText("Description:");

        textarea3.setColumns(20);
        textarea3.setRows(5);
        jScrollPane8.setViewportView(textarea3);

        jLabel14.setText("Description:");

        jLabel40.setText("You can define the remediation background.");

        jLabel41.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
        jLabel41.setForeground(new java.awt.Color(229, 92, 58));
        jLabel41.setText("Remediation Background");

        textarea4.setColumns(20);
        textarea4.setRows(5);
        jScrollPane9.setViewportView(textarea4);

        jLabel15.setText("Description:");

        buttonGroup7.add(first_match);
        first_match.setText("Only once per domain");
        first_match.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                first_matchstringMatchType(evt);
            }
        });

        buttonGroup7.add(all_matches);
        all_matches.setText("All times in a domain");
        all_matches.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                all_matchesregexMatchType(evt);
            }
        });

        jLabel5.setText("Show issue:");

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator7)
            .addComponent(jSeparator8, javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(jSeparator9, javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(jSeparator10, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel33)
                            .addComponent(jLabel35)
                            .addComponent(jLabel34)
                            .addGroup(jPanel12Layout.createSequentialGroup()
                                .addComponent(jLabel13)
                                .addGap(18, 18, 18)
                                .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 612, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel12Layout.createSequentialGroup()
                                .addComponent(jLabel9)
                                .addGap(18, 18, 18)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 612, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel12Layout.createSequentialGroup()
                                .addComponent(jLabel15)
                                .addGap(18, 18, 18)
                                .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 612, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel12Layout.createSequentialGroup()
                                .addComponent(jLabel14)
                                .addGap(18, 18, 18)
                                .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 612, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel37)
                            .addComponent(jLabel36)
                            .addComponent(jLabel39)
                            .addComponent(jLabel38)
                            .addComponent(jLabel32)
                            .addComponent(jLabel41)
                            .addComponent(jLabel40))
                        .addContainerGap(137, Short.MAX_VALUE))
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel12Layout.createSequentialGroup()
                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel12Layout.createSequentialGroup()
                                        .addComponent(radio8)
                                        .addGap(189, 189, 189))
                                    .addGroup(jPanel12Layout.createSequentialGroup()
                                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(radio6)
                                            .addComponent(radio7)
                                            .addComponent(radio5))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel7)
                                        .addGap(18, 18, 18)
                                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(radio9)
                                            .addComponent(radio11)
                                            .addComponent(radio10)))))
                            .addComponent(text4, javax.swing.GroupLayout.PREFERRED_SIZE, 419, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(all_matches)
                            .addComponent(first_match))
                        .addGap(32, 32, 32))))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jLabel33)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel32)
                .addGap(24, 24, 24)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(text4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel12Layout.createSequentialGroup()
                                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel7)
                                    .addComponent(radio9))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(radio10))
                            .addGroup(jPanel12Layout.createSequentialGroup()
                                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel5)
                                    .addComponent(first_match))
                                .addGap(1, 1, 1)
                                .addComponent(all_matches)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radio11))
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(radio5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radio6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radio7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radio8)))
                .addGap(18, 18, 18)
                .addComponent(jSeparator7, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel35)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel34)
                .addGap(25, 25, 25)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jSeparator8, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel37)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel36)
                .addGap(25, 25, 25)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel13)
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jSeparator9, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel39)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel38)
                .addGap(25, 25, 25)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel15)
                    .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jSeparator10, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel41)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel40)
                .addGap(25, 25, 25)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel14)
                    .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jScrollPane10.setViewportView(jPanel12);

        headerstab.addTab("     Issue     ", jScrollPane10);

        removetag.setText("Remove");
        removetag.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removetag(evt);
            }
        });

        addTag.setText("Add");
        addTag.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addTag(evt);
            }
        });

        listtag.setModel(tag);
        jScrollPane11.setViewportView(listtag);

        jLabel46.setText("You can define one or multiple tags for this profile.");

        jLabel47.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
        jLabel47.setForeground(new java.awt.Color(229, 92, 58));
        jLabel47.setText("Set Tags");

        newTagb.setText("New Tag");
        newTagb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newTagbnewTag(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel47)
                    .addComponent(jLabel46)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(newTagb, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(addTag, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(removetag, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane11)
                            .addComponent(newTagCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 468, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jLabel47)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel46)
                .addGap(25, 25, 25)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(newTagb)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removetag)))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(newTagCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addTag))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        headerstab.addTab("          Tags          ", jPanel3);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(text1)
                .addGap(18, 18, 18)
                .addComponent(jLabel18)
                .addGap(18, 18, 18)
                .addComponent(textauthor, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(70, 70, 70))
            .addComponent(headerstab, javax.swing.GroupLayout.DEFAULT_SIZE, 831, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(text1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12)
                    .addComponent(textauthor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(headerstab, javax.swing.GroupLayout.DEFAULT_SIZE, 443, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void button10removeAllGrep(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button10removeAllGrep
        int rowCount = modelgrep.getRowCount();
        for (int i = rowCount - 1; i >= 0; i--) {
            modelgrep.removeRow(i);
        }
    }//GEN-LAST:event_button10removeAllGrep

    private void removetag(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removetag
        int selectedIndex = listtag.getSelectedIndex();
        if (selectedIndex != -1) {
            tag.remove(selectedIndex);
        }
    }//GEN-LAST:event_removetag

    private void addTag(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addTag
        tag.addElement(newTagCombo.getSelectedItem());
    }//GEN-LAST:event_addTag

    private void newTagbnewTag(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newTagbnewTag
        Integer result;
        NewTag nt = new NewTag();
        JOptionPane jopane1 = new JOptionPane(nt, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        JDialog dialog = jopane1.createDialog(this, "New Tag");
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        Object selectedValue = jopane1.getValue();

        if (selectedValue != null) {
            result = ((Integer) selectedValue).intValue();

            if (result == JOptionPane.OK_OPTION) {
                addNewTag(nt.newTagtext.getText());
                showTags();
            }
        }
    }//GEN-LAST:event_newTagbnewTag

    private void headerstabStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_headerstabStateChanged
        int activePane = headerstab.getSelectedIndex();
        if (activePane == 3) {
            showTags();
        }
    }//GEN-LAST:event_headerstabStateChanged

    private void addGrep(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addGrep
        if (modelgrep.getRowCount() == 0) {
            modelgrep.addRow(new Object[]{true, "","Simple String", "", "Value"});
        } else {
            modelgrep.addRow(new Object[]{true, "OR","Simple String","", "Value"});
        }

    }//GEN-LAST:event_addGrep

    private void button18removeMatchReplace(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button18removeMatchReplace
        int[] rows = table4.getSelectedRows();
        Arrays.sort(rows);
        for (int i = rows.length - 1; i >= 0; i--) {
            int row = rows[i];
            int modelRow = table4.convertRowIndexToModel(row);
            modelgrep.removeRow(modelRow);
        }
    }//GEN-LAST:event_button18removeMatchReplace

    private void button8loadGrep(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button8loadGrep
        loadGrepsFile(modelgrep);
    }//GEN-LAST:event_button8loadGrep

    private void button7pasteGrep(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button7pasteGrep
        String element = getClipboardContents();
        List<String> lines = Arrays.asList(element.split("\n"));
        showGreps(lines);
    }//GEN-LAST:event_button7pasteGrep

    private void first_matchstringMatchType(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_first_matchstringMatchType
        // TODO add your handling code here:
    }//GEN-LAST:event_first_matchstringMatchType

    private void all_matchesregexMatchType(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_all_matchesregexMatchType
        // TODO add your handling code here:
    }//GEN-LAST:event_all_matchesregexMatchType


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addTag;
    public javax.swing.JRadioButton all_matches;
    private javax.swing.JButton button10;
    private javax.swing.JButton button18;
    private javax.swing.JButton button19;
    private javax.swing.JButton button7;
    private javax.swing.JButton button8;
    public javax.swing.ButtonGroup buttonGroup1;
    public javax.swing.ButtonGroup buttonGroup2;
    public javax.swing.ButtonGroup buttonGroup3;
    public javax.swing.ButtonGroup buttonGroup4;
    public javax.swing.ButtonGroup buttonGroup5;
    public javax.swing.ButtonGroup buttonGroup6;
    public javax.swing.ButtonGroup buttonGroup7;
    public javax.swing.JRadioButton first_match;
    public javax.swing.JTabbedPane headerstab;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane14;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JSeparator jSeparator10;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JSeparator jSeparator9;
    public javax.swing.JList<String> listtag;
    public javax.swing.JComboBox<String> newTagCombo;
    private javax.swing.JButton newTagb;
    public javax.swing.JRadioButton radio10;
    public javax.swing.JRadioButton radio11;
    public javax.swing.JRadioButton radio5;
    public javax.swing.JRadioButton radio6;
    public javax.swing.JRadioButton radio7;
    public javax.swing.JRadioButton radio8;
    public javax.swing.JRadioButton radio9;
    private javax.swing.JButton removetag;
    public javax.swing.JTable table4;
    public javax.swing.JTextField text1;
    public javax.swing.JTextField text4;
    public javax.swing.JTextArea textarea1;
    public javax.swing.JTextArea textarea2;
    public javax.swing.JTextArea textarea3;
    public javax.swing.JTextArea textarea4;
    public javax.swing.JTextField textauthor;
    public javax.swing.JTextField textgreps;
    // End of variables declaration//GEN-END:variables
}
