/*
 * Copyright (C) 2018 crDroidAndroid Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.qs.tiles;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.service.quicksettings.Tile;

import com.android.systemui.R;
import com.android.systemui.plugins.qs.QSTile.BooleanState;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.SecureSetting;
import com.android.systemui.qs.tileimpl.QSTileImpl;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import static org.lineageos.internal.util.DeviceKeysConstants.*;

public class HWKeysTile extends QSTileImpl<BooleanState> {

    private final SecureSetting mSetting;
    private final Icon mIcon = ResourceIcon.get(R.drawable.ic_qs_hw_keys);

    public HWKeysTile(QSHost host) {
        super(host);

        mSetting = new SecureSetting(mContext, mHandler, Secure.HARDWARE_KEYS_DISABLE) {
            @Override
            protected void handleValueChanged(int value, boolean observedChange) {
                handleRefreshState(value);
            }
        };
    }

    @Override
    public boolean isAvailable() {
        final int deviceKeys = mContext.getResources().getInteger(
                com.android.internal.R.integer.config_deviceHardwareKeys);
        final boolean hasHomeKey = (deviceKeys & KEY_MASK_HOME) != 0;
        final boolean hasBackKey = (deviceKeys & KEY_MASK_BACK) != 0;
        final boolean hasMenuKey = (deviceKeys & KEY_MASK_MENU) != 0;
        final boolean hasAssistKey = (deviceKeys & KEY_MASK_ASSIST) != 0;
        final boolean hasAppSwitchKey = (deviceKeys & KEY_MASK_APP_SWITCH) != 0;

        return (hasHomeKey || hasBackKey || hasMenuKey || hasAssistKey || hasAppSwitchKey);
    }

    @Override
    public BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    protected void handleClick() {
        // Setting is reversed. Disabled: 1 and Enabled: 0.
        mSetting.setValue(mState.value ? 1 : 0);
        refreshState();
    }

    @Override
    public Intent getLongClickIntent() {
        return null;
    }

    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.quick_settings_hw_keys_label);
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        final int value = arg instanceof Integer ? (Integer)arg : mSetting.getValue();
        // Setting is reversed. Disabled: 1 and Enabled: 0.
        final boolean enable = value == 0;
        if (state.slash == null) {
            state.slash = new SlashState();
        }
        state.value = enable;
        state.label = mContext.getString(R.string.quick_settings_hw_keys_label);
        state.icon = mIcon;
        state.slash.isSlashed = !state.value;
        if (enable) {
            state.contentDescription =  mContext.getString(
                    R.string.accessibility_quick_settings_hw_keys_enabled);
            state.state = Tile.STATE_ACTIVE;
        } else {
            state.contentDescription =  mContext.getString(
                    R.string.accessibility_quick_settings_hw_keys_disabled);
            state.state = Tile.STATE_INACTIVE;
        }
    }

    @Override
    protected String composeChangeAnnouncement() {
        if (mState.value) {
            return mContext.getString(
                    R.string.accessibility_quick_settings_hw_keys_enabled);
        } else {
            return mContext.getString(
                    R.string.accessibility_quick_settings_hw_keys_disabled);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.AICP_METRICS;
    }

    @Override
    public void handleSetListening(boolean listening) {
        // Do nothing
    }
}
