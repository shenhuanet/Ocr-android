package com.shenhua.ocr.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.shenhua.ocr.R;
import com.shenhua.ocr.fragment.TakePicFragment;
import com.shenhua.ocr.utils.Common;

import java.io.File;

import static com.shenhua.ocr.utils.Common.ACTION_CROP;
import static com.shenhua.ocr.utils.Common.ACTION_PICK;
import static com.shenhua.ocr.utils.Common.FRAGMENT_DIALOG;
import static com.shenhua.ocr.utils.Common.REQUEST_CROP_PICTURE;
import static com.shenhua.ocr.utils.Common.REQUEST_PICK_PICTURE;
import static com.shenhua.ocr.utils.Common.REQUEST_STORAGE_PERMISSION;

/**
 * Created by shenhua on 2017-10-30-0030.
 *
 * @author shenhua
 *         Email shenhuanet@126.com
 */
public class ChoosePicActivity extends AppCompatActivity {

    private Uri mOutUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestStoragePermission();
            return;
        }
        int action = getIntent().getIntExtra("action", -1);
        if (ACTION_PICK == action) {
            pick();
        } else if (ACTION_CROP == action) {
            Uri uri = getIntent().getParcelableExtra("uri");
            if (uri == null) {
                finishByError(getString(R.string.string_invalid_uri));
            }
            cropImage(uri);
        } else {
            finishByError(getString(R.string.string_invalid_action));
        }
    }

    /**
     * 选取照片
     */
    private void pick() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, REQUEST_PICK_PICTURE);
    }

    /**
     * 复制图片文件到文件
     *
     * @param uri tempUri
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void cropImage(Uri uri) {
        File file = Common.getTempPhoto(this);
        try {
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        mOutUri = Uri.fromFile(file);
        Intent intent = new Intent("com.android.camera.action.CROP");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("scale", true);
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mOutUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, REQUEST_CROP_PICTURE);
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new TakePicFragment.ConfirmationDialog().show(getSupportFragmentManager(), FRAGMENT_DIALOG);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.string_get_file_permission, Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED || resultCode == RESULT_FIRST_USER) {
            setResult(RESULT_CANCELED);
            ChoosePicActivity.this.finish();
            return;
        }
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_PICK_PICTURE:
                    cropImage(data.getData());
                    break;
                case REQUEST_CROP_PICTURE:
                    navRecognition();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 跳转至识别Activity
     */
    private void navRecognition() {
        startActivity(new Intent(this, RecognitionActivity.class).putExtra("temp", mOutUri));
        setResult(RESULT_OK);
        this.finish();
    }

    /**
     * 发生错误时触发并结束当前
     *
     * @param msg 错误消息
     */
    private void finishByError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        setResult(RESULT_CANCELED);
        finish();
    }

}
