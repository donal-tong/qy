/*
 * 官网地站:http://www.ShareSDK.cn
 * 技术支持QQ: 4006852216
 * 官方微信:ShareSDK   （如果发布新版本的话，我们将会第一时间通过微信将版本更新内容推送给您。如果使用过程中有任何问题，也可以通过微信与我们取得联系，我们将会在24小时内给予回复）
 *
 * Copyright (c) 2013年 ShareSDK.cn. All rights reserved.
 */

package com.vikaa.wecontact.wxapi;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.umeng.socialize.view.WXCallbackActivity;

import config.CommonValue;
import tools.AppManager;
import tools.UIHelper;
import ui.AppActivity;
import ui.LoginCode1;


public class WXEntryActivity extends WXCallbackActivity implements  IWXAPIEventHandler{

    private IWXAPI api;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        api = WXAPIFactory.createWXAPI(this, CommonValue.APP_ID, false);
        api.handleIntent(getIntent(), this);
    }

    @Override
    public void onReq(BaseReq req) {
    }

    @Override
    public void onResp(BaseResp resp) {
        switch (resp.getType()){
            case ConstantsAPI.COMMAND_SENDAUTH:
                switch (resp.errCode) {
                    case BaseResp.ErrCode.ERR_OK:
                        SendAuth.Resp resp1 = (SendAuth.Resp)resp;
                        Intent intent = new Intent(CommonValue.ACTION_WECHAT_CODE);
                        intent.putExtra("code", resp1.code);
                        sendBroadcast(intent);
                        WXEntryActivity.this.finish();
                        break;
                    case BaseResp.ErrCode.ERR_USER_CANCEL:
                        WXEntryActivity.this.finish();
                        break;
                    case BaseResp.ErrCode.ERR_AUTH_DENIED:
                    	UIHelper.ToastMessage(WXEntryActivity.this, "微信授权拒绝", Toast.LENGTH_SHORT);
                        WXEntryActivity.this.finish();
                        break;
                    default:
                        break;
                }
                break;
            default:
                WXEntryActivity.this.finish();
                break;
        }

    }

}
