## 提出问题

当谈到 Android 中的 ImageView 时，我们总不能避免 ScaleType 这个属性，这个属性在图片资源和 View 尺寸不一样的时候经常用到，用于对图片资源进行自适应的展示。

所以，我们先来看看结果，看看 ScaleType 在多种情况下的实际表现。






本文源码基于Android API =25。

ScaleType 是 ImageView 的内部类，具有下面代码片中的八种状态，接下来我们就来逐一解释这八种状态。

```java

public enum ScaleType {

        /**
         * Scale using the image matrix when drawing. The image matrix can be set using
         * {@link ImageView#setImageMatrix(Matrix)}.
         */
        MATRIX      (0),
        FIT_XY      (1),
        FIT_START   (2),
        FIT_CENTER  (3),
        FIT_END     (4),
        /**
         * Center the image in the view, but perform no scaling.
         */
        CENTER      (5),
        /**
         * Scale the image uniformly (maintain the image's aspect ratio) so
         * that both dimensions (width and height) of the image will be equal
         * to or larger than the corresponding dimension of the view
         * (minus padding). The image is then centered in the view.
         */
        CENTER_CROP (6),
        /**
         * Scale the image uniformly (maintain the image's aspect ratio) so
         * that both dimensions (width and height) of the image will be equal
         * to or less than the corresponding dimension of the view
         * (minus padding). The image is then centered in the view.
         */
        CENTER_INSIDE (7);

        ...
    }

```

这八种状态可以分为三种情况来考虑，分别是：
- MATRIX
- FIT_XY, FIT_START, FIT_CENTER, FIT_END
- CENTER, CENTER_CROP, CENTER_INSIDE


为什么这么区分呢，我们先来看 ImageView 的 configBounds 方法。

```java
private void configureBounds() {
        if (mDrawable == null || !mHaveFrame) {
            return;
        }
        //默认值
        //mScaleType = ScaleType.FIT_CENTER;   

        //drawable尺寸，drawable为空是，dwidth，dheight为-1
        final int dwidth = mDrawableWidth;
        final int dheight = mDrawableHeight;

        final int vwidth = getWidth() - mPaddingLeft - mPaddingRight;
        final int vheight = getHeight() - mPaddingTop - mPaddingBottom;

        //当不存在drawable或者drawable刚好与view内容尺寸完全一样时，为适合尺寸
        final boolean fits = (dwidth < 0 || vwidth == dwidth)
                && (dheight < 0 || vheight == dheight);

        //如果没有drawable，或者为FIT_XY时，平铺
        if (dwidth <= 0 || dheight <= 0 || ScaleType.FIT_XY == mScaleType) {
            /* If the drawable has no intrinsic size, or we're told to
                scaletofit, then we just fill our entire view.
            */
            mDrawable.setBounds(0, 0, vwidth, vheight);
            mDrawMatrix = null;
        } else {
            // We need to do the scaling ourself, so have the drawable
            // use its native size.
            mDrawable.setBounds(0, 0, dwidth, dheight);

            if (ScaleType.MATRIX == mScaleType) {
                // Use the specified matrix as-is.
                if (mMatrix.isIdentity()) {//是否为单位矩阵
                    mDrawMatrix = null;
                } else {
                    mDrawMatrix = mMatrix;
                }
            } else if (fits) {
                // The bitmap fits exactly, no transform needed.
                mDrawMatrix = null;
            } else if (ScaleType.CENTER == mScaleType) {
                // Center bitmap in view, no scaling.
                mDrawMatrix = mMatrix;
                mDrawMatrix.setTranslate(Math.round((vwidth - dwidth) * 0.5f),
                                         Math.round((vheight - dheight) * 0.5f));
            } else if (ScaleType.CENTER_CROP == mScaleType) {
                mDrawMatrix = mMatrix;

                float scale;
                float dx = 0, dy = 0;

                if (dwidth * vheight > vwidth * dheight) {
                    scale = (float) vheight / (float) dheight;
                    dx = (vwidth - dwidth * scale) * 0.5f;
                } else {
                    scale = (float) vwidth / (float) dwidth;
                    dy = (vheight - dheight * scale) * 0.5f;
                }

                mDrawMatrix.setScale(scale, scale);
                mDrawMatrix.postTranslate(Math.round(dx), Math.round(dy));
            } else if (ScaleType.CENTER_INSIDE == mScaleType) {
                mDrawMatrix = mMatrix;
                float scale;
                float dx;
                float dy;

                if (dwidth <= vwidth && dheight <= vheight) {
                    scale = 1.0f;
                } else {
                    scale = Math.min((float) vwidth / (float) dwidth,
                            (float) vheight / (float) dheight);
                }

                dx = Math.round((vwidth - dwidth * scale) * 0.5f);
                dy = Math.round((vheight - dheight * scale) * 0.5f);

                mDrawMatrix.setScale(scale, scale);
                mDrawMatrix.postTranslate(dx, dy);
            } else {
                // Generate the required transform.
                mTempSrc.set(0, 0, dwidth, dheight);
                mTempDst.set(0, 0, vwidth, vheight);

                mDrawMatrix = mMatrix;
                mDrawMatrix.setRectToRect(mTempSrc, mTempDst, scaleTypeToScaleToFit(mScaleType));
            }
        }
    }
```

还有 initImageView 方法：
```java
private void initImageView() {
     mMatrix = new Matrix();
     mScaleType = ScaleType.FIT_CENTER;

     ···
 }
```

我们在 initImageView 方法里发现 ImageView 的默认值是 FIT_CENTER，然后我们继续看 configBounds 方法，顾名思义，这个方法是用来配置 Drawable 的边界的，类似于裁剪图片，相关的应用方法就是 Drawable 的 setBounds 方法。configBounds 方法前面一段截至 if 判断的部分，声明了drawable尺寸 dwidth, dheight 和 View 的内容尺寸 vwidth, vheight。

ScaleType.MATRIX 这一种模式，和其他的七种都不太一样，
