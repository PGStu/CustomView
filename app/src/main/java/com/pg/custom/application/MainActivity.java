package com.pg.custom.application;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.pg.custom.application.color.ColorView;
import com.pg.custom.application.playview.HorizontalThumbnailView;
import com.pg.custom.application.playview.PlayView;
import com.pg.custom.application.playview.PlayCursorView;
import com.pg.custom.application.ruler.RulerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    PlayView timePlayView;
    PlayView playView2;
    PlayView playView3;
    PlayView playView4;
    private List<Bitmap> bitmapS;
    private TextView et;
    private TextView tv_demo;
    private RulerView rulerView;
    private HorizontalThumbnailView horBitmap;
    private PlayCursorView playlineview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        timePlayView = (PlayView)super.findViewById(R.id.lineView);
        playView2 = (PlayView)super.findViewById(R.id.lineView2);
        playView3 = (PlayView)super.findViewById(R.id.lineView3);
        playView4 = (PlayView)super.findViewById(R.id.lineView4);
        bitmapS = new ArrayList<>();
        for (int i = 0; i < 10;i++){
            bitmapS.add(BitmapFactory.decodeResource(getResources(),R.mipmap.ces));
        }
        timePlayView.setInitType(PlayView.LineViewType.DrawPointer,9000,bitmapS,Color.GREEN);
        playView2.setInitType(PlayView.LineViewType.DrawColorRect,9000,bitmapS,Color.GREEN);
        playView3.setInitType(PlayView.LineViewType.IsCoverRect,9000,bitmapS,Color.GREEN);
        playView4.setInitType(PlayView.LineViewType.CanRolling,9000,bitmapS,Color.GREEN);
        playView3.setCoverOutBorderColor(Color.BLACK);

//        timeLineView.setInitType(LineView.LineViewType.DrawPointer,Color.GREEN);
//        lineView2.setInitType(LineView.LineViewType.DrawColorRect,Color.GREEN);
//        lineView3.setInitType(LineView.LineViewType.IsCoverRect,Color.GREEN);
//        lineView4.setInitType(LineView.LineViewType.CanRolling,Color.GREEN);
//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                timeLineView.setBitmapList(bitmapS);
//                timeLineView.setTotalTime(9000);
//                lineView2.setBitmapList(bitmapS);
//                lineView2.setTotalTime(9000);
//                lineView3.setBitmapList(bitmapS);
//                lineView3.setTotalTime(9000);
//                lineView4.setBitmapList(bitmapS);
//                lineView4.setTotalTime(9000);
//            }
//        },2000);
//
//        lineView3.setCoverOutBorderColor(Color.BLACK);

        et = (TextView)findViewById(R.id.et);
        tv_demo = (TextView)findViewById(R.id.tv_demo);
        ColorView colorview = (ColorView)findViewById(R.id.colorview);
        colorview.setOnColorChangeListener(new ColorView.OnColorChangeListener() {
            @Override
            public void changeColor(int colorId) {
                tv_demo.setTextColor(colorId);
            }
        });
        rulerView = (RulerView)findViewById(R.id.rulerView);
        rulerView.setMaxValueAndPaintColor(36,Color.GRAY);
        timePlayView.setOnSelectTimeChangeListener(new PlayView.OnSelectTimeChangeListener() {
            @Override
            public void onTimeChange(float startTime, float endTime, float selectTime,float startTimePercent,float endTimePercent,float selectTimePercent) {
//                et.setText("您已选择"+selectTime+"秒");
            }
        });

        timePlayView.setOnPlayPointerChangeListener(new PlayView.OnPlayPointerChangeListener() {
            @Override
            public void onPlayPointerPosition(float playPointerPositionTime, float
                    playPointerPositionTimePercent) {
                et.setText(playPointerPositionTime+"秒开始"+playPointerPositionTimePercent);
            }
        });
        playView2.setOnScrollingPlayPositionListener(new PlayView.OnScrollingPlayPositionListener() {
            @Override
            public void onPlayPosition(float playPositionTime, float playPositionTimePercent) {
                et.setText(playPositionTime+"秒开始"+playPositionTimePercent);
            }
        });

        playView2.setOnStopPlayListener(new PlayView.OnStopPlayListener() {
            @Override
            public void stopPlay() {
                Toast.makeText(MainActivity.this,"停止播放了",Toast.LENGTH_SHORT).show();
            }
        });


        playView3.setOnSelectCoverTimeListener(new PlayView.OnSelectCoverTimeListener() {
            @Override
            public void onCoverSelectTime(float startTime, float endTime,float startTimePercent,float endTimePercent) {
                et.setText(startTime+"秒==="+endTime);
            }
        });

        playView4.setOnSelectTimeChangeListener(new PlayView.OnSelectTimeChangeListener() {
            @Override
            public void onTimeChange(float startTime, float endTime, float selectTime,float startTimePercent,float endTimePercent,float selectTimePercent) {
                et.setText("您已选择"+selectTime+"秒");
            }
        });
    }

    public void btnClick(View view) {
//        timeLineView.doRollingPlay();
//        lineView2.startPlay();
//        lineView3.doRollingPlay();
//        lineView4.doRollingPlay();

//        float percent = (float) 0.1;
//        lineView2.moveToPercent(percent);
//        percent = (float) (percent+0.05);
    }


    public void btnCancle(View view) {
        playView2.onPlayStop();
    }

    public void btnDraw(View view) {

        playView2.horizontalThumbnailView.startDrawColorRect(Color.parseColor("#800085d6"));
        playView2.horizontalThumbnailView.startPlay();
    }

    public void btnDraw2(View view) {

        playView2.starDrawColor(Color.parseColor("#80000000"));
    }

    public void btnDraw3(View view) {

        playView2.starDrawColor(Color.parseColor("#800000ff"));
    }

    public void btnDraw4(View view) {

        playView2.starDrawColor(Color.parseColor("#8000ff00"));
    }

    public void btnStopDraw(View view) {
        playView2.stopDrawColor();
    }

    public void btnClear(View view) {
        playView2.removeColor();
    }

}
