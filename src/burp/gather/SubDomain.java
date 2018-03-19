package burp.gather;

import burp.gather.utils.*;

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
    private Object[][] rowData = {
    };

    private static SubDomain instance = new SubDomain();
    private ArrayList<String> targetDomainList = new ArrayList<>();

    private MyLogger myLogger = null;
    private JTable subdomainTable = null;
    private String teemoPath = null;
    private PortScan portScan = null;
    private InfoLeak infoLeak = null;

    private SubDomain() {
        myLogger = MyLogger.getInstance();
        portScan = PortScan.getInstance();
        infoLeak = InfoLeak.getInstance();

    }

    public static SubDomain getInstance() {
        return instance;
    }

    public void setTeemoPath(String teemoPath) {
        this.teemoPath = teemoPath;
    }

    public void setJTable(JTable jTable) {
        subdomainTable = jTable;
        subdomainTable.addMouseListener(new SubdomainPopClickListener());
    }

    public void addTargetDomain(String targetDomain) {
        if (targetDomainList.contains(targetDomain)) {
            System.out.println("The target have been added before.");
            return;
        }
        targetDomainList.add(targetDomain);
        new TeemoScan(targetDomain).start();
        //teemoTargetDomain(targetDomain);
    }

    public static Object[] getColumnNames() {
        return columnNames;
    }

    public Object[][] getRowData() {
        return rowData;
    }

    private HashSet<String> subDomains = new HashSet<String>(); //subdomains

    class TeemoScan extends Thread {
        private String targetDomain = null;

        public TeemoScan(String targetDomain) {
            this.targetDomain = targetDomain;
        }

        @Override
        public void run() {
            super.run();

            try {
                String teemoRootPath = new File(teemoPath).getParent();
                Calendar calendar = Calendar.getInstance();
                String resultFileName = targetDomain + "-"
                        + calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DATE)
                        + "-" + calendar.get(Calendar.HOUR_OF_DAY) + "-" + calendar.get(Calendar.MINUTE) + ".txt";  //baidu.com.18-2-16-13-30.txt
                System.out.println("Using teemo.py to discover subdomains...");
                String[] args1 = new String[]{"python", teemoPath, "-b", "-d", targetDomain, "-o", resultFileName};
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

                // parse target domain log
                String logFileName = teemoRootPath + File.separator + "output" + File.separator + resultFileName;

                String logFileContent = Util.readToString(logFileName);
//                    System.out.println(logFileContent);
                Scanner scanner = new Scanner(logFileContent);
                while (scanner.hasNext()) {
                    String subdomain = scanner.next();
                    if (subdomain.indexOf('@') != -1) //result end
                        break;
                    System.out.println(subdomain);
                    subDomains.add(subdomain);

                }

                //model.setRowCount(0);
                int t_num = 20;
                ValidateScan[] threadPool = new ValidateScan[t_num];
                for (ValidateScan t : threadPool
                        ) {
                    t = new ValidateScan();
                    t.start();
                }
                for(ValidateScan t : threadPool){
                    t.join();
                }
                myLogger.logAddLine("Finish Validate scan..");

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    class ValidateScan extends Thread {
        @Override
        public void run() {
            super.run();
            DefaultTableModel model = (DefaultTableModel) subdomainTable.getModel();
            while (subDomains.size() != 0) {
                String subDomain = null;
                synchronized (subDomains) {
                    subDomain = subDomains.iterator().next();
                    subDomains.remove(subDomain);
                }
                System.out.println(subDomain);
                scan(model, subDomain);
            }
        }
    }

    public void scan(DefaultTableModel model, String subdomain) {

        // id , domain, status_code , title, ip, server
        String title = null, ip = null, server = null;
        Integer code = null;
        Request resquest = new Request("http://" + subdomain);
        resquest.getCon().setConnectTimeout(3000);
//        resquest.getCon().setReadTimeout(3000);
        System.out.println("Scanner--->  http://" + subdomain);
        resquest.setMethod("GET");
        resquest.setFollowRedirects(true);
        Response response = null;
        try {
            response = resquest.getResponse();
        } catch (IOException e) {
//            model.addRow(new Object[]{model.getRowCount() + 1, subdomain, "can't connect", "can't connect", "can't connect", "can't connect"});
            return;
        }
        code = response.getResponseCode();
        server = response.getHeader("Server");
        if (server == null || server.isEmpty()) {
            server = "no server header";
        }
        title = response.getTitle();
        if (title == null || title.isEmpty()) {
            title = "no title";
        }

        String[] temp = NetHelper.getIpAddress(subdomain);
        if (temp == null) {
            return;
        }
        if (temp.length == 1) {
            ip = temp[0];
        } else {
            ip = Util.arrToString(temp);
        }

        model.addRow(new Object[]{model.getRowCount() + 1, subdomain, code, title, ip, server});
    }


    public class SubdomainPopUp extends JPopupMenu {

        JMenuItem anItem;

        public SubdomainPopUp() {
            anItem = new JMenuItem("Send to Port Scan");
            anItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("click send port scan");
                    int[] selectedRows = subdomainTable.getSelectedRows();
                    for (int selectedRow : selectedRows
                            ) {
                        String content = (String) subdomainTable.getValueAt(selectedRow, 4);
                        System.out.println(content);


                        final List<String> list = new ArrayList<String>();
                        final Pattern pa = Pattern.compile("\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b", Pattern.CANON_EQ);
                        final Matcher ma = pa.matcher(content);
                        int num = ma.groupCount();
                        while (ma.find()) {
                            list.add(ma.group());
                        }
                        // if have multiple macher, there might has a cdn,do not use a port scan
                        if (list.size() == 1) {
                            portScan.addIP((list.get(0)));
                        }
//                        for (int i = 0; i < list.size(); i++) {
//                            portScan.addIP(list.get(i));
//                        }

                    }
                }
            });
            add(anItem);
            anItem = new JMenuItem("Send to Info Leck");
            anItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("click send info leck");
                    int[] selectedRows = subdomainTable.getSelectedRows();
                    for (int selectedRow : selectedRows
                            ) {
                        String content = (String) subdomainTable.getValueAt(selectedRow, 1);
                        System.out.println(content);
                        infoLeak.addTargetURL(content);
                    }
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
