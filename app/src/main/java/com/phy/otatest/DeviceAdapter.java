package com.phy.otatest;

import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.phy.ota.sdk.ble.Device;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 蓝牙设备适配器
 * @author llw
 */
public class DeviceAdapter extends BaseQuickAdapter<Device, BaseViewHolder> {

    public DeviceAdapter(int layoutResId, @Nullable List<Device> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, Device item) {
        holder.setText(R.id.tv_device_name, item.getRealName())
                .setText(R.id.tv_mac_address, item.getDevice().getAddress());
        ImageView ivRssi = holder.getView(R.id.iv_rssi);

        if (item.getRssi() <= 0 && item.getRssi() >= -60) {
            ivRssi.setImageResource(R.mipmap.signal_4);
        } else if (-70 <= item.getRssi() && item.getRssi() < -60) {
            ivRssi.setImageResource(R.mipmap.signal_3);
        } else if (-80 <= item.getRssi() && item.getRssi() < -70) {
            ivRssi.setImageResource(R.mipmap.signal_2);
        } else {
            ivRssi.setImageResource(R.mipmap.signal_1);
        }
    }
}
