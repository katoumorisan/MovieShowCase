package com.yzl.movieshowcase.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * <B>全局捕获异常</B>
 *
 * @author <B>设计人员 : </B>yzl
 *         <P>
 * @version <B>版本信息 : </B>1.0.0.0
 *          <P>
 *          <B>日期 : </B>2017年9月12日
 *          <P>
 */
public class CrashHandler implements UncaughtExceptionHandler {

    /**
     * Tag
     */
    private static final String TAG ="MyCrash";
    /**
     * 上下文参数
     */
    private Context mContext;
    /**
     * 系统默认的UncaughtException处理器
     */
    private UncaughtExceptionHandler mDefaultHandler;
    /**
     * 实例
     */
    private static CrashHandler instance = new CrashHandler();
    /**
     * 用来存储设备信息和异常信息
     */
    private Map<String, String> infos = new HashMap<String, String>();
    /**
     * 格式化日期, 作为日志文件名的一部分
     */
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 保证只有一个实例
     */
    private CrashHandler(){

    }

    /**
     * 获取单例
     * @return 单例
     */
    public static CrashHandler getInstance(){
        return instance;
    }

    /**
     * 初始化
     * @param context 上下文参数
     */
    public void init(Context context) {
        mContext = context;
        //获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        //设置该CrashHandler为程序默认的处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, ex);

        } else {
            SystemClock.sleep(3000);
            Process.killProcess(Process.myPid());
            System.exit(1);
        }
    }

    /**
     * 自定义错误处理, 收集错误信息, 发送错误报告等操作在此完成
     * @param ex
     * @return
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        try {
            new Thread(){
                public void run() {
                    Looper.prepare();
                    Toast.makeText(mContext, "an error occur", Toast.LENGTH_SHORT).show();;
                    Looper.loop();
                };
            }.start();

            //收集设备参数信息
            collectDeviceInfo(mContext);
            //保存日志文件
            saveCrashInfoFile(ex);
            SystemClock.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * 收集设备参数信息
     * @param ctx 上下文参数
     */
    public void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
        } catch (NameNotFoundException e) {
            Log.e(TAG, "an error occured when collect package info", e);
        }

        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
            } catch (Exception e) {
                Log.e(TAG, "an error occured when collect crash info", e);
            }
        }
    }

    /**
     * 保存错误信息到文件中
     * @param ex 异常
     * @return 返回文件名称
     * @throws Exception exception
     */
    private String saveCrashInfoFile(Throwable ex) throws Exception {
        StringBuffer sb = new StringBuffer();
        try {
            SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String date = sDateFormat.format(new Date());
            sb.append("\r\n" + date + "\n");
            for (Map.Entry<String, String> entry : infos.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                sb.append(key + "=" + value + "\n");
            }

            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            ex.printStackTrace(printWriter);
            Throwable cause = ex.getCause();
            while (cause != null) {
                cause.printStackTrace(printWriter);
                cause = cause.getCause();
            }

            printWriter.flush();
            printWriter.close();
            String result = writer.toString();
            sb.append(result);

        } catch (Exception e) {
            Log.e(TAG, "an error occured while writing file...", e);
            sb.append("an error occured while writing file...\r\n");

        }
        writeFile(sb.toString());
        return null;
    }

    private String writeFile(String string) throws Exception {
        String time = formatter.format(new Date());
        String fileName = "crash-" + time + ".txt";
        File dir = new File(getPath());
        if (!dir.exists()) {
            dir.mkdirs();
        }

        FileOutputStream fos = new FileOutputStream(getPath() + fileName, true);
        fos.write(string.getBytes());
        fos.flush();
        fos.close();
        return fileName;
    }

    private String getPath(){
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "movieSearch" + File.separator;
    }

}
