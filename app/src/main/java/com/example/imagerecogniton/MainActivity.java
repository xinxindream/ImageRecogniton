package com.example.imagerecogniton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.imagerecogniton.model.GetDiscernResultResponse;
import com.example.imagerecogniton.network.NetCallBack;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.example.imagerecogniton.adapter.DiscernResultAdapter;
import com.example.imagerecogniton.util.Base64Util;
import com.example.imagerecogniton.util.FileUtil;
import com.example.imagerecogniton.network.APIService;
import com.example.imagerecogniton.network.ServiceGenerator;


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;


import retrofit2.Call;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    // 定义打开相册请求码
    private static final int OPEN_ALBUM_CODE = 100;
    // 定义打开相机请求码
    private static final int TAKE_PHOTO_CODE = 101;

    /**
     * 变量声明
     *
     * */
    // 显示图片--网络图片
    private ImageView ivPicture;
    // 进度条
    private ProgressBar pbLoading;
    // 底部弹窗
    private BottomSheetDialog bottomSheetDialog;
    // 弹窗视图
    private View bottomView;
    // 动态请求打开权限
    private RxPermissions rxPermissions;
    // 定义保存拍照后的图片
    private File outputImage;
    // Api服务
    private APIService apiService;
    // 鉴权Toeken
    private String accessToken;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 绑定xml控件id
        ivPicture = findViewById(R.id.iv_picture);
        pbLoading = findViewById(R.id.pb_loading);

        // 实例化底部弹窗
        bottomSheetDialog = new BottomSheetDialog(this);
        bottomView = getLayoutInflater().inflate(R.layout.dialog_bottom, null);

        // 实例化权限请求
        rxPermissions = new RxPermissions(this);

    }

    /**
     * 识别相册图片
     * @author: 潘孝飞
     * */
    @SuppressLint("CheckResult")
    public void IdentifyAlbumPictures(View view) {
        // 判断当前版本大于等于安卓6.0版本，需要具有权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 获取读写权限
            rxPermissions.request(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe(grant -> {
                        if (grant) {
                            //打开相册
                            openAlbum();
                        } else {
                            showMsg("未获取到权限");
                        }
                    });
        } else {
            openAlbum();
        }
    }

    /**
     * 识别拍照图片
     * @author: 潘孝飞
     * */
    @SuppressLint("CheckResult")
    public void IdentifyTakePhotoImage(View view) {
        // 判断当前版本大于等于安卓6.0版本，需要具有权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //请求相机权限
            rxPermissions.request(
                    Manifest.permission.CAMERA)
                    .subscribe(grant -> {
                        if (grant) {
                            //获得权限
                            turnOnCamera();
                        } else {
                            showMsg("未获取到权限");
                        }
                    });
        } else {
            turnOnCamera();
        }
    }

    /**
     * Toast提示
     * @author 徐淳
     * @param msg 内容
     */
    private void showMsg(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示识别的结果列表
     *
     * @param result
     */
    private void showDiscernResult(List<GetDiscernResultResponse.ResultBean> result) {
        bottomSheetDialog.setContentView(bottomView);
        bottomSheetDialog.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet).setBackgroundColor(Color.TRANSPARENT);
        RecyclerView rvResult = bottomView.findViewById(R.id.rv_result);
        DiscernResultAdapter adapter = new DiscernResultAdapter(R.layout.item_result_rv, result);
        rvResult.setLayoutManager(new LinearLayoutManager(this));
        rvResult.setAdapter(adapter);
        //隐藏加载
        pbLoading.setVisibility(View.GONE);
        //显示弹窗
        bottomSheetDialog.show();
    }

    /**
     * 打开相机
     */
    private void turnOnCamera() {
        SimpleDateFormat timeStampFormat = new SimpleDateFormat("HH_mm_ss");
        String filename = timeStampFormat.format(new Date());
        //创建File对象
        outputImage = new File(getExternalCacheDir(), "takePhoto" + filename + ".jpg");
        Uri imageUri;
        //FileProvider于安卓7.0引入，判断当前版本是否大于7.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // 转化所拍照片地址
            imageUri = FileProvider.getUriForFile(this,
                    "com.example.imagerecogniton.fileprovider", outputImage);
        } else {
            imageUri = Uri.fromFile(outputImage);
        }
        //隐式打开相机
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PHOTO_CODE);
    }

    /**
     * 打开相册
     */
    private void openAlbum() {
        //隐式打开相册
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, OPEN_ALBUM_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            pbLoading.setVisibility(View.VISIBLE);
            if (requestCode == OPEN_ALBUM_CODE) {
                //打开相册返回
                String[] filePathColumns = {MediaStore.Images.Media.DATA};
                final Uri imageUri = Objects.requireNonNull(data).getData();
                Cursor cursor = getContentResolver().query(imageUri, filePathColumns, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumns[0]);
                //获取图片路径
                String imagePath = cursor.getString(columnIndex);
                cursor.close();
                //识别
                localImageDiscern(imagePath);

            } else if (requestCode == TAKE_PHOTO_CODE) {
                //拍照返回
                String imagePath = outputImage.getAbsolutePath();
                //识别
                localImageDiscern(imagePath);
            }
        } else {
            showMsg("什么都没有");
        }
    }

    /**
     * 本地图片识别
     */
    private void localImageDiscern(String imagePath) {
        try {
            if (accessToken == null) {
                showMsg("获取AccessToken到null");
                return;
            }
            //通过图片路径显示图片
            Glide.with(this).load(imagePath).into(ivPicture);
            //按字节读取文件
            byte[] imgData = FileUtil.readFileByBytes(imagePath);
            //字节转Base64
            String imageBase64 = Base64Util.encode(imgData);
            //图像识别
            ImageDiscern(accessToken, imageBase64, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 图像识别请求
     *
     * @param token       token
     * @param imageBase64 图片Base64
     * @param imgUrl      网络图片Url
     */
    private void ImageDiscern(String token, String imageBase64, String imgUrl) {
        service.getDiscernResult(token, imageBase64, imgUrl).enqueue(new NetCallBack<GetDiscernResultResponse>() {
            @Override
            public void onSuccess(Call<GetDiscernResultResponse> call, Response<GetDiscernResultResponse> response) {
                if(response.body() == null){
                    showMsg("未获得相应的识别结果");
                    return;
                }
                List<GetDiscernResultResponse.ResultBean> result = response.body().getResult();
                if (result != null && result.size() > 0) {
                    //显示识别结果
                    showDiscernResult(result);
                } else {
                    pbLoading.setVisibility(View.GONE);
                    showMsg("未获得相应的识别结果");
                }
            }

            @Override
            public void onFailed(String errorStr) {
                pbLoading.setVisibility(View.GONE);
                Log.e(TAG, "图像识别失败，失败原因：" + errorStr);
            }
        });
    }
}