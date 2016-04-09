package funyaa.nyaa.meh;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class MySurfaceView extends SurfaceView {

    private SurfaceHolder surfaceHolder;
    private Bitmap bmpIcon;
    private MyThread myThread;
    int[] xPos ;//getWidth()/2;
    int[] yPos ;//getHeight()/2;
    float x = 100,y=100;
    float vx_main=0,vy_main=0;
    float ax_main=0,ay_main=0;
    int k = 2;
    int secs = 0;
    String time = "";
    boolean started = false;
    boolean stop_timer = false;
    double e = 0.1;
    double[] vx ;
    double[] vy ;
    double[] a  ;
    double[] theta ;
    double dt = Math.exp(-5);
    int lives=1000;
    int lives2=1000;
    Paint paint = new Paint();
    ArrayList<ArrayList<Point>> trails = new ArrayList<>();
    //    int deltaX = 5;
//    int deltaY = 5;
    int iconWidth;
    int iconHeight;
    Paint _paintSimple = new Paint();
    private android.os.Handler handler = new android.os.Handler();
    Paint _paintBlur = new Paint();
    Paint _paintSharp = new Paint();
    public MySurfaceView(Context context,int k) {
        super(context);
        xPos = new int[k];//getWidth()/2;
        yPos = new int[k];//getHeight()/2;
        vx = new double[k];
        vy = new double[k];
        a = new double[k];
        theta = new double[k];
        this.k = k;
        init();

    }
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
      /* do what you need to do */
//            foobar();
            if(stop_timer) return;
             secs++;
            int min = secs/60;
            time = "";
            if(min<1) time+="00 : ";
            else if(min<10) time+="0"+min+" : ";
            else time+=min+" : ";
            int s = secs%60;
            if(s<1) time+="00";
            else if(s<10) time+="0"+s;
            else time+=s;

      /* and here comes the "trick" */
            handler.postDelayed(this, 1000);
        }
    };
    public MySurfaceView(Context context,
                         AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MySurfaceView(Context context,
                         AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    public void update(float x,float y)
    {
//        this.x = (int)(getWidth()*(-x+90)/180);
//        this.y = (int)(getHeight()*(-y+90)/180);
        ax_main = -x;
        ay_main = -y;
    }
    private void init(){

        myThread = new MyThread(this);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.WHITE);
        _paintSimple.setAntiAlias(true);
        _paintSimple.setDither(true);
        _paintSimple.setColor(Color.argb(248, 255, 255, 255));
        _paintSimple.setStrokeWidth(10f);
        _paintSimple.setStyle(Paint.Style.STROKE);
        _paintSimple.setStrokeJoin(Paint.Join.ROUND);
        _paintSimple.setStrokeCap(Paint.Cap.ROUND);
        _paintSimple.setTextSize(200);
        _paintSimple.setTextAlign(Paint.Align.CENTER);
        _paintBlur.setColor(Color.argb(235, 74, 138, 255));
        _paintBlur.set(_paintSimple);
        _paintBlur.setStrokeWidth(30f);
        _paintSharp.setStrokeWidth(28f);
        _paintSharp.set(_paintSimple);
        _paintSharp.setStyle(Paint.Style.STROKE);
//        _paintSharp.setMaskFilter(new android.graphics.EmbossMaskFilter())
        _paintBlur.setMaskFilter(new BlurMaskFilter(15, BlurMaskFilter.Blur.NORMAL));

        surfaceHolder = getHolder();
        bmpIcon = BitmapFactory.decodeResource(getResources(),
                android.R.drawable.star_big_on);

        iconWidth = bmpIcon.getWidth();
        iconHeight = bmpIcon.getHeight();
        for(int i=0;i<k;i++)
        {
            xPos[i]=(i/3)*200;
            yPos[i]=(i*200)%800;
            trails.add(new ArrayList<Point>());
        }
//        Log.i("init","init");
        surfaceHolder.addCallback(new SurfaceHolder.Callback(){
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                myThread.setRunning(true);
                myThread.start();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder,
                                       int format, int width, int height) {
                // TODO Auto-generated method stub

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                boolean retry = true;
                myThread.setRunning(false);
                while (retry) {
                    try {
                        myThread.join();
                        retry = false;
                    } catch (InterruptedException e) {
                    }
                }
            }});
    }
    public void draw(Canvas canvas,int i) {
        Path path = new Path();
        boolean first = true;
        for(Point point : trails.get(i)){
            if(first){
                first = false;
                path.moveTo(point.x, point.y);
            }
            else{
                path.lineTo(point.x, point.y);
            }
        }
        canvas.drawPath(path, _paintBlur);
        canvas.drawPath(path, _paintSimple);
    }
    boolean timer_on=false;
    protected void update()
    {
        for(int i=0;i<k;i++)
        {
            double temp = (xPos[i]-x)*(xPos[i]-x)+(yPos[i]-y)*(yPos[i]-y);
            temp = Math.sqrt(temp);
            temp = (iconHeight-(int)temp);
            if(started)if(temp>0) {if(lives>0)lives-=50;else if(lives2>0)lives2-=20;else stop_timer=true;}
            a[i] = (xPos[i]-x+100)*(xPos[i]-x+100)+(yPos[i]-y+100)*(yPos[i]-y+100);
            a[i] = Math.sqrt(a[i])*(i+1+k/2)/(k+1);
//        if(a>1) a = Math.pow(10,3)/a;
//            if((xPos[i]-x)<0) theta[i]+=3.14159;
            theta[i]=Math.atan2(xPos[i] - x, yPos[i] - y);
            vx[i] += -a[i]*Math.sin(theta[i]);
            vy[i] += -a[i]*Math.cos(theta[i]);
//            Log.i("theta",Math.toDegrees(theta)+" "+xPos+" "+x+" "+yPos+" "+y+" "+vx+" "+vy+" "+a);
            xPos[i] += vx[i]*dt;
            yPos[i] += vy[i]*dt;
            if((xPos[i]<0&&vx[i]<0)||(xPos[i]>getWidth()-iconWidth&&vx[i]>0)) vx[i]=-e*vx[i];
            if((yPos[i]<0&&vy[i]<0)||(yPos[i]>getHeight() - iconHeight&&vy[i]>0)) vy[i]=-e*vy[i];
            trails.get(i).add(new Point(xPos[i]+iconWidth/2,yPos[i]+iconHeight/2));
            if(trails.get(i).size()>2) trails.get(i).remove(0);
        }
        if(x+vx_main*dt>0&&x+vx_main*dt<getWidth())
            x+=(vx_main*dt);
        if(y+vy_main*dt>0&&y+vy_main*dt<getHeight())
            y+=(vy_main*dt);
        vy_main+=ay_main*dt*100;
        vx_main+=ax_main*dt*100;
    }

    protected void drawSomething(Canvas canvas) {
        canvas.drawColor(Color.BLACK);

        canvas.drawRect(0, 0, getWidth() * (lives + lives2) / 2000, 50, _paintSharp);

        if (started&&!timer_on) {handler.post(runnable);
            timer_on = true;
        }
        update();
        canvas.drawText(time, getWidth() / 2, getHeight() / 2, _paintSharp);
        for (int i=0;i<k;i++) {
            draw(canvas, i);
//            canvas.drawCircle(
//                    xPos[i]+iconWidth/2, yPos[i]+iconHeight/2,iconHeight/2, _paintBlur);


        }
        _paintBlur.setColor(Color.argb(235, 74 + 181 * (1000 - lives2) / 1000, 138 * lives2 / 1000, 255 * lives / 1000));
        _paintSharp.setColor(Color.argb(235, 74 + 181 * (1000 - lives2) / 1000, 138 * lives2 / 1000, 255 * lives / 1000));
        canvas.drawText(time,getWidth()/2,getHeight()/2,_paintBlur);
        canvas.drawRect(0, 0, getWidth() * (lives + lives2) / 2000, 50, _paintBlur);
        canvas.drawCircle(x, y, iconHeight, _paintBlur);
        canvas.drawCircle(x, y, iconHeight, _paintSimple);
        _paintBlur.setColor(Color.argb(235, 74, 138, 255));
    }

}