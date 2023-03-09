package com.nino.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.annotation.ContentView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.nino.myapplication.utils.ImageUtils;

import java.io.File;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private BottomSheetDialog bottomSheet;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        init();
        onClick();
    }

    private void init() {
        bottomSheet = new BottomSheetDialog(this);//实例化BottomSheetDialog
        bottomSheet.setCancelable(true);//设置点击外部是否可以取消
        bottomSheet.setContentView(R.layout.dialog_photo);//设置对框框中的布局
    }

    private void onClick() {
        findViewById(R.id.face_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(MainActivity.this, ImageClassificationActivity.class);
                intent.putExtra(ImageClassificationActivity.INTENT_MODULE_ASSET_NAME,
                        "mobilenetV3small.ptl");
                intent.putExtra(ImageClassificationActivity.INTENT_INFO_VIEW_TYPE,
                        InfoViewFactory.INFO_VIEW_TYPE_IMAGE_CLASSIFICATION_QMOBILENET);
                startActivity(intent);
            }
        });
        findViewById(R.id.photo_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheet.show();//显示弹窗
            }
        });
        bottomSheet.findViewById(R.id.open_from_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        bottomSheet.findViewById(R.id.open_album).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    int hasWritePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    List<String> permissions = new ArrayList<String>();
                    if (hasWritePermission != PackageManager.PERMISSION_GRANTED) {
                        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    } else {
                        openPhoto(1);
                    }
                    if (!permissions.isEmpty()) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    }
                } else {
                    openPhoto(1);
                }
            }
        });
        bottomSheet.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheet.dismiss();
            }
        });
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private void openCamera() {
        if (PermissionChecker.checkSelfPermission(this, Manifest.permission.CAMERA) == PermissionChecker.PERMISSION_GRANTED) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    /**
     * 从相册中选择图片
     */
    private void openPhoto(int openPhotoCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, openPhotoCode);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode >= 1 && requestCode <= 5) {
            // 从相册返回的数据
            if (data != null) {
                getPhotoResult(requestCode, resultCode, data);
            }
        }
    }

    //处理获取到的图片文件
    private void getPhotoResult(int requestCode, int resultCode, Intent data) {
        // 得到图片的全路径
        if (requestCode == 1) {
            String imagePath;
            if (Build.VERSION.SDK_INT >= 19) {
                imagePath = ImageUtils.handleImageOnKitKat(data, this);
            } else {
                imagePath = ImageUtils.getImagePath(data.getData(), null, this);
            }

            if (!TextUtils.isEmpty(imagePath)) {
                Intent intent = new Intent(MainActivity.this, PhotoActivity.class);
                intent.putExtra("imagePath", imagePath);
                startActivity(intent);
            }
        }
    }


}
