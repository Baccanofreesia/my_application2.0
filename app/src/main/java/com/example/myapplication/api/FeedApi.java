package com.example.myapplication.api;

import android.os.Looper;
import android.os.Handler;
import android.util.Log;

import androidx.compose.foundation.text.Handle;

import com.example.myapplication.model.FeedResponse;
import com.google.gson.Gson;


import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FeedApi {
    private static  final String TAG = "FeedApi";
    private static final String BASE_URL="https://college-training-camp.bytedance.com/feed/";
    private static final OkHttpClient client=new OkHttpClient.Builder()
            .addInterceptor(new TimingInterceptor())//zhuyi
            .build();
    private static final Gson gson=new Gson();
    private static final Handler mainHandler=new Handler(Looper.getMainLooper());
    private static class TimingInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request=chain.request();
            long startTime=System.currentTimeMillis();
            Log.d(TAG,"开始请求: "+request.url());
            try{
                Response response=chain.proceed(request);
                long duration=System.currentTimeMillis()-startTime;
                Log.d(TAG,"请求完成: "+request.url()+"| 耗时: "+duration+"ms | 状态码: "+response.code());
                return response;
            }catch(Exception e){
                long duration=System.currentTimeMillis()-startTime;
                Log.e(TAG,"请求失败: "+request.url()+"| 耗时: "+duration+"ms | 错误: "+e.getMessage());
                throw e;
            }
        }
    }
    public static void getFeedList(int count, boolean acceptVideoClip, ApiCallback<FeedResponse>callback){
        String jsonBody=String.format(
                "{\"count\":%d,\"accept_video_clip\":%b}",
                count,
                acceptVideoClip
        );
        RequestBody requestBody= RequestBody.create(jsonBody, MediaType.parse("application/json"));
        Request request=new Request.Builder()
                .url(BASE_URL)
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(new Callback(){
            @Override
            public void onFailure(Call call, IOException e){
                mainHandler.post(()->callback.onError(e));
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException{
                if(response.isSuccessful()&&response.body()!=null){
                    try{
                        String json=response.body().string();
                        Log.d(TAG,"响应数据: "+json);
                        FeedResponse feedResponse = gson.fromJson(json,FeedResponse.class);
                        if(feedResponse.getStatusCode()==0){
                            mainHandler.post(()->callback.onSuccess(feedResponse));
                        }else{
                            mainHandler.post(()->callback.onError(new Exception("API返回错误: "+feedResponse.getStatusCode())));
                        }
                    }catch (Exception e){
                        Log.e(TAG,"解析失败",e);
                        mainHandler.post(()->callback.onError(e));
                    }
                }else{
                    mainHandler.post(()->callback.onError(new IOException("Response not successful: "+response.code())));
                }

            }
        });
    }
    public interface ApiCallback<T>{
        void onSuccess(T data);
        void onError(Exception e);
    }
}
