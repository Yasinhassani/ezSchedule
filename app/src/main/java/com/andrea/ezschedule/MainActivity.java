package com.andrea.ezschedule;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.database.Cursor;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends Activity {
    private static final int PICK_IMAGE_REQUEST = 1001;

    private final String[] months = new String[]{
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    private final Map<String, LinearLayout> monthContainers = new HashMap<>();
    private Uri selectedImageUri;
    private ImageView selectedPreview;
    private TextView selectedFileName;
    private Spinner monthSpinner;
    private Dialog addDialog;

    private final int background = Color.rgb(10, 17, 40);
    private final int surface = Color.rgb(21, 27, 45);
    private final int surfaceHigh = Color.rgb(28, 37, 61);
    private final int primary = Color.rgb(233, 193, 118);
    private final int textMain = Color.rgb(229, 226, 225);
    private final int textMuted = Color.rgb(148, 163, 184);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.setStatusBarColor(background);
        window.setNavigationBarColor(background);
        buildMainScreen();
    }

    private void buildMainScreen() {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(background);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), dp(28), dp(20), dp(110));
        scrollView.addView(content, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT
        ));

        TextView appTitle = new TextView(this);
        appTitle.setText("ezSchedule");
        appTitle.setTextColor(textMain);
        appTitle.setTextSize(30);
        appTitle.setTypeface(Typeface.DEFAULT_BOLD);
        content.addView(appTitle);

        TextView subtitle = new TextView(this);
        subtitle.setText("Save your schedule images by month");
        subtitle.setTextColor(textMuted);
        subtitle.setTextSize(15);
        subtitle.setPadding(0, dp(4), 0, dp(30));
        content.addView(subtitle);

        TextView year = new TextView(this);
        year.setText("2026");
        year.setTextColor(primary);
        year.setTextSize(38);
        year.setTypeface(Typeface.DEFAULT_BOLD);
        year.setPadding(0, 0, 0, dp(20));
        content.addView(year);

        for (String month : months) {
            addMonthSection(content, month);
        }

        root.addView(scrollView);

        TextView addButton = new TextView(this);
        addButton.setText("+");
        addButton.setGravity(Gravity.CENTER);
        addButton.setTextSize(42);
        addButton.setTypeface(Typeface.DEFAULT_BOLD);
        addButton.setTextColor(background);
        addButton.setBackground(rounded(primary, dp(22), 0, 0));
        addButton.setElevation(dp(10));
        addButton.setOnClickListener(v -> showAddScheduleDialog());

        FrameLayout.LayoutParams addParams = new FrameLayout.LayoutParams(dp(68), dp(68));
        addParams.gravity = Gravity.BOTTOM | Gravity.END;
        addParams.setMargins(0, 0, dp(22), dp(26));
        root.addView(addButton, addParams);

        setContentView(root);
        refreshSchedules();
    }

    private void addMonthSection(LinearLayout parent, String month) {
        TextView title = new TextView(this);
        title.setText(month);
        title.setTextColor(textMain);
        title.setTextSize(23);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setPadding(0, dp(18), 0, dp(10));
        parent.addView(title);

        LinearLayout holder = new LinearLayout(this);
        holder.setOrientation(LinearLayout.VERTICAL);
        holder.setPadding(0, 0, 0, dp(10));
        parent.addView(holder, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        monthContainers.put(month, holder);
    }

    private void refreshSchedules() {
        for (String month : months) {
            LinearLayout holder = monthContainers.get(month);
            if (holder == null) continue;
            holder.removeAllViews();

            File dir = getMonthDirectory(month);
            File[] files = dir.listFiles((file, name) -> {
                String lower = name.toLowerCase(Locale.ROOT);
                return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp");
            });

            if (files == null || files.length == 0) {
                holder.addView(emptyMonthView());
                continue;
            }

            Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());

            LinearLayout row = null;
            for (int i = 0; i < files.length; i++) {
                if (i % 2 == 0) {
                    row = new LinearLayout(this);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.setPadding(0, 0, 0, dp(12));
                    holder.addView(row);
                }
                if (row != null) {
                    row.addView(scheduleCard(files[i]), cardParams(i % 2 == 0));
                }
            }
        }
    }

    private View emptyMonthView() {
        TextView empty = new TextView(this);
        empty.setText("No schedule added yet");
        empty.setTextColor(textMuted);
        empty.setTextSize(14);
        empty.setGravity(Gravity.CENTER);
        empty.setPadding(dp(16), dp(26), dp(16), dp(26));
        empty.setBackground(rounded(surface, dp(18), Color.argb(26, 255, 255, 255), 1));
        return empty;
    }

    private View scheduleCard(File file) {
        FrameLayout card = new FrameLayout(this);
        card.setBackground(rounded(surfaceHigh, dp(18), Color.argb(28, 255, 255, 255), 1));
        card.setPadding(dp(4), dp(4), dp(4), dp(4));
        card.setOnClickListener(v -> showFullImage(file));
        card.setElevation(dp(4));

        ImageView image = new ImageView(this);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setImageURI(Uri.fromFile(file));
        card.addView(image, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                dp(160)
        ));
        return card;
    }

    private LinearLayout.LayoutParams cardParams(boolean leftCard) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(168), 1);
        if (leftCard) {
            params.setMargins(0, 0, dp(6), 0);
        } else {
            params.setMargins(dp(6), 0, 0, 0);
        }
        return params;
    }

    private void showAddScheduleDialog() {
        selectedImageUri = null;
        addDialog = new Dialog(this);
        addDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(22), dp(22), dp(22), dp(22));
        box.setBackground(rounded(surface, dp(24), Color.argb(38, 255, 255, 255), 1));

        TextView title = new TextView(this);
        title.setText("Add Schedule");
        title.setTextColor(primary);
        title.setTextSize(24);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setPadding(0, 0, 0, dp(18));
        box.addView(title);

        TextView monthLabel = label("Choose Month");
        box.addView(monthLabel);

        monthSpinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(adapter);
        box.addView(monthSpinner, matchWrapWithBottom(dp(16)));

        Button selectButton = new Button(this);
        selectButton.setText("Select Image");
        selectButton.setTextColor(background);
        selectButton.setTextSize(15);
        selectButton.setTypeface(Typeface.DEFAULT_BOLD);
        selectButton.setBackground(rounded(primary, dp(14), 0, 0));
        selectButton.setOnClickListener(v -> openImagePicker());
        box.addView(selectButton, buttonParams());

        selectedFileName = new TextView(this);
        selectedFileName.setText("No image selected");
        selectedFileName.setTextColor(textMuted);
        selectedFileName.setTextSize(13);
        selectedFileName.setPadding(0, dp(8), 0, dp(12));
        box.addView(selectedFileName);

        selectedPreview = new ImageView(this);
        selectedPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        selectedPreview.setBackground(rounded(background, dp(16), Color.argb(26, 255, 255, 255), 1));
        box.addView(selectedPreview, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(170)
        ));

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setPadding(0, dp(18), 0, 0);

        Button cancel = new Button(this);
        cancel.setText("Cancel");
        cancel.setTextColor(textMain);
        cancel.setBackground(rounded(surfaceHigh, dp(14), 0, 0));
        cancel.setOnClickListener(v -> addDialog.dismiss());
        buttons.addView(cancel, new LinearLayout.LayoutParams(0, dp(52), 1));

        Button save = new Button(this);
        save.setText("Save");
        save.setTextColor(background);
        save.setTypeface(Typeface.DEFAULT_BOLD);
        save.setBackground(rounded(primary, dp(14), 0, 0));
        save.setOnClickListener(v -> saveSelectedSchedule());
        LinearLayout.LayoutParams saveParams = new LinearLayout.LayoutParams(0, dp(52), 1);
        saveParams.setMargins(dp(12), 0, 0, 0);
        buttons.addView(save, saveParams);

        box.addView(buttons);

        addDialog.setContentView(box);
        Window window = addDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
        addDialog.show();
        Window shownWindow = addDialog.getWindow();
        if (shownWindow != null) {
            shownWindow.setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.92), LinearLayout.LayoutParams.WRAP_CONTENT);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/jpeg", "image/png", "image/webp"});
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                final int flags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                try {
                    getContentResolver().takePersistableUriPermission(selectedImageUri, flags);
                } catch (Exception ignored) {
                    // Some gallery apps do not offer persistable permissions. The image is still copied on Save.
                }
                selectedPreview.setImageURI(selectedImageUri);
                selectedFileName.setText(getDisplayName(selectedImageUri));
            }
        }
    }

    private void saveSelectedSchedule() {
        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
            return;
        }

        String month = months[monthSpinner.getSelectedItemPosition()];
        File targetDir = getMonthDirectory(month);
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            Toast.makeText(this, "Could not create folder", Toast.LENGTH_SHORT).show();
            return;
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File target = new File(targetDir, "schedule_" + timestamp + ".jpg");

        try (InputStream input = getContentResolver().openInputStream(selectedImageUri);
             FileOutputStream output = new FileOutputStream(target)) {
            if (input == null) throw new Exception("No input stream");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            Toast.makeText(this, "Schedule saved", Toast.LENGTH_SHORT).show();
            addDialog.dismiss();
            refreshSchedules();
        } catch (Exception exception) {
            Toast.makeText(this, "Could not save image", Toast.LENGTH_LONG).show();
        }
    }

    private void showFullImage(File file) {
        Dialog viewer = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(background);

        ImageView image = new ImageView(this);
        image.setImageURI(Uri.fromFile(file));
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        image.setAdjustViewBounds(true);
        root.addView(image, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        ImageButton close = new ImageButton(this);
        close.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        close.setColorFilter(textMain);
        close.setBackground(rounded(Color.argb(120, 21, 27, 45), dp(24), Color.argb(35, 255, 255, 255), 1));
        close.setOnClickListener(v -> viewer.dismiss());

        FrameLayout.LayoutParams closeParams = new FrameLayout.LayoutParams(dp(54), dp(54));
        closeParams.gravity = Gravity.TOP | Gravity.END;
        closeParams.setMargins(0, dp(30), dp(20), 0);
        root.addView(close, closeParams);

        viewer.setContentView(root);
        viewer.show();
    }

    private File getMonthDirectory(String month) {
        return new File(getFilesDir(), "schedules/2026/" + month);
    }

    private TextView label(String text) {
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextColor(textMuted);
        label.setTextSize(12);
        label.setTypeface(Typeface.DEFAULT_BOLD);
        label.setPadding(0, 0, 0, dp(8));
        return label;
    }

    private LinearLayout.LayoutParams matchWrapWithBottom(int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, bottom);
        return params;
    }

    private LinearLayout.LayoutParams buttonParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(52)
        );
        return params;
    }

    private String getDisplayName(Uri uri) {
        String result = "Selected image";
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0) {
                    result = cursor.getString(nameIndex);
                }
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    private GradientDrawable rounded(int color, int radius, int strokeColor, int strokeWidth) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        if (strokeWidth > 0) {
            drawable.setStroke(strokeWidth, strokeColor);
        }
        return drawable;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
