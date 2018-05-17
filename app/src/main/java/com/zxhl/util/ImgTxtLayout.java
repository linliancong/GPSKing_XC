package com.zxhl.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.Dimension;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zxhl.gpsking.R;
/**
 * Created by Administrator on 2017/12/1.
 */

public class ImgTxtLayout extends RelativeLayout{

    /**
     * 左右结构，图片在左，文字在右
     */
    public static final int STYLE_ICON_LEFT = 0;
    /**
     * 左右结构，图片在右，文字在左
     */
    public static final int STYLE_ICON_RIGHT = 1;
    /**
     * 上下结构，图片在上，文字在下
     */
    public static final int STYLE_ICON_UP = 2;
    /**
     * 上下结构，图片在下，文字在上
     */
    public static final int STYLE_ICON_DOWN = 3;

    /**
     * 定义控件
     */
    private TextView custom_txt;
    private ImageView custom_img;
    /** 上下文
     */
    private Context mContext;
    /**
     * View的背景色
     */
    private int backColor = 0;
    /**
     * View被按下时的背景色
     */
    private int backColorPress = 0;
    /**
     * icon的背景图片
     */
    private Drawable iconDrawable = null;
    /**
     * icon被按下时显示的背景图片
     */
    private Drawable iconDrawablePress = null;
    /**
     * icon的宽和高
     */
    private int iconWidth=0;
    private int iconHeight=0;
    /**
     * View文字的颜色
     */
    private ColorStateList textColor = null;
    /**
     * View被按下时文字的颜色
     */
    private ColorStateList textColorPress = null;
    /**
     * 两个控件之间的间距，默认为8dp
     */
    private int spacing = 8;
    /**
     * 两个控件的位置结构
     */
    private int mStyle = STYLE_ICON_LEFT;
    /**
     * 设置控件的获取焦点及点击事件
    * */
    private boolean focusable=true;
    private boolean clickable=true;
    /**
     * 标示onTouch方法的返回值，用来解决onClick和onTouch冲突问题
     */
    private boolean isCost = true;

    private OnClickListener onClickListener = null;

    public interface OnClickListener {
        void onClick(View v);
    }

    /**
     * 设置View的Click事件
     *
     * @param l
     */
    public void setOnClickListener(OnClickListener l) {
        this.onClickListener = l;
        isCost = false;
    }

    public ImgTxtLayout(Context context) {
        super(context);
        mContext=context;
        init(context,null,0);
    }

    public ImgTxtLayout(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        mContext=context;
        init(context,attrs,0);
    }

