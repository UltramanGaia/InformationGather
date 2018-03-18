package burp.gather;

import burp.gather.utils.MyLogger;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;

public class PortScan {

    private static PortScan instance = new PortScan();
    private JTree portJTree = null;
    private DefaultTreeModel jTreeModel = null;

    private MyLogger myLogger = null;
    private PortScan(){}
    public static PortScan getInstance(){
        return instance;
    }
    public void setJTree(JTree jTree){
        portJTree = jTree;
        init();
    }
    private void init(){
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
        jTreeModel = new DefaultTreeModel(root);
        portJTree.setModel(jTreeModel);
        myLogger = MyLogger.getInstance();
    }
    public void addIP(String ip){
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) jTreeModel.getRoot();
        jTreeModel.insertNodeInto(new DefaultMutableTreeNode(ip), root, root.getChildCount());
        new NmapScan(ip).start();
    }

    class NmapScan extends Thread{
        private String ip = null;

        public NmapScan(String ip){
            this.ip = ip;
        }

        @Override
        public void run() {
            super.run();
//            String status = "DOWN";
            ArrayList<String> results = new ArrayList<>();
            try {
//                String teemoRootPath = new File(nmapPath).getParent();
//                Calendar calendar = Calendar.getInstance();
//                String resultFileName = targetDomain + "-"
//                        + calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.MONTH) + "-" + calendar.get(Calendar.DATE)
//                        + "-" + calendar.get(Calendar.HOUR_OF_DAY) + "-" + calendar.get(Calendar.MINUTE) + ".txt";  //baidu.com.18-2-16-13-30.txt
                System.out.println("Using nmap to scan ports ...");
                myLogger.logAddLine("Using nmap to scan ports ...");
                String[] args1 = new String[]{"nmap", "-T4", "-A", ip,"--open","-oG","-"};
                Process pr = Runtime.getRuntime().exec(args1);
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        pr.getInputStream()));
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String line;
                        try {
                            while ((line = in.readLine()) != null) {
//                                if (line.indexOf("Up") != -1) {
//                                    status = "UP";
//                                }
                                if (line.indexOf("Ports:") != -1) {
                                    int l = line.indexOf("Ports:") + 6;
                                    int r = line.indexOf("Seq Index") - 2;
                                    String content = line.substring(l, r);
                                    String[] temp = content.split(",");
                                    for (String s : temp
                                            ) {
                                        String t = s.trim();
                                        results.add(s.trim().replaceAll("/|\\ ", "."));
                                    }
                                }
                                System.out.println(line);
                                myLogger.logAddLine(line);
                            }
                        }
                        catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                }).start();

                //in.close();
                pr.waitFor();
            }catch (Exception e){
                e.printStackTrace();
            }
//            if(status.equals("DOWN"))
//                return;
            if(results.size()==0)
                return;

            // add results
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) jTreeModel.getRoot();
            Enumeration children = root.children();
            while(children.hasMoreElements()){
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) children.nextElement();
                if(node.toString().equals(ip)){
                    for (String s:results
                         ) {
                        node.add(new DefaultMutableTreeNode(s));
                        jTreeModel.nodesWereInserted(node, new int[]{node.getChildCount()-1});
                    }
//                    node.add(new DefaultMutableTreeNode("80->http"));
//                    jTreeModel.nodesWereInserted(node, new int[]{node.getChildCount()-1});
                    break;
                }
            }
        }
    }
}

