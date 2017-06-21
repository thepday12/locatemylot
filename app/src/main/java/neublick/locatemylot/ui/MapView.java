package neublick.locatemylot.ui;

// don't implements onMeasure()
// i will do saveState(), restoreState()

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

import java.util.Calendar;
import java.util.List;

import neublick.locatemylot.R;
import neublick.locatemylot.app.Global;
import neublick.locatemylot.database.CLPath;
import neublick.locatemylot.djikstra.Djikstra;
import neublick.locatemylot.djikstra.Vertex;
import neublick.locatemylot.util.BitmapUtil;
import neublick.locatemylot.util.LightweightTimer;

public class MapView extends android.support.v7.widget.AppCompatImageView {

    // real max_zoom = mInitialScale*MAX_ZOOM
    static float MIN_ZOOM = 0.25f;
    static float MAX_ZOOM = 8f;

    public int bitmapWidth;
    public int bitmapHeight;
    public Matrix drawMatrix = new Matrix();

    public Matrix MATRIX_INITIAL_SCALE;

    // global scale
    float mSaveScale = 1f;
    float mOriginalScale = 1f;

    Handler handler = new Handler();

    ScaleGestureDetector scaleDetector;

    // for detecting basic gesture like drag && drop
    GestureDetector flingDetector;

    // interacting scale detector with gesture detector
    boolean onScaling;

    Context context;

    public int VIEW_WIDTH;
    public int VIEW_HEIGHT;

    public UserObjectOverlap userObject = new UserObjectOverlap();
    //thep 2016/02/25
    public ObjectOverlap carObject = new ObjectOverlap();//end
    public ObjectOverlap liftLobbyObject = new ObjectOverlap();

    private Bitmap mOriginalBitmap;

    float initialScale;

    // last time an action drag or zoom trigger
    long lastTimeActionTrigger;
    LightweightTimer timer;

    Runnable moveToMidRunnable = new Runnable() {
        @Override public void run() {
//            long now = Calendar.getInstance().getTimeInMillis();
//            if (now - lastTimeActionTrigger >= 3000 && Global.isUserPositionVisible) {
//                float deltaX = VIEW_WIDTH / 2 - userObject.location.calculateX;
//                float deltaY = VIEW_HEIGHT / 2 - userObject.location.calculateY;
//                le("VIEW_WIDTH ben trong="+VIEW_WIDTH);
//
//                drawMatrix.postTranslate(deltaX, deltaY);
//                setImageMatrix(drawMatrix);
//
//                carObject.applyMatrix(drawMatrix);
//                //thep 2016/02/25
//                liftLobbyObject.applyMatrix(drawMatrix);//end
//                userObject.applyMatrix(drawMatrix);
//                invalidate();
//                le("DELTA_X=" + deltaX + ", DELTA_Y=" + deltaY);
//            }
        }
    };

    public void scheduleMoveToMid(long afterDuration) {
        handler.postDelayed(moveToMidRunnable, afterDuration);
    }

    public void centerByCarObject() {
        Runnable $centerByCarTask = new Runnable() {
            @Override public void run() {
                float deltaX = VIEW_WIDTH / 2 - carObject.location.calculateX;
                float deltaY = VIEW_HEIGHT / 2 - carObject.location.calculateY;

                drawMatrix.postTranslate(deltaX, deltaY);
                setImageMatrix(drawMatrix);

                carObject.applyMatrix(drawMatrix);
                userObject.applyMatrix(drawMatrix);
                invalidate();
            }
        };
        Handler $myhandler = new Handler();
        $myhandler.postDelayed($centerByCarTask, 0);
    }

    public MapView(Context context, AttributeSet attrSet) {
        super(context, attrSet);
        this.context = context;
        setScaleType(ScaleType.MATRIX);
        setClickable(true);
        flingDetector = new GestureDetector(context, new FlingListener());
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        lastTimeActionTrigger = Calendar.getInstance().getTimeInMillis();
    }

    public void setImageBitmap(Bitmap bitmap) {
        // create a mutable bitmap called mOriginalBitmap
        mOriginalBitmap = BitmapUtil.convertToMutable(bitmap);
        bitmap.recycle();
//        mOriginalBitmap =bitmap;
        // it create a new BitmapDrawable from the bitmap parameter
        // may be pass value
        superSetImageBitmap(mOriginalBitmap);

        bitmapWidth = bitmap.getWidth();
        bitmapHeight = bitmap.getHeight();
    }

