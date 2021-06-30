package com.phy.otatest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.phy.ota.sdk.ble.UpdateFirmwareCallback;
import com.phy.ota.sdk.utils.OTASDKUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.phy.ota.sdk.constant.BaseConstant.FILE_HEX;
import static com.phy.ota.sdk.constant.BaseConstant.FILE_HEX16;
import static com.phy.ota.sdk.constant.BaseConstant.FILE_HEXE;
import static com.phy.ota.sdk.constant.BaseConstant.FILE_HEXE16;
import static com.phy.ota.sdk.constant.BaseConstant.FILE_RES;
import static com.phy.otatest.dialog.DialogHelper.*;

public class AutoActivity extends AppCompatActivity {


    private static final String TAG = AutoActivity.class.getSimpleName();
    //文件列表视图
    private RecyclerView rvFile;
    //文件列表
    private List<String> fileList = new ArrayList<>();
    //文件列表适配器
    private FileAdapter fileAdapter;

    private OTASDKUtils otasdkUtils;

    //文件路径
    private String filePath;
    //外部路径
    private String path = Environment.getExternalStorageDirectory().getPath();
    //设备mac地址
    private String macAddress;

    private Toolbar toolbar;
    private TextView tvDeviceName, tvMacAddress, tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto);

        initFileList();
    }

    /**
     * 初始化文件列表
     */
    private void initFileList() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        tvDeviceName = findViewById(R.id.tv_device_name);
        tvMacAddress = findViewById(R.id.tv_mac_address);
        tvStatus = findViewById(R.id.tv_status);
        rvFile = findViewById(R.id.rv_file);

        tvDeviceName.setText(getIntent().getStringExtra("realName"));
        tvMacAddress.setText(getIntent().getStringExtra("macAddress"));
        macAddress = getIntent().getStringExtra("macAddress");
        //列表适配器设置
        fileAdapter = new FileAdapter(R.layout.item_file_rv, fileList);
        rvFile.setLayoutManager(new LinearLayoutManager(this));
        //item点击事件
        fileAdapter.setOnItemClickListener((adapter, view, position) -> {
            //升级固件
            updateFirmware(position);
        });
        rvFile.setAdapter(fileAdapter);
        //搜索文件
        searchFile();
        //初始化固件升级
        initUpdateFirmware();
    }

    /**
     * 初始化固件升级
     */
    private void initUpdateFirmware() {
        //固件升级回调
        otasdkUtils = new OTASDKUtils(getApplicationContext(), new UpdateFirmwareCallback() {
            @Override
            public void onProcess(float process) {
                Log.d(TAG, "onProcess:" + process);
                int progress = Math.round(process);
                //设置当前进度
                runOnUiThread(() -> tvStatus.setText("Upgrade：" + progress + "%"));
            }

            @Override
            public void onUpdateComplete() {
                /*hideLoading();
                Looper.prepare();
                showUpdateResultDialog(AutoActivity.this,true, 0);
                Looper.loop();*/
                Log.d(TAG, "onUpdateComplete");
                runOnUiThread(() -> tvStatus.setText("UpgradeComplete"));

            }

            @Override
            public void onError(int code) {
                /*hideLoading();
                Looper.prepare();
                showUpdateResultDialog(AutoActivity.this,false, 0);
                Looper.loop();*/
                Log.d(TAG, "onError:" + code);
                runOnUiThread(() -> tvStatus.setText("UpgradeFailure: " + code));

            }
        });
    }


    /**
     * 搜索文件
     */
    private void searchFile() {
        fileList.clear();
        File file = new File(path);
        if (file.exists()) {
            File[] listFiles = file.listFiles();
            for (File f : listFiles) {
                if (f.getName().endsWith(FILE_HEX16)
                        || f.getName().endsWith(FILE_HEX)
                        || f.getName().endsWith(FILE_HEXE)
                        || f.getName().endsWith(FILE_RES)
                        || f.getName().endsWith(FILE_HEXE16)) {
                    fileList.add(f.getName());
                    fileAdapter.notifyDataSetChanged();
                }
            }
        } else {
            Toast.makeText(this, "sdcard not found", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 升级固件
     */
    private void updateFirmware(int position) {
        //showLoading(this);
        filePath = path + "/" + fileList.get(position);
        String fileName = fileList.get(position).toLowerCase();
        tvStatus.setText("Loading...");
        if (fileName.endsWith(FILE_HEX) || fileName.endsWith(FILE_HEX16)) {
            otasdkUtils.updateFirmware(macAddress, filePath, false);
        } else if (fileName.endsWith(FILE_HEXE16)) {
            otasdkUtils.updateFirmware(macAddress, filePath, true);
        } else {
            otasdkUtils.updateResource(macAddress, filePath);
        }
    }

}