package app.wordbud.ime;

import android.app.Activity;
import android.os.Bundle;
import android.provider.Settings;
import android.content.Intent;
import android.graphics.Color;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.view.View;

public class SetupActivity extends Activity {
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density);}
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        ScrollView scroll=new ScrollView(this);
        LinearLayout box=new LinearLayout(this); box.setOrientation(1); box.setPadding(dp(24),dp(40),dp(24),dp(32));
        box.setBackgroundColor(Color.rgb(245,248,243)); scroll.addView(box); setContentView(scroll);
        scroll.setOnApplyWindowInsetsListener((v,insets)->{box.setPadding(dp(24),dp(24)+insets.getSystemWindowInsetTop(),dp(24),dp(24)+insets.getSystemWindowInsetBottom()); return insets;});
        text(box,"词芽",34,Color.rgb(43,91,65));
        text(box,"打字的时候，多认识一个词。",18,Color.DKGRAY);
        text(box,"非官方独立实验版 0.1.0\n灵感来自青简，不是青简官方安卓版。",14,Color.GRAY);
        button(box,"1  启用词芽输入法",v->startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)));
        button(box,"2  切换到词芽",v->((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).showInputMethodPicker());
        text(box,"3  在下面试着输入 nihao、kaifa 或 xuexi",16,Color.DKGRAY);
        EditText test=new EditText(this); test.setHint("点击这里试用输入法"); test.setMinLines(3); test.setGravity(48); box.addView(test);
        text(box,"用法\n• 输入全拼，横向滑动候选条查看更多词。\n• 点候选输入中文，长按候选输入英文释义。\n• 空格选择第一个候选；回车确认原始拼音。\n• 中/EN 切换语言；123 切换数字符号。\n• 地球键可切换其他输入法；长按可打开列表。\n• v 表示 ü，例如 lv → 绿。",15,Color.DKGRAY);
        text(box,"第一版限制\n采用逐词选择，不支持智能整句预测、简拼、双拼、语音或手写。英文显示的是词典释义，多义词需要结合语境。候选排序较基础；尚未经过真机验证。",14,Color.GRAY);
        text(box,"离线与隐私\n无网络权限、广告或账号，不保存输入内容或密码。词库在本机加载；首次显示键盘可能需要稍等。",14,Color.GRAY);
        text(box,"词典致谢\nCC-CEDICT，Paul Denisowski 与 CC-CEDICT contributors / MDBG。词典及转换数据采用 CC BY-SA 4.0。转换：简体词条、去除声调、整理释义。\nhttps://www.mdbg.net/chinese/dictionary?page=cc-cedict\nhttps://creativecommons.org/licenses/by-sa/4.0/\n应用原创代码采用 MIT 许可。未复用青简代码、名称或 logo。",12,Color.GRAY);
    }
    private void text(LinearLayout box,String s,int size,int color){ TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);v.setPadding(0,dp(10),0,dp(10));box.addView(v); }
    private void button(LinearLayout box,String s,View.OnClickListener l){Button b=new Button(this);b.setText(s);b.setOnClickListener(l);box.addView(b,new LinearLayout.LayoutParams(-1,dp(56)));}
}
