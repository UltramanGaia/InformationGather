package burp.gather;

import burp.*;
import burp.gather.utils.Request;
import burp.gather.utils.Response;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class CheckLeakFile{
    private static ArrayList<String> testedURL = new ArrayList<>();
    final static String [] fileFilesStart = {"","","."};
    final static String [] fileFilesEnd = {".bak","~",".swp"};
//    final static String [] dirFilesStart = {"","","","","","","",".7z",".tar.gz",".rar"};
    final static String [] dirFilesEnd = {"/.git/index","/.svn/entries","/CVS/ROOT","/WEB-INF/web.xml","/.DS_Store","/.hg/",".zip",".7z",".tar.gz",".rar"};

    String urlStart = null; // http://www.baidu.com
    String urlPath = null; // /web/image/test.jsp

    int errorFlagLength = 0;

    CheckLeakFile(String url, String path){
        urlStart = url;
        urlPath = path;
        errorFlagLength = _getErrorLength(url+"/feagiEgadhi34.php");
    }

    public ArrayList<String> check(){
        String [] dirs = urlPath.split("/");
        ArrayList<String > result = new ArrayList<>();
        for (String dir:dirs
             ) {
            if(!testedURL.contains(urlStart+"/"+dir)) {
                ArrayList<String> tempResult = new ArrayList<>();

                if (dir.endsWith(".php") || dir.endsWith(".asp") || dir.endsWith(".aspx") || dir.endsWith(".jsp")) {//文件
                    for (int i = 0; i < fileFilesStart.length; i++) {
                        String temp = urlStart + "/" + fileFilesStart[i] + dir + fileFilesEnd[i];
                        String status = _RequestGETStatus(temp);
                        if (!status.equals("e")) {
                            tempResult.add(status + "  -->  " + temp);
                        }
                    }

                } else {//目录
                    for (int i = 0; i < dirFilesEnd.length; i++) {
                        String temp = urlStart + "/" + dir + dirFilesEnd[i];

                        String status = _RequestGETStatus(temp);
                        if (!status.equals("e")) {
                            tempResult.add(status + "  -->  " + temp);
                        }
                    }
                }
                testedURL.add(urlStart+"/"+dir);
            }
            urlStart = urlStart + "/" + dir;

        }

        return result;
    }
    private int _getErrorLength(String url){
        Request request = new Request(url);
        request.getCon().setConnectTimeout(3000);
        request.getCon().setReadTimeout(3000);
        request.setMethod("GET");
        request.setFollowRedirects(true);
        Response response = null;
        try {
            response = request.getResponse();
            return response.getBody().length();
        } catch (IOException e) {
//            model.addRow(new Object[]{model.getRowCount() + 1, subdomain, "can't connect", "can't connect", "can't connect", "can't connect"});
            return 0;
        }
    }

    private String _RequestGETStatus(String url) {
        System.out.println(url);
        Request request = new Request(url);
        request.getCon().setConnectTimeout(3000);
        request.getCon().setReadTimeout(3000);
        request.setMethod("GET");
        request.setFollowRedirects(true);
        Response response = null;
        try {
            response = request.getResponse();
            if(errorFlagLength == response.getBody().length()){
                return "e";
            }

            return "" + response.getResponseCode();
        } catch (IOException e) {
//            model.addRow(new Object[]{model.getRowCount() + 1, subdomain, "can't connect", "can't connect", "can't connect", "can't connect"});
            return "e";
        }
    }
}


public class ScannerCheck implements IScannerCheck {

        @Override
    public List<IScanIssue> doPassiveScan(IHttpRequestResponse baseRequestResponse) {
        PrintWriter stdout = BurpExtender.getStdout();

        String host = baseRequestResponse.getHttpService().getHost();
        String scheme = baseRequestResponse.getHttpService().getProtocol();
        int port = baseRequestResponse.getHttpService().getPort();

        String requestRaw = new String(baseRequestResponse.getRequest());

        String queryStr = null;
        final List<String> list = new ArrayList<String>();
        final Pattern pa = Pattern.compile("(GET|POST)\\ (.*)HTTP", Pattern.CANON_EQ);
        final Matcher ma = pa.matcher(requestRaw);
        if (ma.find()) {
            queryStr = ma.group(2);
        }

        if(queryStr.indexOf('?')!=-1){
            queryStr = queryStr.substring(0,queryStr.indexOf('?'));
        }
        queryStr = queryStr.trim();

        String url = scheme + "://"+host+":"+port;


        // 泄露文件检测
        CheckLeakFile checkLeakFile =new CheckLeakFile(url,queryStr);
        ArrayList<String> result = checkLeakFile.check();
            for (String s :result
                 ) {
                stdout.println(s);
            }


        stdout.println(url);

//        stdout.println(new String(baseRequestResponse.getRequest()));
//        stdout.println(new String(baseRequestResponse.getResponse()));
//        stdout.println(baseRequestResponse.getComment());
//        stdout.println(baseRequestResponse.getHighlight());

        return null;
    }

    @Override
    public List<IScanIssue> doActiveScan(IHttpRequestResponse baseRequestResponse, IScannerInsertionPoint insertionPoint) {
        return null;
    }

    @Override
    public int consolidateDuplicateIssues(IScanIssue existingIssue, IScanIssue newIssue) {
        return 0;
    }
}
