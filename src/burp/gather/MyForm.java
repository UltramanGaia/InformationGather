package burp.gather;

import burp.IContextMenuInvocation;
import burp.gather.utils.JTableWithCSV;
import burp.gather.utils.MyLogger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MyForm {

    private IContextMenuInvocation invocation = null; //BurpContext
    SubDomain subDomain = null;

    private JPanel rootPanel;
    private JTabbedPane tabbedPane;
    private JTextField ipTextField;
    private JPasswordField passwordPasswordField;
    private JTextField subDomainTextField;
    private JButton startButton;
    private JTable subDomainTable;
    private JLabel noticeField;
    private JTextField teemoPathTextField;
    private JButton teemoPathButton;
    private JTextField textField3;
    private JButton browseButton2;
    private JTextField textField4;
    private JButton browseButton3;
    private JLabel teemoPathLabel;
    private JTextArea logTextArea;
    private JButton exportButton;
    private JButton importButton;
    private JTree portScanJTree;
    private JButton addButton;
    private MyLogger myLogger;
    private PortScan portScan;

    public MyForm() {
        init();
    }

    public MyForm(IContextMenuInvocation invocation) {
        this();
        this.invocation = invocation;
        subDomainTextField.setText(invocation.getSelectedMessages()[0].getHttpService().getHost());
    }

    public void init() {

        subDomain = new SubDomain(subDomainTable);
        subDomainTable.setModel(new DefaultTableModel(subDomain.getRowData(), SubDomain.getColumnNames()));

        startButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                startSubDomainQuery(e);
            }
        });

        importButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                importSubdomains();
                JOptionPane.showMessageDialog(null, "import csv to JTable successfully");
            }
        });

        exportButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                exportSubdomains();
                JOptionPane.showMessageDialog(null, "export JTable to csv successfully");
            }
        });

        teemoPathButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                String path = browserFiles();
                if (path != null) {
                    teemoPathTextField.setText(path);
                }
            }
        });

        addButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                String ips = ipTextField.getText();
                final List<String> list = new ArrayList<String>();
                final Pattern pa = Pattern.compile("\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b", Pattern.CANON_EQ);
                final Matcher ma = pa.matcher(ips);
                while (ma.find()) {
                    list.add(ma.group());
                }
                for (int i = 0; i < list.size(); i++) {
                    portScan.addIP(list.get(i));
                }
            }
        });

        portScan = PortScan.getInstance();
        portScan.setJTree(portScanJTree);

        myLogger = MyLogger.getInstance();
        myLogger.setLogArea(logTextArea);
    }

    public String browserFiles() {
        JFileChooser jf = new JFileChooser();
        jf.showOpenDialog(null);//显示打开的文件对话框
        File f = jf.getSelectedFile();//使用文件类获取选择器选择的文件
        String s = f.getAbsolutePath();//返回路径名
        //JOptionPane弹出对话框类，显示绝对路径名
        //JOptionPane.showMessageDialog(null, s, "标题",JOptionPane.WARNING_MESSAGE);
        return s;
    }

    private void startSubDomainQuery(MouseEvent e) {
        DefaultTableModel model = (DefaultTableModel) subDomainTable.getModel();

        //subDomainTextField.setText("gbboys");
        System.out.println(subDomainTextField.getText());
        String targetDomain = subDomainTextField.getText();
        subDomain.querySubDomain(model, noticeField, teemoPathTextField.getText(), targetDomain);

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                for(int i = 0; i < 10; ++i ){
//                    //subDomainTable.
//                    model.addRow(new Object[]{model.getRowCount() + 1,"hacker.com","10.0.0.1","404","apache","ne cdn"});
//                    try {
//                        Thread.sleep(1000);
//                    }catch (InterruptedException e){
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }).start();
    }

    private void importSubdomains() {
        String path = browserFiles();
        try {
            DefaultTableModel model = JTableWithCSV.CSVToJTable(path, null);
            subDomainTable.setModel(model);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void exportSubdomains() {
        String path = browserFiles();
        DefaultTableModel model = (DefaultTableModel) subDomainTable.getModel();
        try {
            JTableWithCSV.JTableToCSV(model, path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

    //DISPOSE_ON_CLOSE
    public static void main(String[] args) {
        JFrame frame = new JFrame("Information Gather");
        frame.setContentPane(new MyForm().getRootPanel());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        //subDomain = new SubDomain();
        //subDomainTable = new JTable(subDomain.getRowData(),SubDomain.getColumnNames());
    }
}
