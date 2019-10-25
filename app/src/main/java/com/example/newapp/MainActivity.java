package com.example.newapp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.provider.Telephony;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

        private static final int MSG_SHOW_PROGRESS = 11;
        private static final int MSG_SHOW_IMAGE = 22;

        private static final int START_NUM = 1;
        private static final int ADDING_NUM = 2;
        private static final int ENDING_NUM = 3;
        private static final int CANCEL_NUM = 4;
        private static final String DOWNLOAD_URL = "https://desk-fd.zol-img.com.cn/t_s1920x1080c5/g5/M00/07/07/ChMkJlXw8QmIO6kEABYKy-RYbJ4AACddwM0pT0AFgrj303.jpg";
        private MyHandler myHandler = new MyHandler(this);
        private MyUIHandler uiHandler = new MyUIHandler(this);
        private ProgressBar mpg;
        private TextView mtvTv,mtvTv2;
        private Button mbtnDuo, mbtnYi, mbtnHandler, mbtnAysncTask, mbtnOther;
        private ImageView mivImg;

        private CalculateThread calculateThread;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);



            mpg = findViewById(R.id.pb_main_pro1);
            mtvTv2 = findViewById(R.id.mtv_tv2);
            mtvTv = findViewById(R.id.mtv_tv);
            mbtnDuo = findViewById(R.id.mbtn_duo);
            mbtnYi = findViewById(R.id.mbtn_yi);
            mbtnHandler = findViewById(R.id.mbtn_Handler);
            mbtnAysncTask = findViewById(R.id.mbtn_AsyncTask);
            mbtnOther = findViewById(R.id.mbtn_other);
            mivImg = findViewById(R.id.mIv_img);


        }

        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.mbtn_duo:
                    calculateThread = new CalculateThread();
                    calculateThread.start();
                    break;
                case R.id.mbtn_yi:
                    new Thread(new TicketRunnable()).start();
                    break;
                case R.id.mbtn_Handler:
                    new Thread(new DownloadImageFetcher(DOWNLOAD_URL)).start();
                    break;
                case R.id.mbtn_AsyncTask:
                    new MyAsyncTask().execute("https://guzhaoyuan99.oss-cn-beijing.aliyuncs.com/view/2019-10-19-2.jpg");//传递进入参数

//             mTask.execute();
//             mTask.cancel(true);
//
                    break;
                case R.id.mbtn_other:
                    break;
            }
        }

    private class TicketRunnable implements Runnable {


        public TicketRunnable() {
        }
        private int ticket = 0;

        @Override
        public void run() {
            for (int i = 0; i <=100; i++) {
                //添加同步快
                synchronized (this) {
                    ticket = ticket +i;
                    try {
                        //通过睡眠线程来模拟出最后一张票的抢票场景
                        Thread.sleep(10);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            mtvTv2.setText("结果为:"+ticket);
        }
    }



        class CalculateThread extends Thread {
            @Override
            public void run() {
                int result = 0;
                boolean isCancel = false;
                myHandler.sendEmptyMessage(START_NUM);
                for (int i = 0; i <= 100; i++) {

                    try {
                        Thread.sleep(100);
                        result += i;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        isCancel = true;
                        break;
                    }

                    if (i % 5 == 0) {
                        Message msg = Message.obtain();
                        msg.what = ADDING_NUM;
                        msg.arg1 = i;
                        myHandler.sendMessage(msg);
                    }
                }
                if (!isCancel) {
                    Message msg = myHandler.obtainMessage();
                    msg.what = ENDING_NUM;
                    msg.arg1 = result;
                    myHandler.sendMessage(msg);
                }

            }
        }

        private class DownloadImageFetcher implements Runnable {
            private String imgUrl;

            public DownloadImageFetcher(String strUrl) {
                this.imgUrl = strUrl;
            }

            @Override
            public void run() {
                InputStream in = null;
                uiHandler.obtainMessage(MSG_SHOW_PROGRESS).sendToTarget();
                try {
                    URL url  = new URL(imgUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    in = connection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(in);
                    Message msg = uiHandler.obtainMessage();
                    msg.what=MSG_SHOW_IMAGE;
                    msg.obj = bitmap;
                    uiHandler.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    if (in != null){
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }


        static class MyUIHandler extends Handler {
            private WeakReference<Activity> ref;

            public MyUIHandler(Activity activity) {
                this.ref = new WeakReference<>(activity);
            }

            @Override
            public void handleMessage(Message msg) {

                super.handleMessage(msg);
                MainActivity activity = (MainActivity) ref.get();
                if (activity == null) {
                    return;
                }
                switch (msg.what) {

                    case MSG_SHOW_PROGRESS:
                        activity.mpg.setVisibility(View.VISIBLE);
                        break;
                    case MSG_SHOW_IMAGE:
                        activity.mpg.setVisibility(View.GONE);
                        activity.mivImg.setImageBitmap((Bitmap) msg.obj);
                        break;

                }
            }
        }


        static class MyHandler extends Handler {
            private WeakReference<Activity> ref;

            public MyHandler(Activity activity) {
                this.ref = new WeakReference<>(activity);
            }

            @Override
            public void handleMessage(Message msg) {

                super.handleMessage(msg);
                MainActivity activity = (MainActivity) ref.get();
                if (activity == null) {
                    return;
                }
                switch (msg.what) {
                    case START_NUM:
                        activity.mpg.setVisibility(View.VISIBLE);
                        break;
                    case ADDING_NUM:
                        activity.mpg.setProgress(msg.arg1);
                        activity.mtvTv.setText("计算已完成" + msg.arg1 + "%");
                        break;
                    case ENDING_NUM:
                        activity.mpg.setVisibility(View.GONE);
                        activity.mtvTv.setText("计算已完成，结果为：" + msg.arg1);
                        activity.myHandler.removeCallbacks(activity.calculateThread);
                        break;
                    case CANCEL_NUM:
                        activity.mpg.setProgress(0);
                        activity.mpg.setVisibility(View.GONE);
                        activity.mtvTv.setText("计算已取消");
                        break;
                }
            }
        }

        class MyAsyncTask extends AsyncTask<String, Void, Bitmap> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mpg.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                mpg.setVisibility(View.GONE);
                mivImg.setImageBitmap(bitmap);
            }

            @Override
            protected Bitmap doInBackground(String... params) {
                // 取出参数 params
                String url = params[0];
                Bitmap bitmap = null;
                URLConnection connection;
                InputStream is;
                try {
                    //访问网络的操作
                    connection = new URL(url).openConnection();
                    is = connection.getInputStream();
                    BufferedInputStream bis = new BufferedInputStream(is);
                    //解析输入流
                    bitmap = BitmapFactory.decodeStream(bis);
                    is.close();
                    bis.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                //返回 bitmap
                return bitmap;
            }
        }


    }

