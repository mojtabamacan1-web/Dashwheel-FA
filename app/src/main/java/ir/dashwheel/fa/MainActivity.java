package ir.dashwheel.fa;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private final int bg = Color.rgb(8, 13, 20);
    private final int panel = Color.rgb(18, 27, 38);
    private final int accent = Color.rgb(0, 220, 190);
    private final int white = Color.rgb(239, 246, 250);
    private final int muted = Color.rgb(145, 164, 179);

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        buildScreen();
    }

    private void buildScreen() {
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(22), dp(14), dp(22), dp(14));
        page.setBackgroundColor(bg);

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setOrientation(LinearLayout.HORIZONTAL);
        TextView brand = text("DASHWHEEL  |  فارسی", 19, white, true);
        top.addView(brand, new LinearLayout.LayoutParams(0, dp(42), 1));
        TextClock clock = new TextClock(this);
        clock.setFormat12Hour("hh:mm");
        clock.setFormat24Hour("HH:mm");
        clock.setTextColor(accent);
        clock.setTextSize(23);
        clock.setTypeface(Typeface.DEFAULT_BOLD);
        top.addView(clock);
        page.addView(top);

        TextView subtitle = text("داشبورد هوشمند خودرو", 12, muted, false);
        subtitle.setPadding(0, 0, 0, dp(12));
        page.addView(subtitle);

        ScrollView scroll = new ScrollView(this);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);

        LinearLayout hero = card();
        hero.setGravity(Gravity.CENTER);
        hero.setPadding(dp(14), dp(12), dp(14), dp(12));
        TextView speedCaption = text("سرعت خودرو", 14, muted, false);
        hero.addView(speedCaption);
        TextView speed = text("--", 58, accent, true);
        speed.setPadding(0, dp(1), 0, 0);
        hero.addView(speed);
        hero.addView(text("کیلومتر بر ساعت", 12, muted, false));
        TextView connect = text("برای نمایش اطلاعات واقعی، اتصال به خودرو لازم است", 10, muted, false);
        connect.setPadding(0, dp(8), 0, 0);
        hero.addView(connect);
        content.addView(hero, fullWidth());

        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.addView(tile("⛽", "سوخت", "وضعیت نامشخص"), weightParams());
        row1.addView(tile("🧭", "مسیریابی", "باز کردن نقشه"), weightParams());
        content.addView(row1, fullWidth());

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.addView(tile("♫", "موسیقی", "پخش‌کننده"), weightParams());
        row2.addView(tile("◉", "بلوتوث", "اتصال دستگاه"), weightParams());
        content.addView(row2, fullWidth());

        LinearLayout row3 = new LinearLayout(this);
        row3.setOrientation(LinearLayout.HORIZONTAL);
        row3.addView(tile("◷", "سفر", "اطلاعات مسیر"), weightParams());
        row3.addView(tile("⚙", "تنظیمات", "تنظیمات برنامه"), weightParams());
        content.addView(row3, fullWidth());

        TextView footer = text("نسخه مستقل فارسی  •  ۱.۰", 11, muted, false);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, dp(10), 0, dp(4));
        content.addView(footer);
        scroll.addView(content);
        page.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(page);
    }

    private LinearLayout tile(String symbol, String title, String detail) {
        LinearLayout box = card();
        box.setGravity(Gravity.CENTER);
        box.setPadding(dp(8), dp(12), dp(8), dp(12));
        TextView icon = text(symbol, 25, accent, true);
        box.addView(icon);
        TextView heading = text(title, 16, white, true);
        heading.setPadding(0, dp(4), 0, dp(2));
        box.addView(heading);
        box.addView(text(detail, 10, muted, false));
        box.setOnClickListener(v -> handle(title));
        LinearLayout.LayoutParams p = weightParams();
        p.height = dp(105);
        return box;
    }

    private void handle(String title) {
        try {
            if (title.equals("مسیریابی")) {
                Intent map = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q="));
                startActivity(map);
            } else if (title.equals("موسیقی")) {
                Intent music = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MUSIC);
                startActivity(music);
            } else if (title.equals("بلوتوث")) {
                startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
            } else if (title.equals("تنظیمات")) {
                new AlertDialog.Builder(this).setTitle("تنظیمات داشبورد")
                    .setMessage("این نسخه مستقل در حال توسعه است. امکانات اتصال به خودرو در نسخه‌های بعدی اضافه می‌شود.")
                    .setPositiveButton("متوجه شدم", (d, w) -> d.dismiss()).show();
            } else {
                Toast.makeText(this, "این بخش در نسخه بعدی فعال می‌شود", Toast.LENGTH_SHORT).show();
            }
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(this, "برنامه‌ای برای اجرای این بخش پیدا نشد", Toast.LENGTH_SHORT).show();
        }
    }

    private LinearLayout card() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackground(round(panel, dp(16), Color.rgb(36, 54, 68)));
        LinearLayout.LayoutParams p = fullWidth();
        p.setMargins(0, 0, 0, dp(9));
        box.setLayoutParams(p);
        return box;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER);
        t.setTextDirection(View.TEXT_DIRECTION_RTL);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private GradientDrawable round(int color, int radius, int stroke) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radius);
        d.setStroke(dp(1), stroke);
        return d;
    }

    private LinearLayout.LayoutParams fullWidth() { return new LinearLayout.LayoutParams(-1, -2); }
    private LinearLayout.LayoutParams weightParams() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(105), 1);
        p.setMargins(0, 0, dp(8), dp(1));
        return p;
    }
    private int dp(int value) { return (int)(value * getResources().getDisplayMetrics().density + 0.5f); }
}
