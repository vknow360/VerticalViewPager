package com.sunny.viewpager;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;

import java.util.ArrayList;
import java.util.HashMap;

@DesignerComponent(version = 1,
		versionName = "1.2",
        description = "Extension to create Vertical View Pager<br>Developed By Sunny Gupta",
        nonVisible = true,
        iconName = "https://res.cloudinary.com/andromedaviewflyvipul/image/upload/c_scale,h_20,w_20/v1571472765/ktvu4bapylsvnykoyhdm.png",
        category = ComponentCategory.EXTENSION)
@SimpleObject(external = true)
public class VerticalViewPager extends AndroidNonvisibleComponent{
    public Context context;
    public ViewGroup viewGroup;
    public HashMap<String, VerticalVPager> pagerMap = new HashMap<>();
    public VerticalViewPager(ComponentContainer container){
        super(container.$form());
        context = container.$context();
    }
    @SimpleFunction(description = "Returns number of views added to the view pager")
    public int GetViewsCount(String id){
        if (!pagerMap.containsKey(id)){
            throw new YailRuntimeError("Id does not exist", "VerticalViewPager");
        }else {
            return pagerMap.get(id).pagerAdapter.viewList.size();
        }
    }
    @SimpleFunction(description = "Returns current view index")
    public int GetCurrentViewIndex(String id){
        if (!pagerMap.containsKey(id)){
            throw new YailRuntimeError("Id does not exist", "VerticalViewPager");
        }else {
            return pagerMap.get(id).getCurrentItem();
        }
    }
    @SimpleFunction(description = "Scroll to the given index view and sets as current item")
    public void ScrollTo(String id,int index,boolean smoothScroll){
        if (!pagerMap.containsKey(id)){
            throw new YailRuntimeError("Id does not exist", "VerticalViewPager");
        }else {
            pagerMap.get(id).setCurrentItem(index, smoothScroll);
        }
    }
    @SimpleFunction(description = "Initializes VerticalViewPager in given container")
    public void Initialize(final String id,HVArrangement container){
        if (pagerMap.containsKey(id)){
            throw new YailRuntimeError("Id already exists", "VerticalViewPager");
        }else {
            VerticalVPager vPager = new VerticalVPager(context);
            //vPager.setOnPageChangeListener(listener);
            vPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
                @Override
                public void onPageSelected(int position) {
                    PageChanged(id,position);
                }
            });
            viewGroup = (ViewGroup) container.getView();
            viewGroup.addView(vPager);
            pagerMap.put(id, vPager);
        }
    }
    @SimpleFunction(description = "Adds given component to the view pager")
    public void AddComponent(String id,Object component){
        if (pagerMap.containsKey(id)) {
            View view = ((AndroidViewComponent) component).getView();
            if (view.getParent() != null) {
                ((ViewGroup) view.getParent()).removeView(view);
            }
            VerticalVPager vPager = pagerMap.get(id);
            vPager.pagerAdapter.addView(view);
            if (vPager.pagerAdapter.getCount() == 1) {
                vPager.setCurrentItem(0, true);
            }
        }else {
            throw new YailRuntimeError("Id does not exist", "VerticalViewPager");
        }
    }
    @SimpleFunction(description = "Removes given component from view pager")
    public void RemoveComponentByView(String id,Object component){
        if (pagerMap.containsKey(id)) {
            View view = ((AndroidViewComponent) component).getView();
            pagerMap.get(id).pagerAdapter.removeView(view);
        }else {
            throw new YailRuntimeError("Id does not exist", "VerticalViewPager");
        }
    }
    @SimpleFunction(description = "Removes the view from view pager present at given index")
    public void RemoveComponentByIndex(String id,int index){
        if (pagerMap.containsKey(id)) {
            pagerMap.get(id).pagerAdapter.removeItem(index);
        }else {
            throw new YailRuntimeError("Id does not exist", "VerticalViewPager");
        }
    }
    @SimpleFunction()
    public void RemoveViewPager(String id){
        if (pagerMap.containsKey(id)) {
            VerticalVPager vPager = pagerMap.get(id);
            ViewParent viewParent = vPager.getParent();
            if (viewParent != null) { //not needed though
                ((ViewGroup) viewParent).removeView(vPager);
            }
            pagerMap.remove(id);
        }else {
            throw new YailRuntimeError("Id does not exist", "VerticalViewPager");
        }
    }
    @SimpleEvent(description = "Event invoked when current page is changed")
    public void PageChanged(String id,int index){
        EventDispatcher.dispatchEvent(this,"PageChanged",id,index);
    }
    public static class VerticalVPager extends ViewPager {

        float x = 0;
        float mStartDragX = 0;
        private static final float SWIPE_X_MIN_THRESHOLD = 20; // Decide this magical nuber as per your requirement
        public CustomPagerAdapter pagerAdapter;

        public VerticalVPager(Context context) {
            super(context);
            setPageTransformer(true, new VerticalPageTransformer());
            setOverScrollMode(OVER_SCROLL_NEVER);
            pagerAdapter = new CustomPagerAdapter(this);
            setAdapter(pagerAdapter);
        }

        @Nullable
        @Override
        public PagerAdapter getAdapter() {
            return pagerAdapter;
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
                    vPager.setCurrentItem(0,true);
                }
            }else{
                vPager.setCurrentItem(currentItem,true);
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
