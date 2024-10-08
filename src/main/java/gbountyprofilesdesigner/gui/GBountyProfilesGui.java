package gbountyprofilesdesigner.gui;

import gbountyprofilesdesigner.manager.ActiveProfile;
import gbountyprofilesdesigner.manager.ResponseProfile;
import gbountyprofilesdesigner.manager.RequestProfile;
import gbountyprofilesdesigner.manager.ActiveProfileManager;
import gbountyprofilesdesigner.manager.PassiveRequestProfileManager;
import gbountyprofilesdesigner.manager.PassiveResponseProfileManager;
import gbountyprofilesdesigner.properties.PassiveResponseProfileProperties;
import gbountyprofilesdesigner.properties.PassiveRequestProfileProperties;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import gbountyprofilesdesigner.properties.ActiveProfileProperties;
import gbountyprofilesdesigner.manager.ProfileManager;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class GBountyProfilesGui extends javax.swing.JFrame {

    public static String filename;
    public Integer is_BC;
    public Integer timeFailed;
    public Integer updateFirst;
    public String printcolor;
    public String avoidscan;
    public String avoidscanpassive;
    public String txtBC;
    JsonArray allprofiles;
    JsonArray activeprofiles;
    DefaultTableModel model;
    DefaultTableModel model1;
    DefaultTableModel model2;
    DefaultTableModel tagsmodel;
    private ProfileManager profileManager;

    public GBountyProfilesGui() {
        // Define the configuration directory
        filename = System.getProperty("user.home") + File.separator + ".gbounty" + File.separator + "profiles" + File.separator;

        // Create the directory if it does not exist
        File directory = new File(filename);
        if (!directory.exists()) {
            directory.mkdirs(); // Create all necessary directories
        }

        // Initialize models and arrays
        tagsmodel = new DefaultTableModel();
        allprofiles = new JsonArray();
        activeprofiles = new JsonArray();

        // Configuration for properties file
        Properties properties = new Properties();
        File configFile = new File("config.properties");

        if (!configFile.exists()) {
            try {
                // Create the configuration file if it does not exist
                configFile.createNewFile();
                properties.setProperty("lastUsedDirectory", filename);
                try (OutputStream output = new FileOutputStream("config.properties")) {
                    properties.store(output, "Configuration for profiles directory");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try (InputStream input = new FileInputStream("config.properties")) {
                // Load properties from the existing file
                properties.load(input);
                filename = properties.getProperty("lastUsedDirectory");
                if (filename == null) {
                    filename = System.getProperty("user.home") + File.separator + ".gbounty" + File.separator + "profiles" + File.separator;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        // Initialize table models
        model = createTableModel();
        model1 = createTableModel();
        model2 = createTableModel();

        // Initialize GUI components
        initComponents();
        filenameTextField.setText(filename);

        // Set up the main window
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) (screenSize.getWidth() * 0.8);
        int height = (int) (screenSize.getHeight() * 0.8);
        this.setSize(width, height);
        this.setTitle("GBounty Profiles Designer");
        this.setIconImage(new ImageIcon(getClass().getResource("/Logo.png")).getImage());
        this.setLocationRelativeTo(null);
        this.setVisible(true);

        // Add window listener to save configuration on close
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                guardarConfiguracion();
            }
        });

        // Enable components
        jPanel6.setEnabled(true);
        jLabel43.setEnabled(true);
        jLabel44.setEnabled(true);
        jLabel45.setEnabled(true);
        newTagCombo2.setEnabled(true);
        jtabpane.setEnabled(true);
        jPanel3.setEnabled(true);
        table3.setEnabled(true);
        jButton16.setEnabled(true);
        jButton2.setEnabled(true);
        button13.setEnabled(true);

        // Load profiles and tags
        checkActiveProfilesProperties(filename);
        makeTagsFile();
        showTagsTable();
        showTags();
        showProfiles("All");
        profilesRel();
    }

    /**
     * Creates a DefaultTableModel with custom settings.
     *
     * @return a customized DefaultTableModel
     */
    private DefaultTableModel createTableModel() {
        return new DefaultTableModel() {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };
    }

    public String getFilename() {
        return filename;
    }

    public JsonArray getProfiles() {
        return allprofiles;
    }

    public class ProfilesModelListener implements TableModelListener {

        @Override
        public void tableChanged(TableModelEvent e) {
            int row = e.getFirstRow();
            int column = e.getColumn();
            TableModel model = (TableModel) e.getSource();

            // Check if the "Enabled" column has changed
            if (column == 0) {
                boolean isChecked = (Boolean) model.getValueAt(row, column);
                String profileName = model.getValueAt(row, 1).toString();
                String profileFilePath = filename + profileName + ".bb2";

                updateProfileEnabledStatus(profileFilePath, isChecked);
            }
        }

        private void updateProfileEnabledStatus(String profileFilePath, boolean isEnabled) {
            try {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();

                // Read the profile from the file
                JsonArray jsonArray;
                try (Reader reader = new InputStreamReader(new FileInputStream(profileFilePath), StandardCharsets.UTF_8)) {
                    jsonArray = gson.fromJson(reader, JsonArray.class);
                    if (jsonArray == null || jsonArray.size() == 0) {
                        System.err.println("No data found in file: " + profileFilePath);
                        return;
                    }
                }

                // Update the "enabled" state of the first profile
                JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
                jsonObject.addProperty("enabled", isEnabled);

                // Write the updated profile back to the file
                try (Writer writer = new OutputStreamWriter(new FileOutputStream(profileFilePath), StandardCharsets.UTF_8)) {
                    gson.toJson(jsonArray, writer);
                }

                // Profiles refresh
                checkActiveProfilesProperties(filename);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void showProfiles(String tag) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonArray profiles = getProfiles();
        model.setRowCount(0);
        model.setColumnIdentifiers(new Object[]{"Enabled", "Profile Name", "Tags", "Author's Twitter"});

        // Set table properties
        configureTable(table3, model, new int[]{100, 680, 300, 150}, new int[]{100, 680, 550, 150});

        TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
        table3.setRowSorter(sorter);
        sorter.setSortKeys(Collections.singletonList(new RowSorter.SortKey(1, SortOrder.ASCENDING)));
        table3.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table3.getModel().addTableModelListener(new ProfilesModelListener());

        // Repeat similar setup for model1 and table1 (passive request)
        model1.setRowCount(0);
        model1.setColumnIdentifiers(new Object[]{"Enabled", "Profile Name", "Author's Twitter"});
        configureTable(table1, model1, new int[]{90, 800, 150}, new int[]{90, 800, 150});
        setupTableSorter(table1, model1);
        table1.getModel().addTableModelListener(new ProfilesModelListener());

        // Repeat similar setup for model2 and table2 (passive response)
        model2.setRowCount(0);
        model2.setColumnIdentifiers(new Object[]{"Enabled", "Profile Name", "Author's Twitter"});
        configureTable(table2, model2, new int[]{90, 800, 150}, new int[]{90, 800, 150});
        setupTableSorter(table2, model2);
        table2.getModel().addTableModelListener(new ProfilesModelListener());

        if (profiles != null) {
            for (JsonElement element : profiles) {
                ActiveProfileProperties profile = gson.fromJson(element, ActiveProfileProperties.class);
                if (tag.equals("All") || profileHasTag(profile, tag)) {
                    addProfileToModel(profile);
                }
            }
        }
    }

    private void configureTable(JTable table, DefaultTableModel model, int[] preferredWidths, int[] maxWidths) {
        for (int i = 0; i < table.getColumnCount(); i++) {
            TableColumn column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(preferredWidths[i]);
            column.setMaxWidth(maxWidths[i]);
            if (i >= 2) {
                DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
                centerRenderer.setHorizontalAlignment(JLabel.CENTER);
                column.setCellRenderer(centerRenderer);
            }
        }
    }

    private void setupTableSorter(JTable table, DefaultTableModel model) {
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        sorter.setSortKeys(Collections.singletonList(new RowSorter.SortKey(1, SortOrder.ASCENDING)));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }

    private boolean profileHasTag(ActiveProfileProperties profile, String tag) {
        List<String> tags = profile.getTags();
        if (tags == null) {
            return false;
        }
        for (String profileTag : tags) {
            if (profileTag.equalsIgnoreCase(tag)) {
                return true;
            }
        }
        return false;
    }

    private void addProfileToModel(ActiveProfileProperties profile) {
        Object[] rowData = new Object[]{
            profile.getEnabled(),
            profile.getProfileName(),
            String.join(", ", profile.getTags()),
            profile.getAuthor()
        };
        String scannerType = profile.getScanner();
        if (scannerType.contains("active")) {
            model.addRow(rowData);
        } else if (scannerType.contains("passive_request")) {
            model1.addRow(new Object[]{profile.getEnabled(), profile.getProfileName(), profile.getAuthor()});
        } else if (scannerType.contains("passive_response")) {
            model2.addRow(new Object[]{profile.getEnabled(), profile.getProfileName(), profile.getAuthor()});
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu1 = new javax.swing.JPopupMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem10 = new javax.swing.JMenuItem();
        jPopupMenu2 = new javax.swing.JPopupMenu();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jPopupMenu3 = new javax.swing.JPopupMenu();
        jMenuItem6 = new javax.swing.JMenuItem();
        jMenuItem7 = new javax.swing.JMenuItem();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jPanel6 = new javax.swing.JPanel();
        jLabel43 = new javax.swing.JLabel();
        jLabel44 = new javax.swing.JLabel();
        jLabel45 = new javax.swing.JLabel();
        newTagCombo2 = new javax.swing.JComboBox<>();
        jtabpane = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        table3 = new javax.swing.JTable();
        jButton16 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        button13 = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        table1 = new javax.swing.JTable();
        jButton17 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        button14 = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane10 = new javax.swing.JScrollPane();
        table2 = new javax.swing.JTable();
        jButton18 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        button15 = new javax.swing.JButton();
        jPanel11 = new javax.swing.JPanel();
        jButton13 = new javax.swing.JButton();
        jButton14 = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        tags_table1 = new javax.swing.JTable();
        filenameTextField = new javax.swing.JTextField();
        selectDirectory = new javax.swing.JButton();
        reloadProfiles = new javax.swing.JButton();
        jPanel15 = new javax.swing.JPanel();
        jLabel57 = new javax.swing.JLabel();
        jLabel58 = new javax.swing.JLabel();
        jScrollPane7 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel21 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jLabel55 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();

        jMenuItem2.setText("Enable");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jMenuItem2);

        jMenuItem3.setText("Disable");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jMenuItem3);

        jMenuItem10.setText("Set New Tag");
        jMenuItem10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem10setNewTag(evt);
            }
        });
        jPopupMenu1.add(jMenuItem10);

        jMenuItem4.setText("Enable");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jPopupMenu2.add(jMenuItem4);

        jMenuItem5.setText("Disable");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jPopupMenu2.add(jMenuItem5);

        jMenuItem6.setText("Enable");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        jPopupMenu3.add(jMenuItem6);

        jMenuItem7.setText("Disable");
        jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem7ActionPerformed(evt);
            }
        });
        jPopupMenu3.add(jMenuItem7);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTabbedPane2.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPane2showprofiles(evt);
            }
        });

        jPanel6.setEnabled(false);
        jPanel6.setPreferredSize(new java.awt.Dimension(800, 600));
        jPanel6.setLayout(new BorderLayout());

        jLabel43.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
        jLabel43.setForeground(new java.awt.Color(0, 78, 112));
        jLabel43.setText("Profile Manager");

        jLabel44.setText("In this section you can manage the profiles. ");

        jLabel45.setText("Filter by Tag");

        newTagCombo2.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                newTagCombo2selectTag(evt);
            }
        });
        newTagCombo2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newTagCombo(evt);
            }
        });

        jtabpane.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N

        table3.setAutoCreateRowSorter(true);
        table3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        table3.setModel(model);
        table3.getTableHeader().setReorderingAllowed(false);
        table3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                table3MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                table3MouseReleased(evt);
            }
        });
        jScrollPane5.setViewportView(table3);

        jButton16.setText("Add");
        jButton16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addActiveProfile(evt);
            }
        });

        jButton2.setText("Edit");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editActiveProfile(evt);
            }
        });

        button13.setText("Remove");
        button13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeProfiles(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(button13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton16, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 1124, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane5)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jButton16)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(button13)
                        .addContainerGap(373, Short.MAX_VALUE))))
        );

        jtabpane.addTab("     Active Profiles     ", jPanel3);

        table1.setAutoCreateRowSorter(true);
        table1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        table1.setModel(model1);
        table1.setRowSorter(null);
        table1.getTableHeader().setReorderingAllowed(false);
        table1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                table1MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                table1MouseReleased(evt);
            }
        });
        jScrollPane6.setViewportView(table1);

        jButton17.setText("Add");
        jButton17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addRequestProfile(evt);
            }
        });

        jButton3.setText("Edit");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editRequestProfile(evt);
            }
        });

        button14.setText("Remove");
        button14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removePassiveReqProfiles(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(button14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton17, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(1142, Short.MAX_VALUE))
            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                    .addGap(127, 127, 127)
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 1118, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button14)
                .addContainerGap(373, Short.MAX_VALUE))
            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel5Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 454, Short.MAX_VALUE)))
        );

        jtabpane.addTab("   Passive Request Profiles   ", jPanel5);

        table2.setAutoCreateRowSorter(true);
        table2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        table2.setModel(model2);
        table2.setRowSorter(null);
        table2.getTableHeader().setReorderingAllowed(false);
        table2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                table2MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                table2MouseReleased(evt);
            }
        });
        jScrollPane10.setViewportView(table2);

        jButton18.setText("Add");
        jButton18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addResponseProfile(evt);
            }
        });

        jButton4.setText("Edit");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editResponseProfile(evt);
            }
        });

        button15.setText("Remove");
        button15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeResponseProfiles(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(button15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton18, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(1142, Short.MAX_VALUE))
            .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                    .addGap(128, 128, 128)
                    .addComponent(jScrollPane10, javax.swing.GroupLayout.DEFAULT_SIZE, 1117, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton18)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button15)
                .addContainerGap(373, Short.MAX_VALUE))
            .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane10, javax.swing.GroupLayout.DEFAULT_SIZE, 454, Short.MAX_VALUE)))
        );

        jtabpane.addTab("   Passive Response Profiles   ", jPanel7);

        jButton13.setText("Add");
        jButton13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newTagButton(evt);
            }
        });

        jButton14.setText("Remove");
        jButton14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeTagButton(evt);
            }
        });

        tags_table1.setModel(tagsmodel);
        jScrollPane4.setViewportView(tags_table1);

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton14, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 1116, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addComponent(jButton13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton14)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 454, Short.MAX_VALUE)))
        );

        jtabpane.addTab("     Tags Manager     ", jPanel11);

        filenameTextField.setToolTipText("");

        selectDirectory.setText("Directory");
        selectDirectory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectDirectoryloadConfigFile(evt);
            }
        });

        reloadProfiles.setText("Reload");
        reloadProfiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                profilesReload(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jtabpane, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel45)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(newTagCombo2, javax.swing.GroupLayout.PREFERRED_SIZE, 325, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(selectDirectory, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(reloadProfiles, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(filenameTextField))
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel43)
                                    .addComponent(jLabel44, javax.swing.GroupLayout.PREFERRED_SIZE, 843, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jLabel43)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel44)
                .addGap(18, 18, 18)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(newTagCombo2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel45))
                        .addGap(49, 49, 49)
                        .addComponent(jtabpane))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(selectDirectory)
                            .addComponent(filenameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(reloadProfiles)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jTabbedPane2.addTab("   Profiles   ", jPanel6);

        jLabel57.setText("In this section you can see global variables that will be used for scanning.");

        jLabel58.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
        jLabel58.setForeground(new java.awt.Color(0, 78, 112));
        jLabel58.setText("Variables");

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"{BH}", "Replaced by Blind Host domain automatically."},
                {"{EMAIL}", "Replaced by email address specified at \"email\" parameter."},
                {"{RANDOM}", "Replaced by a random number."},
                {"{CURRENT_URL}", "Replaced by entire original URL request."},
                {"{CURRENT_PORT}", "Replaced bythe original request web server port."},
                {"{CURRENT_PATH}", "Replaced by the original request path."},
                {"{CURRENT_HOST}", "Replaced by the original request host."},
                {"{CURRENT_METHOD}", "Replaced by the original request method. (GET, POST, etc.)"},
                {"{CURRENT_QUERY}", "Replaced by the original request POST data query."},
                {"{CURRENT_SUBDOMAIN}", "Replaced by the original request subdomain. (www,docs,prod,etc)"},
                {"{CURRENT_FILE}", "Replaced by the original request file."},
                {"{CURRENT_PROTOCOL}", "Replaced by the original request protocol. (http,https)"},
                {"{CURRENT_USER_AGENT}", "Replaced by the original request user agent header value."},
                {"{CURRENT_REFERER}", "Replaced by the original request referer header value."},
                {"{CURRENT_ORIGIN}", "Replaced by the original request origin header value."},
                {"{CURRENT_ACCEPT}", "Replaced by the original request accept header value."},
                {"{CURRENT_CONTENT_TYPE}", "Replaced by the original request content type header value."},
                {"{CURRENT_ACCEPT_LANGUAGE}", "Replaced by the original request accept language header value."},
                {"{CURRENT_ACCEPT_ENCODING}", "Replaced by the original request accept encoding header value."},
                {"{CURRENT_CONTENT_LENGTH}", "Replaced by the original request cointent length header value."}
            },
            new String [] {
                "Variable", "Value"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.getTableHeader().setReorderingAllowed(false);
        jTable1.setLayout(new BorderLayout());
        jScrollPane7.setViewportView(jTable1);

        jLabel21.setText("These variables will be replaced automatically during the scan, whenever present in the request/profile.");

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 915, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel58)
                    .addComponent(jLabel57, javax.swing.GroupLayout.PREFERRED_SIZE, 910, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 943, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(308, Short.MAX_VALUE))
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel58)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel57)
                .addGap(18, 18, 18)
                .addComponent(jLabel21)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jTabbedPane2.addTab("     Variables     ", jPanel15);

        jLabel55.setFont(new java.awt.Font("Lucida Grande", 1, 36)); // NOI18N
        jLabel55.setForeground(new java.awt.Color(0, 78, 112));
        jLabel55.setText("About.");

        jLabel10.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel10.setText("<html><body><div style='text-align: center;'>GBounty Profiles Designer v1.0.1 - <span style='color: #0075A8; text-decoration: underline; cursor: hand;'>https://github.com/BountySecurity/GBountyProfilesDesigner</span></div></body></html>");
        jLabel10.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel10.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                clickBounty(evt);
            }
        });

        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Logo_big.png"))); // NOI18N

        jLabel11.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel11.setText("<html><body><p style='text-align:justify;'>GBounty Profiles Designer empowers you to design intricate multistep web vulnerability profiles using a user-friendly graphical interface.<br><br>This tool streamlines the process of creating and customizing vulnerability profiles, enabling swift integration of novel web vulnerabilities into your assessments.<br><br>Boasting unparalleled customization features, including innovative insertion points and search types, the GBounty Profiles Designer ensures thorough web application evaluations by facilitating the design of both passive and active vulnerability profiles.</p></body></html>");
        jLabel11.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel55)
                    .addComponent(jLabel13)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(174, 174, 174)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel8Layout.createSequentialGroup()
                    .addGap(16, 16, 16)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel55)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 234, Short.MAX_VALUE)
                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(154, 154, 154))
            .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel8Layout.createSequentialGroup()
                    .addGap(204, 204, 204)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(247, Short.MAX_VALUE)))
        );

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("     About     ", jPanel9);

        jScrollPane1.setViewportView(jTabbedPane2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 670, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void newTagCombo2selectTag(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_newTagCombo2selectTag
        if ((evt.getStateChange() == java.awt.event.ItemEvent.SELECTED)) {
            showProfiles(newTagCombo2.getItemAt(newTagCombo2.getSelectedIndex()));

        }
    }//GEN-LAST:event_newTagCombo2selectTag

    private void newTagCombo(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newTagCombo
        // TODO add your handling code here:
    }//GEN-LAST:event_newTagCombo

    private void table3MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_table3MousePressed
        if (evt.isPopupTrigger()) {
            jPopupMenu1.show(table3, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_table3MousePressed

    private void table3MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_table3MouseReleased
        if (evt.isPopupTrigger()) {
            jPopupMenu1.show(table3, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_table3MouseReleased

    private void addActiveProfile(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addActiveProfile
        // First, we initialize the UI
        ActiveProfile profileUI = new ActiveProfile();
        JOptionPane jOptionPane = new JOptionPane(profileUI, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        JDialog dialog = jOptionPane.createDialog(jOptionPane, "Add New Active Profile");
        dialog.setSize(new Dimension(920, 760));
        dialog.setResizable(true);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);

        // If the user clicked OK and the profile has a name, we save the profile
        Object selectedValue = jOptionPane.getValue();
        if (selectedValue == null || (Integer) selectedValue != JOptionPane.OK_OPTION || profileUI.textname.getText().isEmpty()) {
            return;
        }

        profileManager = new ProfileManager(filename);
        ActiveProfileManager pm = new ActiveProfileManager(profileManager.getPath());
        ActiveProfileProperties newPP = pm.saveActiveAttackValues(profileUI);
        profileManager.updateProfile("", newPP);
        profilesRel();

    }//GEN-LAST:event_addActiveProfile

    private List<String> readFile(String filename) {
        List<String> records = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                records.add(line.trim());
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + filename);
            e.printStackTrace();
        }
        return records;
    }

    public void showTagsTable() {
        tagsmodel.setRowCount(0);
        tagsmodel.setColumnIdentifiers(new Object[]{"Tag Name"});

        List<String> tags = readFile(filename + File.separator + "tags.txt");
        newTagCombo2.removeAllItems();
        boolean hasAll = false;

        for (String tag : tags) {
            tagsmodel.addRow(new Object[]{tag});
            newTagCombo2.addItem(tag);
            if (tag.equalsIgnoreCase("All")) {
                hasAll = true;
            }
        }
        if (!hasAll) {
            newTagCombo2.addItem("All");
        }
        newTagCombo2.setSelectedItem("All");

        TableRowSorter<TableModel> sorter = new TableRowSorter<>(tagsmodel);
        tags_table1.setRowSorter(sorter);
    }

    public void checkActiveProfilesProperties(String filename) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonArray allData = new JsonArray();

        File dir = new File(filename);

        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".bb2"));
            if (files != null) {
                for (File file : files) {
                    try (Reader reader = new InputStreamReader(
                            new FileInputStream(file), StandardCharsets.UTF_8)) {

                        JsonElement jsonElement = JsonParser.parseReader(reader);

                        if (jsonElement.isJsonArray()) {
                            JsonArray array = jsonElement.getAsJsonArray();
                            for (JsonElement elem : array) {
                                if (elem.isJsonObject()) {
                                    allData.add(elem.getAsJsonObject());
                                }
                            }
                        } else if (jsonElement.isJsonObject()) {
                            allData.add(jsonElement.getAsJsonObject());
                        }

                    } catch (Exception e) {
                        System.err.println("Error reading profile file: " + file.getAbsolutePath());
                        e.printStackTrace();
                    }
                }
            }
        }

        setAllProfiles(allData);
    }

    public void setPassiveProfiles(JsonArray allProfiles) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonArray passiveResponseProfiles = new JsonArray();
        JsonArray passiveRequestProfiles = new JsonArray();

        for (JsonElement element : allProfiles) {
            ActiveProfileProperties profile = gson.fromJson(element, ActiveProfileProperties.class);
            if (profile.getEnabled()) {
                String scanner = profile.getScanner();
                if (scanner.contains("passive_response")) {
                    passiveResponseProfiles.add(element);
                } else if (scanner.contains("passive_request")) {
                    passiveRequestProfiles.add(element);
                }
            }
        }
    }

    public void setAllProfiles(JsonArray allProfiles) {
        this.allprofiles = allProfiles;
        setActiveProfiles(allprofiles);
        setPassiveProfiles(allprofiles);
    }

    public void setActiveProfiles(JsonArray allProfiles) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        activeprofiles = new JsonArray();

        for (JsonElement element : allProfiles) {
            ActiveProfileProperties profile = gson.fromJson(element, ActiveProfileProperties.class);
            if (profile.getEnabled() && profile.getScanner().contains("active")) {
                activeprofiles.add(element);
            }
        }
    }


    private void editActiveProfile(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editActiveProfile
        // First we fetch the selected profile
        String oldName = this.table3.getValueAt(table3.getSelectedRow(), 1).toString();
        profileManager = new ProfileManager(filename);
        ActiveProfileProperties oldPP = profileManager.getActiveByName(oldName);
        if (oldPP == null) {
            return;
        }

        // Then, we initialize the UI
        ActiveProfile profileUI = new ActiveProfile();
        ActiveProfileManager pm = new ActiveProfileManager(profileManager.getPath());
        pm.setActiveAttackValues(profileUI, oldPP);

        // Finally, we draw the UI
        JOptionPane jOptionPane = new JOptionPane(profileUI, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        JDialog dialog = jOptionPane.createDialog(jOptionPane, "Edit Active Profile");
        dialog.setSize(new Dimension(920, 760));
        dialog.setResizable(true);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);

        // If the user clicked OK, we save the profile
        Object selectedValue = jOptionPane.getValue();
        if (selectedValue == null || (Integer) selectedValue != JOptionPane.OK_OPTION) {
            return;
        }

        ActiveProfileProperties newPP = pm.saveActiveAttackValues(profileUI);
        profileManager.updateProfile(oldName, newPP);
        profilesRel();

    }//GEN-LAST:event_editActiveProfile

    private void removeProfiles(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeProfiles
        int activePane = jtabpane.getSelectedIndex();

        if (activePane == 0) {
            deleteProfile(table3);
        }
        profilesRel();
    }//GEN-LAST:event_removeProfiles

    public void deleteProfile(JTable table) {
        int[] rows = table.getSelectedRows();

        for (int row : rows) {
            String profileName = table.getValueAt(row, 1).toString();
            String profileFilePath = filename + profileName + ".bb2";
            Path filePath = Paths.get(profileFilePath);

            try {
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                System.err.println("Error deleting file: " + filePath);
                e.printStackTrace();
            }
        }

        checkActiveProfilesProperties(filename);
        showProfiles("All");
    }

    public void loadConfigFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Profiles Directory");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int userSelection = fileChooser.showOpenDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File selectedDir = fileChooser.getSelectedFile();
            filename = selectedDir.getAbsolutePath() + File.separator;
            filenameTextField.setText(filename);

            checkActiveProfilesProperties(filename);
            makeTagsFile();
            showTagsTable();
            showProfiles("All");
        }
    }

    public void makeTagsFile() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonArray profiles = getProfiles();
        Set<String> tagsSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        for (JsonElement element : profiles) {
            ActiveProfileProperties profile = gson.fromJson(element, ActiveProfileProperties.class);
            List<String> profileTags = profile.getTags();
            if (profileTags != null) {
                tagsSet.addAll(profileTags);
            }
        }

        Path tagsFilePath = Paths.get(filename, "tags.txt");
        try (BufferedWriter writer = Files.newBufferedWriter(tagsFilePath, StandardCharsets.UTF_8)) {
            for (String tag : tagsSet) {
                writer.write(tag);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing tags file: " + tagsFilePath);
            e.printStackTrace();
        }
    }

    public void addNewTag(String tag) {
        Path tagsFilePath = Paths.get(filename, "tags.txt");
        try {
            List<String> existingTags = Files.readAllLines(tagsFilePath, StandardCharsets.UTF_8);
            if (!existingTags.contains(tag)) {
                try (BufferedWriter writer = Files.newBufferedWriter(tagsFilePath, StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
                    writer.write(tag);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Error updating tags file: " + tagsFilePath);
            e.printStackTrace();
        }
    }

    public void removeTag(String tag) {
        Path tagsFilePath = Paths.get(filename, "tags.txt");

        if (Files.exists(tagsFilePath)) {
            Path tempFilePath = Paths.get(filename, "tags_temp.txt");
            try (BufferedReader reader = Files.newBufferedReader(tagsFilePath, StandardCharsets.UTF_8); BufferedWriter writer = Files.newBufferedWriter(tempFilePath, StandardCharsets.UTF_8)) {

                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().equals(tag)) {
                        writer.write(line);
                        writer.newLine();
                    }
                }
            } catch (IOException e) {
                System.err.println("Error processing tags file: " + tagsFilePath);
                e.printStackTrace();
                return;
            }

            try {
                Files.delete(tagsFilePath);
                Files.move(tempFilePath, tagsFilePath);
            } catch (IOException e) {
                System.err.println("Error replacing the original tags file.");
                e.printStackTrace();
            }
        } else {
            System.err.println("Tags file does not exist: " + tagsFilePath);
        }
    }

    public void showTags() {
        showTagsTable();
    }

    public void deleteTagProfiles(String tag) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Path dirPath = Paths.get(filename);

        if (Files.exists(dirPath) && Files.isDirectory(dirPath)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath, "*.bb2")) {
                for (Path filePath : stream) {
                    try {
                        List<ActiveProfileProperties> profiles;
                        try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
                            profiles = gson.fromJson(reader, new TypeToken<List<ActiveProfileProperties>>() {
                            }.getType());
                        }

                        boolean updated = false;
                        for (ActiveProfileProperties profile : profiles) {
                            List<String> tags = profile.getTags();
                            if (tags != null && tags.removeIf(existingTag -> existingTag.equals(tag))) {
                                updated = true;
                            }
                        }

                        if (updated) {
                            try (Writer writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                                gson.toJson(profiles, writer);
                            }
                        }

                    } catch (JsonSyntaxException | IOException e) {
                        System.err.println("Error processing file: " + filePath);
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                System.err.println("Error accessing directory: " + dirPath);
                e.printStackTrace();
            }
        } else {
            System.err.println("Directory does not exist: " + dirPath);
        }

        checkActiveProfilesProperties(filename);
        showProfiles("All");
    }

    public void setEnableDisableProfile(String enable, JTable table) {
        boolean isEnabled = enable.equalsIgnoreCase("Yes");
        int[] rows = table.getSelectedRows();

        for (int row : rows) {
            String profileName = table.getValueAt(row, 1).toString();
            String profileFilePath = filename + profileName + ".bb2";

            try {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();

                // Read the existing profile data
                ActiveProfileProperties profileProperties;
                try (Reader reader = new InputStreamReader(
                        new FileInputStream(profileFilePath), StandardCharsets.UTF_8)) {
                    List<ActiveProfileProperties> profiles = gson.fromJson(
                            reader, new TypeToken<List<ActiveProfileProperties>>() {
                            }.getType());
                    if (profiles != null && !profiles.isEmpty()) {
                        profileProperties = profiles.get(0);
                    } else {
                        System.err.println("No profile data found in file: " + profileFilePath);
                        continue;
                    }
                }

                // Update the "enabled" status
                profileProperties.setEnabled(isEnabled);

                // Write the updated profile back to the file
                try (Writer writer = new OutputStreamWriter(
                        new FileOutputStream(profileFilePath), StandardCharsets.UTF_8)) {
                    gson.toJson(Collections.singletonList(profileProperties), writer);
                }

            } catch (IOException e) {
                System.err.println("Error updating profile: " + profileFilePath);
                e.printStackTrace();
            }
        }

        checkActiveProfilesProperties(filename);
        showProfiles("All");
    }

    public void setProfileTags(String tag) {
        int[] rows = table3.getSelectedRows();

        for (int row : rows) {
            String profileName = table3.getValueAt(row, 1).toString();
            String profileFilePath = filename + profileName + ".bb2";

            try {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();

                // Read the existing profile data
                ActiveProfileProperties profileProperties;
                try (Reader reader = new InputStreamReader(
                        new FileInputStream(profileFilePath), StandardCharsets.UTF_8)) {
                    List<ActiveProfileProperties> profiles = gson.fromJson(
                            reader, new TypeToken<List<ActiveProfileProperties>>() {
                            }.getType());
                    if (profiles != null && !profiles.isEmpty()) {
                        profileProperties = profiles.get(0);
                    } else {
                        System.err.println("No profile data found in file: " + profileFilePath);
                        continue;
                    }
                }

                // Update the tags
                List<String> tags = profileProperties.getTags();
                if (tags == null) {
                    tags = new ArrayList<>();
                }
                if (!tags.contains(tag)) {
                    tags.add(tag);
                    profileProperties.setTags(tags);
                }

                // Write the updated profile back to the file
                try (Writer writer = new OutputStreamWriter(
                        new FileOutputStream(profileFilePath), StandardCharsets.UTF_8)) {
                    gson.toJson(Collections.singletonList(profileProperties), writer);
                }

            } catch (IOException e) {
                System.err.println("Error updating profile tags: " + profileFilePath);
                e.printStackTrace();
            }
        }

        checkActiveProfilesProperties(filename);
        showProfiles("All");
    }

    private void profilesRel() {
        filename = filenameTextField.getText();
        makeTagsFile();
        checkActiveProfilesProperties(filename);
        showTagsTable();
        showProfiles("All");
        filenameTextField.setText(filename);
    }

    private void guardarConfiguracion() {
        Properties properties = new Properties();
        properties.setProperty("lastUsedDirectory", filename);
        try (OutputStream output = new FileOutputStream("config.properties")) {
            properties.store(output, "Configuration for profiles directory");
        } catch (IOException io) {
            io.printStackTrace();
        }
    }


    private void jTabbedPane2showprofiles(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPane2showprofiles

    }//GEN-LAST:event_jTabbedPane2showprofiles

    private void table2MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_table2MousePressed
        if (evt.isPopupTrigger()) {
            jPopupMenu3.show(table2, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_table2MousePressed

    private void table2MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_table2MouseReleased
        if (evt.isPopupTrigger()) {
            jPopupMenu3.show(table2, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_table2MouseReleased

    private void addResponseProfile(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addResponseProfile
        // First, we initialize the UI
        ResponseProfile profileUI = new ResponseProfile();
        JOptionPane jOptionPane = new JOptionPane(profileUI, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        JDialog dialog = jOptionPane.createDialog(jOptionPane, "Add New Passive Response Profile");
        dialog.setSize(new Dimension(920, 760));
        dialog.setResizable(true);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);

        // If the user clicked OK and the profile has a name, we save the profile
        Object selectedValue = jOptionPane.getValue();
        if (selectedValue == null || (Integer) selectedValue != JOptionPane.OK_OPTION || profileUI.text1.getText().isEmpty()) {
            return;
        }

        profileManager = new ProfileManager(filename);
        PassiveResponseProfileManager pm = new PassiveResponseProfileManager(profileManager.getPath());
        PassiveResponseProfileProperties newPP = pm.savePassiveResponseOptions(profileUI);
        profileManager.updateProfile("", newPP);
        profilesRel();
    }//GEN-LAST:event_addResponseProfile

    private void editResponseProfile(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editResponseProfile
        // First we fetch the selected profile
        profileManager = new ProfileManager(filename);
        String oldName = table2.getValueAt(table2.getSelectedRow(), 1).toString();
        PassiveResponseProfileProperties oldPP = profileManager.getResponsesByName(oldName);
        if (oldPP == null) {
            return;
        }

        // Then, we initialize the UI
        ResponseProfile profileUI = new ResponseProfile();
        PassiveResponseProfileManager pm = new PassiveResponseProfileManager(profileManager.getPath());
        pm.setPassiveResponseOptions(profileUI, oldPP);

        // Finally, we draw the UI
        JOptionPane jOptionPane = new JOptionPane(profileUI, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        JDialog dialog = jOptionPane.createDialog(jOptionPane, "Edit Passive Response Profile");
        dialog.setSize(new Dimension(920, 760));
        dialog.setResizable(true);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);

        // If the user clicked OK, we save the profile
        Object selectedValue = jOptionPane.getValue();
        if (selectedValue == null || (Integer) selectedValue != JOptionPane.OK_OPTION) {
            return;
        }

        PassiveResponseProfileProperties newPP = pm.savePassiveResponseOptions(profileUI);
        profileManager.updateProfile(oldName, newPP);
        profilesRel();
    }//GEN-LAST:event_editResponseProfile

    private void removeResponseProfiles(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeResponseProfiles
        int activePane = jtabpane.getSelectedIndex();

        if (activePane == 0) {
            deleteProfile(table3);
        } else if (activePane == 1) {
            deleteProfile(table1);
        } else if (activePane == 2) {
            deleteProfile(table2);
        }
        profilesRel();
    }//GEN-LAST:event_removeResponseProfiles

    private void newTagButton(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newTagButton
        NewTag nt = new NewTag();
        int result = JOptionPane.showConfirmDialog(
                this,
                nt,
                "New Tag",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String newTagText = nt.newTagtext.getText().trim();
            if (!newTagText.isEmpty()) {
                addNewTag(newTagText);
                showTags();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "The tag cannot be empty. Please enter a valid tag.",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        }
    }//GEN-LAST:event_newTagButton

    private void removeTagButton(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeTagButton
        int[] selectedIndices = tags_table1.getSelectedRows();

        for (int index : selectedIndices) {
            if (index != -1) {
                String tag = tagsmodel.getValueAt(index, 0).toString();
                if (!tag.equals("All")) {
                    deleteTagProfiles(tag);
                    removeTag(tag);
                }
            }
        }
        showTagsTable();
    }//GEN-LAST:event_removeTagButton

    private void table1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_table1MousePressed
        if (evt.isPopupTrigger()) {
            jPopupMenu2.show(table1, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_table1MousePressed

    private void table1MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_table1MouseReleased
        if (evt.isPopupTrigger()) {
            jPopupMenu2.show(table1, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_table1MouseReleased

    private void addRequestProfile(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addRequestProfile

        // First, we initialize the UI
        RequestProfile profileUI = new RequestProfile();
        JOptionPane jOptionPane = new JOptionPane(profileUI, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        JDialog dialog = jOptionPane.createDialog(jOptionPane, "Add New Passive Request Profile");
        dialog.setSize(new Dimension(920, 760));
        dialog.setResizable(true);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);

        // If the user clicked OK and the profile has a name, we save the profile
        Object selectedValue = jOptionPane.getValue();
        if (selectedValue == null || (Integer) selectedValue != JOptionPane.OK_OPTION || profileUI.text1.getText().isEmpty()) {
            return;
        }

        profileManager = new ProfileManager(filename);
        PassiveRequestProfileManager pm = new PassiveRequestProfileManager(filename);
        PassiveRequestProfileProperties newPP = pm.savePassiveRequestOptions(profileUI);
        profileManager.updateProfile("", newPP);
        profilesRel();
    }//GEN-LAST:event_addRequestProfile

    private void editRequestProfile(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editRequestProfile
        // First we fetch the selected profile
        profileManager = new ProfileManager(filename);
        String oldName = table1.getValueAt(table1.getSelectedRow(), 1).toString();
        PassiveRequestProfileProperties oldPP = profileManager.getRequestsByName(oldName);
        if (oldPP == null) {
            return;
        }

        // Then, we initialize the UI
        RequestProfile profileUI = new RequestProfile();
        PassiveRequestProfileManager pm = new PassiveRequestProfileManager(profileManager.getPath());
        pm.setPassiveRequestOptions(profileUI, oldPP);

        // Finally, we draw the UI
        JOptionPane jOptionPane = new JOptionPane(profileUI, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        JDialog dialog = jOptionPane.createDialog(jOptionPane, "Edit Passive Request Profile");
        dialog.setSize(new Dimension(920, 760));
        dialog.setResizable(true);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);

        // If the user clicked OK, we save the profile
        Object selectedValue = jOptionPane.getValue();
        if (selectedValue == null || (Integer) selectedValue != JOptionPane.OK_OPTION) {
            return;
        }

        PassiveRequestProfileProperties newPP = pm.savePassiveRequestOptions(profileUI);
        profileManager.updateProfile(oldName, newPP);
        profilesRel();
    }//GEN-LAST:event_editRequestProfile

    private void removePassiveReqProfiles(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removePassiveReqProfiles
        int activePane = jtabpane.getSelectedIndex();

        if (activePane == 0) {
            deleteProfile(table3);
        } else if (activePane == 1) {
            deleteProfile(table1);
        } else if (activePane == 2) {
            deleteProfile(table2);
        }
        profilesRel();
    }//GEN-LAST:event_removePassiveReqProfiles

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        setEnableDisableProfile("Yes", table3);
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        setEnableDisableProfile("No", table3);
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem10setNewTag(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem10setNewTag
        NewTag nt = new NewTag();
        int result = JOptionPane.showConfirmDialog(this, nt, "New Tag", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String newTag = nt.newTagtext.getText().trim();
            if (!newTag.isEmpty()) {
                setProfileTags(newTag);
                addNewTag(newTag);
                showTagsTable();
            } else {
                JOptionPane.showMessageDialog(this, "Tag cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_jMenuItem10setNewTag

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        setEnableDisableProfile("Yes", table1);
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        setEnableDisableProfile("No", table1);
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
        setEnableDisableProfile("Yes", table2);
    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void jMenuItem7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem7ActionPerformed
        setEnableDisableProfile("No", table2);
    }//GEN-LAST:event_jMenuItem7ActionPerformed

    private void clickBounty(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_clickBounty

        try {
            Desktop.getDesktop().browse(new URI("https://github.com/BountySecurity/GBountyProfilesDesigner/"));
        } catch (IOException | URISyntaxException e1) {
            e1.printStackTrace();
        }
    }//GEN-LAST:event_clickBounty

    private void selectDirectoryloadConfigFile(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectDirectoryloadConfigFile
        loadConfigFile();
    }//GEN-LAST:event_selectDirectoryloadConfigFile

    private void profilesReload(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_profilesReload
        profilesRel();
    }//GEN-LAST:event_profilesReload

    /**
     * Main method.
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
        } catch (Exception ex) {
            Logger.getLogger(GBountyProfilesGui.class.getName()).log(Level.SEVERE, null, ex);
        }

        SwingUtilities.invokeLater(() -> {
            new GBountyProfilesGui().setVisible(true);
        });
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton button13;
    private javax.swing.JButton button14;
    private javax.swing.JButton button15;
    public javax.swing.JTextField filenameTextField;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton16;
    private javax.swing.JButton jButton17;
    private javax.swing.JButton jButton18;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    public javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JMenuItem jMenuItem10;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    public javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JPopupMenu jPopupMenu2;
    private javax.swing.JPopupMenu jPopupMenu3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    public javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JTabbedPane jtabpane;
    private javax.swing.JComboBox<String> newTagCombo2;
    private javax.swing.JButton reloadProfiles;
    private javax.swing.JButton selectDirectory;
    private javax.swing.JTable table1;
    private javax.swing.JTable table2;
    private javax.swing.JTable table3;
    private javax.swing.JTable tags_table1;
    // End of variables declaration//GEN-END:variables
}
