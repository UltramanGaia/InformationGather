package burp.gather;

import burp.gather.utils.NetHelper;
import burp.gather.utils.Request;
import burp.gather.utils.Response;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Scanner;

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

    public void querySubDomain(DefaultTableModel model, JLabel noticeField, String teemoPath,String targetDomain){
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    String teemoRootPath = new File(teemoPath).getParent();
                    Calendar calendar = Calendar.getInstance();
                    //String t = "-%d-%d-%d-%d-%d"%(, , ,, );
                    String resultFileName = targetDomain + "-"
                            + calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.MONTH) + "-" + calendar.get(Calendar.DATE)
                            + "-" + calendar.get(Calendar.HOUR_OF_DAY) + "-"+ calendar.get(Calendar.MINUTE) + "-" +".txt";  //baidu.com.12345679.txt
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
                                        System.out.println(subDomain);
                                        subDomains.remove(subDomain);
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
                        t.join();
                    }


//                    for (int i = 0; i < jsonSubDomainArray.length(); i++) {
//                        String subdomain = jsonSubDomainArray.getString(i);
//                        System.out.println(subdomain);
//                        scan(model, subdomain);
//                    }

                    noticeField.setText("Done...All done...");
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
