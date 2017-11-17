package com.kuyue.tinkertest.activity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.provider.DocFile;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.kuyue.tinkertest.R;
import com.tencent.tinker.lib.tinker.TinkerInstaller;

import java.io.File;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    private TextView mTextView, mDesc;
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final String path = getPatchPath(this);
        mDesc = (TextView) findViewById(R.id.desc);
        mDesc.setText(path);
        mTextView = (TextView) findViewById(R.id.tv);
        mButton = (Button) findViewById(R.id.btn);
        mTextView.setText("这不是bug");
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String patchPath = getPatchPath(MainActivity.this);
                DocFile df = new DocFile(new File(patchPath, "patch_signed_7zip.apk"));
                boolean exist = df.exists();
                boolean isFile = df.isFile();
                boolean canRead = df.canRead();
                if (exist && isFile && canRead) {
                    Log.d("PPP", "patch|" + df.getAbsolutePath() + "|exist");
                    TinkerInstaller.onReceiveUpgradePatch(getApplicationContext(),
                            df.getAbsolutePath());
                } else {
                    Log.d("PPP", "patch|" + df.getAbsolutePath() + "|not exist");
                }
            }
        });
    }

    String getPatchPath(Context context) {
        String path = null;
        File dir = null;

        path = getExternalFilesDir(context, Environment.DIRECTORY_PICTURES, "patch");
        dir = new File(path);
        if (dir != null && dir.exists() && dir.isDirectory()) {
            path = dir.getAbsolutePath();
            return path;
        }
        return path;
    }

    String getExternalFilesDir(Context context, String environment, String childOfEnvironment) {
        File dir = null;
        String path = null;
        try {
            dir = context.getExternalFilesDir(environment);
            if (dir == null) {
                path = context.getFilesDir() + File.separator + childOfEnvironment;
            } else {
                path = dir.getAbsolutePath() + File.separator + childOfEnvironment;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        ensureDirectoryExistAndAccessable(path);
        return path;
    }

    boolean ensureDirectoryExistAndAccessable(String path) {
        if (path == null || path.length() == 0) {
            return false;
        }
        File target = new File(path);
        if (!target.exists()) {
            target.mkdirs();
            chmodCompatV23(target, 0755);
            return true;
        } else if (!target.isDirectory()) {
            return false;
        }

        chmodCompatV23(target, 0755);
        return true;
    }

    int chmodCompatV23(File path, int mode) {
        if (Build.VERSION.SDK_INT > 23) {
            return 0;
        }
        return chmod(path, mode);
    }

    int chmod(File path, int mode) {
        @SuppressWarnings("rawtypes")
        Class fileUtils;
        Method setPermissions = null;
        try {
            fileUtils = Class.forName("android.os.FileUtils");
            setPermissions = fileUtils.getMethod("setPermissions",
                    String.class, int.class, int.class, int.class);
            return (Integer) setPermissions.invoke(null, path.getAbsolutePath(),
                    mode, -1, -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
