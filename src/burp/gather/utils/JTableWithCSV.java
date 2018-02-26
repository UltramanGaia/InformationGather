package burp.gather.utils;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class JTableExportCommand {
    private JTable table = null;
    private Component parentComp = null;

    public JTableExportCommand(JTable table, Component parentComp) {
        this.table = table;
        this.parentComp = parentComp;
    }

    public boolean execute() {
        if (table == null) {
            return false;
        }
        File file = showSaveDialog();
        if (file != null) {
            if (file.exists()) {
                if (JOptionPane.showConfirmDialog(table, "The file already exists, do you want to replace it?") != JOptionPane.YES_OPTION) {
                    return false;
                }
            }

            return CSVFileWriter.writeTableModel(table, file);

        }
        return false;
    }

    private File showSaveDialog() {
        JFileChooser chooser = new JFileChooser();
        chooser.removeChoosableFileFilter(chooser.getAcceptAllFileFilter());
        chooser.addChoosableFileFilter(new CSVFileFilter());
        //chooser.addChoosableFileFilter(new XMLFileFilter());
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int ret = chooser.showSaveDialog(parentComp);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            FileFilter filter = chooser.getFileFilter();

            String extension = getExtension(f);
            if (extension == null || !extension.equalsIgnoreCase(((CSVFileFilter) filter).getExtension())) {
                return new File(f.getAbsolutePath() + "." + ((CSVFileFilter) filter).getExtension());
            }
            return f;
        }

        return null;
    }

    static class CSVFileFilter extends FileFilter {

        public boolean accept(File f) {
            if (f != null) {
                if (f.isDirectory()) {
                    return true;
                }
                if (getExtension().equalsIgnoreCase(JTableExportCommand.getExtension(f))) {
                    return true;
                }
            }
            return false;
        }

        public String getDescription() {
            return "csv format";
        }

        public String getExtension() {
            return "csv";
        }
    }

    static class CSVFileWriter {
        public static boolean writeTableModel(JTable fTable, File file) {

            if (fTable == null) {
                return false;
            }

            TableModel tableModel = fTable.getModel();
            StringBuffer fileBuf = new StringBuffer("");
            int rowCount = tableModel.getRowCount();
            int columnCount = tableModel.getColumnCount();
            for (int col = 0; col < columnCount; col++) {
                fileBuf.append(tableModel.getColumnName(col));
                fileBuf.append(",");
            }
            fileBuf.append("\n");
            for (int row = 0; row < rowCount; row++) {
                for (int col = 0; col < columnCount; col++) {
                    fileBuf.append(tableModel.getValueAt(row, col).toString());
                    if (col != columnCount - 1) {
                        fileBuf.append(",");
                    }
                }
                fileBuf.append("\n");
            }
            try {

                FileWriter writer = new FileWriter(file);
                writer.write(fileBuf.toString());
                writer.close();
                return true;
            } catch (IOException e) {
                e.printStackTrace(System.err);
                return false;
            }
        }
    }



}