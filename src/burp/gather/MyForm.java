package burp.gather;

import burp.IContextMenuInvocation;
import burp.gather.utils.MyLogger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;


public class MyForm {

    private IContextMenuInvocation invocation = null; //BurpContext
    SubDomain subDomain = null;

    private JPanel rootPanel;
    private JTabbedPane tabbedPane;
    private JTextField portsTextField;
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


    public MyForm() {
        init();
    }

    public MyForm(IContextMenuInvocation invocation) {
        this();
        this.invocation = invocation;
        subDomainTextField.setText(invocation.getSelectedMessages()[0].getHttpService().getHost());
    }

    public void init() {

        subDomain = new SubDomain();
        subDomainTable.setModel(new DefaultTableModel(subDomain.getRowData(), SubDomain.getColumnNames()));

        startButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                startSubDomainQuery(e);
            }
        });

        teemoPathButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                String path = browserFiles();
                if(path != null){
                    teemoPathTextField.setText(path);
                }
            }
        });

        MyLogger.getInstance().setLogArea(logTextArea);

    }

    public String browserFiles(){
        JFileChooser jf = new JFileChooser();
        jf.showOpenDialog(null);//显示打开的文件对话框
        File f =  jf.getSelectedFile();//使用文件类获取选择器选择的文件
        String s = f.getAbsolutePath();//返回路径名
        //JOptionPane弹出对话框类，显示绝对路径名
        //JOptionPane.showMessageDialog(null, s, "标题",JOptionPane.WARNING_MESSAGE);
        return s;
    }

    private void startSubDomainQuery(MouseEvent e) {
        //clear all data in table
        DefaultTableModel model = (DefaultTableModel) subDomainTable.getModel();

        //subDomainTextField.setText("gbboys");
        System.out.println(subDomainTextField.getText());
        String targetDomain = subDomainTextField.getText();
        subDomain.querySubDomain(model,noticeField, teemoPathTextField.getText(),targetDomain);

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
