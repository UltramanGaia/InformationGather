package burp.gather;

import burp.gather.utils.MyLogger;
import burp.gather.utils.NetHelper;
import burp.gather.utils.Request;
import burp.gather.utils.Response;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubDomain {
    private static final Object[] columnNames = {"Id", "Domain", "Status", "Title", "IP", "Server"};
    private   Object[][] rowData = {
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},

    };

    private MyLogger myLogger = null;
    private JTable subdomainTable = null;
    private PortScan portScan = null;

    public SubDomain(JTable jTable){
        myLogger = MyLogger.getInstance();
        portScan = PortScan.getInstance();
        subdomainTable = jTable;
        subdomainTable.addMouseListener(new SubdomainPopClickListener());

    }


    public static Object[] getColumnNames() {
        return columnNames;
    }

    public Object[][] getRowData() {
        return rowData;
    }

    public void querySubDomain(DefaultTableModel model, JLabel noticeField, String teemoPath,String targetDomain){
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    String teemoRootPath = new File(teemoPath).getParent();
                    Calendar calendar = Calendar.getInstance();
                    String resultFileName = targetDomain + "-"
                            + calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.MONTH) + "-" + calendar.get(Calendar.DATE)
                            + "-" + calendar.get(Calendar.HOUR_OF_DAY) + "-"+ calendar.get(Calendar.MINUTE) +".txt";  //baidu.com.18-2-16-13-30.txt
                    System.out.println("Using teemo.py to discover subdomains...");
                    noticeField.setText("Using teemo.py to discover subdomains...");
                    noticeField.paintImmediately(noticeField.getBounds());
                    String[] args1 = new String[]{"python", teemoPath, "-b","-d", targetDomain, "-o", resultFileName};
                    Process pr = Runtime.getRuntime().exec(args1);
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            pr.getInputStream()));
                    String line;
                    while ((line = in.readLine()) != null) {
                        System.out.println(line);
                        myLogger.logAddLine(line);
                    }
                    in.close();
                    pr.waitFor();
                    System.out.println("Teemo run complete");
                    noticeField.setText("Teemo run complete...Try scanning the subdomains.....");




                    // parse target domain log
                    HashSet<String> subDomains = new HashSet<String>(); //subdomains
                    String logFileName = teemoRootPath + File.separator + "output" + File.separator +  resultFileName;
                    String logFileContent = readToString(logFileName);
//                    System.out.println(logFileContent);
                    Scanner scanner = new Scanner(logFileContent);

                    while(scanner.hasNext()){
                        String subdomain = scanner.next();
                        if(subdomain.indexOf('@') != -1) //result end
                            break;
                        System.out.println(subdomain);
                        subDomains.add(subdomain);
                    }
                    model.setRowCount(0);

                    int t_num = 20;
                    Thread [] threadPool = new Thread[t_num];

                    for(int i = 0; i < t_num; i++){
                        threadPool[i] = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while(subDomains.size() != 0){
                                    String subDomain = null;
                                    synchronized (subDomains){
                                        subDomain = subDomains.iterator().next();
                                        subDomains.remove(subDomain);
                                    }
                                    System.out.println(subDomain);
                                    scan(model,subDomain);
                                }
                            }
                        });
                    }

                    for (Thread t:threadPool
                         ) {
                        t.start();
                    }
                    for (Thread t: threadPool
                         ) {
                        t.join();
                    }

                    noticeField.setText("Done...All done...");
                    noticeField.paintImmediately(noticeField.getBounds());

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();

    }


    public void scan(DefaultTableModel model, String subdomain) {

        // id , domain, status_code , title, ip, server
        String  title = null, ip = null, server = null;
        Integer code = null;
        Request resquest = new Request("http://" + subdomain);
        resquest.getCon().setReadTimeout(1000);
        System.out.println("Scanner--->  http://" + subdomain);
        resquest.setMethod("GET");
        resquest.setFollowRedirects(true);
        Response response = null;
        try {
            response = resquest.getResponse();
        } catch (IOException e) {
            return;
        }
        code = response.getResponseCode();
        server = response.getHeader("Server");
        if(server == null || server.isEmpty()){
            server = "no server header";
        }
        title = response.getTitle();
        if(title == null || title.isEmpty()){
            title = "no title";
        }

        String[] temp = NetHelper.getIpAddress(subdomain);
        if (temp == null) {
            return;
        }
        if (temp.length == 1) {
            ip = temp[0];
        } else {
            ip = arrToString(temp);
        }

        model.addRow(new Object[]{model.getRowCount() + 1, subdomain,code, title, ip, server});
    }

    public static String arrToString(String[] str) {
        String temp = "";
        for (int i = 0; i < str.length; i++) {
            temp += str[i] + "  |  ";
        }
        return temp.substring(0, temp.length() - 5);
    }

    public String readToString(String fileName) {
        String encoding = "UTF-8";
        File file = new File(fileName);
        Long filelength = file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return new String(filecontent, encoding);
        } catch (UnsupportedEncodingException e) {
            System.err.println("The OS does not support " + encoding);
            e.printStackTrace();
            return null;
        }
    }


    public class SubdomainPopUp extends JPopupMenu {

        JMenuItem anItem;
        public SubdomainPopUp() {
            anItem = new JMenuItem("Send to Port Scan");
            anItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("click send port scan");
                    int [] selectedRows = subdomainTable.getSelectedRows();
                    for (int selectedRow:selectedRows
                         ) {
                        String content = (String)subdomainTable.getValueAt(selectedRow,4);
                        System.out.println(content);

                        final List<String> list = new ArrayList<String>();
                        final Pattern pa = Pattern.compile("\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b", Pattern.CANON_EQ);
                        final Matcher ma = pa.matcher(content);
                        int num = ma.groupCount();
                        while (ma.find()) {
                            list.add(ma.group());
                        }
                        // if have multiple macher, there might has a cdn,do not use a port scan
                        if(list.size() == 1){
                            portScan.addIP((list.get(0)));
                        }
//                        for (int i = 0; i < list.size(); i++) {
//                            portScan.addIP(list.get(i));
//                        }

                    }
                }
            });
            add(anItem);
            anItem = new JMenuItem("Send to Dir Scan");
            anItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("click send dirs scan");
                }
            });
            add(anItem);
        }
    }

    public class SubdomainPopClickListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger())
                doPop(e);
        }

        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger())
                doPop(e);
        }

        private void doPop(MouseEvent e) {
            SubdomainPopUp menu = new SubdomainPopUp();
            menu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

}
