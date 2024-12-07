package com.example.reader;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    private ScrollView scrollView;
    private TextView textView;
    private SharedPreferences sharedPreferences;
    private int textSize = 18;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化主题
        setAppTheme();
        setContentView(R.layout.activity_main);

        initViews();
        requestPermissionsIfNeeded();
    }

    private void setAppTheme() {
        sharedPreferences = getSharedPreferences(
                String.format("%s_preferences", getPackageName()), MODE_PRIVATE);

        boolean isNightMode = sharedPreferences.getBoolean("night_mode", false);
        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        setMenuIcons(menu);
        return true;
    }

    private void setMenuIcons(Menu menu) {
        MenuItem nightModeItem = menu.findItem(MenuAction.TOGGLE_NIGHT_MODE.getId());
        MenuItem zoomIn = menu.findItem(MenuAction.ZOOM_IN.getId());
        MenuItem zoomOut = menu.findItem(MenuAction.ZOOM_OUT.getId());
        MenuItem openFile = menu.findItem(MenuAction.OPEN_FILE.getId());
        if (nightModeItem != null) {
            if (isNightModeEnabled()) {
                nightModeItem.setIcon(R.drawable.ic_night);
                nightModeItem.getIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            } else {
                nightModeItem.getIcon().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
            }
        }
        if (zoomIn != null) {
            if (isNightModeEnabled()) {
                zoomIn.getIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            } else {
                zoomIn.getIcon().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
            }
        }
        if (zoomOut != null) {
            if (isNightModeEnabled()) {
                zoomOut.getIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            } else {
                zoomOut.getIcon().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
            }
        }
        if (openFile != null) {
            if (isNightModeEnabled()) {
                openFile.getIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            } else {
                openFile.getIcon().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
            }
        }
    }

    private boolean isNightModeEnabled() {
        return sharedPreferences.getBoolean("night_mode", false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MenuAction action = MenuAction.fromId(item.getItemId());
        if (action == null) return super.onOptionsItemSelected(item);

        switch (action) {
            case TOGGLE_NIGHT_MODE:
                toggleNightMode();
                invalidateOptionsMenu();
                return true;
            case OPEN_FILE:
                handleOpenFile();
                return true;
            case ZOOM_IN:
                handleZoomIn();
                return true;
            case ZOOM_OUT:
                handleZoomOut();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleNightMode() {
        boolean isNightMode = sharedPreferences.getBoolean("night_mode", false);

        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            sharedPreferences.edit().putBoolean("night_mode", false).apply();
            Toast.makeText(this, "切换到白天模式", Toast.LENGTH_SHORT).show();
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            sharedPreferences.edit().putBoolean("night_mode", true).apply();
            Toast.makeText(this, "切换到夜间模式", Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews() {
        textView = findViewById(R.id.textView);
        scrollView = findViewById(R.id.scrollView);

        scrollView.getViewTreeObserver().addOnScrollChangedListener(() ->
                sharedPreferences.edit().putInt("offset", scrollView.getScrollY()).apply());
    }

    private void requestPermissionsIfNeeded() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    101);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "权限已授予", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        final int offset = sharedPreferences.getInt("offset", 0);
        final int size = sharedPreferences.getInt("size", 18);

        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        scrollView.post(() -> scrollView.scrollTo(0, offset));
    }

    private void handleOpenFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/plain");
        startActivityForResult(intent, 1);
    }

    private void handleZoomIn() {
        if (textSize < 30) {
            textSize++;
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
            sharedPreferences.edit().putInt("size", textSize).apply();
        } else {
            Toast.makeText(this, "字体大小已达到最大值", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleZoomOut() {
        if (textSize > 10) {
            textSize--;
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
            sharedPreferences.edit().putInt("size", textSize).apply();
        } else {
            Toast.makeText(this, "字体大小已达到最小值", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                try {
                    String content = readTextFromUri(uri);
                    textView.setText(content);
                    sharedPreferences.edit().putString("file", uri.toString()).apply();
                } catch (IOException e) {
                    Toast.makeText(this, "文件读取失败", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }
    }

    private String readTextFromUri(Uri uri) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        InputStream inputStream = getContentResolver().openInputStream(uri);

        if (inputStream != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            reader.close();
            inputStream.close();
        }

        return stringBuilder.toString();
    }
}
