# FilterFlowLayout
含有过滤的流式布局, 参考[FlowLayout](https://github.com/blazsolar/FlowLayout)

## 系统要求
Android 4.0以上

## 快速使用
    <me.codeboy.android.lib.FilterFlowLayout
        xmlns:cb="http://schemas.android.com/apk/res-auto"
        android:id="@+id/filterFlowLayout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        cb:maxWidthRatio="0.5"
        cb:minWidth="80dp"
        cb:maxLines="3"
        cb:horizontalGap="5dp"
        cb:verticalGap="5dp"
        >

- 最大宽度为FlowLayout的宽度的一半
- 最小宽度是80dp
- 最大行数为3行
- 水平间距为5dp
- 垂直间距为5dp

## xml配置
    maxWidthRatio  最大宽度比例，相对于FlowLayout的总宽度，默认-1
    minWidthRatio  最小宽度比例，相对于FlowLayout的总宽度，默认-1
    maxWidth       最大宽度,默认0
    minWidth       最小宽度，默认0
    maxLines       最大显示行数，默认Integer.MAX_VALUE
    horizontalGap  每一行子试图之间的空隙，默认是0
    verticalGap    多行式行空隙，默认是0


## 相关方法

    setMaxChildWidth(float maxChildWidth)  //设置最大宽度
    setMinChildWidth(float minChildWidth)  //设置最小宽度
    setMaxLines(int maxLines)              //设置最大行数
    setHorizontalGap(int horizontalGap)    //设置水平间距
    setVerticalGap(int verticalGap)        //设置垂直间距


## 注意事项

- xml中同时设置宽度的比例与具体值时，比例的优先级高于真实值。
- 宽度都不设置时，最大宽度为FlowLayout的宽度

