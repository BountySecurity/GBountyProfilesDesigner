package gbountyprofilesdesigner.manager;

import gbountyprofilesdesigner.data.Headers;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ItemEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RequestResponse extends javax.swing.JPanel {
    DefaultListModel payload;
    DefaultListModel grep;
    DefaultListModel encoder;
    List<Headers> headers;
    DefaultTableModel model4;
    DefaultTableModel modelgrep;
    DefaultTableModel modelpayload;
    DefaultTableModel modelnewheaders;
    String filename;
    JComboBox operator;
    JComboBox match_type;
    JComboBox options;

    public RequestResponse(String filename) {
        payload = new DefaultListModel();
        grep = new DefaultListModel();
        encoder = new DefaultListModel();
        model4 = new DefaultTableModel();
        modelgrep = new DefaultTableModel();
        modelpayload = new DefaultTableModel();
        modelnewheaders = new DefaultTableModel();
        headers = new ArrayList();
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

        modelpayload = new DefaultTableModel() {
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
        };

        initComponents();

        this.filename = filename;

        showHeaders(headers);
        showGrepsTable();
        showPayloadsTable();
        showNewHeadersTable();
        combo_changeHTTP.setEnabled(false);
        jTabbedPane1.setSelectedIndex(0);
        jTabbedPane1.setEnabledAt(1, false);
        jTabbedPane1.setEnabledAt(0, true);
        same_insertion_point.setSelected(true);
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

    public void showNewHeadersTable() {
        modelnewheaders.setNumRows(0);
        modelnewheaders.setColumnCount(0);
        modelnewheaders.addColumn("New HTTP Header Name");

        TableRowSorter<TableModel> sorter = new TableRowSorter<>(newHeadersTable.getModel());
        newHeadersTable.setRowSorter(sorter);
        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sorter.setSortKeys(sortKeys);
        sorter.sort();
    }

    public void showHeaders(List<Headers> Header) {

        JComboBox jcb = new JComboBox();
        JComboBox jcb1 = new JComboBox();

        //model for active profiles
        model4.setNumRows(0);
        model4.setColumnCount(0);
        model4.addColumn("Item");
        model4.addColumn("Match");
        model4.addColumn("Replace");
        model4.addColumn("Type");

        jcb.addItem("Payload");
        jcb.addItem("Request");
        jcb1.addItem("String");
        jcb1.addItem("Regex");

        table4.getColumnModel().getColumn(0).setPreferredWidth(140);
        table4.getColumnModel().getColumn(1).setPreferredWidth(400);
        table4.getColumnModel().getColumn(2).setPreferredWidth(450);
        table4.getColumnModel().getColumn(3).setPreferredWidth(120);

        table4.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(jcb));
        table4.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(jcb1));
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(table4.getModel());
        table4.setRowSorter(sorter);
        List<RowSorter.SortKey> sortKeys = new ArrayList<>();

        sortKeys.add(new RowSorter.SortKey(0, SortOrder.DESCENDING));
        sorter.setSortKeys(sortKeys);
        sorter.sort();

        for (int i = 0; i < Header.size(); i++) {
            model4.addRow(new Object[]{Header.get(i).type, Header.get(i).match, Header.get(i).replace, Header.get(i).regex});
        }
    }

    public void swap(int a, int b) {
        Object aObject = encoder.getElementAt(a);
        Object bObject = encoder.getElementAt(b);
        encoder.set(a, bObject);
        encoder.set(b, aObject);
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

    public void showNewHeaders(List<String> newHeaders) {
        if (newHeaders != null && !newHeaders.isEmpty()) {
            for (String header : newHeaders) {
                modelnewheaders.addRow(new Object[]{header});
            }
        }
    }

    public void showPayloads(List<String> payloads) {

        for (String payloadline : payloads) {
            if (payloadline.startsWith("true,") || payloadline.startsWith("false,")) {
                List<String> array = Arrays.asList(payloadline.split(",", 2));
                if (array.get(0).equals("true")) {
                    modelpayload.addRow(new Object[]{true, array.get(1)});
                } else {
                    modelpayload.addRow(new Object[]{false, array.get(1)});
                }
            } else {
                modelpayload.addRow(new Object[]{true, payloadline});
            }
        }
    }

    public void loadPayloadsFile(DefaultListModel list) {
        //Load file for implement payloads and match load button
        List<String> payloads = new ArrayList();
        String line;
        JFrame parentFrame = new JFrame();
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Specify a file to load");

        int userSelection = fileChooser.showOpenDialog(parentFrame);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileload = fileChooser.getSelectedFile();
            textpayloads.setText(fileload.getAbsolutePath());
            try {
                BufferedReader bufferreader = new BufferedReader(new FileReader(fileload.getAbsolutePath()));
                line = bufferreader.readLine();

                while (line != null) {
                    payloads.add(line);
                    line = bufferreader.readLine();
                }
                bufferreader.close();
                showPayloads(payloads);
            } catch (FileNotFoundException ex) {
                System.out.println(ex.getMessage());
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
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
        match_type.addItem("Blind Host");
        match_type.addItem("Status Code");
        match_type.addItem("Time Delay");
        match_type.addItem("Content Type");
        match_type.addItem("Content Length");
        match_type.addItem("Content Length Diff");
        match_type.addItem("URL Extension");

        options.addItem("");
        options.addItem("Case sensitive");
        options.addItem("Only in Headers");
        options.addItem("Not in Headers");

        table6.getColumnModel().getColumn(0).setPreferredWidth(5);
        table6.getColumnModel().getColumn(1).setPreferredWidth(25);
        table6.getColumnModel().getColumn(2).setPreferredWidth(90);
        table6.getColumnModel().getColumn(4).setPreferredWidth(250);
        table6.getColumnModel().getColumn(3).setPreferredWidth(65);
        table6.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(operator));
        table6.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(match_type));
        table6.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(options));

        TableRowSorter<TableModel> sorter = new TableRowSorter<>(table6.getModel());
        table6.setRowSorter(sorter);
        table6.getTableHeader().setReorderingAllowed(false);
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
                        modelgrep.addRow(new Object[]{true, "", array.get(2), array.get(3), array.get(4)});
                    } else {
                        modelgrep.addRow(new Object[]{false, "", array.get(2), array.get(3), array.get(4)});
                    }
                } else {
                    if (array.get(0).equals("true")) {
                        modelgrep.addRow(new Object[]{true, array.get(1), array.get(2), array.get(3), array.get(4)});
                    } else {
                        modelgrep.addRow(new Object[]{false, array.get(1), array.get(2), array.get(3), array.get(4)});
                    }
                }
            } else {
                if (modelgrep.getRowCount() == 0) {
                    modelgrep.addRow(new Object[]{true, "", "Simple String", "", grepline});
                } else {
                    modelgrep.addRow(new Object[]{true, "OR", "Simple String", "", grepline});
                }
            }
        }
    }

    public void showPayloadsTable() {
        modelpayload.setNumRows(0);
        modelpayload.setColumnCount(0);
        modelpayload.addColumn("Enabled");
        modelpayload.addColumn("Value");

        table7.getColumnModel().getColumn(0).setPreferredWidth(5);
        table7.getColumnModel().getColumn(1).setPreferredWidth(415);

        TableRowSorter<TableModel> sorter = new TableRowSorter<>(table7.getModel());
        table7.setRowSorter(sorter);
        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sorter.setSortKeys(sortKeys);
        sorter.sort();
    }

    public void setGrepOtions(boolean state) {
        table6.setEnabled(state);
        textgreps.setEnabled(state);
        button8.setEnabled(state);
        button7.setEnabled(state);
        button10.setEnabled(state);
        rb1.setEnabled(state);
        rb2.setEnabled(state);
        rb3.setEnabled(state);
        rb4.setEnabled(state);
        sp1.setEnabled(state);
        jLabel6.setEnabled(state);
        jLabel2.setEnabled(state);

    }

    public void setGrepOtionsPayload(boolean state) {
        table6.setEnabled(state);
        textgreps.setEnabled(state);
        button8.setEnabled(state);
        button7.setEnabled(state);
        button10.setEnabled(state);

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
        jPanel7 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane5 = new javax.swing.JScrollPane();
        jPanel10 = new javax.swing.JPanel();
        jSeparator2 = new javax.swing.JSeparator();
        text5 = new javax.swing.JTextField();
        jButton9 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jScrollPane14 = new javax.swing.JScrollPane();
        table4 = new javax.swing.JTable();
        jLabel22 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        list3 = new javax.swing.JList<>();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel55 = new javax.swing.JLabel();
        jLabel52 = new javax.swing.JLabel();
        jSeparator4 = new javax.swing.JSeparator();
        button18 = new javax.swing.JButton();
        button19 = new javax.swing.JButton();
        combo2 = new javax.swing.JComboBox<>();
        jLabel54 = new javax.swing.JLabel();
        jButton6 = new javax.swing.JButton();
        jLabel19 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        check8 = new javax.swing.JCheckBox();
        jLabel23 = new javax.swing.JLabel();
        jLabel53 = new javax.swing.JLabel();
        jButton7 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        userprovided = new javax.swing.JCheckBox();
        paramnamexmlattr = new javax.swing.JCheckBox();
        parambody = new javax.swing.JCheckBox();
        extensionprovided = new javax.swing.JCheckBox();
        paramnamecookie = new javax.swing.JCheckBox();
        urlpathfolder = new javax.swing.JCheckBox();
        paramxml = new javax.swing.JCheckBox();
        paramurl = new javax.swing.JCheckBox();
        paramcookie = new javax.swing.JCheckBox();
        paramnamebody = new javax.swing.JCheckBox();
        paramnamemultipartattr = new javax.swing.JCheckBox();
        All = new javax.swing.JCheckBox();
        paramjson = new javax.swing.JCheckBox();
        urlpathfilename = new javax.swing.JCheckBox();
        parammultipartattr = new javax.swing.JCheckBox();
        entirebody = new javax.swing.JCheckBox();
        paramnamejson = new javax.swing.JCheckBox();
        paramxmlattr = new javax.swing.JCheckBox();
        paramnamexml = new javax.swing.JCheckBox();
        entirebodyxml = new javax.swing.JCheckBox();
        entirebodyjson = new javax.swing.JCheckBox();
        entirebodymultipart = new javax.swing.JCheckBox();
        paramnameurl = new javax.swing.JCheckBox();
        single_extensionprovided = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        host = new javax.swing.JCheckBox();
        accept = new javax.swing.JCheckBox();
        acceptlanguage = new javax.swing.JCheckBox();
        useragent = new javax.swing.JCheckBox();
        referer = new javax.swing.JCheckBox();
        contenttype = new javax.swing.JCheckBox();
        origin = new javax.swing.JCheckBox();
        acceptencoding = new javax.swing.JCheckBox();
        newHeaderPanel = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        newHeadersTable = new javax.swing.JTable();
        button9 = new javax.swing.JButton();
        button11 = new javax.swing.JButton();
        button12 = new javax.swing.JButton();
        button13 = new javax.swing.JButton();
        payload_position_combo = new javax.swing.JComboBox<>();
        button5 = new javax.swing.JButton();
        button4 = new javax.swing.JButton();
        button2 = new javax.swing.JButton();
        button6 = new javax.swing.JButton();
        button3 = new javax.swing.JButton();
        textpayloads = new javax.swing.JTextField();
        jScrollPane17 = new javax.swing.JScrollPane();
        table7 = new javax.swing.JTable();
        txt_changeHTTP = new javax.swing.JCheckBox();
        combo_changeHTTP = new javax.swing.JComboBox<>();
        jPanel6 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jLabel44 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txt_rawrequest = new javax.swing.JTextArea();
        jScrollPane6 = new javax.swing.JScrollPane();
        jPanel11 = new javax.swing.JPanel();
        button10 = new javax.swing.JButton();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jSeparator5 = new javax.swing.JSeparator();
        rb1 = new javax.swing.JRadioButton();
        rb2 = new javax.swing.JRadioButton();
        rb3 = new javax.swing.JRadioButton();
        rb4 = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        sp1 = new javax.swing.JSpinner();
        jLabel6 = new javax.swing.JLabel();
        button22 = new javax.swing.JButton();
        button23 = new javax.swing.JButton();
        jScrollPane16 = new javax.swing.JScrollPane();
        table6 = new javax.swing.JTable();
        textgreps = new javax.swing.JTextField();
        button8 = new javax.swing.JButton();
        button7 = new javax.swing.JButton();
        jScrollPane10 = new javax.swing.JScrollPane();
        jPanel12 = new javax.swing.JPanel();
        jLabel32 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
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
        jLabel8 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<>();
        jComboBox2 = new javax.swing.JComboBox<>();
        jComboBox3 = new javax.swing.JComboBox<>();
        jLabel11 = new javax.swing.JLabel();
        request_position = new javax.swing.JComboBox<>();
        same_insertion_point = new javax.swing.JRadioButton();
        any_insertion_point = new javax.swing.JRadioButton();

        jScrollPane5.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane5.setPreferredSize(new java.awt.Dimension(0, 0));
        jScrollPane5.getVerticalScrollBar().setUnitIncrement(20);

        jPanel10.setMaximumSize(new java.awt.Dimension(0, 0));

        jButton9.setText("Remove");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9removeEncoder(evt);
            }
        });

        jButton8.setText("Up");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8upEncoder(evt);
            }
        });

        table4.setFont(new java.awt.Font("Lucida Grande", 0, 13)); // NOI18N
        table4.setModel(model4);
        table4.setShowGrid(false);
        jScrollPane14.setViewportView(table4);

        jLabel22.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
        jLabel22.setForeground(new java.awt.Color(229, 92, 58));
        jLabel22.setText("Payload Encoding");

        list3.setModel(encoder);
        jScrollPane4.setViewportView(list3);

        jLabel55.setText("You can define the some options.");

        jLabel52.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
        jLabel52.setForeground(new java.awt.Color(229, 92, 58));
        jLabel52.setText("Match and Replace");

        button18.setText("Remove");
        button18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button18removeMatchReplace(evt);
            }
        });

        button19.setText("Add");
        button19.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button19addMatchReplace(evt);
            }
        });

        combo2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "URL-encode key characters", "URL-encode all characters", "URL-encode all characters (Unicode)", "HTML-encode key characters", "HTML-encode all characters", "Base64-encode" }));

        jLabel54.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
        jLabel54.setForeground(new java.awt.Color(229, 92, 58));
        jLabel54.setText("Options");

        jButton6.setText("Add");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6addEncoder(evt);
            }
        });

        jLabel19.setText("You can define one or more payloads. Each payload of this section will be sent at each insertion point that you can defined.");

        jLabel10.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        jLabel10.setText("Payload position:");

        jLabel5.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(229, 92, 58));
        jLabel5.setText("Payloads");

        check8.setText("URL-Encode these characters:");

        jLabel23.setText("You can define the encoding of payloads. You can encode each payload multiple times.");

        jLabel53.setText("These settings are used to automatically replace part of request when the active scanner run.");

        jButton7.setText("Down");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7downEncoder(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Insertion Point Type", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 14))); // NOI18N

        userprovided.setText("User provided");

        paramnamexmlattr.setText("Param xml attr name");

        parambody.setText("Param body value");

        extensionprovided.setText("Multiple Path discovery");

        paramnamecookie.setText("Param cookie name");

        urlpathfolder.setText("Url path folder");

        paramxml.setText("Param xml value");
        paramxml.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                paramxmlActionPerformed(evt);
            }
        });

        paramurl.setText("Param url value");

        paramcookie.setText("Param cookie value");

        paramnamebody.setText("Param body name");

        paramnamemultipartattr.setText("Param multipart attr name");

        All.setText("All ");
        All.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                AllItemStateChanged(evt);
            }
        });

        paramjson.setText("Param json value");

        urlpathfilename.setText("Url path filename");

        parammultipartattr.setText("Param multipart attr value");

        entirebody.setText("Entire body");

        paramnamejson.setText("Param json name");

        paramxmlattr.setText("Param xml attr value");

        paramnamexml.setText("Param xml name");

        entirebodyxml.setText("Entire body xml");

        entirebodyjson.setText("Entire body json");

        entirebodymultipart.setText("Entire body multipart");

        paramnameurl.setText("Param url name");

        single_extensionprovided.setText("Single Path discovery");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(All)
                    .addComponent(parambody)
                    .addComponent(paramnamebody)
                    .addComponent(paramurl)
                    .addComponent(entirebody)
                    .addComponent(paramnameurl))
                .addGap(26, 26, 26)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(paramjson)
                        .addComponent(paramnamecookie, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(paramcookie))
                    .addComponent(paramnamejson)
                    .addComponent(entirebodyjson)
                    .addComponent(userprovided))
                .addGap(26, 26, 26)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(paramxml)
                    .addComponent(paramnamexml)
                    .addComponent(paramxmlattr)
                    .addComponent(paramnamexmlattr)
                    .addComponent(parammultipartattr)
                    .addComponent(paramnamemultipartattr))
                .addGap(26, 26, 26)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(entirebodyxml)
                    .addComponent(urlpathfilename)
                    .addComponent(urlpathfolder)
                    .addComponent(entirebodymultipart)
                    .addComponent(extensionprovided)
                    .addComponent(single_extensionprovided))
                .addContainerGap(19, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(All)
                    .addComponent(paramxml)
                    .addComponent(userprovided)
                    .addComponent(extensionprovided))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(paramnamexml)
                                    .addComponent(paramcookie)
                                    .addComponent(single_extensionprovided))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(paramxmlattr)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(paramnamexmlattr))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(parambody)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(paramnamebody)
                                    .addComponent(paramnamecookie))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(paramjson)
                                    .addComponent(paramurl))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(parammultipartattr)
                                .addComponent(paramnamejson))
                            .addComponent(paramnameurl))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(entirebody)
                                .addComponent(entirebodyjson))
                            .addComponent(paramnamemultipartattr)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(urlpathfolder)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(urlpathfilename)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(entirebodyxml)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(entirebodymultipart)))
                .addContainerGap())
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "HTTP Headers", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 12))); // NOI18N

        host.setText("Host");

        accept.setText("Accept");

        acceptlanguage.setText("Accept Language");

        useragent.setText("User Agent");

        referer.setText("Referer");

        contenttype.setText("Content Type");

        origin.setText("Origin");

        acceptencoding.setText("Accept Encoding");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(useragent)
                    .addComponent(host))
                .addGap(69, 69, 69)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(acceptlanguage)
                    .addComponent(accept))
                .addGap(69, 69, 69)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(contenttype)
                    .addComponent(acceptencoding))
                .addGap(69, 69, 69)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(referer)
                    .addComponent(origin))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(host)
                    .addComponent(accept)
                    .addComponent(acceptencoding)
                    .addComponent(origin))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(contenttype)
                    .addComponent(useragent)
                    .addComponent(acceptlanguage)
                    .addComponent(referer))
                .addContainerGap(9, Short.MAX_VALUE))
        );

        newHeaderPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "New HTTP Headers", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 12))); // NOI18N

        newHeadersTable.setModel(modelnewheaders);
        jScrollPane3.setViewportView(newHeadersTable);

        button9.setText("Add");
        button9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button9setNewHeader(evt);
            }
        });

        button11.setText("Paste");
        button11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button11pasteNewHeaders(evt);
            }
        });

        button12.setText("Remove");
        button12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button12removeNewHeaders(evt);
            }
        });

        button13.setText("Clear");
        button13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button13removeAllHeaders(evt);
            }
        });

        javax.swing.GroupLayout newHeaderPanelLayout = new javax.swing.GroupLayout(newHeaderPanel);
        newHeaderPanel.setLayout(newHeaderPanelLayout);
        newHeaderPanelLayout.setHorizontalGroup(
            newHeaderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, newHeaderPanelLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(newHeaderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(button9, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(newHeaderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(newHeaderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(button12, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(button13, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(button11, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane3)
                .addContainerGap())
        );
        newHeaderPanelLayout.setVerticalGroup(
            newHeaderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(newHeaderPanelLayout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addGroup(newHeaderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(newHeaderPanelLayout.createSequentialGroup()
                        .addComponent(button9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(button11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(button12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(button13))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(newHeaderPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(newHeaderPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        payload_position_combo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Replace", "Append", "Insert" }));

        button5.setText("Clear");
        button5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button5removeAllPayloads(evt);
            }
        });

        button4.setText("Remove");
        button4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button4removePayload(evt);
            }
        });

        button2.setText("Paste");
        button2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button2pastePayload(evt);
            }
        });

        button6.setText("Add");
        button6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button6setToPayload(evt);
            }
        });

        button3.setText("Load File");
        button3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button3loadPayloads(evt);
            }
        });

        textpayloads.setToolTipText("");

        table7.setFont(new java.awt.Font("Lucida Grande", 0, 13)); // NOI18N
        table7.setModel(modelpayload);
        table7.setShowGrid(false);
        jScrollPane17.setViewportView(table7);

        txt_changeHTTP.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        txt_changeHTTP.setText("Change HTTP Method:");
        txt_changeHTTP.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                txt_changeHTTPchangeMethodlistener(evt);
            }
        });

        combo_changeHTTP.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "POST to GET", "GET to POST", "GET <-> POST" }));

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator4)
            .addComponent(jSeparator3, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator2)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(button6, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(button3, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(button4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(button5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(button2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(textpayloads)
                            .addComponent(jScrollPane17)))
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel10Layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanel10Layout.createSequentialGroup()
                                        .addComponent(check8)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(text5))
                                    .addComponent(jLabel53, javax.swing.GroupLayout.PREFERRED_SIZE, 704, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel52)
                                    .addGroup(jPanel10Layout.createSequentialGroup()
                                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(jButton9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jButton6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jButton8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(18, 18, 18)
                                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 660, Short.MAX_VALUE)
                                            .addComponent(combo2, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                    .addGroup(jPanel10Layout.createSequentialGroup()
                                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(button18, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(button19, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(18, 18, 18)
                                        .addComponent(jScrollPane14, javax.swing.GroupLayout.PREFERRED_SIZE, 664, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jLabel22)
                                    .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 704, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(jLabel5)
                            .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 777, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel54)
                            .addComponent(jLabel55, javax.swing.GroupLayout.PREFERRED_SIZE, 286, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(jPanel10Layout.createSequentialGroup()
                                    .addComponent(jLabel10)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(payload_position_combo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(txt_changeHTTP)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(combo_changeHTTP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 79, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel19)
                .addGap(18, 18, 18)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textpayloads, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(button3))
                .addGap(18, 18, 18)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(button6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(button2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(button4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(button5))
                    .addComponent(jScrollPane17, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, Short.MAX_VALUE)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel54)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel55)
                .addGap(18, 18, 18)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(payload_position_combo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txt_changeHTTP)
                    .addComponent(combo_changeHTTP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel52)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel53)
                .addGap(18, 18, 18)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(button19)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(button18))
                    .addComponent(jScrollPane14, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel22)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel23)
                .addGap(25, 25, 25)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(jButton9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton7))
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(combo2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton6))
                .addGap(19, 19, 19)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(check8)
                    .addComponent(text5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        jScrollPane5.setViewportView(jPanel10);

        jTabbedPane1.addTab("     Original Request     ", jScrollPane5);

        jLabel17.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(229, 92, 58));
        jLabel17.setText("Raw Request");

        jLabel44.setText("You can define entire raw request.");

        txt_rawrequest.setColumns(20);
        txt_rawrequest.setRows(5);
        jScrollPane2.setViewportView(txt_rawrequest);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel44, javax.swing.GroupLayout.PREFERRED_SIZE, 704, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17))
                .addGap(0, 176, Short.MAX_VALUE))
            .addComponent(jScrollPane2)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel44)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 919, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("   Raw Request   ", jPanel6);

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

        jLabel28.setText("You can define how your profile handles redirections.");

        jLabel29.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
        jLabel29.setForeground(new java.awt.Color(229, 92, 58));
        jLabel29.setText("Redirections");

        buttonGroup4.add(rb1);
        rb1.setText("Never");

        buttonGroup4.add(rb2);
        rb2.setText("On-site only");

        buttonGroup4.add(rb3);
        rb3.setText("In-scope only");

        buttonGroup4.add(rb4);
        rb4.setText("Always");

        jLabel2.setText("Max redirections:");

        jLabel6.setText("Follow redirections: ");

        button22.setText("Add");
        button22.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button22addGrep(evt);
            }
        });

        button23.setText("Remove");
        button23.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button23removeMatchReplace(evt);
            }
        });

        table6.setFont(new java.awt.Font("Lucida Grande", 0, 13)); // NOI18N
        table6.setModel(modelgrep);
        table6.setShowGrid(false);
        jScrollPane16.setViewportView(table6);

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
                    .addComponent(jSeparator5)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel11Layout.createSequentialGroup()
                                .addComponent(button8, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(textgreps))
                            .addComponent(jLabel25)
                            .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 769, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel11Layout.createSequentialGroup()
                                .addGap(20, 20, 20)
                                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel6)
                                    .addComponent(jLabel2))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(rb1)
                                    .addComponent(rb2)
                                    .addComponent(rb3)
                                    .addComponent(rb4)
                                    .addComponent(sp1, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(jLabel29)
                            .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, 769, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel11Layout.createSequentialGroup()
                                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(button23, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(button10, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(button7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(button22, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addComponent(jScrollPane16, javax.swing.GroupLayout.PREFERRED_SIZE, 681, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 78, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel25)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel24)
                .addGap(25, 25, 25)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textgreps, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(button8))
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane16, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addComponent(button22)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(button7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(button10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(button23)))
                .addGap(18, 18, 18)
                .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel29)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel28)
                .addGap(25, 25, 25)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rb1)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rb2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rb3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rb4)
                .addGap(18, 18, 18)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(sp1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(427, Short.MAX_VALUE))
        );

        JScrollPane responseresScroll = new JScrollPane(jPanel11,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        jScrollPane6.setViewportView(jPanel11);

        jTabbedPane1.addTab("     Response     ", jScrollPane6);

        jPanel12.setAutoscrolls(true);

        jLabel32.setText("You can define the issue properties.");

        jLabel33.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
        jLabel33.setForeground(new java.awt.Color(229, 92, 58));
        jLabel33.setText("Issue Properties");

        jLabel3.setText("Issue Name:");

        jLabel4.setText("Severity:");

        jLabel7.setText("Confidence:");

        text4.setText("Example Name");

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

        jLabel8.setText("Show Issue:");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Show all issues of this type per domain", "Show only one issue of this type per domain", "Not show this issue" }));
        jComboBox1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                ChangeIssueFields(evt);
            }
        });

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "High", "Medium", "Low", "Informational" }));

        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Certain", "Firm", "Tentative" }));

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
                        .addContainerGap(172, Short.MAX_VALUE))
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel12Layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addGap(18, 18, 18)
                                .addComponent(text4))
                            .addGroup(jPanel12Layout.createSequentialGroup()
                                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel8)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel7))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jComboBox1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jComboBox2, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jComboBox3, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jLabel33)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel32)
                .addGap(18, 18, 18)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(text4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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

        jTabbedPane1.addTab("     Issue     ", jScrollPane10);

        jLabel11.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        jLabel11.setText("Request type");

        request_position.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        request_position.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Original Request", "Raw Request" }));
        request_position.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                request_positionchangeRequestType(evt);
            }
        });

        buttonGroup1.add(same_insertion_point);
        same_insertion_point.setText("Same insertion point than the previous request");

        buttonGroup1.add(any_insertion_point);
        any_insertion_point.setText("Any insertion point");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(request_position, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(60, 60, 60)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(any_insertion_point)
                    .addComponent(same_insertion_point, javax.swing.GroupLayout.PREFERRED_SIZE, 325, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(218, Short.MAX_VALUE))
            .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 839, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(request_position, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(same_insertion_point))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(any_insertion_point)
                .addGap(0, 0, 0))
            .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                    .addGap(0, 71, Short.MAX_VALUE)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 457, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 839, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanel7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 560, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton9removeEncoder(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9removeEncoder
        int selectedIndex = list3.getSelectedIndex();
        if (selectedIndex != -1) {
            encoder.remove(selectedIndex);
        }
    }//GEN-LAST:event_jButton9removeEncoder

    private void jButton8upEncoder(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8upEncoder
        int selectedIndex = list3.getSelectedIndex();
        if (selectedIndex != 0) {
            swap(selectedIndex, selectedIndex - 1);
            list3.setSelectedIndex(selectedIndex - 1);
            list3.ensureIndexIsVisible(selectedIndex - 1);

        }
    }//GEN-LAST:event_jButton8upEncoder

    private void button18removeMatchReplace(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button18removeMatchReplace
        int[] rows = table4.getSelectedRows();
        Arrays.sort(rows);
        for (int i = rows.length - 1; i >= 0; i--) {
            int row = rows[i];
            int modelRow = table4.convertRowIndexToModel(row);
            model4.removeRow(modelRow);
        }
    }//GEN-LAST:event_button18removeMatchReplace

    private void button19addMatchReplace(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button19addMatchReplace
        model4.addRow(new Object[]{"Payload", "Leave blank to add a new header", "Leave blank to remove a matched header", "String"});
    }//GEN-LAST:event_button19addMatchReplace

    private void jButton6addEncoder(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6addEncoder
        if (!encoder.isEmpty() && encoder.firstElement().equals(" ")) {
            encoder.removeElementAt(0);
            encoder.addElement(combo2.getSelectedItem().toString());
        } else {
            encoder.addElement(combo2.getSelectedItem().toString());
        }
    }//GEN-LAST:event_jButton6addEncoder

    private void jButton7downEncoder(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7downEncoder
        int selectedIndex = list3.getSelectedIndex();
        if (selectedIndex != encoder.getSize() - 1) {
            swap(selectedIndex, selectedIndex + 1);
            list3.setSelectedIndex(selectedIndex + 1);
            list3.ensureIndexIsVisible(selectedIndex + 1);

        }
    }//GEN-LAST:event_jButton7downEncoder

    private void paramxmlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_paramxmlActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_paramxmlActionPerformed

    private void AllItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_AllItemStateChanged
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            extensionprovided.setSelected(true);
            useragent.setSelected(true);
            entirebody.setSelected(true);
            parambody.setSelected(true);
            paramcookie.setSelected(true);
            paramjson.setSelected(true);
            urlpathfolder.setSelected(true);
            parammultipartattr.setSelected(true);
            paramnamebody.setSelected(true);
            paramnameurl.setSelected(true);
            userprovided.setSelected(true);
            paramurl.setSelected(true);
            paramxml.setSelected(true);
            paramxmlattr.setSelected(true);
            urlpathfilename.setSelected(true);
            paramnamecookie.setSelected(true);
            paramnamexml.setSelected(true);
            paramnamexmlattr.setSelected(true);
            paramnamemultipartattr.setSelected(true);
            paramnamejson.setSelected(true);
            referer.setSelected(true);
            origin.setSelected(true);
            host.setSelected(true);
            contenttype.setSelected(true);
            accept.setSelected(true);
            acceptlanguage.setSelected(true);
            acceptencoding.setSelected(true);
            entirebodyxml.setSelected(true);
            entirebodyjson.setSelected(true);
            entirebodymultipart.setSelected(true);
        } else {
            extensionprovided.setSelected(false);
            useragent.setSelected(false);
            entirebody.setSelected(false);
            parambody.setSelected(false);
            paramcookie.setSelected(false);
            paramjson.setSelected(false);
            urlpathfolder.setSelected(false);
            parammultipartattr.setSelected(false);
            paramnamebody.setSelected(false);
            paramnameurl.setSelected(false);
            userprovided.setSelected(false);
            paramurl.setSelected(false);
            paramxml.setSelected(false);
            paramxmlattr.setSelected(false);
            urlpathfilename.setSelected(false);
            paramnamecookie.setSelected(false);
            paramnamexml.setSelected(false);
            paramnamexmlattr.setSelected(false);
            paramnamemultipartattr.setSelected(false);
            paramnamejson.setSelected(false);
            referer.setSelected(false);
            origin.setSelected(false);
            host.setSelected(false);
            contenttype.setSelected(false);
            accept.setSelected(false);
            acceptlanguage.setSelected(false);
            acceptencoding.setSelected(false);
            entirebodyxml.setSelected(false);
            entirebodyjson.setSelected(false);
            entirebodymultipart.setSelected(false);
        }
    }//GEN-LAST:event_AllItemStateChanged

    private void button9setNewHeader(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button9setNewHeader
        modelnewheaders.addRow(new Object[]{"New HTTP Header Name"});
    }//GEN-LAST:event_button9setNewHeader

    private void button11pasteNewHeaders(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button11pasteNewHeaders
        String element = getClipboardContents();
        List<String> lines = Arrays.asList(element.split("\n"));
        showNewHeaders(lines);
    }//GEN-LAST:event_button11pasteNewHeaders

    private void button12removeNewHeaders(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button12removeNewHeaders
        int[] rows = newHeadersTable.getSelectedRows();
        Arrays.sort(rows);
        for (int i = rows.length - 1; i >= 0; i--) {
            int row = rows[i];
            int modelRow = newHeadersTable.convertRowIndexToModel(row);
            modelnewheaders.removeRow(modelRow);
        }
    }//GEN-LAST:event_button12removeNewHeaders

    private void button13removeAllHeaders(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button13removeAllHeaders
        int rowCount = modelnewheaders.getRowCount();
        for (int i = rowCount - 1; i >= 0; i--) {
            modelnewheaders.removeRow(i);
        }
    }//GEN-LAST:event_button13removeAllHeaders

    private void button5removeAllPayloads(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button5removeAllPayloads
        int rowCount = modelpayload.getRowCount();
        for (int i = rowCount - 1; i >= 0; i--) {
            modelpayload.removeRow(i);
        }
    }//GEN-LAST:event_button5removeAllPayloads

    private void button4removePayload(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button4removePayload
        int[] rows = table7.getSelectedRows();
        Arrays.sort(rows);
        for (int i = rows.length - 1; i >= 0; i--) {
            int row = rows[i];
            int modelRow = table7.convertRowIndexToModel(row);
            modelpayload.removeRow(modelRow);
        }
    }//GEN-LAST:event_button4removePayload

    private void button2pastePayload(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button2pastePayload
        String element = getClipboardContents();
        List<String> lines = Arrays.asList(element.split("\n"));
        showPayloads(lines);
    }//GEN-LAST:event_button2pastePayload

    private void button6setToPayload(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button6setToPayload
        modelpayload.addRow(new Object[]{true, "Value"});
    }//GEN-LAST:event_button6setToPayload

    private void button3loadPayloads(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button3loadPayloads
        loadPayloadsFile(payload);
    }//GEN-LAST:event_button3loadPayloads

    private void txt_changeHTTPchangeMethodlistener(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_txt_changeHTTPchangeMethodlistener
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            combo_changeHTTP.setEnabled(true);
        } else if (evt.getStateChange() == java.awt.event.ItemEvent.DESELECTED) {
            combo_changeHTTP.setEnabled(false);
        }
    }//GEN-LAST:event_txt_changeHTTPchangeMethodlistener

    private void request_positionchangeRequestType(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_request_positionchangeRequestType
        if (request_position.getSelectedIndex() == 0) {
            jTabbedPane1.setSelectedIndex(0);
            jTabbedPane1.setEnabledAt(1, false);
            jTabbedPane1.setEnabledAt(0, true);

        } else if (request_position.getSelectedIndex() == 1) {
            jTabbedPane1.setSelectedIndex(1);
            jTabbedPane1.setEnabledAt(0, false);
            jTabbedPane1.setEnabledAt(1, true);
        }
    }//GEN-LAST:event_request_positionchangeRequestType

    private void button10removeAllGrep(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button10removeAllGrep
        int rowCount = modelgrep.getRowCount();
        for (int i = rowCount - 1; i >= 0; i--) {
            modelgrep.removeRow(i);
        }
    }//GEN-LAST:event_button10removeAllGrep

    private void button22addGrep(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button22addGrep
        if (modelgrep.getRowCount() == 0) {
            modelgrep.addRow(new Object[]{true, "", "Simple String", "", "Value"});
        } else {
            modelgrep.addRow(new Object[]{true, "OR", "Simple String", "", "Value"});
        }
    }//GEN-LAST:event_button22addGrep

    private void button23removeMatchReplace(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button23removeMatchReplace
        int[] rows = table4.getSelectedRows();
        Arrays.sort(rows);
        for (int i = rows.length - 1; i >= 0; i--) {
            int row = rows[i];
            int modelRow = table4.convertRowIndexToModel(row);
            modelgrep.removeRow(modelRow);
        }
    }//GEN-LAST:event_button23removeMatchReplace

    private void button8loadGrep(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button8loadGrep
        loadGrepsFile(modelgrep);
    }//GEN-LAST:event_button8loadGrep

    private void button7pasteGrep(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button7pasteGrep
        String element = getClipboardContents();
        List<String> lines = Arrays.asList(element.split("\n"));
        showGreps(lines);
    }//GEN-LAST:event_button7pasteGrep

    private void ChangeIssueFields(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_ChangeIssueFields
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            String selectedItem = (String) evt.getItem();
            if (selectedItem.equals("Not show this issue")) {
                jComboBox2.setEnabled(false);
                jComboBox3.setEnabled(false);
                text4.setEnabled(false);
                textarea1.setEnabled(false);
                textarea2.setEnabled(false);
                textarea3.setEnabled(false);
                textarea4.setEnabled(false);
            } else {
                jComboBox2.setEnabled(true);
                jComboBox3.setEnabled(true);
                text4.setEnabled(true);
                textarea1.setEnabled(true);
                textarea2.setEnabled(true);
                textarea3.setEnabled(true);
                textarea4.setEnabled(true);
            }
        }
    }//GEN-LAST:event_ChangeIssueFields


    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JCheckBox All;
    public javax.swing.JCheckBox accept;
    public javax.swing.JCheckBox acceptencoding;
    public javax.swing.JCheckBox acceptlanguage;
    public javax.swing.JRadioButton any_insertion_point;
    private javax.swing.JButton button10;
    public javax.swing.JButton button11;
    public javax.swing.JButton button12;
    public javax.swing.JButton button13;
    private javax.swing.JButton button18;
    private javax.swing.JButton button19;
    public javax.swing.JButton button2;
    private javax.swing.JButton button22;
    private javax.swing.JButton button23;
    public javax.swing.JButton button3;
    public javax.swing.JButton button4;
    public javax.swing.JButton button5;
    public javax.swing.JButton button6;
    private javax.swing.JButton button7;
    private javax.swing.JButton button8;
    public javax.swing.JButton button9;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.ButtonGroup buttonGroup4;
    public javax.swing.JCheckBox check8;
    public javax.swing.JComboBox<String> combo2;
    public javax.swing.JComboBox<String> combo_changeHTTP;
    public javax.swing.JCheckBox contenttype;
    public javax.swing.JCheckBox entirebody;
    public javax.swing.JCheckBox entirebodyjson;
    public javax.swing.JCheckBox entirebodymultipart;
    public javax.swing.JCheckBox entirebodyxml;
    public javax.swing.JCheckBox extensionprovided;
    public javax.swing.JCheckBox host;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    public javax.swing.JComboBox<String> jComboBox1;
    public javax.swing.JComboBox<String> jComboBox2;
    public javax.swing.JComboBox<String> jComboBox3;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
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
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    public javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane14;
    private javax.swing.JScrollPane jScrollPane16;
    private javax.swing.JScrollPane jScrollPane17;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JSeparator jSeparator10;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JSeparator jSeparator9;
    private javax.swing.JTabbedPane jTabbedPane1;
    public javax.swing.JList<String> list3;
    private javax.swing.JPanel newHeaderPanel;
    public javax.swing.JTable newHeadersTable;
    public javax.swing.JCheckBox origin;
    public javax.swing.JCheckBox parambody;
    public javax.swing.JCheckBox paramcookie;
    public javax.swing.JCheckBox paramjson;
    public javax.swing.JCheckBox parammultipartattr;
    public javax.swing.JCheckBox paramnamebody;
    public javax.swing.JCheckBox paramnamecookie;
    public javax.swing.JCheckBox paramnamejson;
    public javax.swing.JCheckBox paramnamemultipartattr;
    public javax.swing.JCheckBox paramnameurl;
    public javax.swing.JCheckBox paramnamexml;
    public javax.swing.JCheckBox paramnamexmlattr;
    public javax.swing.JCheckBox paramurl;
    public javax.swing.JCheckBox paramxml;
    public javax.swing.JCheckBox paramxmlattr;
    public javax.swing.JComboBox<String> payload_position_combo;
    public javax.swing.JRadioButton rb1;
    public javax.swing.JRadioButton rb2;
    public javax.swing.JRadioButton rb3;
    public javax.swing.JRadioButton rb4;
    public javax.swing.JCheckBox referer;
    public javax.swing.JComboBox<String> request_position;
    public javax.swing.JRadioButton same_insertion_point;
    public javax.swing.JCheckBox single_extensionprovided;
    public javax.swing.JSpinner sp1;
    public javax.swing.JTable table4;
    public javax.swing.JTable table6;
    public javax.swing.JTable table7;
    public javax.swing.JTextField text4;
    public javax.swing.JTextField text5;
    public javax.swing.JTextArea textarea1;
    public javax.swing.JTextArea textarea2;
    public javax.swing.JTextArea textarea3;
    public javax.swing.JTextArea textarea4;
    public javax.swing.JTextField textgreps;
    public javax.swing.JTextField textpayloads;
    public javax.swing.JCheckBox txt_changeHTTP;
    public javax.swing.JTextArea txt_rawrequest;
    public javax.swing.JCheckBox urlpathfilename;
    public javax.swing.JCheckBox urlpathfolder;
    public javax.swing.JCheckBox useragent;
    public javax.swing.JCheckBox userprovided;
    // End of variables declaration//GEN-END:variables
}
