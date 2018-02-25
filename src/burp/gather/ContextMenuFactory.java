package burp.gather;
import burp.IContextMenuFactory;
import burp.IContextMenuInvocation;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class ContextMenuFactory implements IContextMenuFactory {
    @Override
    public List<JMenuItem> createMenuItems(IContextMenuInvocation invocation) {
        List<JMenuItem> list = new ArrayList<JMenuItem>();
        JMenuItem menuItem = new JMenuItem("Send to InfoGather");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        JFrame frame = new JFrame("Information Gather");
                        frame.setContentPane(new MyForm(invocation).getRootPanel());
                        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        frame.pack();
                        frame.setVisible(true);
                        //new MyForm(invocation);
                    }
                }).start();
            }
        });

        list.add(menuItem);
        return list;
    }
}
