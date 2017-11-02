package com.shenhua.ocr.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.shenhua.ocr.R;
import com.shenhua.ocr.activity.ChoosePicActivity;
import com.shenhua.ocr.widget.CameraPreview;
import com.shenhua.ocr.widget.PermissionDeclarationDialogFragment;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.shenhua.ocr.utils.Common.ACTION_CROP;
import static com.shenhua.ocr.utils.Common.ACTION_PICK;
import static com.shenhua.ocr.utils.Common.FRAGMENT_DIALOG;
import static com.shenhua.ocr.utils.Common.REQUEST_CAMERA_PERMISSION;
import static com.shenhua.ocr.utils.Common.REQUEST_CROP_PICTURE;
import static com.shenhua.ocr.utils.Common.REQUEST_PICK_PICTURE;

/**
 * Created by shenhua on 2017-10-19-0019.
 *
 * @author shenhua
 *         Email shenhuanet@126.com
 */
public class TakePicFragment extends Fragment implements ActivityCompat.OnRequestPermissionsResultCallback {

    @BindView(R.id.cameraView)
    CameraPreview mCameraView;
    private Unbinder mUnBinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_takepic, container, false);
        mUnBinder = ButterKnife.bind(this, mRootView);
        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
            return;
        }
        mCameraView.onResume();
    }

    @Override
    public void onPause() {
        mCameraView.onPause();
        super.onPause();
    }

    @OnClick({R.id.captureBtn, R.id.backBtn, R.id.albumBtn})
    void clicks(View view) {
        switch (view.getId()) {
            case R.id.captureBtn:
                mCameraView.takePicture(new CameraPreview.CapturePictureListener() {
                    @Override
                    public void onCapture(File file) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Uri uri = Uri.fromFile(file);
                        startActivityForResult(new Intent(getContext(), ChoosePicActivity.class)
                                .putExtra("action", ACTION_CROP)
                                .putExtra("uri", uri), REQUEST_CROP_PICTURE);
                    }
                });
                break;
            case R.id.backBtn:
                getFragmentManager().popBackStack();
                break;
            case R.id.albumBtn:
                startActivityForResult(new Intent(getContext(), ChoosePicActivity.class)
                        .putExtra("action", ACTION_PICK), REQUEST_PICK_PICTURE);
                break;
            default:
                break;
        }
    }

    private void requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            new PermissionDeclarationDialogFragment().show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), R.string.string_camera_permission, Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnBinder.unbind();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_CANCELED || resultCode == Activity.RESULT_FIRST_USER) {
            return;
        }
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_PICK_PICTURE || requestCode == REQUEST_CROP_PICTURE) {
                getFragmentManager().popBackStack();
            }
        }
    }

}
