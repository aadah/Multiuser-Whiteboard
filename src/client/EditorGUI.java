package client;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;

/**
 * EditorGUI contains all of the JComponents the user needs to change
 * their drawing style. It has options for free hand, line segments, and
 * the like. It also allows for erasing and adjusting stroke size.
 * 
 * There are two constructors: one for testing the layout, and one for actual
 * use in the final application.
 */

/*
 * REPRESENTATION INVARIANT:
 * 
 *      No JComponent field is null.
 */
public class EditorGUI extends JFrame{
    private final JLabel modeMsg;
    private final JRadioButton pencilRdioBtn;
    private final JRadioButton segmentRdioBtn;
    private final JRadioButton fillRdioBtn;
    private final JRadioButton eraseRdioBtn;
    private final ButtonGroup buttonGrp;
    private final JLabel strokeSizeMsg;
    private final JSlider strokeSizeSldr;
    
    /**
     * Creates an EditorGUI.
     */
    public EditorGUI() {
        modeMsg = new JLabel("Mode:");
        pencilRdioBtn = new JRadioButton("Pencil");
        pencilRdioBtn.setSelected(true);
        segmentRdioBtn = new JRadioButton("Segment");
        fillRdioBtn = new JRadioButton("Text");
        eraseRdioBtn = new JRadioButton("Erase");
        buttonGrp = new ButtonGroup();
        buttonGrp.add(pencilRdioBtn);
        buttonGrp.add(segmentRdioBtn);
        buttonGrp.add(fillRdioBtn);
        buttonGrp.add(eraseRdioBtn);
        strokeSizeMsg = new JLabel("Stroke size:");
        strokeSizeSldr = new JSlider(0,100,1);
        strokeSizeSldr.setMajorTickSpacing(10);
        strokeSizeSldr.setMinorTickSpacing(5);
        strokeSizeSldr.setPaintLabels(true);
        strokeSizeSldr.setPaintTicks(true);

        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(modeMsg)
                        .addComponent(pencilRdioBtn)
                        .addComponent(segmentRdioBtn)
                        .addComponent(fillRdioBtn)
                        .addComponent(eraseRdioBtn))
                .addGroup(layout.createSequentialGroup()
                        .addComponent(strokeSizeMsg)
                        .addComponent(strokeSizeSldr)));
        
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(modeMsg)
                        .addComponent(pencilRdioBtn)
                        .addComponent(segmentRdioBtn)
                        .addComponent(fillRdioBtn)
                        .addComponent(eraseRdioBtn))
                .addGroup(layout.createParallelGroup()
                        .addComponent(strokeSizeMsg)
                        .addComponent(strokeSizeSldr)));
        
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        this.setMinimumSize(new Dimension(500,this.getSize().height));
        this.pack();
        this.setLocationRelativeTo(null);
        this.setTitle("Editor Beta");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    /**
     * Creates a EditorGUI with the given JComponents.
     * 
     * @param modeMsg
     * @param pencilRdioBtn
     * @param segmentRdioBtn
     * @param fillRdioBtn
     * @param eraseRdioBtn
     * @param buttonGrp
     * @param strokeSizeMsg
     * @param strokeSizeSldr
     */
    public EditorGUI(JLabel modeMsg, JRadioButton pencilRdioBtn, JRadioButton segmentRdioBtn, JRadioButton fillRdioBtn,
                        JRadioButton eraseRdioBtn, ButtonGroup buttonGrp, JLabel strokeSizeMsg, JSlider strokeSizeSldr) {
        this.modeMsg = modeMsg;
        this.pencilRdioBtn = pencilRdioBtn;
        this.pencilRdioBtn.setSelected(true);
        this.segmentRdioBtn = segmentRdioBtn;
        this.fillRdioBtn = fillRdioBtn;
        this.eraseRdioBtn = eraseRdioBtn;
        this.buttonGrp = buttonGrp;
        this.buttonGrp.add(pencilRdioBtn);
        this.buttonGrp.add(segmentRdioBtn);
        this.buttonGrp.add(fillRdioBtn);
        this.buttonGrp.add(eraseRdioBtn);
        this.strokeSizeMsg = strokeSizeMsg;
        this.strokeSizeSldr = strokeSizeSldr;
        this.strokeSizeSldr.setMajorTickSpacing(10);
        this.strokeSizeSldr.setMinorTickSpacing(5);
        this.strokeSizeSldr.setPaintLabels(true);
        this.strokeSizeSldr.setPaintTicks(true);

        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(this.modeMsg)
                        .addComponent(this.pencilRdioBtn)
                        .addComponent(this.segmentRdioBtn)
//                        .addComponent(this.fillRdioBtn)
                        .addComponent(this.eraseRdioBtn))
                .addGroup(layout.createSequentialGroup()
                        .addComponent(this.strokeSizeMsg)
                        .addComponent(this.strokeSizeSldr)));
        
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(this.modeMsg)
                        .addComponent(this.pencilRdioBtn)
                        .addComponent(this.segmentRdioBtn)
//                        .addComponent(this.fillRdioBtn)
                        .addComponent(this.eraseRdioBtn))
                .addGroup(layout.createParallelGroup()
                        .addComponent(this.strokeSizeMsg)
                        .addComponent(this.strokeSizeSldr)));
        
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        this.setMinimumSize(new Dimension(500,this.getSize().height));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) screenSize.getWidth();
        this.setLocation(width-this.getWidth(), 0);
        this.pack();
        this.setTitle("Editor");
    }
    
    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                EditorGUI main = new EditorGUI();
                main.setVisible(true);
            }
        });
    }

}
