package com.phy.otatest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;
import com.phy.ota.sdk.ble.BandUtil;
import com.phy.ota.sdk.ble.Device;
import com.phy.ota.sdk.ble.ScanDeviceCallback;
import com.phy.ota.sdk.utils.BleHelper;
import com.phy.ota.sdk.utils.KLog;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    //权限请求码
    private static final int REQUEST_PERMISSION_CODE = 9527;
    //打开蓝牙请求码
    private static final int REQUEST_ENABLE_BLUETOOTH = 100;

    //列表视图
    private RecyclerView rvDevice;
    //设备列表
    private List<Device> deviceList = new ArrayList<>();
    //设备列表适配器
    private DeviceAdapter deviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initDeviceList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        deviceList.clear();
        deviceAdapter.notifyDataSetChanged();
        //检查Android版本
        checkAndroidVersion();
    }

    /**
     * 初始化设备列表
     */
    private void initDeviceList() {
        rvDevice = findViewById(R.id.rv_device);
        deviceAdapter = new DeviceAdapter(R.layout.item_device_rv, deviceList);
        rvDevice.setLayoutManager(new LinearLayoutManager(this));
        //item点击事件
        deviceAdapter.setOnItemClickListener((adapter, view, position) -> {
            //跳转页面
            jumpActivity(deviceList.get(position).getRealName(),deviceList.get(position).getDevice().getAddress());
        });
        rvDevice.setAdapter(deviceAdapter);
    }

    /**
     * 跳转页面
     */
    private void jumpActivity(String realName, String macAddress) {
        //停止扫描
        BandUtil.getBandUtil(this).stopScanDevice();
        Intent intent = new Intent(this, AutoActivity.class);
        intent.putExtra("realName", realName);
        intent.putExtra("macAddress", macAddress);
        startActivity(intent);
    }

    /**
     * 添加到设备列表
     * @param device 蓝牙设备
     */
    private void addDeviceList(Device device) {
        if (device.getDevice().getName() == null) {
            return;
        }
        if (!deviceList.contains(device)) {
            deviceList.add(device);
            //刷新列表适配器
            deviceAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 蓝牙是否打开
     */
    private void blueToothIsOpen() {
        if (BandUtil.isBleOpen(this)) {
            //开始扫描设备
            scanDevice();
        } else {
            showMsg("请打开蓝牙");
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BLUETOOTH);
        }
    }

    /**
     * 检查Android版本
     */
    private void checkAndroidVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Android 6.0及以上动态请求权限
            requestPermission();
        } else {
            //不需要请求权限
            blueToothIsOpen();
        }
    }

    /**
     * 扫描蓝牙设备
     */
    private void scanDevice() {
        //设置扫描回调
        BandUtil.setBleCallBack(new ScanDeviceCallback() {
            @Override
            public void onScanDevice(BluetoothDevice device, int rssi, byte[] scanRecord) {
                //通过扫描记录解析返回设备真实名称
                String realName = BleHelper.parseDeviceName(scanRecord);
                //Log.d(TAG, "设备名称：" + realName + ", 信号强度: " + rssi);
                addDeviceList(new Device(device, rssi, 1, realName));
            }

            @Override
            public void onConnectDevice(boolean connect) {
                KLog.i(TAG, "connect change");
            }
        });
        //开始扫描
        BandUtil.getBandUtil(this).scanDevice();
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_CODE)
    private void requestPermission() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            //有权限
            blueToothIsOpen();
        } else {
            // 没有权限
            EasyPermissions.requestPermissions(this, "需要权限", REQUEST_PERMISSION_CODE, perms);
        }
    }

    private void showMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 结果返回
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == RESULT_OK) {
                //扫描设备
                scanDevice();
            } else {
                showMsg("蓝牙打开失败，你将无法进行设备扫描。");
            }
        }
    }
}