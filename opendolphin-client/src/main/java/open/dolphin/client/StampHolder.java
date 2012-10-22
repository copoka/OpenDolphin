package open.dolphin.client;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.io.*;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JPopupMenu;
import javax.swing.text.Position;
import open.dolphin.infomodel.BundleDolphin;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.ModuleModel;
import open.dolphin.order.StampEditor;
import open.dolphin.project.Project;
import open.dolphin.util.ZenkakuUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

/**
 * KartePane に Component　として挿入されるスタンプを保持スルクラス。
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public final class StampHolder extends AbstractComponentHolder implements ComponentHolder {

    private static final String KEY_MODEL = "model";
    private static final String KEY_HINTS = "hints";
    private static final String KEY_STAMP_NAME = "stampName";
    private static final String KEY_STAMP_HOLDER = "stmpHolder";
    private static final String KEY_SHIFT_JIS = "SHIFT_JIS";
    private static final String DOT_VM = ".vm";
    
    private static final Color FOREGROUND = new Color(20, 20, 140);
    private static final Color BACKGROUND = Color.white;
    private static final Color SELECTED_BORDER = new Color(255, 0, 153);
    
    private ModuleModel stamp;
    private StampRenderingHints hints;
    private KartePane kartePane;
    private Position start;
    private Position end;
    private boolean selected;
    
    private Color foreGround = FOREGROUND;
    private Color background = BACKGROUND;
    private Color selectedBorder = SELECTED_BORDER;
    
    /** Creates new StampHolder2 */
    public StampHolder(KartePane kartePane, ModuleModel stamp) {
        super();
        this.kartePane = kartePane;
        setHints(new StampRenderingHints());
        setForeground(foreGround);
        setBackground(background);
        setBorder(BorderFactory.createLineBorder(kartePane.getTextPane().getBackground()));
        setStamp(stamp);
    }
    
    /**
     * Popupメニューを表示する。
     */
    @Override
    public void mabeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            JPopupMenu popup = new JPopupMenu();
            ChartMediator mediator = kartePane.getMediator();
            popup.add(mediator.getAction(GUIConst.ACTION_CUT));
            popup.add(mediator.getAction(GUIConst.ACTION_COPY));
            
            // copyAsText
            AbstractAction copyAsTextAction = new AbstractAction("テキストとしてコピー") {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    IInfoModel im = stamp.getModel();
                    if (im instanceof BundleDolphin) {
                        BundleDolphin bundle = (BundleDolphin)im;
                        StringSelection ss = new StringSelection(bundle.toString());
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
                    }
                }
            };
            popup.add(copyAsTextAction);
            
            popup.add(mediator.getAction(GUIConst.ACTION_PASTE));
            
            // 編集可の時のみ
            if (kartePane.getTextPane().isEditable()) {
                popup.addSeparator();

                // 右クリックで編集
                AbstractAction editAction = new AbstractAction("編集") {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        edit();
                    }
                };
                popup.add(editAction);
            }
            
            popup.show(e.getComponent(), e.getX(), e.getY());
        }
    }
    
    /**
     * このスタンプホルダのKartePaneを返す。
     */
    @Override
    public KartePane getKartePane() {
        return kartePane;
    }
    
    /**
     * スタンプホルダのコンテントタイプを返す。
     */
    @Override
    public int getContentType() {
        return ComponentHolder.TT_STAMP;
    }
    
    /**
     * このホルダのモデルを返す。
     * @return
     */
    public ModuleModel getStamp() {
        return stamp;
    }
    
    /**
     * このホルダのモデルを設定する。
     * @param stamp
     */
    public void setStamp(ModuleModel stamp) {
        if (this.stamp!=stamp) {
            this.stamp = stamp;
        }
        setMyText();
    }
    
    public StampRenderingHints getHints() {
        return hints;
    }
    
    public void setHints(StampRenderingHints hints) {
        this.hints = hints;
    }
    
    /**
     * 選択されているかどうかを返す。
     * @return 選択されている時 true
     */
    @Override
    public boolean isSelected() {
        return selected;
    }
    
    /**
     * 選択属性を設定する。
     * @param selected 選択の時 true
     */
    @Override
    public void setSelected(boolean selected) {
        boolean old = this.selected;
        this.selected = selected;
        if (old != this.selected) {
            if (this.selected) {
                this.setBorder(BorderFactory.createLineBorder(selectedBorder));
            } else {
                this.setBorder(BorderFactory.createLineBorder(kartePane.getTextPane().getBackground()));
            }
        }
    }
    
    /**
     * KartePane でこのスタンプがダブルクリックされた時コールされる。
     * StampEditor を開いてこのスタンプを編集する。
     */
    @Override
    public void edit() {
        if (kartePane.getTextPane().isEditable()) {
            StampEditor nse = new StampEditor(stamp, StampHolder.this);
        }
    }
    
    /**
     * エディタで編集した値を受け取り内容を表示する。
     */
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        
        ModuleModel newStamp = (ModuleModel) e.getNewValue();
        
        if (newStamp != null) {
            // スタンプを置き換える
            importStamp(newStamp);
        }
    }
    
    /**
     * スタンプの内容を置き換える。
     * @param newStamp
     */
    public void importStamp(ModuleModel newStamp) {
        setStamp(newStamp);
        kartePane.setDirty(true);
        kartePane.getTextPane().validate();
        kartePane.getTextPane().repaint();
    }
    
    /**
     * TextPane内での開始と終了ポジションを保存する。
     */
    @Override
    public void setEntry(Position start, Position end) {
        this.start = start;
        this.end = end;
    }
    
    /**
     * 開始ポジションを返す。
     */
    @Override
    public int getStartPos() {
        return start.getOffset();
    }
    
    /**
     * 終了ポジションを返す。
     */
    @Override
    public int getEndPos() {
        return end.getOffset();
    }
    
    /**
     * Velocity を利用してスタンプの内容を表示する。
     */
    private void setMyText() {

        if (getStamp()==null) {
            return;
        }
        
        try {
            IInfoModel model = getStamp().getModel();
            VelocityContext context = ClientContext.getVelocityContext();
            context.put(KEY_MODEL, model);
            context.put(KEY_HINTS, getHints());
            context.put(KEY_STAMP_NAME, getStamp().getModuleInfoBean().getStampName());
            
            String templateFile = getStamp().getModel().getClass().getName() + DOT_VM;
            
            // このスタンプのテンプレートファイルを得る
            if (getStamp().getModuleInfoBean().getEntity().equals(IInfoModel.ENTITY_LABO_TEST)) {
                if (Project.getBoolean("laboFold", true)) {
                    templateFile = "labo.vm";
                }  
            } 
            
            // Merge する
            StringWriter sw = new StringWriter();
            BufferedWriter bw = new BufferedWriter(sw);
            InputStream instream = ClientContext.getTemplateAsStream(templateFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(instream, KEY_SHIFT_JIS));
            Velocity.evaluate(context, bw, KEY_STAMP_HOLDER, reader);
            bw.flush();
            bw.close();
            reader.close();
            
            // 全角数字とスペースを直す
            String text = sw.toString();
            this.setText(ZenkakuUtils.toHalfNumber(text));
            
            // カルテペインへ展開された時広がるのを防ぐ
            this.setMaximumSize(this.getPreferredSize());
            
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
