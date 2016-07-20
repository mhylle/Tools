package com.mhylle;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.*;
import java.util.List;

public class FileCompare {

    private List<Result> result;
    private DefaultTableModel resultModel;


    public static void main(String[] args) {
        FileCompare folderCompare = new FileCompare();
        JFrame frame = folderCompare.setupUI();

        folderCompare.doLayout(frame);
        frame.setVisible(true);
    }

    private JFrame setupUI() {
        JFrame frame = new JFrame("Folder compare");

        frame.setSize(800, 600);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                System.exit(0);
            }
        });
        return frame;
    }

    private void doLayout(JFrame frame) {
        JPanel mainPanel = new JPanel(new BorderLayout());

        JLabel folder1Label = new JLabel("Folder1");
        JTextField folder1 = new JTextField();
        folder1.setPreferredSize(new Dimension(250, 24));
        JButton folder1Button = new JButton("Select folder");
        folder1Button.addActionListener(new MyActionListener(frame, folder1));

        JLabel folder2Label = new JLabel("Folder2");
        JTextField folder2 = new JTextField();
        folder1.setText("E:/projects/Praksys/Puppet/deploy/development");
        folder2.setText("E:/projects/Praksys/Puppet/deploy/tst");
        JButton folder2Button = new JButton("Select folder");
        folder2Button.addActionListener(new MyActionListener(frame, folder2));
        folder2.setPreferredSize(new Dimension(250, 24));

        JPanel folder1Panel = new JPanel(new BorderLayout());
        folder1Panel.add(folder1Label, BorderLayout.WEST);
        folder1Panel.add(folder1, BorderLayout.CENTER);
        folder1Panel.add(folder1Button, BorderLayout.EAST);

        JPanel folder2Panel = new JPanel(new BorderLayout());
        folder2Panel.add(folder2Label, BorderLayout.WEST);
        folder2Panel.add(folder2, BorderLayout.CENTER);
        folder2Panel.add(folder2Button, BorderLayout.EAST);
        GridLayout resultLayout = new GridLayout(2, 0);
        JPanel resultPanel = new JPanel(resultLayout);
        JLabel statusLabel = new JLabel();
//        resultPanel.add(statusLabel);
        resultModel = new MyAbstractTableModel();
        JTable resultTable = new JTable(resultModel);
        JScrollPane scrollPane = new JScrollPane(resultTable);
        JPanel actionPanel = new JPanel();
        JButton doSearch = new JButton("Compare");
        doSearch.addActionListener(new ComparisonAction(folder1, folder2));
        actionPanel.add(doSearch);
        resultPanel.add(scrollPane);
        JPanel selectionPanel = new JPanel(new GridLayout(2, 0));
        selectionPanel.add(folder1Panel);
        selectionPanel.add(folder2Panel);
        mainPanel.add(selectionPanel, BorderLayout.NORTH);
        JPanel resultPanels = new JPanel(new BorderLayout());
        resultPanels.add(actionPanel, BorderLayout.NORTH);
        resultPanels.add(resultPanel, BorderLayout.CENTER);
        mainPanel.add(resultPanels, BorderLayout.CENTER);

        frame.add(mainPanel, BorderLayout.NORTH);

    }

    private class MyActionListener implements ActionListener {
        private final JFrame frame;
        private final JTextField folder;

        MyActionListener(JFrame frame, JTextField folder) {
            this.frame = frame;
            this.folder = folder;
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File("."));
            chooser.setDialogTitle("Select folder");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);
            if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                File currentDirectory = chooser.getCurrentDirectory();
                folder.setText(currentDirectory.getAbsolutePath());
            } else {
                folder.setText("No Folder Selected");
            }

        }
    }

    private class Result {

        private final String f1;
        private final String f2;
        private final String compareValue;

        Result(String f1, String f2, String compareValue) {
            this.f1 = f1;
            this.f2 = f2;
            this.compareValue = compareValue;
        }

        public String getF1() {
            return f1;
        }

        public String getF2() {
            return f2;
        }

        public String getCompareValue() {
            return compareValue;
        }
    }

    private class ComparisonAction implements ActionListener {
        private JTextField folder1;
        private JTextField folder2;


        ComparisonAction(JTextField folder1, JTextField folder2) {
            this.folder1 = folder1;
            this.folder2 = folder2;
        }

        public void actionPerformed(ActionEvent e) {
            String folder1Text = folder1.getText();
            String folder2Text = folder2.getText();

            try {
                result = new ArrayList<>();
                getDiff(new File(folder1Text), new File(folder2Text));

            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void getDiff(File dirA, File dirB) throws IOException {

        File[] fileList1 = dirA.listFiles();
        File[] fileList2 = dirB.listFiles();
        assert fileList1 != null;
        Arrays.sort(fileList1);
        assert fileList2 != null;
        Arrays.sort(fileList2);
        HashMap<String, File> map1;
        if (fileList1.length < fileList2.length) {
            map1 = new HashMap<>();
            for (File aFileList1 : fileList1) {
                map1.put(aFileList1.getName(), aFileList1);
            }

            compareNow(fileList2, map1);
        } else {
            map1 = new HashMap<>();
            for (File aFileList2 : fileList2) {
                map1.put(aFileList2.getName(), aFileList2);
            }
            compareNow(fileList1, map1);
        }
    }

    private void compareNow(File[] fileArr, HashMap<String, File> map) throws IOException {

        for (File aFileArr : fileArr) {
            String fName = aFileArr.getName();
            File fComp = map.get(fName);
            map.remove(fName);
            if (fComp != null) {
                if (fComp.isDirectory()) {
                    getDiff(aFileArr, fComp);
                } else {
                    String cSum1 = checksum(aFileArr);
                    String cSum2 = checksum(fComp);
                    assert cSum1 != null;
                    if (!cSum1.equals(cSum2)) {
                        result.add(new Result(aFileArr.getAbsolutePath(), fComp.getAbsolutePath(), "Different"));

                        Result res = result.get(result.size() - 1);
                        System.out.println(res.getF1() + " and " + res.getF2() + " " + res.getCompareValue());

                    }
                }
            } else {
                compareDirs(aFileArr);
            }
        }
        Set<String> set = map.keySet();
        for (String n : set) {
            File fileFrmMap = map.get(n);
            map.remove(n);
            compareDirs(fileFrmMap);
        }
        if (result.size() == 0) {
            System.out.println("Content was identical");
        }
        resultModel.fireTableDataChanged();
    }

    private void compareDirs(File fileFrmMap) {
        if (fileFrmMap.isDirectory()) {
            traverseDirectory(fileFrmMap);
        } else {
            result.add(new Result(fileFrmMap.getName(), "", "only in " + fileFrmMap.getParent()));
            Result res = result.get(result.size() - 1);
            System.out.println(res.getF1() + " and " + res.getF2() + " " + res.getCompareValue());
        }
    }

    private void traverseDirectory(File dir) {
        File[] list = dir.listFiles();
        assert list != null;
        for (File aList : list) {
            if (aList.isDirectory()) {
                traverseDirectory(aList);
            } else {
                result.add(new Result(aList.getName(), "", "only in " + aList.getParent()));
                System.out.println(aList.getName() + "\t\t" + "only in " + aList.getParent());
            }
        }
    }

    private String checksum(File file) {
        try {
            InputStream fin = new FileInputStream(file);
            java.security.MessageDigest md5er = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[1024];
            int read;
            do {
                read = fin.read(buffer);
                if (read > 0)
                    md5er.update(buffer, 0, read);
            } while (read != -1);
            fin.close();
            byte[] digest = md5er.digest();
            if (digest == null)
                return null;
            String strDigest = "0x";
            for (byte aDigest : digest) {
                strDigest += Integer.toString((aDigest & 0xff) + 0x100, 16).substring(1).toUpperCase();
            }
            return strDigest;
        } catch (Exception e) {
            return null;
        }
    }

    private class MyAbstractTableModel extends DefaultTableModel {

        @Override
        public int getRowCount() {
            if (result != null) {
                return result.size();
            }
            return 0;
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return result.get(rowIndex).getF1();
                case 1:
                    return result.get(rowIndex).getF2();
                case 2:
                    return result.get(rowIndex).getCompareValue();
            }
            System.out.println("RETURNING NULL");
            return null;
        }
    }
}

