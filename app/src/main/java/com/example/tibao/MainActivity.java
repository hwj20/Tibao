package com.example.tibao;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.hjq.toast.ToastUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static com.example.tibao.CameraState.CROP_REQUEST_CODE;
import static com.example.tibao.CameraState.RESULT_CAMERA_IMAGE;
import static com.example.tibao.CameraState.RESULT_LOAD_IMAGE;

//import org.apache.http.*;
//import org.apache.commons.*;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "CameraActivity";
    private String mCurrentPhotoPath = "";
    private final String requestUrl = "http://192.168.1.101/";
//    String requestUrl = "http://101.201.35.173/";     // tibao.com
    private final int port = 8000;
    private Context mContext;
    private ImageButton button_camera;
    private RecyclerView recyclerView;
    private String dataset[] = {"news1: guess what", "news2: a hill", "news3: we make it", "news4: here's a dog",
    "news5: ?????????????????????????????????????????????????????????", "new6: ??????????????????"};
    private String answers[] = {"top secret", "top secret","top secret","top secret","bobo", "never"};
    private int imageId[] = {R.drawable.doge0,R.drawable.doge1,R.drawable.doge2,R.drawable.doge3,R.drawable.doge4,R.drawable.doge5};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();
        ToastUtils.init(getApplication());
        button_camera = findViewById(R.id.button_camera);
        button_camera.setOnClickListener(view -> showPopupWindow());
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) !=  PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
        }

        recyclerView = findViewById(R.id.recycle_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new RecycleViewAdapter(dataset, answers,imageId,this));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK ) {
            if ((requestCode == RESULT_LOAD_IMAGE || requestCode == RESULT_CAMERA_IMAGE) && null != data) {
                Uri selectedImage = data.getData();
                cropPhoto(selectedImage);
                ToastUtils.show("Load image ready");
            }
            if (requestCode == CROP_REQUEST_CODE && null != data) {
                Uri selectedImage = data.getData();
//                cropPhoto(selectedImage);
                Bundle extras = data.getExtras();
                Bitmap pic = extras.getParcelable("data");

//                String[] filePathColumn = {MediaStore.Images.Media.DATA};
//
//                Cursor cursor = getContentResolver().query(selectedImage,
//                        filePathColumn, null, null, null);
//                cursor.moveToFirst();
//
//                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//                final String picturePath = cursor.getString(columnIndex);
//
//                File f = new File(picturePath);
                Thread thread = new Thread(() -> {
                    String feedback = UploadUtil.uploadFile(saveMyBitmap(new BitmapDrawable(getResources(), pic)), requestUrl, port);
                    Log.d(TAG, "upload image");
                    runOnUiThread(() -> showAnswerWindow(feedback));

                });
                thread.start();
//                cursor.close();
            }
          else if (requestCode == RESULT_CAMERA_IMAGE){      // camera
                SimpleTarget target = new SimpleTarget<BitmapDrawable>() {

                    @Override
                    public void onLoadStarted(Drawable placeholder) {
                        super.onLoadStarted(placeholder);
                    }

                    @Override
                    public void onLoadFailed(Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                    }

                    @Override
                    public void onResourceReady(@NonNull @NotNull BitmapDrawable resource, @Nullable @org.jetbrains.annotations.Nullable Transition<? super BitmapDrawable> transition) {
                        ToastUtils.show("Camera image ready");

                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String feedback = UploadUtil.uploadFile(saveMyBitmap(resource), requestUrl, port);
                                Log.d(TAG, "upload camera image");
                                runOnUiThread(()->showAnswerWindow(feedback));
                            }
                        });
                        thread.start();
                    }
                };

                Glide.with(this).load(mCurrentPhotoPath)
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .override(1080, 1920)//????????????
                        .centerCrop()
                        .dontAnimate()
                        .into(target);

            }
        }
    }

    // ????????????
    private void cropPhoto(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        intent.putExtra("outputX", 350);
        intent.putExtra("outputY", 350);
        intent.putExtra("return-data", true);

        startActivityForResult(intent, CROP_REQUEST_CODE);
    }


    //???bitmap?????????png??????
    public File saveMyBitmap(BitmapDrawable mBitmapDrawable){
//        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        Log.d(TAG, "save bitmap");
        Bitmap mBitmap = mBitmapDrawable.getBitmap();
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = null;
        try {
            file = File.createTempFile(
                    generateFileName(),  /* prefix */
//                    "tmpImageUpload",
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );

            Log.d(TAG, file.getName());
            FileOutputStream out=new FileOutputStream(file);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 20, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return  file;
    }


    private void takeCamera(int num) {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != 			PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 3);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != 			PackageManager.PERMISSION_GRANTED) {
            ToastUtils.show("???????????????");
            return;
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent

        if (takePictureIntent.resolveActivity(mContext.getPackageManager()) != null) {
            // Create the File where the photo should go
//            File photoFile = null;
//            photoFile = createImageFile();
            Uri photoURI = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", createImageFile());
            // Continue only if the File was successfully created
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                    photoURI);
        }

        startActivityForResult(takePictureIntent, num);//????????????????????????????????????
    }


    private File createImageFile() {
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//        File storageDir = this.getDataDir();
        File image = null;
        try {
            image = File.createTempFile(
                    generateFileName(),  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (image != null) {
            mCurrentPhotoPath = image.getAbsolutePath();
        }
        return image;
    }

    public static String generateFileName() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        return imageFileName;
    }

    private void showPopupWindow(){
        View popView = View.inflate(this,R.layout.popup_window,null);
        Button bt_album = (Button) popView.findViewById(R.id.btn_pop_album);
        Button bt_camera = (Button) popView.findViewById(R.id.btn_pop_camera);
        Button bt_cancel = (Button) popView.findViewById(R.id.btn_pop_cancel);
        //??????????????????
        int weight = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels/3;

        final PopupWindow popupWindow = new PopupWindow(popView,weight,height);
//        popupWindow.setAnimationStyle(R.style.anim_popup_dir);
        popupWindow.setFocusable(true);
        //????????????popupWindow??????
        popupWindow.setOutsideTouchable(true);

        bt_album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RESULT_LOAD_IMAGE);
                popupWindow.dismiss();

            }
        });
        bt_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeCamera(RESULT_CAMERA_IMAGE);
                popupWindow.dismiss();

            }
        });
        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();

            }
        });
        //popupWindow???????????????????????????
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1.0f;
                getWindow().setAttributes(lp);
            }
        });
        //popupWindow???????????????????????????
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        getWindow().setAttributes(lp);
        popupWindow.showAtLocation(popView, Gravity.BOTTOM,0,50);

    }

    public void showAnswerWindow(String answer){
        String decode = ""+answer;
//        try {
//            decode = new String(answer.getBytes(StandardCharsets), StandardCharsets.UTF_8);
//
//        }catch (UnsupportedEncodingException e){
//            Log.e(TAG, e.toString());
//        }
        View popView = View.inflate(this,R.layout.popup_answer_window,null);

        TextView feedText = (TextView) popView.findViewById(R.id.feedback_text);

        //??????????????????
        int weight = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels*2 /3;

        final PopupWindow popupWindow = new PopupWindow(popView,weight,height);
//        popupWindow.setAnimationStyle(R.style.anim_popup_dir);
        popupWindow.setFocusable(true);
        //????????????popupWindow??????
        popupWindow.setOutsideTouchable(true);

        feedText.setText(decode);
        Log.d(TAG, answer+'\n'+decode);

        //popupWindow???????????????????????????
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1.0f;
                getWindow().setAttributes(lp);
            }
        });
        //popupWindow???????????????????????????
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        getWindow().setAttributes(lp);
        popupWindow.showAtLocation(popView, Gravity.BOTTOM,0,50);

    }}