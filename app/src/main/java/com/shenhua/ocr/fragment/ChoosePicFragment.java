package com.shenhua.ocr.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

import static android.app.Activity.RESULT_OK;

/**
 * Created by shenhua on 2017-10-30-0030.
 *
 * @author shenhua
 *         Email shenhuanet@126.com
 */
public class ChoosePicFragment extends Fragment {

    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 2;
    private static final int REQUEST_PICK_PICTURE = 11;
    private static final int REQUEST_TAKE_PICTURE = 12;
    private static final int REQUEST_CROP_PICTURE = 13;
    private static final String FRAGMENT_DIALOG = "dialog";
    private Uri mOutUri;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void requestStoragePermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new TakePicFragment.ConfirmationDialog().show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "需要获取文件访问权限,否则该功能无法使用", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_TAKE_PICTURE:

                    break;
                case REQUEST_PICK_PICTURE:
                    cropImage(data.getData());
                    break;
                case REQUEST_CROP_PICTURE:
                    Log.d("shenhuaLog -- " + TakePicFragment.class.getSimpleName(), "onActivityResult: >> " + mOutUri);
                    break;
                default:
                    break;
            }
        }
    }

    public void pick() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestStoragePermission();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, REQUEST_PICK_PICTURE);
    }

    public void cropImage(Uri uri) {
        File file = new File(getContext().getExternalCacheDir(), "temp.jpg");
        try {
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        Log.d("shenhuaLog -- " + TakePicFragment.class.getSimpleName(), "startCrop: >>> " + uri);
        mOutUri = Uri.fromFile(file);
//        Uri outUri = FileProvider.getUriForFile(getContext(), "com.shenhua.ocr.fileProvider", file);
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
        Log.d("shenhuaLog -- " + TakePicFragment.class.getSimpleName(), "startCrop: >>> " + mOutUri);
    }

    public void addCallback() {

    }
}




