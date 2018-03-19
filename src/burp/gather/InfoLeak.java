package burp.gather;

import burp.gather.utils.MyLogger;
import burp.gather.utils.Request;
import burp.gather.utils.Response;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.IOException;
import java.util.ArrayList;

public class InfoLeak {

    private static final Object[] columnNames = {"Id", "URL", ".git", ".svn", ".cvs","WEB-INF", "DS_Store",".hg"};

    private Object[][] rowData = {
    };

    private static InfoLeak instance = new InfoLeak();
    private ArrayList<String> targetURLList = new ArrayList<>();

    private MyLogger myLogger = null;
    private JTable infoLeakTable = null;

    private InfoLeak() {
        myLogger = MyLogger.getInstance();
    }

    public static InfoLeak getInstance() {
        return instance;
    }

    public void setJTable(JTable jTable) {
        infoLeakTable = jTable;
//        infoLeakTable.addMouseListener(new SubdomainPopClickListener());
    }

    public void addTargetURL(String targetUrl) {
        myLogger.logAddLine("add a target url " + targetUrl);
        if (targetURLList.contains(targetUrl)) {
            System.out.println("The target URL have been added before.");
            return;
        }
        targetURLList.add(targetUrl);
        new InfoLeakScan(targetUrl).start();
        //teemoTargetDomain(targetDomain);
    }

    public static Object[] getColumnNames() {
        return columnNames;
    }

    public Object[][] getRowData() {
        return rowData;
    }

    class InfoLeakScan extends Thread {
        private String targetURL = null;

        public InfoLeakScan(String targetURL) {
            this.targetURL = targetURL;
        }

        @Override
        public void run() {
            super.run();

            DefaultTableModel model = (DefaultTableModel) infoLeakTable.getModel();

            try {
                scan(model, targetURL);
                myLogger.logAddLine("Finish InfoLeck scan..");

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    private String _RequestGETStatus(String url){
        System.out.println(url);
        Request request = new Request( url );
        request.getCon().setConnectTimeout(3000);
        request.getCon().setReadTimeout(3000);
        request.setMethod("GET");
        request.setFollowRedirects(true);
        Response response = null;
        try {
            response = request.getResponse();
            return "" + response.getResponseCode();
        } catch (IOException e) {
//            model.addRow(new Object[]{model.getRowCount() + 1, subdomain, "can't connect", "can't connect", "can't connect", "can't connect"});
            return "e";
        }

    }

    public void scan(DefaultTableModel model, String domain) {

        String url = "http://" + domain;

        String git = _RequestGETStatus(url+"/.git/index");
        String svn = _RequestGETStatus(url+"/.svn/entries");
        String cvs = _RequestGETStatus(url+"/CVS/");
        String web_inf = _RequestGETStatus(url+"/WEB-INF/web.xml");
        String dr_store = _RequestGETStatus(url+"/.DS_Store");
        String hg = _RequestGETStatus(url+"/.hg/");
        System.out.println(url + " -> git: " + git + " svn: "+ svn + " cvs: "+ cvs+" web_inf: "+web_inf+" dr_store: " + dr_store + " hg: "+ hg );
        myLogger.logAddLine(url + " -> git: " + git + " svn: "+ svn + " cvs: "+ cvs+" web_inf: "+web_inf+" dr_store: " + dr_store + " hg: "+ hg);
        if(!(git.equals(svn)  && git.equals(cvs) && git.equals(web_inf) && git.equals(dr_store) && git.equals(hg)   )) {
            model.addRow(new Object[]{model.getRowCount() + 1, url, git, svn, cvs, web_inf, dr_store, hg});
        }

        url = "https://" + domain;
        git = _RequestGETStatus(url+"/.git/index");
        svn = _RequestGETStatus(url+"/.svn/entries");
        cvs = _RequestGETStatus(url+"/CVS/");
        web_inf = _RequestGETStatus(url+"/WEB-INF/web.xml");
        dr_store = _RequestGETStatus(url+"/.DS_Store");
        hg = _RequestGETStatus(url+"/.hg/");
        System.out.println(url + " -> git: " + git + " svn: "+ svn + " cvs: "+ cvs+" web_inf: "+web_inf+" dr_store: " + dr_store + " hg: "+ hg );
        myLogger.logAddLine(url + " -> git: " + git + " svn: "+ svn + " cvs: "+ cvs+" web_inf: "+web_inf+" dr_store: " + dr_store + " hg: "+ hg);
        if(!(git.equals(svn)  && git.equals(cvs) && git.equals(web_inf) && git.equals(dr_store) && git.equals(hg)   )) {
            model.addRow(new Object[]{model.getRowCount() + 1, url, git, svn, cvs, web_inf, dr_store, hg});
        }

    }



}
