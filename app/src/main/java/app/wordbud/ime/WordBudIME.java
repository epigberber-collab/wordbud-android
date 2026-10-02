package app.wordbud.ime;

import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.os.Looper;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;
import java.util.*;

public final class WordBudIME extends InputMethodService {
    private volatile Lexicon lexicon;
    private volatile boolean loadFailed;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final StringBuilder composing=new StringBuilder();
    private LinearLayout root, candidateRow, keys;
    private HorizontalScrollView candidates;
    private TextView status;
    private boolean english, symbols, caps, direct;
    private List<Lexicon.Candidate> choices=Collections.emptyList();
    private final int ink=Color.rgb(33,53,43),green=Color.rgb(46,106,75);
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density);}
    @Override public void onCreate(){
        super.onCreate();
        new Thread(()->{
            try { lexicon=Lexicon.load(getAssets().open("lexicon.tsv.gz")); }
            catch(Exception e){loadFailed=true;}
            main.post(this::refresh);
        },"wordbud-dictionary").start();
    }
    @Override public boolean onEvaluateFullscreenMode(){return false;}
    @Override public View onCreateInputView(){
        root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(4),dp(2),dp(4),dp(4));root.setBackgroundColor(Color.rgb(232,238,230));
        status=new TextView(this);status.setTextColor(green);status.setTextSize(13);status.setPadding(dp(10),0,dp(10),0);status.setGravity(Gravity.CENTER_VERTICAL);root.addView(status,new LinearLayout.LayoutParams(-1,dp(28)));
        candidates=new HorizontalScrollView(this);candidates.setHorizontalScrollBarEnabled(false);
        candidateRow=new LinearLayout(this);candidateRow.setOrientation(0);candidates.addView(candidateRow);root.addView(candidates,new LinearLayout.LayoutParams(-1,dp(68)));
        keys=new LinearLayout(this);keys.setOrientation(1);root.addView(keys);buildKeys();refresh();return root;
    }
    @Override public void onStartInput(EditorInfo info,boolean restarting){
        super.onStartInput(info,restarting); composing.setLength(0);choices=Collections.emptyList();caps=false;
        int type=info.inputType & InputType.TYPE_MASK_CLASS;
        int variation=info.inputType & InputType.TYPE_MASK_VARIATION;
        boolean number=type==InputType.TYPE_CLASS_NUMBER || type==InputType.TYPE_CLASS_PHONE || type==InputType.TYPE_CLASS_DATETIME;
        boolean password=type==InputType.TYPE_CLASS_TEXT && (variation==InputType.TYPE_TEXT_VARIATION_PASSWORD || variation==InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD || variation==InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD);
        boolean latin=type==InputType.TYPE_CLASS_TEXT && (variation==InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS || variation==InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS || variation==InputType.TYPE_TEXT_VARIATION_URI);
        direct=number || password || latin;english=direct;symbols=number;
        if(keys!=null)buildKeys(); refresh();
    }
    @Override public void onFinishInput(){composing.setLength(0);choices=Collections.emptyList();super.onFinishInput();refresh();}
    @Override public void onFinishInputView(boolean finishingInput){
        InputConnection ic=getCurrentInputConnection();
        if(ic!=null)ic.finishComposingText();composing.setLength(0);choices=Collections.emptyList();super.onFinishInputView(finishingInput);refresh();
    }
    @Override public void onUpdateSelection(int os,int oe,int ns,int ne,int cs,int ce){
        super.onUpdateSelection(os,oe,ns,ne,cs,ce);
        if(composing.length()>0 && (ns!=ce || ne!=ce)){
            composing.setLength(0);InputConnection ic=getCurrentInputConnection();if(ic!=null)ic.finishComposingText();refresh();
        }
    }
    private void buildKeys(){
        keys.removeAllViews();
        if(symbols){ row("1","2","3","4","5","6","7","8","9","0");row("@","#","$","%","&","*","-","+","(",")");row("ABC","/",":",";","\"","'","?","!","⌫"); }
        else { row("q","w","e","r","t","y","u","i","o","p");row("a","s","d","f","g","h","j","k","l");row("⇧","z","x","c","v","b","n","m","⌫"); }
        row("🌐",symbols?"ABC":"123",english?"EN":"中",english?",":"，","空格",english?".":"。","↵");
    }
    private void row(String... labels){
        LinearLayout r=new LinearLayout(this);r.setGravity(Gravity.CENTER);keys.addView(r,new LinearLayout.LayoutParams(-1,dp(49)));
        if(labels.length==9 && labels[0].equals("a")){View spacer=new View(this);r.addView(spacer,new LinearLayout.LayoutParams(dp(14),1));}
        for(String label:labels){
            TextView b=new TextView(this);b.setText(caps && label.matches("[a-z]")?label.toUpperCase(Locale.ROOT):label);b.setTextSize(label.equals("空格")?15:19);b.setTextColor(ink);b.setGravity(Gravity.CENTER);b.setBackground(shape(label.equals("空格")?0xffd5e4d3:Color.WHITE));
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-1,label.equals("空格")?2.6f:1f);p.setMargins(dp(2),dp(3),dp(2),dp(3));r.addView(b,p);b.setOnClickListener(v->key(label));
            if(label.equals("🌐"))b.setOnLongClickListener(v->{((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).showInputMethodPicker();return true;});
            if(label.equals("⌫"))b.setOnLongClickListener(v->{if(composing.length()>0){composing.setLength(0);sync();return true;}return false;});
        }
        if(labels.length==9 && labels[0].equals("a")){View spacer=new View(this);r.addView(spacer,new LinearLayout.LayoutParams(dp(14),1));}
    }
    private GradientDrawable shape(int color){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(7));return d;}
    private void key(String k){
        InputConnection ic=getCurrentInputConnection();if(ic==null)return;
        if(k.equals("🌐")){finishRaw(); if(!switchToNextInputMethod(false))((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).showInputMethodPicker();return;}
        if(k.equals("中")||k.equals("EN")){finishRaw();if(!direct){english=!english;buildKeys();}refresh();return;}
        if(k.equals("123")||k.equals("ABC")){finishRaw();symbols=!symbols;buildKeys();refresh();return;}
        if(k.equals("⇧")){caps=!caps;buildKeys();return;}
        if(k.equals("⌫")){
            if(composing.length()>0){composing.deleteCharAt(composing.length()-1);sync();}
            else {ic.beginBatchEdit(); CharSequence selected=ic.getSelectedText(0);if(selected!=null && selected.length()>0)ic.commitText("",1);else ic.deleteSurroundingTextInCodePoints(1,0);ic.endBatchEdit();}return;
        }
        if(k.equals("空格")){if(composing.length()>0){if(!choices.isEmpty())choose(choices.get(0),false);else finishRaw();}else ic.commitText(" ",1);return;}
        if(k.equals("↵")){
            if(composing.length()>0){finishRaw();return;}
            EditorInfo info=getCurrentInputEditorInfo();int action=info.imeOptions & EditorInfo.IME_MASK_ACTION;
            if(action!=EditorInfo.IME_ACTION_NONE && action!=EditorInfo.IME_ACTION_UNSPECIFIED && (info.imeOptions & EditorInfo.IME_FLAG_NO_ENTER_ACTION)==0)ic.performEditorAction(action);else ic.commitText("\n",1);return;
        }
        if(!english && !direct && !symbols && k.matches("[a-z]")){
            if(composing.length()<80){composing.append(k);sync();}return;
        }
        if(composing.length()>0){if(!choices.isEmpty())choose(choices.get(0),false);finishRaw();}
        ic.commitText(caps && english?k.toUpperCase(Locale.ROOT):k,1);
    }
    private void finishRaw(){
        InputConnection ic=getCurrentInputConnection();if(ic!=null && composing.length()>0){ic.commitText(composing.toString(),1);ic.finishComposingText();}composing.setLength(0);refresh();
    }
    private void choose(Lexicon.Candidate c,boolean translated){
        InputConnection ic=getCurrentInputConnection();if(ic==null)return;
        ic.beginBatchEdit();ic.commitText(translated?c.gloss:c.word,1);composing.delete(0,Math.min(c.consumed,composing.length()));sync();ic.endBatchEdit();
    }
    private void sync(){InputConnection ic=getCurrentInputConnection();if(ic!=null){if(composing.length()==0){ic.commitText("",1);ic.finishComposingText();}else ic.setComposingText(composing,1);}refresh();}
    private void refresh(){
        if(candidateRow==null)return;
        choices=(lexicon!=null && composing.length()>0 && !direct)?lexicon.query(composing.toString()):Collections.emptyList();candidateRow.removeAllViews();
        status.setText(composing.length()>0?composing.toString():direct?"直接输入 · 当前输入框不显示译词":lexicon!=null?"词芽 · 点选中文，长按输入释义":loadFailed?"词库加载失败 · 可输入英文":"正在加载离线词库…");
        if(choices.isEmpty()){
            TextView hint=new TextView(this);hint.setText(composing.length()>0?"暂无候选 · 回车输入原文":"中文候选\nEnglish meaning");hint.setTextSize(15);hint.setTextColor(0xff718071);hint.setPadding(dp(12),dp(7),dp(12),0);candidateRow.addView(hint);
        } else for(Lexicon.Candidate c:choices){
            LinearLayout card=new LinearLayout(this);card.setOrientation(1);card.setPadding(dp(12),dp(5),dp(12),dp(3));
            TextView cn=new TextView(this);cn.setText(c.word);cn.setTextColor(ink);cn.setTextSize(21);cn.setTypeface(null,Typeface.BOLD);card.addView(cn);
            TextView en=new TextView(this);en.setText(c.gloss);en.setTextColor(green);en.setTextSize(12);en.setSingleLine(true);en.setMaxWidth(dp(200));en.setEllipsize(android.text.TextUtils.TruncateAt.END);card.addView(en);
            card.setContentDescription(c.word+", "+c.gloss);card.setOnClickListener(v->choose(c,false));card.setOnLongClickListener(v->{choose(c,true);return true;});candidateRow.addView(card);
        }
        candidates.scrollTo(0,0);
    }
}
