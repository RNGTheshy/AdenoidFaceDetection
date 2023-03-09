package com.nino.myapplication.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 创建人：xuqing
 * 创建时间：2020年8月25日16:42:38
 * 类说明：图片加载类
 */
public class ImageUtils {
    //加载网络图片
    public static void showImage(final Activity context, final String url, final ImageView
            imageView) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Bitmap bitmap = getImageBitmap(url);
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(bitmap);
                    }
                });

            }
        }).start();
    }

    //将图片URL地址转换成Bitmap
    public static Bitmap getImageBitmap(String url) {
        URL imgUrl = null;
        Bitmap bitmap = null;
        try {
            imgUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) imgUrl
                    .openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    // 将uri 转换成path路径
    @SuppressLint("Range")
    public static String getImagePath(Uri uri, String selection, Activity activity) {
        String path = null;
        Cursor cursor = activity.getContentResolver().query(uri,
                null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }

            cursor.close();
        }
        return path;
    }

    public static String handleImageOnKitKat(Intent data, Activity activity) {
        String imagePath = null;
        Uri uri = data.getData();

        if (DocumentsContract.isDocumentUri(activity, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                //Log.d(TAG, uri.toString());
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = ImageUtils.getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, activity);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                //Log.d(TAG, uri.toString());
                Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(docId));
                imagePath = ImageUtils.getImagePath(contentUri, null, activity);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            //Log.d(TAG, "content: " + uri.toString());
            imagePath = getImagePath(uri, null, activity);
        }
        return imagePath;
    }

    /**
     * 裁剪图片
     * * https://www.jianshu.com/p/3b92a0e30828
     */
    public static void cropPhoto(Uri uri, String imagePath, Activity activity) {
        //在7.0以上系统裁剪完毕之后，会提示“无法保存经过裁剪的图片”
        //这是因为，我们在7.0以上跨文件传输uri时候，需要用FileProvider,但是这里需要用
        //Uri.fromFile(file)生成的，而不是使用FileProvider.getUriForFile
        //intent.putExtra("set-as-wallpaper",true); 默认是false,当你弄成true的时候，你就会发现打开不是裁剪的，而是设置为壁纸的操作。
        // intent.putExtra("return-data", true);下面就可以获取到该bitmap
        // if (data != null && data.getParcelableExtra("data") != null) {
        //                mStream = new ByteArrayOutputStream();
        //                mBitmap = data.getParcelableExtra("data");
        //                mBitmap.compress(Bitmap.CompressFormat.PNG, 100, mStream);
        //                /**图片可以应用了*/
        //                /**接下来就是上传到服务器*/
        //                File files = creatFile(mBitmap);//变成文件
        //                ...后续根据需要来...
        //}
        Uri contentUri = Uri.fromFile(new File(imagePath));
        Intent intent = new Intent("com.android.camera.action.CROP");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //Android 7.0需要临时添加读取Url的权限， 添加此属性是为了解决：调用裁剪框时候提示：图片无法加载或者加载图片失败或者无法加载此图片
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");//发送裁剪信号，去掉也能进行裁剪
        intent.putExtra("scale", true);// 设置缩放
        intent.putExtra("scaleUpIfNeeded", true);// 去黑边
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        //上述两个属性控制裁剪框的缩放比例。
        //当用户用手拉伸裁剪框时候，裁剪框会按照上述比例缩放。
        intent.putExtra("outputX", 300);//属性控制裁剪完毕，保存的图片的大小格式。
        intent.putExtra("outputY", 300);//你按照1:1的比例来裁剪的，如果最后成像是800*400，那么按照2:1的样式保存，
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());//输出裁剪文件的格式
        intent.putExtra("return-data", true);//是否返回裁剪后图片的Bitmap
        intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);//设置输出路径
        activity.startActivityForResult(intent, 1);
    }

}