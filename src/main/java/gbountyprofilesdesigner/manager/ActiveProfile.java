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

import gbountyprofilesdesigner.gui.GBountyProfilesGui;
import gbountyprofilesdesigner.gui.NewTag;
import gbountyprofilesdesigner.properties.ActiveProfileProperties;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author eduardogarcia
 */
public class ActiveProfile extends javax.swing.JPanel {

    public DefaultListModel<String> tag;
    DefaultListModel tagmanager;
    private DefaultTableModel modelGrep;
    private DefaultTableModel modelPayload;
    private DefaultTableModel modelNewHeaders;
    private String filename;
    private List<RequestResponse> requestResponseList;
    private ActiveProfileProperties properties;

    public ActiveProfile() {
        tag = new DefaultListModel<>();
        tagmanager = new DefaultListModel();
        modelGrep = createGrepTableModel();
        modelPayload = createPayloadTableModel();
        modelNewHeaders = new DefaultTableModel();
        requestResponseList = new ArrayList<>();
        filename = GBountyProfilesGui.filename;

        initComponents();
        setupTabbedPane();
        addInitialRequestResponse();
        showTags();
    }

    private void setupTabbedPane() {
        jTabbedPane2.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int tabIndex = jTabbedPane2.indexAtLocation(e.getX(), e.getY());
                if (tabIndex == jTabbedPane2.getTabCount() - 1) {
                    addNewRequestResponseTab();
                }
            }
        });

        jTabbedPane2.addChangeListener(e -> {
            if (jTabbedPane2.getSelectedIndex() == jTabbedPane2.getTabCount() - 1) {
                jTabbedPane2.setSelectedIndex(jTabbedPane2.getTabCount() - 2);
            }
        });
    }

    private void addInitialRequestResponse() {
        RequestResponse rr = new RequestResponse(filename);
        rr.same_insertion_point.setVisible(false);
        rr.any_insertion_point.setVisible(false);
        addRequestResponseTab(rr, "Step " + jTabbedPane2.getTabCount());
    }

    private void addNewRequestResponseTab() {
        RequestResponse rr = new RequestResponse(filename);
        String tabTitle = "Step " + jTabbedPane2.getTabCount();
        addRequestResponseTab(rr, tabTitle);
    }

    private void addRequestResponseTab(RequestResponse rr, String tabTitle) {
        int index = jTabbedPane2.getTabCount() - 1;
        jTabbedPane2.insertTab(tabTitle, null, rr, null, index);
        jTabbedPane2.setTabComponentAt(index, new CloseableTabHeader(jTabbedPane2, tabTitle, requestResponseList));
        jTabbedPane2.setSelectedIndex(index);
        requestResponseList.add(rr);
    }

    private DefaultTableModel createGrepTableModel() {
        return new DefaultTableModel() {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return !(row == 0 && column == 1);
            }
        };
    }

    private DefaultTableModel createPayloadTableModel() {
        return new DefaultTableModel() {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : String.class;
            }
        };
    }

    public List<RequestResponse> getRequestResponseList() {
        return requestResponseList;
    }

    public void setRequestResponseList(int length) {
        for (int i = 0; i < length; i++) {
            addNewRequestResponseTab();
        }
    }

    public void showPayloads(List<String> payloads) {
        for (String payloadLine : payloads) {
            boolean isEnabled = true;
            String payload = payloadLine;

            String[] parts = payloadLine.split(",", 2);
            if (parts.length == 2 && ("true".equals(parts[0]) || "false".equals(parts[0]))) {
                isEnabled = Boolean.parseBoolean(parts[0]);
                payload = parts[1];
            }

            modelPayload.addRow(new Object[]{isEnabled, payload});
        }
    }

    public void showNewHeaders(List<String> newHeaders) {
        for (String header : newHeaders) {
            if (!header.isEmpty()) {
                modelNewHeaders.addRow(new Object[]{header});
            }
        }
    }

    public String getClipboardContents() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            return (String) clipboard.getData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException | IOException ex) {
            System.out.println(ex.getMessage());
            return "";
        }
    }

    public void addNewTag(String str) {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(filename.concat("tags.txt"), true))) {
            out.write(str + System.lineSeparator());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void removeTag(String tag) {
        String filePath = filename.concat("tags.txt");
        File inputFile = new File(filePath);
        File tempFile = new File(inputFile.getAbsolutePath() + ".tmp");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile)); PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().equals(tag)) {
                    writer.println(line);
                }
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

        if (!inputFile.delete()) {
            System.out.println("Could not delete file");
            return;
        }

        if (!tempFile.renameTo(inputFile)) {
            System.out.println("Could not rename file");
        }
    }

    public void showTags() {
        List<String> tags = readFile(filename.concat("tags.txt"));

        newTagCombo.removeAllItems();
        tagmanager.removeAllElements();
        for (String tagItem : tags) {
            newTagCombo.addItem(tagItem);
            tagmanager.addElement(tagItem);
        }
    }

    private List<String> readFile(String filename) {
        List<String> records = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                records.add(line);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return records;
    }

    class CloseableTabHeader extends JPanel {

        private JTabbedPane pane;
        private List<RequestResponse> requestResponseList;

        public CloseableTabHeader(JTabbedPane pane, String title, List<RequestResponse> requestResponseList) {
            this.pane = pane;
            this.requestResponseList = requestResponseList;
            setOpaque(false);
            setLayout(new BorderLayout());

            JPanel centerPanel = new JPanel();
            centerPanel.setOpaque(false);
            centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));

            JLabel titleLabel = new JLabel(title);
            centerPanel.add(titleLabel);
            centerPanel.add(Box.createHorizontalStrut(5));

            JButton closeButton = createCloseButton();
            centerPanel.add(closeButton);

            add(centerPanel, BorderLayout.CENTER);
        }

        private JButton createCloseButton() {
            JButton closeButton = new JButton();
            ImageIcon closeIcon = new ImageIcon(getClass().getResource("/close_icon.png"));
            closeButton.setIcon(closeIcon);
            closeButton.setPreferredSize(new Dimension(closeIcon.getIconWidth(), closeIcon.getIconHeight()));
            closeButton.setContentAreaFilled(false);
            closeButton.setBorderPainted(false);
            closeButton.setFocusable(false);

            closeButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    closeButton.setBorderPainted(true);
                    closeButton.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    closeButton.setBorderPainted(false);
                    closeButton.setBorder(null);
                }
            });

            closeButton.addActionListener(e -> {
                int index = pane.indexOfTabComponent(CloseableTabHeader.this);
                if (index != -1) {
                    requestResponseList.remove(index);
                    pane.remove(index);
                }
            });

            return closeButton;
        }
    }

    public List<RequestResponse> getRequestResponseClass() {
        return requestResponseList;
    }

    public void setRequestResponseClass(int length) {
        for (int i = 0; i < length; i++) {
            RequestResponse rr = new RequestResponse(filename);
            String tabTitle = "Step " + (jTabbedPane2.getTabCount());
            jTabbedPane2.insertTab(tabTitle, null, rr, null, jTabbedPane2.getTabCount() - 1);
            jTabbedPane2.setTabComponentAt(jTabbedPane2.getTabCount() - 2, new CloseableTabHeader(jTabbedPane2, tabTitle, requestResponseList));
            requestResponseList.add(rr);
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();
        buttonGroup5 = new javax.swing.ButtonGroup();
        buttonGroup6 = new javax.swing.ButtonGroup();
        buttonGroup7 = new javax.swing.ButtonGroup();
        buttonGroup8 = new javax.swing.ButtonGroup();
        textname = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        textauthor = new javax.swing.JTextField();
        headerstab = new javax.swing.JTabbedPane();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jPanel8 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        removetag = new javax.swing.JButton();
        addTag = new javax.swing.JButton();
        newTagCombo = new javax.swing.JComboBox<>();
        jScrollPane11 = new javax.swing.JScrollPane();
        listtag = new javax.swing.JList<>();
        jLabel46 = new javax.swing.JLabel();
        jLabel47 = new javax.swing.JLabel();
        newTagb = new javax.swing.JButton();
        jSeparator12 = new javax.swing.JSeparator();

        setPreferredSize(new java.awt.Dimension(831, 664));

        textname.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N

        jLabel18.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        jLabel18.setText("Author:");

        jLabel12.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        jLabel12.setText("Name:");

        textauthor.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N

        headerstab.setAutoscrolls(true);
        headerstab.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        headerstab.setPreferredSize(new java.awt.Dimension(750, 500));
        headerstab.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                headerstabStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 831, Short.MAX_VALUE)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 475, Short.MAX_VALUE)
        );

        jTabbedPane2.addTab("+", jPanel8);

        headerstab.addTab("Requests/Responses", jTabbedPane2);

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
            .addComponent(headerstab, javax.swing.GroupLayout.DEFAULT_SIZE, 831, Short.MAX_VALUE)
            .addComponent(jSeparator12)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(textname)
                .addGap(18, 18, 18)
                .addComponent(jLabel18)
                .addGap(18, 18, 18)
                .addComponent(textauthor, javax.swing.GroupLayout.PREFERRED_SIZE, 228, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12)
                    .addComponent(textauthor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18))
                .addGap(18, 18, 18)
                .addComponent(jSeparator12, javax.swing.GroupLayout.PREFERRED_SIZE, 5, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(headerstab, javax.swing.GroupLayout.DEFAULT_SIZE, 537, Short.MAX_VALUE))
        );

        headerstab.getAccessibleContext().setAccessibleName("Requests/Responses");
    }// </editor-fold>//GEN-END:initComponents

    private void removetag(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removetag
        int selectedIndex = listtag.getSelectedIndex();
        if (selectedIndex != -1) {
            tag.remove(selectedIndex);
        }
    }//GEN-LAST:event_removetag

    private void addTag(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addTag
        String selectedItem = (String) newTagCombo.getSelectedItem();
        if (selectedItem != null) {
            tag.addElement(selectedItem);
        }
    }//GEN-LAST:event_addTag

    private void newTagbnewTag(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newTagbnewTag
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
    }//GEN-LAST:event_newTagbnewTag

    private void headerstabStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_headerstabStateChanged

    }//GEN-LAST:event_headerstabStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addTag;
    public javax.swing.ButtonGroup buttonGroup1;
    public javax.swing.ButtonGroup buttonGroup2;
    public javax.swing.ButtonGroup buttonGroup3;
    public javax.swing.ButtonGroup buttonGroup4;
    public javax.swing.ButtonGroup buttonGroup5;
    public javax.swing.ButtonGroup buttonGroup6;
    private javax.swing.ButtonGroup buttonGroup7;
    private javax.swing.ButtonGroup buttonGroup8;
    public javax.swing.JTabbedPane headerstab;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JSeparator jSeparator12;
    private javax.swing.JTabbedPane jTabbedPane2;
    public javax.swing.JList<String> listtag;
    public javax.swing.JComboBox<String> newTagCombo;
    private javax.swing.JButton newTagb;
    private javax.swing.JButton removetag;
    public javax.swing.JTextField textauthor;
    public javax.swing.JTextField textname;
    // End of variables declaration//GEN-END:variables
}