    public ImgTxtLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext=context;
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        //加载布局
        LayoutInflater.from(context).inflate(R.layout.img_txt,this,true);
        //初始化控件
        custom_txt= (TextView) findViewById(R.id.custom_txt);
        custom_img= (ImageView) findViewById(R.id.custom_img);
        setGravity(Gravity.CENTER);
        TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.ImgTxtLayout, defStyle, 0);
        if (a != null) {
            //设置背景色
            ColorStateList colorList = a.getColorStateList(R.styleable.ImgTxtLayout_backColor);
            if (colorList != null) {
                backColor = colorList.getColorForState(getDrawableState(), 0);
                if (backColor != 0) {
                    setBackgroundColor(backColor);
                }
            }
            //记录View被按下时的背景色
            ColorStateList colorListPress = a.getColorStateList(R.styleable.ImgTxtLayout_backColorPress);
            if (colorListPress != null) {
                backColorPress = colorListPress.getColorForState(getDrawableState(), 0);
            }
            //设置icon
            iconDrawable = a.getDrawable(R.styleable.ImgTxtLayout_iconDrawable);
            if (iconDrawable != null) {
                custom_img.setImageDrawable(iconDrawable);
            }
            //记录View被按下时的icon的图片
            iconDrawablePress = a.getDrawable(R.styleable.ImgTxtLayout_iconDrawablePress);
            //设置文字的颜色
            textColor = a.getColorStateList(R.styleable.ImgTxtLayout_textColor);
            if (textColor != null) {
                custom_txt.setTextColor(textColor);
            }
            //记录View被按下时文字的颜色
            textColorPress = a.getColorStateList(R.styleable.ImgTxtLayout_textColorPress);
            //设置显示的文本内容
            String text = a.getString(R.styleable.ImgTxtLayout_text);
            if (text != null) {
                //默认为隐藏的，设置文字后显示出来
                custom_txt.setVisibility(VISIBLE);
                custom_txt.setText(text);
            }
            //设置文本字体大小
            float textSize = a.getFloat(R.styleable.ImgTxtLayout_textSize, 0);
            if (textSize != 0) {
                custom_txt.setTextSize(textSize);
            }
            //设置两个控件的位置结构
            float dp=a.getDimension(R.styleable.ImgTxtLayout_spacing,0);
            if(dp!=0)
            {
                Float f1=new Float(dp);
                spacing=f1.intValue();
            }
            mStyle = a.getInt(R.styleable.ImgTxtLayout_style, 0);
            setIconStyle(mStyle);
            //设置图片的宽、高
            float width=a.getDimension(R.styleable.ImgTxtLayout_iconWidth,0);
            float height=a.getDimension(R.styleable.ImgTxtLayout_iconHeight,0);
            if(width!=0 && height!=0)
            {
                Float f1=new Float(width);
                iconWidth=f1.intValue();
                Float f2=new Float(height);
                iconHeight=f2.intValue();
                //LayoutParams lp= (LayoutParams) custom_img.getLayoutParams();
                /*lp.width=iconWidth;
                lp.height=iconHeight;*/
                LayoutParams lp=new RelativeLayout.LayoutParams(iconWidth,iconHeight);
                custom_img.setLayoutParams(lp);
            }
            focusable=a.getBoolean(R.styleable.ImgTxtLayout_focusable,true);
            clickable=a.getBoolean(R.styleable.ImgTxtLayout_clickable,true);
            a.recycle();
        }

        if (focusable)
        {
            setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View arg0, MotionEvent event) {
                    //根据touch事件设置按下抬起的样式
                    return setTouchStyle(event.getAction());
                }
            });
        }

        if(clickable)
        {
            setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onClickListener != null) {
                        onClickListener.onClick(v);
                    }
                }
            });
        }
    }

    /**
     * 根据按下或者抬起来改变背景和文字样式
     *
     * @param state
     * @return isCost
     */
    private boolean setTouchStyle(int state) {
        if (state == MotionEvent.ACTION_DOWN) {
            if (backColorPress != 0) {
                setBackgroundColor(backColorPress);
            }
            if (iconDrawablePress != null) {
                custom_img.setImageDrawable(iconDrawablePress);
            }
            if (textColorPress != null) {
                custom_txt.setTextColor(textColorPress);
            }
        }
        if (state == MotionEvent.ACTION_UP) {
            if (backColor != 0) {
                setBackgroundColor(backColor);
            }
            if (iconDrawable != null) {
                custom_img.setImageDrawable(iconDrawable);
            }
            if (textColor != null) {
                custom_txt.setTextColor(textColor);
            }
        }
        return isCost;
    }

    /**
     * 设置图标位置
     * 通过重置LayoutParams来设置两个控件的摆放位置
     * @param style
     */
    public void setIconStyle(int style) {
        mStyle = style;
        RelativeLayout.LayoutParams lp;
        switch (style) {
            case STYLE_ICON_LEFT:
                lp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                lp.addRule(RelativeLayout.CENTER_VERTICAL);
                custom_img.setLayoutParams(lp);
                lp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                lp.addRule(RelativeLayout.CENTER_VERTICAL);
                lp.addRule(RelativeLayout.RIGHT_OF, custom_img.getId());
                lp.leftMargin = spacing;
                custom_txt.setLayoutParams(lp);
                break;
            case STYLE_ICON_RIGHT:
                lp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                lp.addRule(RelativeLayout.CENTER_VERTICAL);
                custom_txt.setLayoutParams(lp);
                lp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                lp.addRule(RelativeLayout.CENTER_VERTICAL);
                lp.addRule(RelativeLayout.RIGHT_OF, custom_txt.getId());
                lp.leftMargin = spacing;
                custom_img.setLayoutParams(lp);
                break;
            case STYLE_ICON_UP:
                lp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
                custom_img.setLayoutParams(lp);
                lp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
                lp.addRule(RelativeLayout.BELOW, custom_img.getId());
                lp.leftMargin = spacing;
                custom_txt.setLayoutParams(lp);
                break;
            case STYLE_ICON_DOWN:
                lp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
                custom_txt.setLayoutParams(lp);
                lp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
                lp.addRule(RelativeLayout.BELOW, custom_txt.getId());
                lp.leftMargin = spacing;
                custom_img.setLayoutParams(lp);
                break;
            default:
                break;
        }
    }

    /**
     * 设置View的背景色
     *
     * @param backColor
     */
    public void setBackColor(int backColor) {
        this.backColor = backColor;
        setBackgroundColor(backColor);
    }

    /**
     * 设置View被按下时的背景色
     *
     * @param backColorPress
     */
    public void setBackColorPress(int backColorPress) {
        this.backColorPress = backColorPress;
    }

    /**
     * 设置icon的图片
     *
     * @param iconDrawable
     */
    public void setIconDrawable(Drawable iconDrawable) {
        this.iconDrawable = iconDrawable;
        custom_img.setImageDrawable(iconDrawable);
    }

    /**
     * 设置View被按下时的icon的图片
     *
     * @param iconDrawablePress
     */
    public void setIconDrawablePress(Drawable iconDrawablePress) {
        this.iconDrawablePress = iconDrawablePress;
    }

    /**
     * 设置文字的颜色
     *
     * @param textColor
     */
    public void setTextColor(int textColor) {
        if (textColor == 0) return;
        this.textColor = ColorStateList.valueOf(textColor);
        custom_txt.setTextColor(this.textColor);
    }

    /**
     * 设置View被按下时文字的颜色
     *
     * @param textColorPress
     */
    public void setTextColorPress(int textColorPress) {
        if (textColorPress == 0) return;
        this.textColorPress = ColorStateList.valueOf(textColorPress);
    }

    /**
     * 设置显示的文本内容
     *
     * @param text
     */
    public void setText(CharSequence text) {
        //默认为隐藏的，设置文字后显示出来
        custom_txt.setVisibility(VISIBLE);
        custom_txt.setText(text);
    }

    /**
     * 获取显示的文本
     *
     * @return
     */
    public String getText() {
        return custom_txt.getText().toString();
    }

    /**
     * 设置文本字体大小
     *
     * @param size
     */
    public void setTextSize(float size) {
        custom_txt.setTextSize(size);
    }
}
