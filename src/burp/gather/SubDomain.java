package burp.gather;

import burp.gather.utils.NetHelper;
import burp.gather.utils.Request;
import burp.gather.utils.Response;
import org.json.JSONArray;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.util.HashSet;

public class SubDomain {
    private static final Object[] columnNames = {"Id", "Domain", "Ip", "Status", "Server", "CDN"};
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

    public static Object[] getColumnNames() {
        return columnNames;
    }

    public Object[][] getRowData() {
        return rowData;
    }

    public void querySubDomain(DefaultTableModel model, JLabel noticeField, String wydomainPath, String wydicPath ,String targetDomain){
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    String wyPath = new File(wydomainPath).getParent();

                    //using dnsburte.py
                    noticeField.setText("using dnsburte.py to discover subdomains...");
                    noticeField.paintImmediately(noticeField.getBounds());
                    String[] args1 = new String[]{"python", wyPath+File.separator+"dnsburte.py", "-d", targetDomain, "-f", wydicPath};
                    Process pr = Runtime.getRuntime().exec(args1);
//            BufferedReader in = new BufferedReader(new InputStreamReader(
//                    pr.getInputStream()));
//            String line;
//            while ((line = in.readLine()) != null) {
//                System.out.println(line);
//            }
//            in.close();
                    pr.waitFor();

                    //using wydomain.py
                    noticeField.setText("using wydomain.py to discover subdomains...");
                    noticeField.paintImmediately(noticeField.getBounds());
                    String[] args2 = new String[]{"python", wydomainPath, "-d", targetDomain, "-o", targetDomain + ".log"};
                    Process pr2 = Runtime.getRuntime().exec(args2);

//            BufferedReader in2 = new BufferedReader(new InputStreamReader(
//                    pr2.getInputStream()));
//            String line2;
//            while ((line2 = in2.readLine()) != null) {
//                System.out.println(line2);
//            }
//            in2.close();
                    pr2.waitFor();

                    noticeField.setText("done...parsing subdomains...");
                    noticeField.paintImmediately(noticeField.getBounds());

                    // parse target domain log

                    String logFileName = wyPath + File.separator + targetDomain + ".log";
                    String logFileContent = readToString(logFileName);
//                    System.out.println(logFileContent);

                    model.setRowCount(0);
                    JSONArray jsonSubDomainArray = new JSONArray(logFileContent);

                    HashSet<String> dic = new HashSet<String>(); //读取字典列表
                    for(int i = 0; i < jsonSubDomainArray.length(); i++){
                        dic.add(jsonSubDomainArray.getString(i));
                    }
                    // burte dic by myself
//                    String content = readToString(wydicPath);
//                    Scanner scanner = new Scanner(content);
//                    while(scanner.hasNext()){
//                        String head = scanner.next();
//                        System.out.println(head);
//                        dic.add(head+"."+targetDomain);
//                    }

                    int t_num = 20;
                    Thread [] threadPool = new Thread[t_num];

                    for(int i = 0; i < t_num; i++){
                        threadPool[i] = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while(dic.size() != 0){
                                    String subDomain = null;
                                    synchronized (dic){
                                        subDomain = dic.iterator().next();
                                        System.out.println(subDomain);
                                        dic.remove(subDomain);
                                    }
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
                        t.wait();
                    }


//                    for (int i = 0; i < jsonSubDomainArray.length(); i++) {
//                        String subdomain = jsonSubDomainArray.getString(i);
//                        System.out.println(subdomain);
//                        scan(model, subdomain);
//                    }

                    noticeField.setText("done...All done...");
                    noticeField.paintImmediately(noticeField.getBounds());

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();

    }


    public void scan(DefaultTableModel model, String subdomain) {

        // domain ip cdn server
        String domain = null, ip = null, cdn = null, server = null;
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
        String[] temp = NetHelper.getIpAddress(subdomain);
        if (temp == null) {
            return;
        }
        if (temp.length == 1) {
            ip = temp[0];
            cdn = "NO CDN";
        } else {
            ip = "";
            cdn = arrToString(temp);
        }
        model.addRow(new Object[]{model.getRowCount() + 1, subdomain, ip, code, server, cdn});
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


}
