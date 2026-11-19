package ir.dashwheel.fa;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.DragEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity {
    private SharedPreferences prefs;
    private LinearLayout root, body, nav;
    private String page = "داشبورد";
    private boolean light = false;
    private int accentIndex = 0;
    private final int[] accents = {0xff12d6b0,0xff4da3ff,0xffffb84d,0xffc18cff,0xffff6685};
    private final String[] accentNames = {"فیروزه‌ای","آبی","کهربایی","بنفش","صورتی"};
    private final String[] widgetIds = {"speed","fuel","navigation","music","bluetooth","weather","trip","tire","car"};
    private final String[] widgetTitles = {"سرعت‌سنج","سوخت","مسیریابی","موسیقی","بلوتوث","آب‌وهوا","اطلاعات سفر","فشار تایر","تصویر خودرو"};
    private boolean[] enabled = {true,true,true,true,true,true,true,false,true};
    private List<String> order = new ArrayList<>(Arrays.asList(widgetIds));
    private int bg, panel, fg, muted, accent, stroke;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("dashwheel_fa", MODE_PRIVATE);
        loadPrefs();
        applyPalette();
        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        render();
    }

    private void loadPrefs() {
        light = prefs.getBoolean("light", false);
        accentIndex = prefs.getInt("accent", 0);
        for (int i=0;i<widgetIds.length;i++) enabled[i] = prefs.getBoolean("widget_"+widgetIds[i], enabled[i]);
        String saved = prefs.getString("order", "");
        if (!saved.isEmpty()) {
            List<String> candidate = Arrays.asList(saved.split(","));
            order.clear();
            for (String id : candidate) if (indexOf(id)>=0 && !order.contains(id)) order.add(id);
            for (String id : widgetIds) if (!order.contains(id)) order.add(id);
        }
    }
    private void savePrefs() {
        SharedPreferences.Editor e=prefs.edit().putBoolean("light",light).putInt("accent",accentIndex).putString("order",join(order));
        for(int i=0;i<widgetIds.length;i++) e.putBoolean("widget_"+widgetIds[i],enabled[i]);
        e.apply();
    }
    private String join(List<String> values) { StringBuilder b=new StringBuilder(); for(String s:values){if(b.length()>0)b.append(",");b.append(s);}return b.toString(); }
    private int indexOf(String id) { for(int i=0;i<widgetIds.length;i++)if(widgetIds[i].equals(id))return i;return -1; }
    private void applyPalette() {
        accent=accents[Math.max(0,Math.min(accentIndex,accents.length-1))];
        bg=light?0xffeef2f5:0xff08111a;
        panel=light?Color.WHITE:0xff142331;
        fg=light?0xff14202b:0xfff0f5f8;
        muted=light?0xff637381:0xff91a5b5;
        stroke=light?0xffd5dfe6:0xff2b4050;
    }

    private void render() {
        applyPalette();
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(bg);
        root.setPadding(dp(16),dp(10),dp(16),dp(8));
        LinearLayout header=new LinearLayout(this); header.setGravity(Gravity.CENTER_VERTICAL);
        TextView logo=txt("DASHWHEEL فارسی",18,fg,true); header.addView(logo,new LinearLayout.LayoutParams(0,dp(42),1));
        TextClock clock=new TextClock(this); clock.setFormat24Hour("HH:mm"); clock.setFormat12Hour("hh:mm"); clock.setTextColor(accent); clock.setTextSize(22); clock.setTypeface(null,Typeface.BOLD); header.addView(clock);
        root.addView(header);
        TextView sub=txt("داشبورد هوشمند خودرو  •  نسخه مستقل",11,muted,false); sub.setGravity(Gravity.RIGHT); root.addView(sub);
        body=new LinearLayout(this); body.setOrientation(LinearLayout.VERTICAL);
        ScrollView sc=new ScrollView(this); sc.setFillViewport(false); sc.addView(body);
        root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));
        nav=new LinearLayout(this); nav.setOrientation(LinearLayout.HORIZONTAL); nav.setGravity(Gravity.CENTER);
        body.setOnDragListener((view,event)->{ if(event.getAction()==DragEvent.ACTION_DRAG_STARTED) return event.getClipDescription()!=null; if(event.getAction()==DragEvent.ACTION_DROP){ String id=event.getClipData().getItemAt(0).getText().toString(); int from=order.indexOf(id); if(from>=0){order.remove(from); int to=Math.max(0,Math.min(order.size(),(int)((event.getY()/Math.max(1,view.getHeight()))*order.size()))); order.add(to,id);savePrefs();render();} return true;} return true; });
        String[] tabs={"داشبورد","شخصی‌سازی","پروفایل‌ها","تنظیمات"};
        for(String t:tabs){ Button b=button(t,t.equals(page)?accent:panel,t.equals(page)?Color.BLACK:fg); b.setTextSize(11); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(44),1);p.setMargins(dp(2),dp(6),dp(2),0);nav.addView(b,p);b.setOnClickListener(v->{page=t;render();});}
        root.addView(nav); setContentView(root);
        if(page.equals("داشبورد")) dashboard(); else if(page.equals("شخصی‌سازی")) editor(); else if(page.equals("پروفایل‌ها")) profiles(); else settings();
    }

    private void dashboard() {
        section("داشبورد خودرو","نمای کلی اطلاعات و میانبرهای رانندگی");
        LinearLayout hero=card();
        hero.setPadding(dp(8),dp(10),dp(8),dp(8));
        hero.addView(new GaugeView(),new LinearLayout.LayoutParams(-1,dp(230)));
        LinearLayout stats=new LinearLayout(this);stats.setOrientation(LinearLayout.HORIZONTAL);
        String[][] values={{"۲۱۰۰","دور موتور"},{"۹۰°","دمای آب"},{"۴۰٪","سوخت"},{"۳۱۰","برد (کیلومتر)"}};
        for(String[] v:values){LinearLayout cell=new LinearLayout(this);cell.setGravity(Gravity.CENTER);cell.setOrientation(LinearLayout.VERTICAL);cell.addView(txt(v[0],17,accent,true));cell.addView(txt(v[1],10,muted,false));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(48),1);stats.addView(cell,p);}
        hero.addView(stats);body.addView(hero,full());
        int count=0; LinearLayout row=null;
        for(String id:order){int i=indexOf(id);if(i<0||!enabled[i])continue;
            if(count%2==0){row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);body.addView(row,full());}
            LinearLayout tile=widgetCard(id);
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(108),1);p.setMargins(dp(3),dp(4),dp(3),dp(4));row.addView(tile,p);count++;
        }
        if(count==0) body.addView(txt("هیچ ویجتی فعال نیست؛ از بخش شخصی‌سازی ویجت اضافه کن.",14,muted,false));
        TextView note=txt("اطلاعات سرعت، سوخت و دما در این نسخه نمونه هستند و هنوز از ECU خودرو خوانده نمی‌شوند.",10,muted,false);
        note.setPadding(dp(4),dp(8),dp(4),dp(8));body.addView(note,full());
    }

    private class GaugeView extends View {
        private final Paint p=new Paint(3);
        GaugeView(){super(MainActivity.this);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
        @Override protected void onDraw(Canvas c){
            super.onDraw(c);float w=getWidth(),h=getHeight();float cx=w/2f,cy=h*.72f;
            float radius=Math.min(w*.34f,h*.62f);
            p.setColor(panel);p.setStyle(Paint.Style.FILL);c.drawRoundRect(new RectF(0,0,w,h),dp(14),dp(14),p);
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(13));p.setStrokeCap(Paint.Cap.ROUND);
            RectF oval=new RectF(cx-radius,cy-radius,cx+radius,cy+radius);
            p.setColor(stroke);c.drawArc(oval,135,270,false,p);
            p.setColor(accent);c.drawArc(oval,135,112,false,p);
            p.setStrokeWidth(dp(2));p.setColor(muted);
            for(int i=0;i<=22;i++){double a=Math.toRadians(135+i*(270.0/22));float r1=radius-dp(13),r2=radius-(i%2==0?dp(27):dp(21));c.drawLine(cx+(float)Math.cos(a)*r1,cy+(float)Math.sin(a)*r1,cx+(float)Math.cos(a)*r2,cy+(float)Math.sin(a)*r2,p);}
            p.setStyle(Paint.Style.FILL);p.setColor(fg);p.setTextAlign(Paint.Align.CENTER);p.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));p.setTextSize(dp(48));c.drawText("--",cx,cy+dp(9),p);
            p.setTextSize(dp(12));p.setColor(muted);c.drawText("کیلومتر بر ساعت",cx,cy+dp(31),p);
            p.setTextSize(dp(11));p.setColor(accent);c.drawText("DASHWHEEL  •  فارسی",cx,dp(24),p);
        }
    }

    private LinearLayout widgetCard(String id) {
        int i=indexOf(id); String title=widgetTitles[i]; String symbol="◈", detail="آماده";
        switch(id){
            case "speed":symbol="◉";detail="سرعت فعلی";break;
            case "fuel":symbol="⛽";detail="اطلاعات سوخت";break;
            case "navigation":symbol="⌖";detail="باز کردن نقشه";break;
            case "music":symbol="♫";detail="پخش موسیقی";break;
            case "bluetooth":symbol="ᛒ";detail="تنظیمات اتصال";break;
            case "weather":symbol="☼";detail="نیازمند اینترنت";break;
            case "trip":symbol="↗";detail="مسافت و زمان";break;
            case "tire":symbol="◉";detail="فشار تایرها";break;
            case "car":symbol="▰";detail="نمای خودرو";break;
        }
        LinearLayout box=card();box.setGravity(Gravity.CENTER);box.setPadding(dp(5),dp(7),dp(5),dp(7));
        box.addView(txt(symbol,24,accent,true));box.addView(txt(title,14,fg,true));box.addView(txt(detail,10,muted,false));
        box.setOnClickListener(v->launchWidget(id));return box;
    }
    private void launchWidget(String id) {
        try {
            if(id.equals("navigation")) startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("geo:0,0?q=")));
            else if(id.equals("music")) startActivity(Intent.makeMainSelectorActivity(Intent.ACTION_MAIN,Intent.CATEGORY_APP_MUSIC));
            else if(id.equals("bluetooth")) startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
            else if(id.equals("weather")) Toast.makeText(this,"نمایش آب‌وهوا در به‌روزرسانی بعدی فعال می‌شود",Toast.LENGTH_SHORT).show();
            else if(id.equals("speed")||id.equals("fuel")||id.equals("tire")) Toast.makeText(this,"این داده هنوز به حسگر خودرو متصل نیست",Toast.LENGTH_SHORT).show();
            else Toast.makeText(this,"این ویجت در حال حاضر نمایشی است",Toast.LENGTH_SHORT).show();
        }catch(ActivityNotFoundException e){Toast.makeText(this,"برنامه مناسب پیدا نشد",Toast.LENGTH_SHORT).show();}
    }

    private void editor() {
        section("مرکز شخصی‌سازی","ویجت‌ها را روشن یا خاموش کن و ترتیب نمایش را تغییر بده.");
        LinearLayout theme=card();theme.setPadding(dp(10),dp(8),dp(10),dp(8));theme.addView(txt("قالب ظاهری",15,fg,true));
        LinearLayout tr=new LinearLayout(this);tr.setOrientation(LinearLayout.HORIZONTAL);
        Button dark=button("تیره",!light?accent:bg,!light?Color.BLACK:fg);Button lite=button("روشن",light?accent:bg,light?Color.BLACK:fg);
        tr.addView(dark,new LinearLayout.LayoutParams(0,dp(40),1));tr.addView(lite,new LinearLayout.LayoutParams(0,dp(40),1));theme.addView(tr);
        dark.setOnClickListener(v->{light=false;savePrefs();render();});lite.setOnClickListener(v->{light=true;savePrefs();render();});
        theme.addView(txt("رنگ تأکیدی",13,muted,false));LinearLayout colors=new LinearLayout(this);
        for(int i=0;i<accents.length;i++){final int n=i;Button c=button(accentNames[i],accents[i],Color.BLACK);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(38),1);p.setMargins(dp(2),dp(3),dp(2),dp(3));colors.addView(c,p);c.setOnClickListener(v->{accentIndex=n;savePrefs();render();});}
        theme.addView(colors);body.addView(theme,full());
        section("کتابخانه ویجت","نمایش هر پنل را مدیریت کن.");
        for(int i=0;i<widgetIds.length;i++){
            final int n=i;LinearLayout line=new LinearLayout(this);line.setGravity(Gravity.CENTER_VERTICAL);line.setBackground(round(panel,dp(12),stroke));line.setPadding(dp(10),dp(5),dp(10),dp(5));
            TextView name=txt(widgetTitles[i],14,fg,true);line.addView(name,new LinearLayout.LayoutParams(0,dp(44),1));
            Button toggle=button(enabled[i]?"فعال":"غیرفعال",enabled[i]?accent:bg,enabled[i]?Color.BLACK:fg);line.addView(toggle,new LinearLayout.LayoutParams(dp(86),dp(38)));
            toggle.setOnClickListener(v->{enabled[n]=!enabled[n];savePrefs();render();});
            LinearLayout.LayoutParams p=full();p.setMargins(0,0,0,dp(5));body.addView(line,p);
        }
        section("ترتیب پنل‌ها","با دکمه‌های بالا و پایین ترتیب نمایش را عوض کن.");
        for(int pos=0;pos<order.size();pos++){
            final int at=pos;String id=order.get(pos);int ix=indexOf(id);
            LinearLayout line=new LinearLayout(this);line.setGravity(Gravity.CENTER_VERTICAL);line.setBackground(round(panel,dp(10),stroke));line.setPadding(dp(8),dp(3),dp(8),dp(3));
            line.addView(txt(widgetTitles[ix],13,fg,true),new LinearLayout.LayoutParams(0,dp(40),1));
            Button up=button("↑",panel,fg),down=button("↓",panel,fg);line.addView(up,new LinearLayout.LayoutParams(dp(42),dp(36)));line.addView(down,new LinearLayout.LayoutParams(dp(42),dp(36)));
            up.setOnClickListener(v->{if(at>0){String x=order.remove(at);order.add(at-1,x);savePrefs();render();}});
            down.setOnClickListener(v->{if(at<order.size()-1){String x=order.remove(at);order.add(at+1,x);savePrefs();render();}});
            line.setOnLongClickListener(v->{ ClipData data=ClipData.newPlainText("widget",id); if(android.os.Build.VERSION.SDK_INT>=24) v.startDragAndDrop(data,new View.DragShadowBuilder(v),null,0); else v.startDrag(data,new View.DragShadowBuilder(v),null,0); return true; });
            LinearLayout.LayoutParams p=full();p.setMargins(0,0,0,dp(4));body.addView(line,p);
        }
    }

    private void profiles() {
        section("پروفایل‌های داشبورد","تنظیمات دلخواهت را ذخیره کن و هر زمان برگردان.");
        profileCard("پروفایل روزانه","چیدمان و رنگ فعلی را ذخیره کن","daily");
        profileCard("پروفایل شب","قالب تیره با رنگ فیروزه‌ای","night");
        profileCard("پروفایل سفارشی","ذخیره تنظیمات فعلی در پروفایل شخصی","custom");
        Button reset=button("بازگشت به تنظیمات اولیه",panel,fg);body.addView(reset,full());
        reset.setOnClickListener(v->{new AlertDialog.Builder(this).setTitle("بازنشانی").setMessage("همه تنظیمات و چیدمان به حالت اولیه برگردد؟").setNegativeButton("انصراف",null).setPositiveButton("بازنشانی",(d,w)->{prefs.edit().clear().apply();loadPrefs();render();}).show();});
    }
    private void profileCard(String title,String desc,String key) {
        LinearLayout c=card();c.setPadding(dp(12),dp(9),dp(12),dp(9));c.addView(txt(title,15,fg,true));c.addView(txt(desc,11,muted,false));
        LinearLayout actions=new LinearLayout(this);Button save=button("ذخیره",accent,Color.BLACK);Button load=button("بازیابی",bg,fg);
        actions.addView(save,new LinearLayout.LayoutParams(0,dp(40),1));actions.addView(load,new LinearLayout.LayoutParams(0,dp(40),1));c.addView(actions);
        save.setOnClickListener(v->{prefs.edit().putString("profile_"+key,serialize()).apply();Toast.makeText(this,"پروفایل ذخیره شد",Toast.LENGTH_SHORT).show();});
        load.setOnClickListener(v->{String data=prefs.getString("profile_"+key,"");if(data.isEmpty()){Toast.makeText(this,"هنوز پروفایلی ذخیره نشده",Toast.LENGTH_SHORT).show();return;}restore(data);savePrefs();render();Toast.makeText(this,"پروفایل بازیابی شد",Toast.LENGTH_SHORT).show();});
        LinearLayout.LayoutParams p=full();p.setMargins(0,0,0,dp(8));body.addView(c,p);
    }
    private String serialize(){StringBuilder b=new StringBuilder();b.append(light).append("|").append(accentIndex).append("|").append(join(order));for(boolean v:enabled)b.append("|").append(v);return b.toString();}
    private void restore(String data){try{String[] a=data.split("\\|");light=Boolean.parseBoolean(a[0]);accentIndex=Integer.parseInt(a[1]);order.clear();order.addAll(Arrays.asList(a[2].split(",")));for(int i=0;i<enabled.length;i++)enabled[i]=Boolean.parseBoolean(a[i+3]);}catch(Exception e){Toast.makeText(this,"فایل پروفایل قابل خواندن نیست",Toast.LENGTH_SHORT).show();}}

    private void settings() {
        section("تنظیمات برنامه","مدیریت زبان، نمایش و اطلاعات نسخه.");
        info("زبان رابط","فارسی — راست‌چین");
        info("نسخه برنامه","۱.۰.۰ آزمایشی");
        info("اتصال خودرو","در این نسخه فعال نیست؛ سرعت و سوخت نمایشی‌اند.");
        info("سازگاری","اندروید ۶ به بالا؛ مناسب نمایشگر افقی خودرو");
        Button bt=button("تنظیمات بلوتوث دستگاه",panel,fg);body.addView(bt,full());bt.setOnClickListener(v->{try{startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));}catch(Exception e){}});
        Button map=button("باز کردن نقشه",panel,fg);body.addView(map,full());map.setOnClickListener(v->{try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("geo:0,0?q=")));}catch(Exception e){}});
        Button about=button("درباره برنامه",panel,fg);body.addView(about,full());about.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Dashwheel فارسی").setMessage("یک داشبورد مستقل فارسی با قابلیت شخصی‌سازی. این پروژه کپی کد یا فایل‌های برنامه اصلی نیست.").setPositiveButton("باشه",null).show());
    }
    private void info(String title,String detail){LinearLayout c=card();c.setPadding(dp(10),dp(7),dp(10),dp(7));c.addView(txt(title,14,fg,true));c.addView(txt(detail,11,muted,false));LinearLayout.LayoutParams p=full();p.setMargins(0,0,0,dp(6));body.addView(c,p);}
    private void section(String title,String description){TextView h=txt(title,17,fg,true);h.setGravity(Gravity.RIGHT);h.setPadding(0,dp(13),0,dp(3));body.addView(h);TextView d=txt(description,11,muted,false);d.setGravity(Gravity.RIGHT);d.setPadding(0,0,0,dp(8));body.addView(d);}
    private LinearLayout card(){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setBackground(round(panel,dp(15),stroke));return c;}
    private TextView txt(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setGravity(Gravity.CENTER);t.setTextDirection(View.TEXT_DIRECTION_RTL);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private Button button(String s,int color,int textColor){Button b=new Button(this);b.setText(s);b.setTextSize(12);b.setTextColor(textColor);b.setAllCaps(false);b.setBackground(round(color,dp(10),stroke));return b;}
    private GradientDrawable round(int color,int radius,int line){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(radius);d.setStroke(dp(1),line);return d;}
    private LinearLayout.LayoutParams full(){return new LinearLayout.LayoutParams(-1,-2);}
    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+0.5f);}
}
