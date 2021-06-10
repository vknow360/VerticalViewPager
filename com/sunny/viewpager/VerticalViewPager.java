package com.sunny.viewpager;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.*;
import java.util.ArrayList;

@DesignerComponent(version = 1,
        description = "Extension to create Vertical View Pager<br>Developed By Sunny Gupta",
        nonVisible = true,
        iconName = "https://res.cloudinary.com/andromedaviewflyvipul/image/upload/c_scale,h_20,w_20/v1571472765/ktvu4bapylsvnykoyhdm.png",
        category = ComponentCategory.EXTENSION)
@SimpleObject(external = true)
public class VerticalViewPager extends AndroidNonvisibleComponent{
    public Context context;
    public ViewGroup viewGroup;
    public VerticalVPager vPager;
    public CustomPagerAdapter pagerAdapter;
    public VerticalViewPager(ComponentContainer container){
        super(container.$form());
        context = container.$context();
        vPager = new VerticalVPager(context);
        //vPager.setOnPageChangeListener(listener);
        vPager.addOnPageChangeListener(listener);
        pagerAdapter = new CustomPagerAdapter(vPager);
        vPager.setAdapter(pagerAdapter);
    }
    @SimpleFunction(description = "Returns current view index")
    public int GetCurrentViewIndex(){
        return vPager.getCurrentItem();
    }
	@SimpleFunction(description = "Scroll to the given index view and sets as current item")
	public void ScrollTo(int index,boolean smoothScroll){
		vPager.setCurrentItem(index,smoothScroll);
	}
    @SimpleFunction(description = "Inintializes VerticalViewPager in given container")
    public void Initialize(HVArrangement container){
        viewGroup = (ViewGroup)container.getView();
        viewGroup.addView(vPager);
    }
    @SimpleFunction(description = "Adds given component to the view pager")
    public void AddComponent(AndroidViewComponent component){
        View view = component.getView();
        if (view.getParent() != null) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
        pagerAdapter.addView(view);
        if(pagerAdapter.getCount() == 1){
            vPager.setCurrentItem(0,true);
        }
    }
    @SimpleFunction(description = "Removes given component from view pager")
    public void RemoveComponentByView(AndroidViewComponent component){
        View view = component.getView();
        pagerAdapter.removeView(view);
    }
    @SimpleFunction(description = "Removes the view from view pager present at given index")
    public void RemoveComponentByIndex(int index){
        pagerAdapter.removeItem(index);
    }
    @SimpleEvent(description = "Event invoked when current page is changed")
    public void PageChanged(int index){
        EventDispatcher.dispatchEvent(this,"PageChanged",index);
    }
    public ViewPager.OnPageChangeListener listener = new ViewPager.SimpleOnPageChangeListener(){
        @Override
        public void onPageSelected(int position) {
            PageChanged(position);
        }
    };
    public static class VerticalVPager extends ViewPager {

        float x = 0;
        float mStartDragX = 0;
        private static final float SWIPE_X_MIN_THRESHOLD = 20; // Decide this magical nuber as per your requirement

        public VerticalVPager(Context context) {
            super(context);
            init();
        }

        private void init() {
            setPageTransformer(true, new VerticalPageTransformer());
            setOverScrollMode(OVER_SCROLL_NEVER);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (getAdapter() != null) {
                if (getCurrentItem() >= 0 || getCurrentItem() < getAdapter().getCount()) {
                    swapXY(event);
                    final int action = event.getAction();
                    switch (action & 255) {
                        case MotionEvent.ACTION_MOVE:
                            break;
                        case MotionEvent.ACTION_UP:
                            mStartDragX = event.getX();
                            if (x < mStartDragX
                                    && (mStartDragX - x > SWIPE_X_MIN_THRESHOLD)
                                    && getCurrentItem() > 0) {
                                setCurrentItem(getCurrentItem() - 1, true);
                                return true;
                            } else if (x > mStartDragX
                                    && (x - mStartDragX > SWIPE_X_MIN_THRESHOLD)
                                    && getCurrentItem() < getAdapter().getCount() - 1) {
                                mStartDragX = 0;
                                setCurrentItem(getCurrentItem() + 1, true);
                                return true;
                            }
                            break;
                    }
                } else {
                    mStartDragX = 0;
                }
                swapXY(event);
                return super.onTouchEvent(swapXY(event));
            }
            return false;
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent event) {
            boolean intercepted = super.onInterceptTouchEvent(swapXY(event));
            if ((event.getAction() & 255) == MotionEvent.ACTION_DOWN) {
                x = event.getX();
            }
            swapXY(event);
            return intercepted;
        }

        private MotionEvent swapXY(MotionEvent ev) {
            float width = getWidth();
            float height = getHeight();
            float newX = (ev.getY() / height) * width;
            float newY = (ev.getX() / width) * height;
            ev.setLocation(newX, newY);
            return ev;
        }

        private static class VerticalPageTransformer implements PageTransformer {
            @Override
            public void transformPage(@NonNull View view, float position) {

                if (position < -1) {
                    view.setAlpha(0);
                } else if (position <= 1) {
                    view.setAlpha(1);
                    view.setTranslationX(view.getWidth() * -position);
                    float yPosition = position * view.getHeight();
                    view.setTranslationY(yPosition);
                } else {
                    view.setAlpha(0);
                }
            }
        }
    }
    public static class CustomPagerAdapter extends PagerAdapter {
        public ArrayList<View> viewList = new ArrayList<>();
        public VerticalVPager vPager;
        public int currentItem = 0;
        public CustomPagerAdapter(VerticalVPager pager){
            super();
            vPager = pager;
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            if (viewList.contains(object)){
                return viewList.indexOf(object);
            }
            return 0;
        }

        @Override
        public int getCount() {
            return viewList.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
            return view == o;
        }

        @NonNull
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = viewList.get(position);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View)object);
        }
        public void removeItem(int position){
            currentItem = vPager.getCurrentItem();
            vPager.setAdapter(null);
            viewList.remove(position);
            vPager.setAdapter(this);
            //notifyDataSetChanged();
            if (position <= currentItem){
                if (currentItem != 0) {
                    vPager.setCurrentItem(currentItem - 1, true);
                }else {
                    vPager.setCurrentItem(currentItem + 1,true);
                }
            }else{
                vPager.setCurrentItem(currentItem);
            }
        }
        public void addView(View view){
            viewList.add(view);
            notifyDataSetChanged();
        }
        public void removeView(View view){
            removeItem(viewList.indexOf(view));
        }

    }
}
