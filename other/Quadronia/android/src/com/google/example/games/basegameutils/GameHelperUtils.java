package com.google.example.games.basegameutils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import northern.captain.quadronia.android.R;

/**
 * Created by btco on 2/10/14.
 */
class GameHelperUtils {
    public static final int R_UNKNOWN_ERROR = 0;
    public static final int R_SIGN_IN_FAILED = 1;
    public static final int R_APP_MISCONFIGURED = 2;
    public static final int R_LICENSE_FAILED = 3;

    private final static String[] FALLBACK_STRINGS = {
            "*Unknown error.",
            "*Failed to sign in. Please check your network connection and try again.",
            "*The application is incorrectly configured. Check that the package name and signing certificate match the client ID created in Developer Console. Also, if the application is not yet published, check that the account you are trying to sign in with is listed as a tester account. See logs for more information.",
            "*License check failed."
    };

    private final static int[] RES_IDS = {
            R.string.gamehelper_unknown_error, R.string.gamehelper_sign_in_failed,
            R.string.gamehelper_app_misconfigured, R.string.gamehelper_license_failed
    };

    static String getAppIdFromResource(Context ctx) {
        try {
            Resources res = ctx.getResources();
            String pkgName = ctx.getPackageName();
            int res_id = res.getIdentifier("app_id", "string", pkgName);
            return res.getString(res_id);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "??? (failed to retrieve APP ID)";
        }
    }

    static String getSHA1CertFingerprint(Context ctx) {
        try {
            Signature[] sigs = ctx.getPackageManager().getPackageInfo(
                    ctx.getPackageName(), PackageManager.GET_SIGNATURES).signatures;
            if (sigs.length == 0) {
                return "ERROR: NO SIGNATURE.";
            } else if (sigs.length > 1) {
                return "ERROR: MULTIPLE SIGNATURES";
            }
            byte[] digest = MessageDigest.getInstance("SHA1").digest(sigs[0].toByteArray());
            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < digest.length; ++i) {
                if (i > 0) {
                    hexString.append(":");
                }
                byteToString(hexString, digest[i]);
            }
            return hexString.toString();

        } catch (PackageManager.NameNotFoundException ex) {
            ex.printStackTrace();
            return "(ERROR: package not found)";
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
            return "(ERROR: SHA1 algorithm not found)";
        }
    }

    static void byteToString(StringBuilder sb, byte b) {
        int unsigned_byte = b < 0 ? b + 256 : b;
        int hi = unsigned_byte / 16;
        int lo = unsigned_byte % 16;
        sb.append("0123456789ABCDEF".substring(hi, hi + 1));
        sb.append("0123456789ABCDEF".substring(lo, lo + 1));
    }

    static String getString(Context ctx, int whichString) {
        whichString = whichString >= 0 && whichString < RES_IDS.length ? whichString : 0;
        int resId = RES_IDS[whichString];
        try {
            return ctx.getString(resId);
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.w(GameHelper.TAG, "*** GameHelper could not found resource id #" + resId + ". " +
                "This probably happened because you included it as a stand-alone JAR. " +
                "BaseGameUtils should be compiled as a LIBRARY PROJECT, so that it can access " +
                "its resources. Using a fallback string.");
            return FALLBACK_STRINGS[whichString];
        }
    }
}
