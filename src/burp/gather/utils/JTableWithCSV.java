package burp.gather.utils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Vector;

public class JTableWithCSV {

    /**
     * Rudimentary quick and dirty demo code
     *
     * @param args (Not used)
     */
    public static void main(String[] args) {
        try {
            // Read a csv file called 'data.txt' and save it to a more
            // correctly named 'data.csv'
            DefaultTableModel m = CSVToJTable("data.txt", null);
            JFrame f = new JFrame();
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.getContentPane().add(new JScrollPane(new JTable(m)));
            f.setSize(200, 300);
            f.setVisible(true);

            JTableToCSV(m, "data.csv");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @param dtm         The DefaultTableModel to save to stream
     * @param outFileName The output file name
     */
    public static void JTableToCSV(DefaultTableModel dtm,
                                   String outFileName) throws IOException {
        FileWriter out = new FileWriter(outFileName);

        final String LINE_SEP = System.getProperty("line.separator");
        int numCols = dtm.getColumnCount();
        int numRows = dtm.getRowCount();

        // Write headers
        String sep = "";

        for (int i = 0; i < numCols; i++) {
            out.write(sep);
            out.write(dtm.getColumnName(i));
            sep = ",,,";
        }

        out.write(LINE_SEP);

        for (int r = 0; r < numRows; r++) {
            sep = "";

            for (int c = 0; c < numCols; c++) {
                out.write(sep);
                out.write(dtm.getValueAt(r, c).toString());
                sep = ",,,";
            }

            out.write(LINE_SEP);
        }
        out.close();
    }


    /**
     * @param inFileName A CSV  file name
     * @param headers    A Vector containing the column headers. If this is null, it's assumed
     *                   that the first row contains column headers
     * @return A DefaultTableModel containing the CSV values as type String
     */
    public static DefaultTableModel CSVToJTable(String inFileName,
                                                Vector<Object> headers) throws IOException {
        DefaultTableModel model = null;
        Scanner s = null;
        FileReader in = new FileReader(inFileName);

        try {
            Vector<Vector<Object>> rows = new Vector<Vector<Object>>();
            s = new Scanner(in);

            while (s.hasNextLine()) {
                rows.add(new Vector<Object>(Arrays.asList(s.nextLine()
                        .split("\\s*,,,\\s*",
                                -1))));
            }

            if (headers == null) {
                headers = rows.remove(0);
                model = new DefaultTableModel(rows, headers);
            } else {
                model = new DefaultTableModel(rows, headers);
            }

            return model;
        } finally {
            s.close();
        }
    }


}