    public void setInitialScale(int viewWidth, int viewHeight) {
        float initScale = (float)Math.min(viewWidth*1.0/bitmapWidth, viewHeight*1.0/bitmapHeight);
        lw("viewWidth= " + viewWidth);
        lw("bitmapWidth= " + bitmapWidth);
        lw("viewHeight= " + viewHeight);
        lw("bitmapHeight= " + bitmapHeight);
        lw("initScale= " + initScale);
        setInitialScale(initScale);
        mSaveScale = initScale;
    }

    public void setInitialScale(float initialScale) {
        drawMatrix = new Matrix();
        drawMatrix.setScale(initialScale, initialScale);
        setImageMatrix(drawMatrix);
        invalidate();
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        // http://stackoverflow.com/questions/17890558/weird-onscroll-event-triggered-after-onscale-event
        scaleDetector.onTouchEvent(event);
        flingDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            onScaling = false;
            handler.removeCallbacks(moveToMidRunnable);
        } else if (event.getAction() == MotionEvent.ACTION_UP && userObject.visible() ) {
            handler.postDelayed(moveToMidRunnable, 2500);
        }
        return true;
    }

    public Matrix restoreState() {
        SharedPreferences data = context.getSharedPreferences("mapViewData", context.MODE_PRIVATE);
        String s = data.getString("data", "1:0:0");
        if (s.equalsIgnoreCase("1:0:0")) {
            return null;
        }
        String ss[] = s.split(":");
        lw(ss.toString());
        float scaleValue = 1;
        float tranX = 0;
        float tranY = 0;
        try {
            scaleValue = Float.valueOf(ss[0]);
            tranX = Float.valueOf(ss[1]);
            tranY = Float.valueOf(ss[2]);
        } catch(Exception e) {
            return null;
        }

        // khoi phuc bien saveScale
        mSaveScale = scaleValue;

        drawMatrix = new Matrix();

        // Co the ap dung 2 dong lenh nay vi ko co rotation :-)
        drawMatrix.setScale(scaleValue, scaleValue);
        drawMatrix.postTranslate(tranX, tranY);
        setImageMatrix(drawMatrix);
        invalidate();
/*
		carObject.original(carX, carY).applyMatrix(drawMatrix);
*/
        lw(String.format("restoreState(scale=%.4f, transX=%.4f, transY=%.4f)", scaleValue, tranX, tranY));
        return drawMatrix;
    }

    public void deallocate() {
        vertexList = null;
    }

    public String saveState() {
        SharedPreferences data = context.getSharedPreferences("mapViewData", context.MODE_PRIVATE);
        float[] values = new float[9];
        drawMatrix.getValues(values);
        // du lieu gom co scale:tranX:tranY
        String s = String.format("%f:%f:%f",
                values[Matrix.MSCALE_X],
                values[Matrix.MTRANS_X],
                values[Matrix.MTRANS_Y]
        );
        data.edit().putString("data", s).apply();
        deallocate();
        return s;
    }

    public interface IObjectOverlap {
        IObjectOverlap view(View v);
        IObjectOverlap original(float pX, float pY);
        IObjectOverlap calculate(float pX, float pY);
        IObjectOverlap applyMatrix(Matrix matrix);
        IObjectOverlap zone(String z);
        IObjectOverlap floor(String floor);
        IObjectOverlap visible(boolean visible);
        IObjectOverlap updateViewLocation();
        IObjectOverlap measure(int measureX, int measureY);
    }

    public class ObjectOverlap implements IObjectOverlap {
        public View view;
        public PointFlt location = new PointFlt();

        public int VIEW_WIDTH;
        public int VIEW_HEIGHT;

        public String zone;
        public String floor;

        public ObjectOverlap view(View v) {
            view = v;
            return this;
        }

        public ObjectOverlap measure(int measureX, int measureY) {
            VIEW_WIDTH = measureX;
            VIEW_HEIGHT = measureY;
            return this;
        }

        public ObjectOverlap original(float pX, float pY) {
            location.original(pX, pY);
            return this;
        }

        public ObjectOverlap calculate(float pX, float pY) {
            location.calculate(pX, pY);
            return this;
        }

        public ObjectOverlap zone(String z) {
            zone = z;
            return this;
        }

        public ObjectOverlap floor(String flooR) {
            floor = flooR;
            return this;
        }

        public boolean visible() {
            return (view.getVisibility() == View.VISIBLE)? true: false;
        }

        public ObjectOverlap visible(boolean visible) {
            view.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
            return this;
        }

        public ObjectOverlap applyMatrix(Matrix matrix) {
            location.applyMatrix(matrix);
            updateViewLocation();
            return this;
        }

        public ObjectOverlap updateViewLocation() {
            view.post(new Runnable() {
                @Override public void run() {
                    view.setX(location.calculateX - VIEW_WIDTH / 2);
                    view.setY(location.calculateY - VIEW_HEIGHT / 2);
                }
            });
            return this;
        }
    }

    // UserObjectOverlap va ObjectOverlap ve co ban la khong khac nhau :))
    // do thay doi ve giao dien
    public class UserObjectOverlap extends ObjectOverlap implements SensorEventListener {

        public UserObjectOverlap() {
            super();
        }

        @Override public ObjectOverlap updateViewLocation() {
            super.updateViewLocation();
            return this;
        }

        @Override public void onSensorChanged(SensorEvent sensorEvent) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
                view.setRotation(sensorEvent.values[0]);
                view.setRotation((Global.calibAngle + sensorEvent.values[0]) % 360);
            }
        }

        // khong implement gi het
        @Override public void onAccuracyChanged(Sensor sensor, int arg) {

        }
    }

    public static class PointFlt {
        // location based on (relative to) original bitmap
        public float originalX;
        public float originalY;

        // calculated position relative to current view
        public float calculateX;
        public float calculateY;

        public PointFlt() { }

        // setter
        public PointFlt original(float px, float py) {
            originalX = px;
            originalY = py;
            return this;
        }

        // setter
        public PointFlt calculate(float pX, float pY) {
            calculateX = pX;
            calculateY = pY;
            return this;
        }

        // apply a matrix transformation
        // scale first then translate
        public PointFlt applyMatrix(Matrix matrix) {
            float[] floats = new float[9];
            matrix.getValues(floats);
            calculateX = 	originalX*floats[Matrix.MSCALE_X]*Global.mRatioX; //Tung modify *Global.mRatioX, *Global.mRatioY
            calculateY = 	originalY*floats[Matrix.MSCALE_Y]*Global.mRatioY;
            calculateX += 	floats[Matrix.MTRANS_X];
            calculateY += 	floats[Matrix.MTRANS_Y];
            return this;
        }
    }

    class FlingListener extends GestureDetector.SimpleOnGestureListener {
        @Override public boolean onDown(MotionEvent event) {
            return true;
        }
        @Override public boolean onScroll(MotionEvent ev1, MotionEvent ev2, float distanceX, float distanceY) {
            if (onScaling) {
                return false;
            }
            drawMatrix.postTranslate(-distanceX, -distanceY);
            setImageMatrix(drawMatrix);
            invalidate();

            // objects overlap the map
            carObject.applyMatrix(drawMatrix);
            //thep 2016/02/25
            liftLobbyObject.applyMatrix(drawMatrix);//end
            userObject.applyMatrix(drawMatrix);
            return true;
        }
    }

    class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override public boolean onScaleBegin(ScaleGestureDetector s) {
            onScaling = true;
            return true;
        }
        @Override public boolean onScale(ScaleGestureDetector s) {
            // save the scale factory
            float scale = s.getScaleFactor();

            // if scale in (1 - alpha, 1 + alpha) we edit the scale to the threshold
            ScaleThreshold threshold = new ScaleThreshold(scale, 0.0005f);
            scale = threshold.getValue();

            // save the current value of total scale
            mOriginalScale = mSaveScale;

            // set the total scale to original bitmap that applying scale by initialScale
            mSaveScale *= scale;

            mSaveScale = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, mSaveScale));

            // re-calculate the real scale factor
            scale = mSaveScale/mOriginalScale;

            float focusX = s.getFocusX();
            float focusY = s.getFocusY();
            drawMatrix.postScale(scale, scale, focusX, focusY);
            le("SCALE = " + scale + " saveScale = " +mSaveScale);
            setImageMatrix(drawMatrix);
            invalidate();

            // objects overlap the map
            carObject.applyMatrix(drawMatrix);
            //thep 2016/02/25
            liftLobbyObject.applyMatrix(drawMatrix);//end
            userObject.applyMatrix(drawMatrix);

            return true;
        }
    }

    // =================================================
    // CHUC NANG VE DUONG DI
    // =================================================
    // same as super.setImageBitmap()
    public void superSetImageBitmap(final Bitmap pBitmap) {
        super.setImageBitmap(pBitmap);
    }

    public boolean wayMode;

    public void wayClear() {
        superSetImageBitmap(mOriginalBitmap);
        setImageMatrix(drawMatrix);
        invalidate();
    }

    List<Vertex> vertexList;
    void drawLine(Canvas drawer, float startX,float startY,float stopX,float stopY, Paint paint){
        drawer.drawLine(startX*Global.mRatioX,startY*Global.mRatioY,stopX*Global.mRatioX,stopY*Global.mRatioY,paint);
    }
    public void wayDraw() {
        System.gc();
        if (mOriginalBitmap == null) {
            return;
        }
        if (vertexList == null) {
            le("vertexList=" + vertexList);
            vertexList = CLPath.getAllVertexByCarparkId(Global.getCarparkID());
/*
			// tinh toan toa do (x, y) cua beacon tren local
			for(int i = 0; i < vertexList.size(); ++i) {
				Vertex vertexItem = vertexList.get(i);
				vertexItem.x = vertexItem.x * Global.mRatioX;
				vertexItem.y = vertexItem.y * Global.mRatioY;
			}
*/
        }

        // tinh toan khoang cach tu cac diem vertex toi userObject va carObject
        for(int i = 0; i < vertexList.size(); ++i) {
            Vertex vertexItem = vertexList.get(i);
            vertexItem.calculateDistanceToCarObject(carObject);
            vertexItem.calculateDistanceToUserObject(userObject);
            vertexItem.minDistance = Double.POSITIVE_INFINITY;
            vertexItem.previous = null;
        }

        VertexPair vertexPair = findVertexPair(vertexList);

        Djikstra.computePaths(vertexPair.source);
        List<Vertex> minWay = Djikstra.getShortestPathTo(vertexPair.target);

        if (minWay.size() >= 2) {
            System.gc();
            // constructor save the reference Bitmap so we must create a copy bitmap
            Bitmap mutableBitmap = Bitmap.createScaledBitmap(mOriginalBitmap, bitmapWidth, bitmapHeight, false);
            Canvas drawer = new Canvas(mutableBitmap);

            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

            paint.setColor(context.getResources().getColor(R.color.colorWay));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(10);
            paint.setPathEffect(new DashPathEffect(new float[] {10, 5}, 0));

            int sizeOfMinWay = minWay.size();
            if (sizeOfMinWay == 0) {
                // noi 2 diem userObject toi carObject
                drawLine(drawer,
                        userObject.location.originalX,
                        userObject.location.originalY,
                        carObject.location.originalX,
                        carObject.location.originalY,
                        paint
                );
            } else if (sizeOfMinWay == 1) {

                Vertex midPoint = minWay.get(0);
                drawLine(drawer, userObject.location.originalX, userObject.location.originalY, midPoint.x, midPoint.y, paint);
                drawLine(drawer, midPoint.x, midPoint.y, carObject.location.originalX, carObject.location.originalY, paint);

            } else if (sizeOfMinWay == 2) {

                Vertex vertex1 = minWay.get(0);
                Vertex vertex2 = minWay.get(1);
                PointF midPoint = new PointF((vertex1.x + vertex2.x)/2, (vertex1.y + vertex2.y)/2);
                drawLine(drawer, userObject.location.originalX, userObject.location.originalY, midPoint.x, midPoint.y, paint);
                drawLine(drawer,midPoint.x, midPoint.y, carObject.location.originalX, carObject.location.originalY, paint);

            } else if (sizeOfMinWay >= 3) {
                // tao diem noi voi user-object
                PointF start = new PointF(
                        (minWay.get(0).x + minWay.get(1).x) / 2,
                        (minWay.get(0).y + minWay.get(1).y) / 2
                );

                // tao diem noi voi car-object
                PointF end = new PointF(
                        (minWay.get(sizeOfMinWay - 2).x + minWay.get(sizeOfMinWay - 1).x) / 2,
                        (minWay.get(sizeOfMinWay - 2).y + minWay.get(sizeOfMinWay - 1).y) / 2
                );

                // ve doan thang noi car-object voi trung diem cua vertex 0 va vertex 1 (goi la diem start)
                drawLine(drawer, userObject.location.originalX, userObject.location.originalY, start.x, start.y, paint);

                // ve doan thang noi diem start voi vertex 1
                drawLine(drawer, start.x, start.y, minWay.get(1).x, minWay.get(1).y, paint);

                for(int i = 1; i < sizeOfMinWay - 2; ++i) {
                    Vertex fromVertex = minWay.get(i);
                    Vertex toVertex = minWay.get(i + 1);
                    drawLine(drawer, fromVertex.x, fromVertex.y, toVertex.x, toVertex.y, paint);
                }

                // ve doan thang noi vertex[size-2] toi diem end
                drawLine(drawer, minWay.get(sizeOfMinWay - 2).x, minWay.get(sizeOfMinWay - 2).y, end.x, end.y, paint);

                // ve doan thang noi diem end toi user-object
                drawLine(drawer, end.x, end.y, carObject.location.originalX, carObject.location.originalY, paint);
            }
            superSetImageBitmap(mutableBitmap); // save reference
            setImageMatrix(drawMatrix);
            invalidate();
        }
        le("the minWay: " + minWay.toString());
    }

    // tim diem dau va diem cuoi cua duong di
    public VertexPair findVertexPair(List<Vertex> vertexList) {
        int source = 0;
        int target = 0;
        for(int i = 1; i < vertexList.size(); ++i) {
            if (vertexList.get(source).distanceToCarObject > vertexList.get(i).distanceToCarObject) {
                source = i;
            }
            if (vertexList.get(target).distanceToUserObject > vertexList.get(i).distanceToUserObject) {
                target = i;
            }
        }
        return new VertexPair(vertexList.get(source), vertexList.get(target));
    }

    // store the source vertex && target vertex
    public static class VertexPair {
        Vertex source;
        Vertex target;
        public VertexPair(Vertex src, Vertex tgt) {
            source = src;
            target = tgt;
        }
    }

    static void lw(String s) {
        final String TAG = MapView.class.getSimpleName();
        android.util.Log.w(TAG, s);
    }

    static void le(String s) {
        final String TAG = MapView.class.getSimpleName();
        android.util.Log.e(TAG, s);
    }

    @Override protected void onMeasure(int specW, int specH) {
        super.onMeasure(specW, specH);
        VIEW_WIDTH = MeasureSpec.getSize(specW);
        VIEW_HEIGHT = MeasureSpec.getSize(specH);

        initialScale = (float) Math.min(VIEW_WIDTH * 1.0 / bitmapWidth, VIEW_HEIGHT * 1.0 / bitmapHeight);
        mSaveScale = initialScale;
        drawMatrix = new Matrix();
        drawMatrix.setScale(initialScale, initialScale);

        setImageMatrix(drawMatrix);
        invalidate();

        // day la hang so luu lai matrix dung de tao trang thai initial_Scale cho map
        MATRIX_INITIAL_SCALE = new Matrix();
        float[] values = new float[9];
        drawMatrix.getValues(values);
        MATRIX_INITIAL_SCALE.setValues(values);

        le("__bitmapWidth= " + bitmapWidth);
        le("__initialScale= " + initialScale);
    }

    public void setSaveScale(float saveScale) {
        mSaveScale = saveScale;
    }

    // if the scale factor <= (1 - alpha) or >= (1 + alpha) we use threshold
    public class ScaleThreshold {
        private float mAlpha;
        private float mScale;
        private boolean mUseThreshold;

        public ScaleThreshold(float scale, float alpha) {
            mScale = scale;
            mAlpha = alpha;
            if (1 - mAlpha < mScale && mScale < 1 + mAlpha) mUseThreshold = true;
        }

        public float left() {
            return 1 - mAlpha;
        }

        public float right() {
            return 1 + mAlpha;
        }

        public float getValue() {
            float value = mScale;
            if (mUseThreshold) {
                value = (mScale >= 1) ? right() : left();
            }
            String fmt = String.format("use_threshold = %s, scale = %s", mUseThreshold, value);
            android.util.Log.d("_ScaleThreshold", fmt);
            return value;
        }
    }
}