package com.customerui.seekbar_v2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * seekBar滑动条
 * Created by shijunduan on 2018/6/19.
 */

public class SeekBarWithBezier extends View {
    private final static String TAG = SeekBarWithBezier.class.getSimpleName();
    private Bitmap my_bg = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.libui_seekbar_my2);//我的 背景
    private Bitmap rec_bg = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.libui_seekbar_rec2);//推荐 背景
    private int my_index = 0;//我的 位置
    private int rec_index =0;//推荐 位置
    private String myText = "我的";
    private String recText= "推荐";
    private int bgColor = Color.WHITE;//控件背景色
    private int viewHight = 200;//控件的高度
    private int rSliderBlock = 40;//圆形滑块半径
    private float drawX = 0;//滑块滑动时x坐标
    private int index = 6;//滑块停止滑动后，当前选中的位置 1-9
    private int preIndexPx = 150;//每个位置的间距（18个分隔）
    private int lineWidth = 5;//线背景的宽度
    private int dengSize = 30;//等号字体大小
    private int gaoDiSize = 30;//“高低”字体大小
    private int zhiShuSize = 40;//“风险指数”字体大小
    private boolean isMoving = false;//是否正在滑动
    private int tmpIndex = 0;//用来保存滑块滑动超过index的preIndexPx的距离范围时，index值在tmpIndex中不变

    private Paint paint = new Paint();//横线背景
    private Paint paintCover = new Paint();//滑块下遮盖横线的画笔
    private Paint paint1 = new Paint();//弧度背景--无用
    private Paint paintSliderBlock = new Paint();//绘制滑块的画笔
    private Paint paintScaleNum = new Paint();//刻度数字
    private Paint paintScale = new Paint();//刻度
    private Paint paintMsg = new Paint();//底部中间，风险指数的画笔
    private Paint paintMyRec = new Paint();//我的 和 推荐

    public SeekBarWithBezier(Context context) {
        this(context, null);
    }

    public SeekBarWithBezier(Context context, AttributeSet attrs) {
        super(context, attrs);
        setMy_index(1);
        setRec_index(5);
    }

    public void setMy_index(int my_index) {
        this.my_index = my_index;
    }

    public void setRec_index(int rec_index) {
        this.rec_index = rec_index;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        viewHight = getHeight();
        preIndexPx = getWidth() / 18;
        //绘制控件背景
        if (bgColor != 0) {
            canvas.drawColor(bgColor);
        }
        //绘制横线，添加渐变背景 绘制刻度，和数字
        drawLine(canvas);
        //绘制凸起的弧度
        drawArcBezier(canvas);
        //绘制圆形滑块
        drawSliderBlock(canvas);
        //绘制底部文字
        drawBottomMsg(canvas);
        //绘制"我的"和“推荐”
        drawMyAndRec(canvas);
    }

    private void drawMyAndRec(Canvas canvas) {
        paintMyRec.setAntiAlias(true);
        paintMyRec.setColor(Color.parseColor("#ffffff"));
        paintMyRec.setTextSize(gaoDiSize-5);
        paintMyRec.setStyle(Paint.Style.FILL);
        float[] postions = getDistance();
        float rec_stop_y = getHeight() * 0.3f - rec_bg.getHeight() / 2;//最小值
        if (rec_index != 0) {
            int rec_index_px = changeValue(rec_index) * preIndexPx;
            float rec_start_x = rec_index_px - rec_bg.getWidth() / 2;
            if (tmpIndex == rec_index) { //等于当前选中的位置
                canvas.drawBitmap(rec_bg, rec_start_x, postions[4], paintMyRec);
                canvas.drawText(recText, rec_start_x + my_bg.getWidth() * 0.32f, postions[4] + my_bg.getHeight() * 0.58f, paintMyRec);
            }else if (getNextValue() == rec_index) {//下一个
                canvas.drawBitmap(rec_bg, rec_start_x, postions[5], paintMyRec);
                canvas.drawText(recText, rec_start_x + my_bg.getWidth() * 0.32f, postions[5] + my_bg.getHeight() * 0.58f, paintMyRec);
            }else{
                canvas.drawBitmap(rec_bg, rec_start_x, rec_stop_y, paintMyRec);
                canvas.drawText(recText, rec_start_x + my_bg.getWidth() * 0.32f, rec_stop_y + my_bg.getHeight() * 0.58f, paintMyRec);
            }
        }

        if (my_index != 0) {
            int my_index_px = changeValue(my_index) * preIndexPx;
            float my_start_x = my_index_px - my_bg.getWidth() / 2;

            if (tmpIndex == my_index) { //等于当前选中的位置
                canvas.drawBitmap(my_bg, my_start_x, postions[4], paintMyRec);
                canvas.drawText(myText, my_start_x + my_bg.getWidth() * 0.32f, postions[4] + my_bg.getHeight() * 0.58f, paintMyRec);
            }else if (getNextValue() == my_index) {//下一个
                canvas.drawBitmap(my_bg, my_start_x, postions[5], paintMyRec);
                canvas.drawText(myText, my_start_x + my_bg.getWidth() * 0.32f, postions[5] + my_bg.getHeight() * 0.58f, paintMyRec);
            }else{
                canvas.drawBitmap(my_bg, my_start_x,rec_stop_y, paintMyRec);
                canvas.drawText(myText, my_start_x + my_bg.getWidth() * 0.32f, rec_stop_y + my_bg.getHeight() * 0.58f, paintMyRec);
            }

        }
    }

    private void drawBottomMsg(Canvas canvas) {
        //绘制“低”“高”
        paintMyRec.setAntiAlias(true);
        paintMyRec.setTextSize(gaoDiSize);
        paintMyRec.setStyle(Paint.Style.FILL);

        paintMyRec.setColor(Color.parseColor("#2ad8bb"));
        canvas.drawText("低", preIndexPx - gaoDiSize * 0.5f, viewHight * 0.5f + rSliderBlock * 2, paintMyRec);
        paintMyRec.setColor(Color.parseColor("#6080dd"));
        canvas.drawText("高", getWidth() - preIndexPx - gaoDiSize * 0.5f, viewHight * 0.5f + rSliderBlock * 2, paintMyRec);


        paintMsg.setAntiAlias(true);
        paintMsg.setColor(Color.parseColor("#999999"));
        paintMsg.setStyle(Paint.Style.FILL);
        paintMsg.setTextSize(zhiShuSize);
        //绘制风险指数
        String msg = "风险指数";
        canvas.drawText(msg, 9 * preIndexPx - msg.length() * zhiShuSize * 0.5f, viewHight * 0.5f + rSliderBlock * 2.2f, paintMsg);
    }

    /**
     * 绘制圆形滑块
     *
     * @param canvas
     */
    private void drawSliderBlock(Canvas canvas) {
        //画实心圆
        paintSliderBlock.setAntiAlias(true);
        paintSliderBlock.setStyle(Paint.Style.FILL);
        paintSliderBlock.setColor(Color.parseColor("#6388ea"));
        paintSliderBlock.setStrokeWidth(lineWidth);
        //计算滑块小圆圆心坐标
        float centerRX = 0;
        if (isMoving) {//移动中，使用滑动的X坐标进行计算
            centerRX = drawX;
        } else {
            centerRX = preIndexPx * changeValue(index);
        }
        float centerRY = viewHight * 0.5f;
        RectF oval2 = new RectF(centerRX - rSliderBlock, (centerRY - rSliderBlock), centerRX + rSliderBlock, (centerRY + rSliderBlock));
        canvas.drawArc(oval2, 0, 365, false, paintSliderBlock);
        //小圆 画边线
        paintSliderBlock.setStyle(Paint.Style.STROKE);
        paintSliderBlock.setColor(Color.parseColor("#647dd4"));
        paintSliderBlock.setStrokeWidth(2);
        RectF oval3 = new RectF(centerRX - rSliderBlock, (centerRY - rSliderBlock), centerRX + rSliderBlock, (centerRY + rSliderBlock));
        canvas.drawArc(oval3, 0, 365, false, paintSliderBlock);
        //画等号
        paintSliderBlock.setStyle(Paint.Style.FILL);
        paintSliderBlock.setColor(Color.WHITE);
        paintSliderBlock.setStrokeWidth(2);
        paintSliderBlock.setTextSize(dengSize);
        canvas.drawText("||", centerRX - dengSize / 4, centerRY + dengSize / 4, paintSliderBlock);

    }

    /**
     * 绘制横线，添加渐变背景 绘制刻度，和数字
     *
     * @param canvas
     */
    private void drawLine(Canvas canvas) {
        // 设置渐变
        Shader mShader = new LinearGradient(0, viewHight * 0.5f, getWidth(), viewHight * 0.5f, new int[]{Color.parseColor("#01e9a4"), Color.parseColor("#08e5a9"), Color.parseColor("#16deb0"), Color.parseColor("#28d5ba"), Color.parseColor("#36cec2"), Color.parseColor("#50c1cf"), Color.parseColor("#64b7da"), Color.parseColor("#77aee5"), Color.parseColor("#88a5ee"), Color.parseColor("#959ff5"), Color.parseColor("#9a9cf8")}, null, Shader.TileMode.REPEAT);
        paint.setShader(mShader);
        // 画进度条线
        paint.setStrokeWidth(lineWidth);
        canvas.drawLine(0, viewHight * 0.5f, getWidth(), viewHight * 0.5f, paint);
        // 画刻度 画数字 画遮盖线
        int count = 0;
        int countTxt = 1;
        paintScale.setStyle(Paint.Style.FILL);
        paintScale.setColor(Color.GRAY);
        paintScale.setStrokeWidth(2);
        paintScaleNum.setStyle(Paint.Style.FILL);
        paintScaleNum.setColor(Color.BLACK);
        paintScaleNum.setStrokeWidth(2);
        paintScaleNum.setTextSize(dengSize);

        for (float f = 0; f <= getWidth(); f = f + getWidth() / 18.0f) {
            if (count % 2 == 1) {
                float[] postions = getDistance();
                if (tmpIndex == countTxt) { //等于当前选中的位置
                    //不论是否滑动都可以用偏移量比例计算刻度的高度
//                    paintScaleNum.setTextSize(dengSize * 2);

                    canvas.drawLine(f, postions[0] - 8 - preIndexPx * 0.5f, f, postions[0] - preIndexPx * 0.5f, paintScale);
                    paintScaleNum.setTextSize(postions[2]);
                    canvas.drawText(countTxt + "", f -  postions[2]*0.3f, postions[0] - dengSize * 1.6f, paintScaleNum);// dengSize*1.6f的含义是， 偏移量

                    paintScaleNum.setTextSize(dengSize);

                    //绘制覆盖线
                    paintCover.setAntiAlias(true);
                    paintCover.setStyle(Paint.Style.FILL);
                    paintCover.setColor(bgColor);
                    paintCover.setStrokeWidth(lineWidth * 1.5f);
                    if (isMoving) {//移动中，使用滑动的X坐标进行计算
                        canvas.drawLine(drawX - 2 * preIndexPx, viewHight * 0.5f, drawX + 2 * preIndexPx, viewHight * 0.5f, paintCover);
                    } else {
                        canvas.drawLine(changeValue(index - 1) * preIndexPx, viewHight * 0.5f, changeValue(index + 1) * preIndexPx, viewHight * 0.5f, paintCover);
                    }
                } else if (getNextValue() == countTxt) {//下一个,可能是 向左移动 下一个就是-1，也可能是向右移动 下一个就是+1
                    canvas.drawLine(f, postions[1] - 8 - preIndexPx * 0.5f, f, postions[1] - preIndexPx * 0.5f, paintScale);
                    paintScaleNum.setTextSize(postions[3]);
                    canvas.drawText(countTxt + "", f - postions[3]*0.3f, postions[1] - dengSize * 1.6f, paintScaleNum);
                    paintScaleNum.setTextSize(dengSize);

                    //绘制覆盖线
                    paintCover.setAntiAlias(true);
                    paintCover.setStyle(Paint.Style.FILL);
                    paintCover.setColor(bgColor);
                    paintCover.setStrokeWidth(lineWidth * 1.5f);
                    if (isMoving) {//移动中，使用滑动的X坐标进行计算
                        canvas.drawLine(drawX - 2 * preIndexPx, viewHight * 0.5f, drawX + 2 * preIndexPx, viewHight * 0.5f, paintCover);
                    } else {
                        canvas.drawLine(changeValue(index - 1) * preIndexPx, viewHight * 0.5f, changeValue(index + 1) * preIndexPx, viewHight * 0.5f, paintCover);
                    }
                } else {//其他位置

                    canvas.drawLine(f, viewHight * 0.5f - 9 - 8, f, viewHight * 0.5f - 9, paintScale);
                    canvas.drawText(countTxt + "", f - dengSize / 4, viewHight * 0.5f - 9 - 8 - 8 - 13, paintScaleNum);

                }
                countTxt++;
            }
            count++;
        }
    }

    /**
     * 通过当前滑块的值，判断下一个的值
     * @return
     */
    private int getNextValue() {
        int retValue = tmpIndex;
        if ((changeValue(tmpIndex) * preIndexPx - 2 * preIndexPx) <= drawX && drawX <= (changeValue(tmpIndex) * preIndexPx)) {
            return retValue - 1;
        } else if (drawX <= (changeValue(tmpIndex) * preIndexPx + 2 * preIndexPx) && drawX >= (changeValue(tmpIndex) * preIndexPx)) {
            return retValue + 1;
        }
        return retValue;
    }

    /**
     * 绘制凸起的弧度
     *
     * @param canvas
     */
    private void drawArcBezier(Canvas canvas) {
        Path path = new Path();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(lineWidth);

        paint1.setAntiAlias(true);
        paint1.setStyle(Paint.Style.STROKE);
        paint1.setColor(Color.RED);
        paint1.setStrokeWidth(2);

        //将高阶贝塞尔曲线降阶到二阶：分成4段进行绘制
        if (isMoving) {//移动中，使用滑动的X坐标进行计算
            path.moveTo(drawX - preIndexPx * 2, viewHight * 0.5f);
            path.quadTo(drawX - preIndexPx * 2 + preIndexPx * 0.5f, viewHight * 0.5f, drawX - preIndexPx * 2 + preIndexPx, viewHight * 0.5f - preIndexPx * 0.5f);
            path.quadTo(drawX - preIndexPx * 0.5f, viewHight * 0.5f - preIndexPx, drawX, viewHight * 0.5f - preIndexPx);//viewHight * 0.5f-preIndexPx表示：弧度的顶点为一个preIndexPx的高度
            path.quadTo(drawX + preIndexPx * 0.5f, viewHight * 0.5f - preIndexPx, drawX + preIndexPx, viewHight * 0.5f - preIndexPx * 0.5f);//viewHight * 0.5f-preIndexPx表示：弧度的顶点为一个preIndexPx的高度
            path.quadTo(drawX + preIndexPx * 2 - preIndexPx * 0.5f, viewHight * 0.5f, drawX + preIndexPx * 2, viewHight * 0.5f);
        } else {
            path.moveTo(preIndexPx * (changeValue(index - 1)), viewHight * 0.5f);
            path.quadTo(preIndexPx * (changeValue(index - 1)) + preIndexPx * 0.5f, viewHight * 0.5f, preIndexPx * (changeValue(index - 1)) + preIndexPx, viewHight * 0.5f - preIndexPx * 0.5f);
            path.quadTo(preIndexPx * (changeValue(index)) - preIndexPx * 0.5f, viewHight * 0.5f - preIndexPx, preIndexPx * (changeValue(index)), viewHight * 0.5f - preIndexPx);//viewHight * 0.5f-preIndexPx表示：弧度的顶点为一个preIndexPx的高度
            path.quadTo(preIndexPx * (changeValue(index)) + preIndexPx * 0.5f, viewHight * 0.5f - preIndexPx, preIndexPx * (changeValue(index)) + preIndexPx, viewHight * 0.5f - preIndexPx * 0.5f);//viewHight * 0.5f-preIndexPx表示：弧度的顶点为一个preIndexPx的高度
            path.quadTo(preIndexPx * (changeValue(index + 1)) - preIndexPx * 0.5f, viewHight * 0.5f, preIndexPx * (changeValue(index + 1)), viewHight * 0.5f);
        }
        canvas.drawPath(path, paint);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            isMoving = true;
            calculateGroupSelected(event);
            if (event.getX() < getWidth() / 9.0f / 2.0f) {
                drawX = (getWidth()) / 9.0f / 2.0f;
            } else if (event.getX() > getWidth() - (getWidth()) / 9.0f / 2.0f) {
                drawX = getWidth() - (getWidth()) / 9.0f / 2.0f;
            } else {
                drawX = event.getX();
            }
            if (getParent() != null) {
                getParent().requestDisallowInterceptTouchEvent(true);
            }
            redraw();
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            isMoving = false;
            calculateGroupSelected(event);

            redraw();
        }

        return true;
    }


    /**
     * 重画View
     */
    private void redraw() {
        postInvalidate();
    }

    /**
     * 将显示的index值转化成对应的value值（9份转18份），像素值
     *
     * @param index
     * @return
     */
    private int changeValue(int index) {
        int retInt = index + (index - 1);
        return retInt;
    }

    /**
     * 计算距离最近的index
     *
     * @param event
     */
    private void calculateGroupSelected(MotionEvent event) {
        double minX = Double.MAX_VALUE;
        int count = 1;
        int step = 0;
        for (float f = 0; f <= getWidth(); f = f + getWidth() / 9.0f / 2.0f) {

            if (step % 2 == 1) {
                if (Math.abs(event.getX() - f) < minX) {
                    minX = Math.abs(event.getX() - f);
                    index = count;
                }
                count++;
            }
            step++;
        }
    }

    /**
     * 用偏移量比例计算刻度的高度
     * 将滑块的X的坐标值drawX,从index的位置增大到index+1的位置时的距离值的变化范围，
     * 映射成刻度线和数字的Y坐标，从viewHight * 0.5f到viewHight * 0.5f-preIndexPx 的变化
     *
     * @return float[] 0:index位置的高度变化（偏离index位置的绝对值变大则高度变小），1：index+1 或者index-1位置的高度变化（偏离index位置的绝对值变大则高度变大）
     */
    private float[] getDistance() {
        float[] retValue = new float[]{viewHight * 0.5f, viewHight * 0.5f, 0, 0, 0, 0};
        if (tmpIndex == 0) {//首次计算时
            tmpIndex = index;
        }
        if (drawX == 0) {
            drawX = changeValue(tmpIndex) * preIndexPx;
        }
        if ((changeValue(tmpIndex) * preIndexPx - 2 * preIndexPx) >= drawX || drawX >= (changeValue(tmpIndex) * preIndexPx + 2 * preIndexPx)) {
            tmpIndex = index;//滑块滑动距离超过2*preIndexPx时对tmpIndex更新
        }
        float b = Math.abs(drawX - changeValue(tmpIndex) * preIndexPx) / (preIndexPx * 2);//计算百分比
        retValue[0] = viewHight * 0.5f - preIndexPx + preIndexPx * b;//index位置的距离变化值
        retValue[1] = viewHight * 0.5f - preIndexPx * b;//index+1或者index-1位置的距离变化值
        retValue[2] = 2 * dengSize - dengSize * b;//index的数字大小
        retValue[3] = dengSize + dengSize * b;//下一个 数字的大小
        float rec_stop_y = getHeight() * 0.3f - rec_bg.getHeight() / 2;
        float rec_start_y = getHeight() * 0.2f - rec_bg.getHeight() / 2;
        float chaValue = rec_stop_y - rec_start_y;
        retValue[4] =rec_start_y + chaValue * b;//index 我的
        retValue[5] =  rec_stop_y - chaValue * b;//下一个 我的
        Log.d(TAG, tmpIndex + "移动的结果：" + retValue[0] + "；" + retValue[1] + "；drawX：" + drawX + "；index和drawx的差值：" + Math.abs(drawX - changeValue(tmpIndex) * preIndexPx) + "；2格值：" + preIndexPx * 2 + "；百分比" + b + "；控件位置：" + viewHight * 0.5f);
        return retValue;
    }
}
