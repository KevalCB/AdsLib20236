package com.ads.mylibrary2023;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import unified.vpn.sdk.AuthMethod;
import unified.vpn.sdk.AvailableCountries;
import unified.vpn.sdk.Callback;
import unified.vpn.sdk.ClientInfo;
import unified.vpn.sdk.CompletableCallback;
import unified.vpn.sdk.Country;
import unified.vpn.sdk.HydraTransport;
import unified.vpn.sdk.OpenVpnTransport;
import unified.vpn.sdk.SessionConfig;
import unified.vpn.sdk.TrackingConstants;
import unified.vpn.sdk.UnifiedSdk;
import unified.vpn.sdk.User;
import unified.vpn.sdk.VpnException;

public class VPN

{
    public void INIT_SDK(Activity activity) {
        Ads_Preference adsDataPrefs;
        adsDataPrefs = new Ads_Preference(activity);
        ClientInfo clientInfo = ClientInfo.newBuilder()
                .addUrl(adsDataPrefs.get_VPN_URL())
                .carrierId(adsDataPrefs.get_VPN_ID())
                .build();

        UnifiedSdk.clearInstances();
        UnifiedSdk.getInstance(clientInfo);
        if (UnifiedSdk.getInstance().getBackend().isLoggedIn()) {
            START_VPN(activity);
        } else {
            VPN_SIGNING(activity);
        }
    }

    protected void VPN_SIGNING(Activity activity) {
        AuthMethod authMethod = AuthMethod.anonymous();
        UnifiedSdk.getInstance().getBackend().login(authMethod, new Callback<User>() {
            @Override
            public void success(User user) {
                Log.e("login to vpn", "success");
                START_VPN(activity);
            }

            @Override
            public void failure(VpnException e) {
                Log.e("login to vpn", "error");
            }
        });
    }

    protected void START_VPN(Activity activity) {
        List<String> fallbackOrder = new ArrayList<>();
        fallbackOrder.add(HydraTransport.TRANSPORT_ID);
        fallbackOrder.add(OpenVpnTransport.TRANSPORT_ID_TCP);
        fallbackOrder.add(OpenVpnTransport.TRANSPORT_ID_UDP);

        UnifiedSdk.getInstance().getBackend().countries(new Callback<AvailableCountries>() {
            @Override
            public void success(@NonNull AvailableCountries availableCountries) {
                List<Country> countries = availableCountries.getCountries();
                Log.e("MKK", "list:-" + countries);
//                String unused = countries.get(new Random().nextInt(countries.size() + 0 + 1) + 0).getCountry();

                UnifiedSdk.getInstance().getVpn().start(new SessionConfig.Builder()
                        .withReason(TrackingConstants.GprReasons.M_UI)
                        .withTransportFallback(fallbackOrder)
                        .withTransport(HydraTransport.TRANSPORT_ID)
                        .withVirtualLocation("CA")
                        .build(), new CompletableCallback() {

                    @Override
                    public void complete() {

                        Toast.makeText(activity, "VPN Connected", Toast.LENGTH_SHORT).show();
                        Log.e("vpn connect", "ok");
                    }

                    @Override
                    public void error(@NonNull VpnException e) {
                        Toast.makeText(activity, "VPN Not Connected", Toast.LENGTH_SHORT).show();
                        Log.e("vpn connect", "error");
                    }
                });
            }

            @Override
            public void failure(@NonNull VpnException e) {

                Toast.makeText(activity, "VPN Not Connected", Toast.LENGTH_SHORT).show();
                Log.e("vpn connect","fail");
            }
        });
    }
}
